package net.reversteam.desecratedcompat.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.reversteam.desecratedcompat.config.DesecratedCompatConfig;

@Mod.EventBusSubscriber(modid = "desecratedcompat", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodBottleEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getFace() == null) return;

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        Item held = heldItem.getItem();

        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be == null) return;

        Block clickedBlock = event.getLevel().getBlockState(event.getPos()).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(clickedBlock);
        
        boolean isVampirismBloodContainer = blockId != null && blockId.equals(new ResourceLocation("vampirism", "blood_container"));
        boolean isBloodGrate = blockId != null && blockId.equals(new ResourceLocation("butchery", "blood_grate"));
        boolean isBasicTank = blockId != null && blockId.equals(new ResourceLocation("neepmeat", "basic_tank"));
        boolean isBasicGlassTank = blockId != null && blockId.equals(new ResourceLocation("neepmeat", "basic_glass_tank"));
        boolean isCustomTank = isBloodGrate || isBasicTank || isBasicGlassTank;

        if (event.getLevel().isClientSide()) {
            if (isCustomTank && held == Items.GLASS_BOTTLE) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.PASS);
            }
            return;
        }

        // Glass Bottle
        if (held == Items.GLASS_BOTTLE) {
            if (DesecratedCompatConfig.DISABLE_GLASS_BOTTLE_INTERACTION.get()) {
                return;
            }
            if (isCustomTank && DesecratedCompatConfig.DISABLE_CUSTOM_TANK_GLASS_BOTTLE_INTERACTION.get()) {
                return;
            }
            if (isVampirismBloodContainer && DesecratedCompatConfig.DISABLE_VAMPIRISM_CONTAINER_INTERACTION.get()) {
                return;
            }

            be.getCapability(ForgeCapabilities.FLUID_HANDLER, event.getFace()).ifPresent(handler -> {
                ResourceLocation desecratedBloodLoc = new ResourceLocation("desecratedcore", "blood");
                ResourceLocation vampirismBloodLoc = new ResourceLocation("vampirism", "blood");

                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack tankFluid = handler.getFluidInTank(i);
                    if (tankFluid.isEmpty()) continue;
                    
                    Fluid fluid = tankFluid.getFluid();
                    ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
                    if (fluidId == null) continue;
                    
                    boolean isTargetBlood = fluidId.equals(desecratedBloodLoc) || fluidId.equals(vampirismBloodLoc);
                    
                    if (isTargetBlood && tankFluid.getAmount() >= 900) {
                        FluidStack toDrain = new FluidStack(fluid, 900);
                        FluidStack drained = handler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                        
                        if (drained.getAmount() >= 900) {
                            be.setChanged(); 
                            
                            heldItem.shrink(1);
                            
                            ItemStack bloodBottle = new ItemStack(ForgeRegistries.ITEMS.getValue(
                                    new ResourceLocation("vampirism", "blood_bottle")
                            ));
                            
                            if (bloodBottle != null && bloodBottle.getItem() != Items.AIR) {
                                bloodBottle.setDamageValue(9);
                                if (!player.getInventory().add(bloodBottle)) {
                                    player.drop(bloodBottle, false);
                                }
                            }
                            
                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.SUCCESS);
                            return;
                        }
                    }
                }
            });
            return;
        }

        if (isVampirismBloodContainer || isCustomTank) {
            if (DesecratedCompatConfig.DISABLE_BUCKET_INTERACTION.get()) {
                return;
            }

            be.getCapability(ForgeCapabilities.FLUID_HANDLER, event.getFace()).ifPresent(handler -> {
                Fluid vampirismBlood = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("vampirism", "blood"));
                Item desecratedBloodBucket = ForgeRegistries.ITEMS.getValue(new ResourceLocation("desecratedcore", "blood_bucket"));
                Item vampirismBloodBucket = ForgeRegistries.ITEMS.getValue(new ResourceLocation("vampirism", "blood_bucket"));

                if (vampirismBlood == null || desecratedBloodBucket == null) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.PASS);
                    return;
                }

                ResourceLocation desecratedBloodLoc = new ResourceLocation("desecratedcore", "blood");
                ResourceLocation vampirismBloodLoc = new ResourceLocation("vampirism", "blood");
                ResourceLocation butcheryBloodLoc = new ResourceLocation("butchery", "blood");
                TagKey<Fluid> forgeBloodTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), new ResourceLocation("forge", "blood"));

                if (held == desecratedBloodBucket || held == vampirismBloodBucket) {
                    FluidStack toFill = new FluidStack(vampirismBlood, 1000);
                    int filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
                    
                    if (filled >= 1000) {
                        handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                        be.setChanged();
                        
                        heldItem.shrink(1);
                        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                        if (!player.getInventory().add(emptyBucket)) {
                            player.drop(emptyBucket, false);
                        }
                        
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                    } else {
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.PASS);
                    }
                } 
                else if (held == Items.BUCKET) {
                    boolean hasTargetBlood = false;
                    Fluid foundBlood = null;
                    ResourceLocation foundBloodId = null;

                    for (int i = 0; i < handler.getTanks(); i++) {
                        FluidStack tank = handler.getFluidInTank(i);
                        if (!tank.isEmpty() && tank.getAmount() >= 1000) {
                            ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(tank.getFluid());
                            if (fluidId != null && (fluidId.equals(desecratedBloodLoc) ||
                                                    fluidId.equals(vampirismBloodLoc) ||
                                                    fluidId.equals(butcheryBloodLoc) ||
                                                    tank.getFluid().is(forgeBloodTag))) {
                                hasTargetBlood = true;
                                foundBlood = tank.getFluid();
                                foundBloodId = fluidId;
                                break;
                            }
                        }
                    }
                    
                    if (hasTargetBlood) {
                        FluidStack toDrain = new FluidStack(foundBlood, 1000);
                        FluidStack drained = handler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                        
                        if (drained.getAmount() >= 1000) {
                            be.setChanged();
                            heldItem.shrink(1);
                            
                            ItemStack newBucket;
                            if (vampirismBloodLoc.equals(foundBloodId)) {
                                if (DesecratedCompatConfig.DISABLE_VAMPIRISM_BUCKET_CONVERSION.get()) {
                                    newBucket = new ItemStack(desecratedBloodBucket);
                                } else {
                                    newBucket = new ItemStack(vampirismBloodBucket != null ? vampirismBloodBucket : Items.BUCKET);
                                }
                            } else {
                                newBucket = new ItemStack(desecratedBloodBucket);
                            }

                            if (!player.getInventory().add(newBucket)) {
                                player.drop(newBucket, false);
                            }
                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.SUCCESS);
                        } else {
                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.PASS);
                        }
                    } else {
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.PASS);
                    }
                }
            });
        }
    }
}