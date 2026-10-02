package net.reversteam.desecratedcompat.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {
    com.reverseeon.mod.stormmod.StormMod.StormWeatherHandler.class,
    com.reverseeon.mod.stormmod.StormSyndrome.class,
    com.reverseeon.mod.stormmod.StormUnmaking.class,
    com.reverseeon.mod.stormmod.ManusRaid.class,
    com.reverseeon.mod.stormmod.DoveCoverage.class,
    com.reverseeon.mod.stormmod.DoveArrayView.class,
    com.reverseeon.mod.stormmod.ChunkRevertState.class
}, remap = false)
public class StormDimensionMixin {

    private static final ResourceKey<Level> CUSTOM_STORM_DIM = ResourceKey.create(
        net.minecraft.core.registries.Registries.DIMENSION, 
        ResourceLocation.parse("reverseeon:island_dim")
    );

    @Redirect(
        method = {
            "onCommand", "onRegisterCommands", "onServerTick", "isUnderDove", // StormWeatherHandler
            "onPlayerTick", // StormSyndrome
            "onSpawn", // StormUnmaking
            "start", "onLevelTick", // ManusRaid
            "isDoveDimension", // DoveCoverage
            "of", // DoveArrayView
            "shouldRevert" // ChunkRevertState
        },
        at = @At(
            value = "FIELD", 
            target = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;", 
            remap = true
        )
    )
    private static ResourceKey<Level> redirectStormDimension() {
        return CUSTOM_STORM_DIM;
    }
}