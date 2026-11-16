package net.reversteam.desecratedcompat.mixin;

import net.mcreator.butchery.init.ButcheryModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = net.mcreator.butchery.world.inventory.MeatgrinderguiMenu.class, remap = true)
public abstract class MeatgrinderguiMenuMixin {

    @Shadow private IItemHandler internal;

    /**
     * @author DesecratedCompat
     * @reason Allow Vampire's Delight Blood Syrup in Slot 3 alongside the original Blood Bottle.
     */
    @ModifyArg(
        method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/FriendlyByteBuf;)V",
        // FIX: The owner must be MeatgrinderguiMenu, not AbstractContainerMenu, 
        // because the decompiled code uses "this.m_38897_()"
        at = @At(value = "INVOKE", target = "Lnet/mcreator/butchery/world/inventory/MeatgrinderguiMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;")
    )
    private Slot modifyBloodSlot(Slot slot) {
        // getSlotIndex() is a Forge method, so it is never obfuscated and works in all environments
        if (slot instanceof SlotItemHandler sih && sih.getSlotIndex() == 3) {
            return new SlotItemHandler(this.internal, 3, 134, 20) {
                // Written in Mojang mappings. Mixin will remap this to 'm_5857_' in production.
                @Override
                public boolean mayPlace(ItemStack stack) {
                    Item stackItem = stack.getItem();
                    return ButcheryModItems.BOTTLE_OF_BLOOD.get() == stackItem 
                        || stackItem == getBloodSyrup()
                        || stack.is(ItemTags.create(ResourceLocation.parse("c:blood_syrup")))
                        || stack.is(ItemTags.create(ResourceLocation.parse("forge:blood_syrup")));
                }
            };
        }
        // For all other slots, pass the original slot through unchanged
        return slot;
    }

    private static Item getBloodSyrup() {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("vampiresdelight:blood_syrup"));
        return item != null ? item : Items.AIR;
    }
}