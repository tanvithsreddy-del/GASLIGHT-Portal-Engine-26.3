package qouteall.imm_ptl.peripheral;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.platform_specific.IPConfig;
import qouteall.imm_ptl.peripheral.dim_stack.DimStackInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * S19-A 1:1 port of IP:peripheral/IPOuterClientMisc.java (held out at S16, landed with the
 * wand-runtime bring-up — it is the first entry in IP's PeripheralModMain.initClient()).
 * Outer per-install client state ({@code imm_ptl_state.json} in the game dir):
 * legacy dimension-stack-preset upgrade into IPConfig. The first-portal wiki
 * chat tip is intentionally absent from the GASLIGHT-facing engine.
 */
@Environment(EnvType.CLIENT)
public class IPOuterClientMisc {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static class OuterConfig {
        public boolean wikiInformed = false;
        public boolean portalHelperInformed = false;
        public boolean lagInformed = false;

        // the dimension stack preset has been moved into IPConfig
        // this is still here for deserialization for upgrade
        @Nullable
        public DimStackInfo dimensionStackDefault = null;

        public OuterConfig() {}
    }

    private static OuterConfig readFromFile() {
        File storageFile = getStorageFile();

        if (storageFile.exists()) {

            OuterConfig result = null;
            try (FileReader fileReader = new FileReader(storageFile)) {
                result = IPGlobal.gson.fromJson(fileReader, OuterConfig.class);
            } catch (Throwable e) {
                e.printStackTrace();
                return new OuterConfig();
            }

            if (result == null) {
                return new OuterConfig();
            }

            return result;
        }

        return new OuterConfig();
    }

    private static File getStorageFile() {
        return new File(Minecraft.getInstance().gameDirectory, "imm_ptl_state.json");
    }

    private static void writeToFile(OuterConfig outerConfig) {
        try (FileWriter fileWriter = new FileWriter(getStorageFile())) {

            IPGlobal.gson.toJson(outerConfig, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static OuterConfig outerConfig = new OuterConfig();

    public static void initClient() {
        outerConfig = readFromFile();

        // upgrade dimension stack preset
        DimStackInfo dimStackPreset = outerConfig.dimensionStackDefault;
        if (dimStackPreset != null) {
            try {
                IPConfig config = IPConfig.getConfig();
                config.dimStackPreset = ((JsonObject) IPGlobal.gson.toJsonTree(dimStackPreset));
                config.saveConfigFile();
                outerConfig.dimensionStackDefault = null;
                writeToFile(outerConfig);
                LOGGER.info("Successfully upgraded dimension stack preset");
            }
            catch (Exception e) {
                LOGGER.info("Failed to upgrade dimension stack preset", e);
            }
        }

    }
}
