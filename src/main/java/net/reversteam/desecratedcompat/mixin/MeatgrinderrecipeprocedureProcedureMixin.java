package net.reversteam.desecratedcompat.mixin;

import net.mcreator.butchery.configuration.ButcheryconfigConfiguration;
import net.mcreator.butchery.init.ButcheryModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

@Mixin(value = net.mcreator.butchery.procedures.MeatgrinderrecipeprocedureProcedure.class, remap = false)
public abstract class MeatgrinderrecipeprocedureProcedureMixin {

    @Overwrite
    public static void execute(LevelAccessor world, double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        IntFunction<ItemStack> getItem = (slotid) -> {
            if (blockEntity != null) {
                return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                        .map(cap -> cap.getStackInSlot(slotid).copy())
                        .orElse(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        };

        IntFunction<Integer> getAmount = (slotid) -> {
            if (blockEntity != null) {
                return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                        .map(cap -> cap.getStackInSlot(slotid).getCount())
                        .orElse(0);
            }
            return 0;
        };

        BiConsumer<Integer, ItemStack> setStack = (slotid, stack) -> {
            if (blockEntity != null) {
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(cap -> {
                    if (cap instanceof IItemHandlerModifiable) {
                        ((IItemHandlerModifiable) cap).setStackInSlot(slotid, stack);
                    }
                });
            }
        };

        BiConsumer<Integer, Integer> shrink = (slotid, amount) -> {
            if (blockEntity != null) {
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(cap -> {
                    if (cap instanceof IItemHandlerModifiable) {
                        ItemStack stk = cap.getStackInSlot(slotid).copy();
                        stk.shrink(amount);
                        ((IItemHandlerModifiable) cap).setStackInSlot(slotid, stk);
                    }
                });
            }
        };

        BiConsumer<String, Double> setNbtDouble = (tag, value) -> {
            if (blockEntity != null) {
                blockEntity.getPersistentData().putDouble(tag, value);
            }
        };

        Function<String, Double> getNbtDouble = (tag) -> {
            if (blockEntity != null) {
                return blockEntity.getPersistentData().getDouble(tag);
            }
            return -1.0;
        };

        Runnable updateBlock = () -> {
            if (world instanceof Level level) {
                BlockState bs = world.getBlockState(pos);
                level.sendBlockUpdated(pos, bs, bs, 3);
            }
        };

        ItemStack slot0 = getItem.apply(0);
        if (slot0.getItem() == Blocks.AIR.asItem()) {
            if (!world.isClientSide()) {
                setNbtDouble.accept("craftingTime", 0.0);
                updateBlock.run();
                setNbtDouble.accept("craftingProgress", 0.0);
                updateBlock.run();
            }
        } else {
            Item item0 = slot0.getItem();
            BlockState defaultState = (item0 instanceof BlockItem bi) ? bi.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();

            boolean isScrappable = defaultState.is(BlockTags.create(ResourceLocation.parse("butchery:scrappable"))) || slot0.is(ItemTags.create(ResourceLocation.parse("butchery:scrappable")));
            boolean slot4Valid = getAmount.apply(4) == 0 || (getItem.apply(4).getItem() == ButcheryModItems.MEAT_SCRAPS.get() && getAmount.apply(4) <= 63);

            if (!isScrappable || !slot4Valid) {
                boolean isPork = slot0.is(ItemTags.create(ResourceLocation.parse("forge:raw_pork"))) || slot0.is(ItemTags.create(ResourceLocation.parse("c:raw_pork")));
                boolean slot4SausageValid = getAmount.apply(4) == 0 || (getItem.apply(4).getItem() == ButcheryModItems.RAW_SAUSAGE.get() && getAmount.apply(4) <= 63);
                boolean hasAttachment = getItem.apply(2).getItem() == ButcheryModItems.SAUSAGE_ATTACHMENT.get();
                boolean hasIntestines = getItem.apply(1).getItem() == ButcheryModItems.INTESTINES.get();
                boolean slot3Empty = getAmount.apply(3) == 0;

                if (isPork && slot4SausageValid && hasAttachment && hasIntestines && slot3Empty) {
                    if (!world.isClientSide()) {
                        setNbtDouble.accept("craftingTime", 150.0);
                        updateBlock.run();
                        setNbtDouble.accept("craftingProgress", getNbtDouble.apply("craftingProgress") + 1.0);
                        updateBlock.run();
                    }
                    if (getNbtDouble.apply("craftingProgress") >= 150.0) {
                        shrink.accept(0, 1);
                        shrink.accept(1, 1);
                        ItemStack setstack = new ItemStack(ButcheryModItems.RAW_SAUSAGE.get());
                        setstack.setCount(getAmount.apply(4) + 1);
                        setStack.accept(4, setstack);
                        if (!world.isClientSide()) {
                            setNbtDouble.accept("craftingProgress", 0.0);
                            updateBlock.run();
                        }
                    }
                } else {
                    boolean slot4BloodSausageValid = getAmount.apply(4) == 0 || (getItem.apply(4).getItem() == getVampireSausage() && getAmount.apply(4) <= 63);
                    ItemStack slot3 = getItem.apply(3);
                    ItemStack slot5 = getItem.apply(5);
                    boolean hasBloodSyrup = slot3.getItem() == getBloodSyrup() || slot3.is(ItemTags.create(ResourceLocation.parse("c:blood_syrup"))) || slot5.getItem() == getBloodSyrup() || slot5.is(ItemTags.create(ResourceLocation.parse("forge:blood_syrup")));

                    if (!isPork || !slot4BloodSausageValid || !hasAttachment || !hasIntestines || !hasBloodSyrup) {
                        boolean isMutton = slot0.is(ItemTags.create(ResourceLocation.parse("forge:raw_mutton"))) || slot0.is(ItemTags.create(ResourceLocation.parse("c:raw_mutton")));
                        boolean slot4LambValid = getAmount.apply(4) == 0 || (getItem.apply(4).getItem() == ButcheryModItems.RAW_LAMB_MINCE.get() && getAmount.apply(4) <= 63);

                        if (!isMutton || !slot4LambValid) {
                            boolean isBeef = slot0.is(ItemTags.create(ResourceLocation.parse("forge:raw_beef"))) || slot0.is(ItemTags.create(ResourceLocation.parse("c:raw_beef")));
                            boolean slot4BeefValid = getAmount.apply(4) == 0 || getItem.apply(4).getItem() == ButcheryModItems.RAW_BEEF_MINCE.get() || (getItem.apply(4).getItem() == ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("farmersdelight:minced_beef")) && getAmount.apply(4) <= 63);

                            if (isBeef && slot4BeefValid) {
                                if (!world.isClientSide()) {
                                    setNbtDouble.accept("craftingTime", 150.0);
                                    updateBlock.run();
                                    setNbtDouble.accept("craftingProgress", getNbtDouble.apply("craftingProgress") + 1.0);
                                    updateBlock.run();
                                }
                                if (getNbtDouble.apply("craftingProgress") >= 150.0) {
                                    shrink.accept(0, 1);
                                    Item beefMince = (ModList.get().isLoaded("farmersdelight") && ButcheryconfigConfiguration.FARMERS_DELIGHT.get()) 
                                            ? ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("farmersdelight:minced_beef")) 
                                            : ButcheryModItems.RAW_BEEF_MINCE.get();
                                    ItemStack setstack = new ItemStack(beefMince);
                                    setstack.setCount(getAmount.apply(4) + 1);
                                    setStack.accept(4, setstack);
                                    if (!world.isClientSide()) {
                                        setNbtDouble.accept("craftingProgress", 0.0);
                                        updateBlock.run();
                                    }
                                }
                            }
                        } else {
                            if (!world.isClientSide()) {
                                setNbtDouble.accept("craftingTime", 150.0);
                                updateBlock.run();
                                setNbtDouble.accept("craftingProgress", getNbtDouble.apply("craftingProgress") + 1.0);
                                updateBlock.run();
                            }
                            if (getNbtDouble.apply("craftingProgress") >= 150.0) {
                                shrink.accept(0, 1);
                                ItemStack setstack = new ItemStack(ButcheryModItems.RAW_LAMB_MINCE.get());
                                setstack.setCount(getAmount.apply(4) + 1);
                                setStack.accept(4, setstack);
                                if (!world.isClientSide()) {
                                    setNbtDouble.accept("craftingProgress", 0.0);
                                    updateBlock.run();
                                }
                            }
                        }
                    } else {
                        // Process Blood Sausage (MODIFIED)
                        if (!world.isClientSide()) {
                            setNbtDouble.accept("craftingTime", 150.0);
                            updateBlock.run();
                            setNbtDouble.accept("craftingProgress", getNbtDouble.apply("craftingProgress") + 1.0);
                            updateBlock.run();
                        }
                        if (getNbtDouble.apply("craftingProgress") >= 150.0) {
                            shrink.accept(0, 1);
                            shrink.accept(1, 1);

                            if (slot3.getItem() == getBloodSyrup() || slot3.is(ItemTags.create(ResourceLocation.parse("c:blood_syrup")))) {
                                shrink.accept(3, 1);
                            } else if (slot5.getItem() == getBloodSyrup() || slot5.is(ItemTags.create(ResourceLocation.parse("forge:blood_syrup")))) {
                                shrink.accept(5, 1);
                            }

                            // Output Glass Bottle to slot 5 (Fixes original bug that read from slot 6)
                            ItemStack bottleStack = new ItemStack(Items.GLASS_BOTTLE);
                            bottleStack.setCount(getAmount.apply(5) + 1);
                            setStack.accept(5, bottleStack);

                            // Output Vampire's Delight Blood Sausage to slot 4
                            ItemStack sausageStack = new ItemStack(getVampireSausage());
                            sausageStack.setCount(getAmount.apply(4) + 1);
                            setStack.accept(4, sausageStack);

                            if (!world.isClientSide()) {
                                setNbtDouble.accept("craftingProgress", 0.0);
                                updateBlock.run();
                            }
                        }
                    }
                }
            } else {
                // Process Scrappable
                if (!world.isClientSide()) {
                    setNbtDouble.accept("craftingTime", 150.0);
                    updateBlock.run();
                    setNbtDouble.accept("craftingProgress", getNbtDouble.apply("craftingProgress") + 1.0);
                    updateBlock.run();
                }
                if (getNbtDouble.apply("craftingProgress") >= 150.0) {
                    shrink.accept(0, 1);
                    Item entItem = getItem.apply(0).getItem();
                    BlockState checkState = (entItem instanceof BlockItem bi) ? bi.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();

                    if (!checkState.is(BlockTags.create(ResourceLocation.parse("butchery:carcass")))) {
                        Item entItem2 = getItem.apply(0).getItem();
                        BlockState checkState2 = (entItem2 instanceof BlockItem bi) ? bi.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();

                        if (!checkState2.is(BlockTags.create(ResourceLocation.parse("butchery:drained_carcass")))) {
                            ItemStack setstack = new ItemStack(ButcheryModItems.MEAT_SCRAPS.get());
                            setstack.setCount(getAmount.apply(4) + 1);
                            setStack.accept(4, setstack);
                        } else {
                            ItemStack setstack = new ItemStack(ButcheryModItems.MEAT_SCRAPS.get());
                            setstack.setCount(getAmount.apply(4) + Mth.nextInt(RandomSource.create(), 3, 8));
                            setStack.accept(4, setstack);
                        }
                    } else {
                        ItemStack setstack = new ItemStack(ButcheryModItems.MEAT_SCRAPS.get());
                        setstack.setCount(getAmount.apply(4) + Mth.nextInt(RandomSource.create(), 3, 8));
                        setStack.accept(4, setstack);
                    }
                    if (!world.isClientSide()) {
                        setNbtDouble.accept("craftingProgress", 0.0);
                        updateBlock.run();
                    }
                }
            }
        }

        // Blockstate visual updates
        updateBlockState(world, pos, getItem);
    }

    private static Item getBloodSyrup() {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("vampiresdelight:blood_syrup"));
        return item != null ? item : Items.AIR;
    }

    private static Item getVampireSausage() {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("vampiresdelight:blood_sausage"));
        return item != null ? item : ButcheryModItems.RAW_BLOOD_SAUSAGE.get(); // Fallback to original if mod is missing
    }

    private static void updateBlockState(LevelAccessor world, BlockPos pos, IntFunction<ItemStack> getItem) {
        boolean hasMeat = getItem.apply(0).is(ItemTags.create(ResourceLocation.parse("forge:raw_meat"))) || getItem.apply(0).is(ItemTags.create(ResourceLocation.parse("c:raw_meat")));
        boolean hasAttachment = getItem.apply(2).getItem() == ButcheryModItems.SAUSAGE_ATTACHMENT.get();
        boolean hasIntestines = getItem.apply(1).getItem() == ButcheryModItems.INTESTINES.get();

        if (!hasMeat && !hasAttachment && !hasIntestines) setBlockStateValue(world, pos, 0);
        if (hasMeat && !hasAttachment && !hasIntestines) setBlockStateValue(world, pos, 1);
        if (hasAttachment && (!hasIntestines || !hasMeat)) setBlockStateValue(world, pos, 2);
        if (hasAttachment && hasMeat) setBlockStateValue(world, pos, 3);
        if (hasAttachment && hasIntestines && !hasMeat) setBlockStateValue(world, pos, 4);
        if (hasAttachment && hasIntestines && hasMeat) setBlockStateValue(world, pos, 5);
    }

    private static void setBlockStateValue(LevelAccessor world, BlockPos pos, int value) {
        BlockState bs = world.getBlockState(pos);
        Property<?> prop = bs.getProperties().stream().filter(p -> p.getName().equals("blockstate")).findFirst().orElse(null);
        if (prop instanceof IntegerProperty intProp) {
            if (intProp.getPossibleValues().contains(value)) {
                world.setBlock(pos, bs.setValue(intProp, value), 3);
            }
        }
    }
}