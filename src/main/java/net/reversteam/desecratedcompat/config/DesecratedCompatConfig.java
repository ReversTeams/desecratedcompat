package net.reversteam.desecratedcompat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class DesecratedCompatConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DISABLE_VAMPIRISM_CONTAINER_INTERACTION;
    public static final ForgeConfigSpec.BooleanValue DISABLE_VAMPIRISM_BUCKET_CONVERSION;
    public static final ForgeConfigSpec.BooleanValue DISABLE_GLASS_BOTTLE_INTERACTION;
    public static final ForgeConfigSpec.BooleanValue DISABLE_CUSTOM_TANK_GLASS_BOTTLE_INTERACTION;
    public static final ForgeConfigSpec.BooleanValue DISABLE_BUCKET_INTERACTION;

    static {
        BUILDER.push("general");
        
        DISABLE_VAMPIRISM_CONTAINER_INTERACTION = BUILDER
                .comment("Disable interactions with Vampirism Blood Containers.")
                .define("disableVampirismContainerInteraction", true);
                
        DISABLE_VAMPIRISM_BUCKET_CONVERSION = BUILDER
                .comment("Disable interactions with Vampirism Blood Buckets.")
                .define("disableVampirismBucketConversion", true);

        DISABLE_GLASS_BOTTLE_INTERACTION = BUILDER
                .comment("Disable glass bottle interactions.")
                .define("disableGlassBottleInteraction", false);

        DISABLE_CUSTOM_TANK_GLASS_BOTTLE_INTERACTION = BUILDER
                .comment("Disable glass bottle interactions for Neepmeat and Butchery.")
                .define("disableCustomTankGlassBottleInteraction", false);

        DISABLE_BUCKET_INTERACTION = BUILDER
                .comment("Disable bucket interactions.")
                .define("disableBucketInteraction", true);
                
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "desecratedcompat-common.toml");
    }
}