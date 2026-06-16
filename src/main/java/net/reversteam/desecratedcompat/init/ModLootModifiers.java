package net.reversteam.desecratedcompat.init;

import com.mojang.serialization.Codec;
import net.reversteam.desecratedcompat.loot.RemoveItemModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = 
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "desecratedcompat");

    public static final RegistryObject<Codec<RemoveItemModifier>> REMOVE_ITEM = 
        LOOT_MODIFIER_SERIALIZERS.register("remove_item", () -> RemoveItemModifier.CODEC.get());

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}