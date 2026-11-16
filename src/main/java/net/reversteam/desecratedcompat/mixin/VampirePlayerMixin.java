package net.reversteam.desecratedcompat.mixin;

import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// remap = false tells the compiler not to look for Vampirism class mappings
@Mixin(value = VampirePlayer.class, remap = false)
public class VampirePlayerMixin {

    // remap = true is explicitly required here because we are targeting a Minecraft method
    @Redirect(
        method = "isGettingSundamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
        ),
        remap = true
    )
    private ItemStack redirectGetMainHandItem(Player instance) {
        // If the umbrella is in EITHER hand, return a dummy umbrella stack
        if (instance.getMainHandItem().getItem() == ModItems.UMBRELLA.get() || 
            instance.getOffhandItem().getItem() == ModItems.UMBRELLA.get()) {
            return new ItemStack(ModItems.UMBRELLA.get());
        }
        // Otherwise, proceed with the normal main hand item
        return instance.getMainHandItem();
    }
}