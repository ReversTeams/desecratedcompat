package net.reversteam.desecratedcompat.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "desecratedcompat", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodBottleEventHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);

        if (!heldItem.is(Items.GLASS_BOTTLE)) return;

        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be == null) return;

        be.getCapability(ForgeCapabilities.FLUID_HANDLER, event.getFace()).ifPresent(handler -> {
            ResourceLocation bloodId = new ResourceLocation("desecratedcore", "blood");
            
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack tankFluid = handler.getFluidInTank(i);
                \
                ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(tankFluid.getFluid());
                if (bloodId.equals(fluidId) && tankFluid.getAmount() >= 1000) {
                    
                    FluidStack toDrain = new FluidStack(tankFluid.getFluid(), 1000);
                    FluidStack drained = handler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                    
                    if (drained.getAmount() >= 1000) {
                        be.setChanged(); 
                        
                        heldItem.shrink(1);
                        
                        ItemStack bloodBottle = new ItemStack(ForgeRegistries.ITEMS.getValue(
                                new ResourceLocation("vampirism", "blood_bottle")
                        ));
                        
                        bloodBottle.setDamageValue(8); 
                        
                        player.getInventory().add(bloodBottle);
                        
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        });
    }
}