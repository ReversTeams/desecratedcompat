package net.reversteam.desecratedcompat.mixin;

import com.reverseeon.mod.client.screen.DialogueScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DialogueScreen.class, remap = false)
public abstract class DialogueScreenMixin {

    @Unique
    private String desecratedCompat$npcId;

    private static final String DIA_ID = "dia";

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(String npcId, String line, int talkCd, int giftCd, int keepsakeCd, int specialCd, int have, CallbackInfo ci) {
        this.desecratedCompat$npcId = npcId;
    }

    @Inject(method = "nameFor", at = @At("HEAD"), cancellable = true)
    private static void onNameFor(String npc, CallbackInfoReturnable<String> cir) {
        if (DIA_ID.equals(npc)) {
            cir.setReturnValue("???");
        }
    }

    @Inject(method = "faceFor", at = @At("HEAD"), cancellable = true)
    private static void onFaceFor(String npc, CallbackInfoReturnable<ResourceLocation> cir) {
        if (DIA_ID.equals(npc)) {
            cir.setReturnValue(new ResourceLocation("desecrated", "textures/entity/the_world.png"));
        }
    }

    @Inject(method = "drawFace", at = @At("HEAD"), cancellable = true)
    private void onDrawFace(GuiGraphics g, ResourceLocation tex, int x, int y, int size, CallbackInfo ci) {
        if (DIA_ID.equals(this.desecratedCompat$npcId)) {
            g.pose().pushPose();
            
            // Scale up by 4x (16 * 4 = 64)
            g.pose().scale(4.0F, 4.0F, 1.0F);
            
            // Divide the centered coordinates by the scale factor
            g.blit(tex, (int)((x - 12) / 4.0F), (int)((y - 11) / 4.0F), 0, 0, 16, 16, 16, 16);
            
            g.pose().popPose();
            ci.cancel();
        }
    }

    @Inject(method = "openLabelFor", at = @At("HEAD"), cancellable = true)
    private static void onOpenLabelFor(String npc, CallbackInfoReturnable<String> cir) {
        if (DIA_ID.equals(npc)) {
            cir.setReturnValue("Reset Timeline");
        }
    }

    @Inject(method = "accentFor", at = @At("HEAD"), cancellable = true)
    private static void onAccentFor(String npc, CallbackInfoReturnable<Integer> cir) {
        if (DIA_ID.equals(npc)) {
            cir.setReturnValue(0xFCA800);
        }
    }

    @Inject(method = "availableFor", at = @At("HEAD"), cancellable = true)
    private void onAvailableFor(int choice, CallbackInfoReturnable<Boolean> cir) {
        if (DIA_ID.equals(this.desecratedCompat$npcId)) {
            // Choice 0 = Trade/Open, Choice 2 = Gift. 
            // Returning false forces the base game to render them as grayed out and unclickable.
            if (choice == 0 || choice == 1 || choice == 2) {
                cir.setReturnValue(false);
            }
        }
    }
}