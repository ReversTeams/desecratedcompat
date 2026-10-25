package net.reversteam.desecratedcompat;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.reversteam.desecratedcompat.config.DesecratedCompatConfig;

@Mod(Desecratedcompat.MODID)
public class Desecratedcompat {
    public static final String MODID = "desecratedcompat";

    public Desecratedcompat(FMLJavaModLoadingContext context) {
        net.reversteam.desecratedcompat.network.ModNetwork.register();
        DesecratedCompatConfig.register();
    }
}