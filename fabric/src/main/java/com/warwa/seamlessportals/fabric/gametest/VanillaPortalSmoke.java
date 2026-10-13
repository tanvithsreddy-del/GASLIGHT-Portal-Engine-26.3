package com.warwa.seamlessportals.fabric.gametest;

import com.warwa.seamlessportals.SeamlessPortalsConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.platform_specific.IPConfig;
import qouteall.imm_ptl.core.portal.EndPortalEntity;
import qouteall.imm_ptl.core.portal.nether_portal.NetherPortalEntity;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** A real 26.3 client and disposable integrated server exercise vanilla portal formation. */
public final class VanillaPortalSmoke implements FabricClientGameTest {
    private static final String LOG = "[VANILLA PORTALS] ";

    @Override
    public void runTest(ClientGameTestContext context) {
        if (!"vanilla-portals".equals(System.getProperty("seamlessportals.gametest.only"))) return;

        try (TestSingleplayerContext ignored = context.worldBuilder()
            .setUseConsistentSettings(true)
            .adjustSettings(s -> {
                s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
                s.setName("vanilla-portal-smoke");
            }).create()) {
            context.waitTicks(40);
            BlockPos feet = context.computeOnClient(mc -> mc.player.blockPosition());
            BlockPos nether = feet.offset(8, 0, 4);
            BlockPos end = feet.offset(20, 0, 4);

            onServer(context, server -> {
                if (!IPConfig.getConfig().preserveVanillaDimensionPortals
                    || IPGlobal.netherPortalMode != IPGlobal.NetherPortalMode.vanilla
                    || IPGlobal.endPortalMode != IPGlobal.EndPortalMode.vanilla) {
                    throw new AssertionError(LOG + "legacy converted-portal config escaped the vanilla guard");
                }
                ServerLevel world = server.getLevel(Level.OVERWORLD);
                if (world == null) throw new AssertionError(LOG + "missing overworld");

                // Obsidian 4x5 frame, two-by-three interior; real fire placement
                // must create six purple vanilla Nether-portal blocks.
                for (int x = 0; x < 4; x++) for (int y = 0; y < 5; y++) {
                    BlockPos p = nether.offset(x, y, 0);
                    world.setBlockAndUpdate(p, (x == 0 || x == 3 || y == 0 || y == 4)
                        ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
                world.setBlockAndUpdate(nether.offset(1, 1, 0), Blocks.FIRE.defaultBlockState());

                // Vanilla eye insertion into the twelfth frame must form the
                // nine ordinary End-portal blocks, never IP placeholders.
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                    world.setBlockAndUpdate(end.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
                }
                for (int k = -1; k <= 1; k++) {
                    frame(world, end.offset(k, 0, -2), Direction.SOUTH, k != 0);
                    frame(world, end.offset(k, 0, 2), Direction.NORTH, true);
                    frame(world, end.offset(-2, 0, k), Direction.EAST, true);
                    frame(world, end.offset(2, 0, k), Direction.WEST, true);
                }
                var player = server.getPlayerList().getPlayers().getFirst();
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ENDER_EYE));
                BlockPos last = end.offset(0, 0, -2);
                Items.ENDER_EYE.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(last), Direction.UP, last, false)));
            });
            context.waitTicks(20);

            onServer(context, server -> {
                ServerLevel world = server.getLevel(Level.OVERWORLD);
                int netherBlocks = 0;
                for (int x = 1; x <= 2; x++) for (int y = 1; y <= 3; y++) {
                    if (world.getBlockState(nether.offset(x, y, 0)).is(Blocks.NETHER_PORTAL)) netherBlocks++;
                }
                int endBlocks = 0;
                for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                    if (world.getBlockState(end.offset(dx, 0, dz)).is(Blocks.END_PORTAL)) endBlocks++;
                }
                AABB bounds = new AABB(nether.getX() - 2, nether.getY() - 2, nether.getZ() - 2,
                    end.getX() + 4, end.getY() + 7, end.getZ() + 4);
                int converted = world.getEntitiesOfClass(NetherPortalEntity.class, bounds, e -> true).size()
                    + world.getEntitiesOfClass(EndPortalEntity.class, bounds, e -> true).size();
                if (netherBlocks != 6 || endBlocks != 9 || converted != 0) {
                    throw new AssertionError(LOG + "formation failed: netherBlocks=" + netherBlocks
                        + " endBlocks=" + endBlocks + " convertedEntities=" + converted);
                }
                SeamlessPortalsConstants.LOGGER.info(LOG
                    + "PASS: six vanilla Nether blocks, nine vanilla End blocks, zero converted portal entities");
            });

            teleport(context, nether.getX() + 1.5, nether.getY() + 1.0, nether.getZ() + 0.5);
            context.waitFor(mc -> mc.player != null && mc.player.level().dimension().equals(Level.NETHER), 400);
            SeamlessPortalsConstants.LOGGER.info(LOG + "PASS: vanilla Nether portal transported the player");

            teleport(context, feet.getX() + 0.5, feet.getY() + 1.0, feet.getZ() + 0.5);
            context.waitFor(mc -> mc.player != null && mc.player.level().dimension().equals(Level.OVERWORLD), 120);
            // A dimension change gives entities a 300-tick vanilla portal
            // cooldown. Wait away from both portals; standing inside one would
            // reset that cooldown on every tick.
            context.waitTicks(320);
            onServer(context, server -> {
                int cooldown = server.getPlayerList().getPlayers().getFirst().getPortalCooldown();
                if (cooldown != 0) throw new AssertionError(LOG + "cooldown did not expire: " + cooldown);
            });
            teleport(context, end.getX() + 0.5, end.getY() + 0.1, end.getZ() + 0.5);
            context.waitFor(mc -> mc.player != null && mc.player.level().dimension().equals(Level.END), 400);
            SeamlessPortalsConstants.LOGGER.info(LOG + "PASS: vanilla End portal transported the player");
        }
    }

    private static void teleport(ClientGameTestContext context, double x, double y, double z) {
        onServer(context, server -> {
            CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput();
            String command = "execute as @a in minecraft:overworld run tp @s " + x + " " + y + " " + z;
            server.getCommands().performPrefixedCommand(source, command);
        });
    }

    private static void frame(ServerLevel world, BlockPos pos, Direction facing, boolean eye) {
        world.setBlockAndUpdate(pos, Blocks.END_PORTAL_FRAME.defaultBlockState()
            .setValue(EndPortalFrameBlock.FACING, facing)
            .setValue(EndPortalFrameBlock.HAS_EYE, eye));
    }

    private static void onServer(ClientGameTestContext context, Consumer<MinecraftServer> action) {
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<Boolean> done = new AtomicReference<>(false);
        context.runOnClient(mc -> {
            MinecraftServer server = mc.getSingleplayerServer();
            if (server == null) {
                error.set(new AssertionError(LOG + "missing integrated server"));
                done.set(true);
                return;
            }
            server.execute(() -> {
                try { action.accept(server); }
                catch (Throwable t) { error.set(t); }
                finally { done.set(true); }
            });
        });
        context.waitFor(mc -> done.get(), 100);
        if (error.get() != null) throw new AssertionError(LOG + "server action failed", error.get());
    }
}
