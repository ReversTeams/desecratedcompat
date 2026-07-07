package net.reversteam.desecratedcompat.mixin;

import net.mcreator.thedulling.debug.PerfProbe;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PerfProbe.class, remap = false)
public class PerfProbeMixin {

    @Inject(method = "begin", at = @At("HEAD"), cancellable = true)
    private static void disableBegin(String name, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "end", at = @At("HEAD"), cancellable = true)
    private static void disableEnd(String name, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "count", at = @At("HEAD"), cancellable = true)
    private static void disableCount(String name, long units, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onServerTick", at = @At("HEAD"), cancellable = true)
    private static void disableServerTick(TickEvent.ServerTickEvent e, CallbackInfo ci) {
        ci.cancel();
    }
}