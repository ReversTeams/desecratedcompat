package net.reversteam.desecratedcompat.mixin;

import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import me.anticode.ascendant_arcana.networking.EnchantingScreenSync;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(value = AArcanaEnchantingMenu.class, remap = false)
public abstract class AArcanaEnchantingMenuPowerMixin {

    @Shadow public int[] enchantmentPower;
    @Shadow public List<Enchantment> unlockedTreasures;
    @Shadow public Player player;
    @Shadow private ContainerLevelAccess context;

    @Overwrite
    private void updatePower() {
        //System.out.println("[DesecratedCompat] DEBUG: AArcanaEnchantingMenu.updatePower() MIXIN APPLIED!");
        
        context.execute((level, pos) -> {
            int i = 0;

            for (BlockPos blockPos : AArcanaEnchantingMenu.getOffsetsForTable(level, pos)) {
                BlockPos checkPos = pos.offset(blockPos);
                if (level.getBlockState(checkPos).is(BlockTags.ENCHANTMENT_POWER_PROVIDER)) {
                    if (level.getBlockEntity(checkPos, BlockEntityType.CHISELED_BOOKSHELF).isPresent()) {
                        ChiseledBookShelfBlockEntity chiseledBookshelf = (ChiseledBookShelfBlockEntity) level.getBlockEntity(checkPos);
                        assert chiseledBookshelf != null;
                        for (int j = 0; j < chiseledBookshelf.getContainerSize(); j++) {
                            var itemStack1 = chiseledBookshelf.getItem(j);
                            for (Map.Entry<Enchantment, Integer> enchantInstance : EnchantmentHelper.getEnchantments(itemStack1).entrySet()) {
                                int rarityMultiplier = switch (enchantInstance.getKey().getRarity()) {
                                    case UNCOMMON -> 2;
                                    case RARE -> 3;
                                    case VERY_RARE -> 5;
                                    default -> 1;
                                };
                                i += enchantInstance.getValue() * rarityMultiplier;
                                if ((enchantInstance.getKey().isTreasureOnly() || me.anticode.ascendant_arcana.AscendantArcana.config.books_remove_scrap_cost || me.anticode.ascendant_arcana.AscendantArcana.config.books_tier_bypass != 0) && !unlockedTreasures.contains(enchantInstance.getKey())) {
                                    unlockedTreasures.add(enchantInstance.getKey());
                                }
                            }
                        }
                    } else {
                        float bonus = level.getBlockState(checkPos).getEnchantPowerBonus(level, checkPos);
                        //System.out.println("[DesecratedCompat] DEBUG: Found non-chiseled provider at " + checkPos + " | Bonus: " + bonus);
                        i += (int) bonus;
                    }
                }
            }
            
            NetworkManager.sendToPlayer((ServerPlayer) player, EnchantingScreenSync.Id, 
                new EnchantingScreenSync(((AbstractContainerMenu)(Object)this).containerId, unlockedTreasures).write());
            
            if (level.getBlockState(pos).getBlock().defaultBlockState().is(AArcanaBlocks.COPPER_ENCHANTING_TABLE.get())) {
                if (i > me.anticode.ascendant_arcana.AscendantArcana.config.uncommon_enchanting_power) {
                    i = me.anticode.ascendant_arcana.AscendantArcana.config.uncommon_enchanting_power;
                }
            }
            enchantmentPower[0] = i;
            //System.out.println("[DesecratedCompat] DEBUG: Final calculated enchantment power: " + i);
        });
    }
}