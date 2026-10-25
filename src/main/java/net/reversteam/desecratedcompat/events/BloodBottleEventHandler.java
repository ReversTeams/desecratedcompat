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
        if (event.getLevel().isClientSide() == false && event.getFace() == null) return;

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        Item held = heldItem.getItem();

        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be == null) return;

        Block clickedBlock = event.getLevel().getBlockState(event.getPos()).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(clickedBlock);
        
        boolean isVampirismBloodContainer = blockId != null && blockId.equals(new ResourceLocation("vampirism", "blood_container"));
        
        if (isVampirismBloodContainer && DesecratedCompatConfig.DISABLE_VAMPIRISM_CONTAINER_INTERACTION.get()) {
            return; 
        }

        boolean isBloodGrate = blockId != null && blockId.equals(new ResourceLocation("butchery", "blood_grate"));
        boolean isBasicTank = blockId != null && blockId.equals(new ResourceLocation("neepmeat", "basic_tank"));
        boolean isBasicGlassTank = blockId != null && blockId.equals(new ResourceLocation("neepmeat", "basic_glass_tank"));
        boolean isCustomTank = isBloodGrate || isBasicTank || isBasicGlassTank;

        Fluid vampirismBlood = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("vampirism", "blood"));
        Item desecratedBloodBucket = ForgeRegistries.ITEMS.getValue(new ResourceLocation("desecratedcore", "blood_bucket"));
        Item vampirismBloodBucket = ForgeRegistries.ITEMS.getValue(new ResourceLocation("vampirism", "blood_bucket"));

        ResourceLocation desecratedBloodLoc = new ResourceLocation("desecratedcore", "blood");
        ResourceLocation vampirismBloodLoc = new ResourceLocation("vampirism", "blood");
        ResourceLocation butcheryBloodLoc = new ResourceLocation("butchery", "blood");
        TagKey<Fluid> forgeBloodTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), new ResourceLocation("forge", "blood"));

        if (event.getLevel().isClientSide()) {
            if (isCustomTank) {
                if (held == Items.GLASS_BOTTLE || held == Items.BUCKET || 
                   (desecratedBloodBucket != null && held == desecratedBloodBucket) || 
                   (vampirismBloodBucket != null && held == vampirismBloodBucket)) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.PASS);
                }
            }
            return;
        }

        // Blood Bucket Logic
        if (isVampirismBloodContainer || isCustomTank) {
            be.getCapability(ForgeCapabilities.FLUID_HANDLER, event.getFace()).ifPresent(handler -> {
                if (vampirismBlood == null || desecratedBloodBucket == null) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.PASS);
                    return;
                }

                if (held == desecratedBloodBucket || held == vampirismBloodBucket) {
                    FluidStack toFill = new FluidStack(vampirismBlood, 1000);
                    int filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
                    
                    if (filled >= 1000) {
                        handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                        be.setChanged();
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
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
                                    Item bucketToGive = vampirismBloodBucket != null ? vampirismBloodBucket : desecratedBloodBucket;
                                    newBucket = new ItemStack(bucketToGive != null ? bucketToGive : Items.BUCKET);
                                } else {
                                    Item bucketToGive = desecratedBloodBucket != null ? desecratedBloodBucket : vampirismBloodBucket;
                                    newBucket = new ItemStack(bucketToGive != null ? bucketToGive : Items.BUCKET);
                                }
                            } else {
                                Item bucketToGive = desecratedBloodBucket != null ? desecratedBloodBucket : Items.BUCKET;
                                newBucket = new ItemStack(bucketToGive);
                            }

                            if (newBucket.getItem() == null || newBucket.isEmpty()) {
                                newBucket = new ItemStack(Items.BUCKET);
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

        // Glass Bottle Logic
        if (held != Items.GLASS_BOTTLE) return;
        if (isVampirismBloodContainer) return;

        be.getCapability(ForgeCapabilities.FLUID_HANDLER, event.getFace()).ifPresent(handler -> {
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack tankFluid = handler.getFluidInTank(i);
                if (tankFluid.isEmpty()) continue;
                
                Fluid fluid = tankFluid.getFluid();
                ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
                if (fluidId == null) continue;
                
                boolean isTargetBlood = fluidId.equals(desecratedBloodLoc) || 
                                        fluidId.equals(vampirismBloodLoc) || 
                                        fluidId.equals(butcheryBloodLoc) ||
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
                        
                        if (bloodBottle != null && bloodBottle.getItem() != Items.AIR) {
                            bloodBottle.setDamageValue(9); 
                            player.getInventory().add(bloodBottle);
                        }
                        
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        return;
                    }
                }
            }
        });
    }
}