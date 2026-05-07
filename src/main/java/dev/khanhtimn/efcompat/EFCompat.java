package dev.khanhtimn.efcompat;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod(EFCompat.MODID)
public class EFCompat {
    public static final String MODID = "epicfighteverycompat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EFCompat(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
