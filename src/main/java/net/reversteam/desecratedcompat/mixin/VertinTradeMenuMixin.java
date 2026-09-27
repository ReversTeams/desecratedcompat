package net.reversteam.desecratedcompat.mixin;

import com.reverseeon.mod.item.ModItems;
import com.reverseeon.mod.menu.VertinTradeMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = VertinTradeMenu.class, remap = false)
public class VertinTradeMenuMixin {

    @ModifyArg(
        method = "m_6366_",
        at = @At(
            value = "INVOKE",
            target = "Lcom/reverseeon/mod/menu/VertinTradeMenu;exchange(Lnet/minecraft/world/entity/player/Player;IILnet/minecraft/world/item/ItemStack;)Z",
            ordinal = 0
        ),
        index = 3
    )
    private ItemStack redirectMythMuralToFrencheBlue(ItemStack original) {
        // Return the new reward item instead of the original
        return new ItemStack(ModItems.FRENCH_BLUE.get());
    }
}