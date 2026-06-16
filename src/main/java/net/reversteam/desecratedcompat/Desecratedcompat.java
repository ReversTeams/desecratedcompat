package net.reversteam.desecratedcompat;

import org.slf4j.LoggerFactory;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.reversteam.desecratedcompat.init.ModLootModifiers;

@Mod(Desecratedcompat.MODID)
public class Desecratedcompat {
    public static final String MODID = "desecratedcompat";

    public Desecratedcompat(FMLJavaModLoadingContext context) {
        LoggerFactory.getLogger("DesecratedCompat").info("[DESECRATED] Main mod class constructor executed! Registering loot modifiers...");
        IEventBus modEventBus = context.getModEventBus();
        
        ModLootModifiers.register(modEventBus);
    }
}