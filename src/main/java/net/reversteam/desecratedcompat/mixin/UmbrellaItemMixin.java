package net.reversteam.desecratedcompat.mixin;

import com.google.common.collect.Multimap;
import de.teamlapen.vampirism.items.UmbrellaItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = UmbrellaItem.class, remap = false)
public class UmbrellaItemMixin {

    @Shadow
    private Multimap<Attribute, AttributeModifier> modifierMultimap;

    @Inject(method = "getAttributeModifiers", at = @At("HEAD"), cancellable = true, remap = false)
    public void onGetAttributeModifiers(EquipmentSlot equipmentSlot, ItemStack stack, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        if (equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND) {
            cir.setReturnValue(modifierMultimap);
        }
    }
}