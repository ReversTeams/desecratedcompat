package net.reversteam.desecratedcompat.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class RemoveItemModifier extends LootModifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveItemModifier.class);

    public static final Supplier<Codec<RemoveItemModifier>> CODEC = Suppliers.memoize(() -> 
        RecordCodecBuilder.create(inst -> codecStart(inst).and(
            ResourceLocation.CODEC.fieldOf("item").forGetter(m -> m.itemToRemove)
        ).apply(inst, RemoveItemModifier::new))
    );

    private final ResourceLocation itemToRemove;

    protected RemoveItemModifier(LootItemCondition[] conditionsIn, ResourceLocation itemToRemove) {
        super(conditionsIn);
        this.itemToRemove = itemToRemove;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();
        String idString = lootTableId.toString();

        boolean isTargetChest = idString.equals("minecraft:chests/simple_dungeon") ||
                                idString.equals("minecraft:chests/desert_pyramid") ||
                                idString.equals("minecraft:chests/jungle_temple") ||
                                idString.equals("minecraft:chests/village/village_armorer") ||
                                idString.equals("minecraft:chests/village/village_butcher");

        if (!isTargetChest) {
            return generatedLoot; 
        }

        Item target = ForgeRegistries.ITEMS.getValue(itemToRemove);

        LOGGER.info("[DESECRATED] Chest: {}. Checking for {}...", generatedLoot.size(), itemToRemove);
        for (ItemStack stack : generatedLoot) {
            if (stack.is(target)) {
                LOGGER.info("[DESECRATED] >>> FOUND EGG! {}", stack.getCount());
            }
        }

        int beforeSize = generatedLoot.size();
        generatedLoot.removeIf(stack -> stack.is(target));
        
        if (generatedLoot.size() < beforeSize) {
            LOGGER.info("[DESECRATED] EGG ERADICATED");
        }
        
        return generatedLoot;
    }

    @Override
    public Codec<? extends RemoveItemModifier> codec() {
        return CODEC.get();
    }
}