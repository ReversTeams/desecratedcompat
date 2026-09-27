package net.reversteam.desecratedcompat.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// remap = false is required because we are targeting a mod class
@Mixin(targets = "com.reverseeon.mod.stormmod.StormMod$StormWeatherHandler", remap = false)
public class StormDimensionMixin {

    // ==========================================
    // CHANGE THIS TO YOUR DESIRED DIMENSION
    // ==========================================
    private static final ResourceKey<Level> CUSTOM_STORM_DIM = ResourceKey.create(
        net.minecraft.core.registries.Registries.DIMENSION, 
        new ResourceLocation("reverseeon", "island_dim") // <-- Change to your actual dimension ID
    );

    /**
     * Redirects all Level.OVERWORLD references to our custom dimension.
     * We use the obfuscated name "f_46428_" and remap = false to completely 
     * bypass the "No refMap loaded" error.
     */
    @Redirect(
        method = {
            "onCommand",
            "onRegisterCommands",
            "onServerTick",
            "isUnderDove"
        },
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;f_46428_:Lnet/minecraft/resources/ResourceKey;", remap = false)
    )
    private static ResourceKey<Level> redirectStormDimension() {
        return CUSTOM_STORM_DIM;
    }
}