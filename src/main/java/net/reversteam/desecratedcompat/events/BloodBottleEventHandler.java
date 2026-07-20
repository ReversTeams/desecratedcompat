package net.reversteam.desecratedcompat.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
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
        if (event.getFace() == null) return;

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        
        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be == null) return;

        Block clickedBlock = event.getLevel().getBlockState(event.getPos()).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(clickedBlock);
        boolean isVampirismBloodContainer = blockId != null && blockId.equals(new ResourceLocation("vampirism", "blood_container"));
        Fluid vampirismBlood = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("vampirism", "blood"));
        Item desecratedBloodBucket = ForgeRegistries.ITEMS.getValue(new ResourceLocation("desecratedcore", "blood_bucket"));
        Item vampirismBloodBucket = ForgeRegistries.ITEMS.getValue(new ResourceLocation("vampirism", "blood_bucket"));

        // Blood Bucket
        if (isVampirismBloodContainer) {
            be.getCapability(ForgeCapabilities.FLUID_HANDLER, event.getFace()).ifPresent(handler -> {

                if (vampirismBlood == null || desecratedBloodBucket == null) return;

                Item held = heldItem.getItem();

                if (held == desecratedBloodBucket || held == vampirismBloodBucket) {
                    FluidStack toFill = new FluidStack(vampirismBlood, 1000);
                    int filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
                    
                    if (filled >= 1000) {
                        handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                        be.setChanged();
                        
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        event.setCanceled(true);
                    }
                } 
                else if (held == Items.BUCKET) {
                    boolean hasVampirismBlood = false;
                    for (int i = 0; i < handler.getTanks(); i++) {
                        FluidStack tank = handler.getFluidInTank(i);
                        if (!tank.isEmpty() && tank.getFluid() == vampirismBlood && tank.getAmount() >= 1000) {
                            hasVampirismBlood = true;
                            break;
                        }
                    }
                    
                    if (hasVampirismBlood) {
                        FluidStack toDrain = new FluidStack(vampirismBlood, 1000);
                        FluidStack drained = handler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                        
                        if (drained.getAmount() >= 1000) {
                            be.setChanged();
                            heldItem.shrink(1);
                            
                            ItemStack newBucket = new ItemStack(desecratedBloodBucket);
                            if (!player.getInventory().add(newBucket)) {
                                player.drop(newBucket, false);
                            }
                            event.setCanceled(true);
                        }
                    }
                }
            });
            
            if (heldItem.getItem() == Items.BUCKET || 
                heldItem.getItem() == desecratedBloodBucket || 
                heldItem.getItem() == vampirismBloodBucket) {
                return;
            }
        }

        // Glass Bottle
        if (!heldItem.is(Items.GLASS_BOTTLE)) return;
        if (isVampirismBloodContainer) return;

        be.getCapability(ForgeCapabilities.FLUID_HANDLER, event.getFace()).ifPresent(handler -> {
            ResourceLocation desecratedBlood = new ResourceLocation("desecratedcore", "blood");
            ResourceLocation vampirismBloodLoc = new ResourceLocation("vampirism", "blood");
            TagKey<Fluid> forgeBloodTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), new ResourceLocation("forge", "blood"));
            
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack tankFluid = handler.getFluidInTank(i);
                if (tankFluid.isEmpty()) continue;
                
                Fluid fluid = tankFluid.getFluid();
                ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
                if (fluidId == null) continue;
                
                boolean isTargetBlood = fluidId.equals(desecratedBlood) || 
                                        fluidId.equals(vampirismBloodLoc) || 
                                        fluid.is(forgeBloodTag);
                
                if (isTargetBlood && tankFluid.getAmount() >= 900) {
                    FluidStack toDrain = new FluidStack(fluid, 900);
                    FluidStack drained = handler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                    
                    if (drained.getAmount() >= 900) {
                        be.setChanged(); 
                        heldItem.shrink(1);
                        
                        ItemStack bloodBottle = new ItemStack(ForgeRegistries.ITEMS.getValue(
                                new ResourceLocation("vampirism", "blood_bottle")
                        ));
                        
                        if (bloodBottle.getItem() != Items.AIR) {
                            bloodBottle.setDamageValue(9); 
                            player.getInventory().add(bloodBottle);
                        }
                        
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        });
    }
}