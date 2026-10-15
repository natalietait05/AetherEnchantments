
package com.aetheris.aetherenchantments.harvester;

import com.aetheris.aetherenchantments.AetherEnchantments;
import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Harvester {

    private static final Map<UUID, Boolean> PENDING_ORIGIN =
            new HashMap<>();

    private Harvester() {
    }

    /*
     * Called before the player breaks a block.
     * Harvests mature crops around the target, excluding the target itself.
     */
    public static void beforeBreak(
            Level level,
            Player player,
            BlockPos origin,
            BlockState originState
    ) {
        if (level.isClientSide() || player.isSpectator()) {
            return;
        }

        ItemStack tool = player.getMainHandItem();

        if (!canUse(tool)) {
            return;
        }

        int enchantmentLevel = EnchantmentUtil.getLevel(
                level,
                tool,
                AetherEnchantments.HARVESTER
        );

        if (enchantmentLevel <= 0) {
            return;
        }

        boolean originIsMature = isMatureCrop(originState);

        PENDING_ORIGIN.put(
                player.getUUID(),
                originIsMature
        );

        if (originIsMature) {
            PENDING_CROP.put(player.getUUID(), originState.getBlock());
        } else {
            PENDING_CROP.remove(player.getUUID());
        }

        int radius = getRadius(enchantmentLevel);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {

                BlockPos current = origin.offset(x, 0, z);

                if (current.equals(origin)) {
                    continue;
                }

                BlockState state = level.getBlockState(current);

                if (!isMatureCrop(state)) {
                    continue;
                }

                harvestCrop(level, player, current, state, tool);
            }
        }
    }

    /*
     * Called after the original block has been broken.
     * Replants the original crop only if it was mature before breaking.
     */
    public static void afterBreak(
            Level level,
            Player player,
            BlockPos origin
    ) {
        if (level.isClientSide()) {
            return;
        }

        Boolean wasMature = PENDING_ORIGIN.remove(player.getUUID());

        if (wasMature == null || !wasMature) {
            return;
        }

        BlockPos below = origin.below();

        BlockState soil = level.getBlockState(below);

        // Determine the crop from the block that was originally broken.
        // The original crop type is saved separately before it disappears.
        Block crop = PENDING_CROP.remove(player.getUUID());

        if (crop == null) {
            return;
        }

        replant(level, player, origin, crop);
    }

    private static final Map<UUID, Block> PENDING_CROP =
            new HashMap<>();

    private static void harvestCrop(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            ItemStack tool
    ) {
        Block crop = state.getBlock();

        Block.dropResources(
                state,
                level,
                pos,
                level.getBlockEntity(pos),
                player,
                tool
        );

        level.removeBlock(pos, false);

        replant(level, player, pos, crop);
    }

    private static void replant(
            Level level,
            Player player,
            BlockPos pos,
            Block crop
    ) {
        Item plantingItem = getPlantingItem(crop);

        if (plantingItem == null) {
            return;
        }

        ItemStack plantingStack = findItem(
                player,
                plantingItem
        );

        if (plantingStack == null) {
            return;
        }

        BlockState plantState = crop.defaultBlockState();

        if (!plantState.canSurvive(level, pos)) {
            return;
        }

        if (level.setBlock(pos, plantState, 3)) {
            plantingStack.shrink(1);
        }
    }

    /*
     * Applies bone meal to supported crops within the enchantment area.
     */
    public static boolean fertilize(
            Level level,
            Player player,
            BlockPos origin
    ) {
        if (player.isSpectator()) {
            return false;
        }

        ItemStack tool = player.getMainHandItem();

        if (!canUse(tool)) {
            return false;
        }

        int enchantmentLevel = EnchantmentUtil.getLevel(
                level,
                tool,
                AetherEnchantments.HARVESTER
        );

        if (enchantmentLevel <= 0) {
            return false;
        }

        ItemStack boneMeal = findItem(player, Items.BONE_MEAL);

        if (boneMeal == null) {
            return false;
        }

        int radius = getRadius(enchantmentLevel);

        if (level.isClientSide()) {
            return true;
        }

        boolean applied = false;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {

                if (boneMeal.isEmpty()) {
                    return applied;
                }

                BlockPos current = origin.offset(x, 0, z);
                BlockState state = level.getBlockState(current);

                if (!isBoneMealCrop(state)) {
                    continue;
                }

                if (BoneMealItem.growCrop(
                        boneMeal,
                        level,
                        current
                )) {
                    applied = true;
                }
            }
        }

        return applied;
    }

    private static boolean canUse(ItemStack tool) {
        return !tool.isEmpty()
                && tool.is(AetherEnchantments.HARVESTER_TOOLS);
    }

    private static int getRadius(int level) {
        return switch (Math.min(level, 3)) {
            case 1 -> 0;
            case 2 -> 2;
            default -> 7;
        };
    }

    private static boolean isMatureCrop(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof CropBlock) {
            return state.getValue(CropBlock.AGE)
                    == ((CropBlock) block).getMaxAge();
        }

        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) == 3;
        }

        return false;
    }

    private static boolean isBoneMealCrop(BlockState state) {
        Block block = state.getBlock();

        return block == Blocks.WHEAT
                || block == Blocks.CARROTS
                || block == Blocks.POTATOES
                || block == Blocks.BEETROOTS;
    }

    private static Item getPlantingItem(Block crop) {
        if (crop == Blocks.WHEAT) {
            return Items.WHEAT_SEEDS;
        }

        if (crop == Blocks.CARROTS) {
            return Items.CARROT;
        }

        if (crop == Blocks.POTATOES) {
            return Items.POTATO;
        }

        if (crop == Blocks.BEETROOTS) {
            return Items.BEETROOT_SEEDS;
        }

        if (crop == Blocks.NETHER_WART) {
            return Items.NETHER_WART;
        }

        return null;
    }

    private static ItemStack findItem(
            Player player,
            Item item
    ) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (!stack.isEmpty() && stack.is(item)) {
                return stack;
            }
        }

        return null;
    }


    /*
     * Plants crops in the enchantment area using the offhand item.
     * The clicked block must be farmland.
     */
    public static boolean plantArea(
            Level level,
            Player player,
            BlockPos origin
    ) {
        if (player.isSpectator()) {
            return false;
        }

        ItemStack tool = player.getMainHandItem();

        if (!canUse(tool)) {
            return false;
        }

        int enchantmentLevel = EnchantmentUtil.getLevel(
                level,
                tool,
                AetherEnchantments.HARVESTER
        );

        if (enchantmentLevel <= 0) {
            return false;
        }

        ItemStack plantingStack = player.getOffhandItem();

        if (plantingStack.isEmpty()) {
            return false;
        }

        Block crop = getCropForPlantingItem(plantingStack.getItem());

        if (crop == null) {
            return false;
        }

        if (!level.getBlockState(origin).is(Blocks.FARMLAND)) {
            return false;
        }

        // Client acknowledges the interaction.
        if (level.isClientSide()) {
            return true;
        }

        int radius = getRadius(enchantmentLevel);
        boolean planted = false;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {

                if (plantingStack.isEmpty()) {
                    return planted;
                }

                BlockPos soilPos = origin.offset(x, 0, z);

                if (!level.getBlockState(soilPos).is(Blocks.FARMLAND)) {
                    continue;
                }

                BlockPos cropPos = soilPos.above();

                if (!level.getBlockState(cropPos).isAir()) {
                    continue;
                }

                BlockState cropState = crop.defaultBlockState();

                if (!cropState.canSurvive(level, cropPos)) {
                    continue;
                }

                if (level.setBlock(cropPos, cropState, 3)) {
                    plantingStack.shrink(1);
                    planted = true;
                }
            }
        }

        return planted;
    }

    /*
     * Determines which crop corresponds to the offhand item.
     */
    private static Block getCropForPlantingItem(Item item) {

        if (item == Items.WHEAT_SEEDS) {
            return Blocks.WHEAT;
        }

        if (item == Items.CARROT) {
            return Blocks.CARROTS;
        }

        if (item == Items.POTATO) {
            return Blocks.POTATOES;
        }

        if (item == Items.BEETROOT_SEEDS) {
            return Blocks.BEETROOTS;
        }

        return null;
    }

}
