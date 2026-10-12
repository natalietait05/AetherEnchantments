package com.aetheris.aetherenchantments.treecapitator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public final class Treecapitator {
    private static boolean processingTree;

    private Treecapitator() {
    }

    public static boolean isProcessingTree() {
        return processingTree;
    }

    public static boolean canUse(ItemStack tool) {
        return tool.is(ItemTags.AXES);
    }

    public static Set<BlockPos> detectTree(Level level, BlockPos origin, int maxLogs) {
        BlockState originState = level.getBlockState(origin);
        if (!TreeDetector.isTreeTrunk(originState)) {
            return Set.of();
        }

        Set<BlockPos> logs = TreeDetector.findConnectedLogs(level, origin, maxLogs);
        if (logs.isEmpty()) {
            return Set.of();
        }

        if (TreeDetector.isCrimsonStem(originState) || TreeDetector.isWarpedStem(originState)) {
            return TreeDetector.hasNetherGround(level, logs) ? logs : Set.of();
        }

        return TreeDetector.hasNearbyLeaves(level, logs) ? logs : Set.of();
    }

    public static ItemStack findHoe(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ItemTags.HOES)) {
            return offhand;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ItemTags.HOES)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void breakNetherVegetation(Level level, Player player, Set<BlockPos> vegetation) {
        if (vegetation.isEmpty()) {
            return;
        }

        ItemStack hoe = findHoe(player);
        if (hoe.isEmpty()) {
            return;
        }

        if (!player.isCreative()) {
            int remaining = hoe.getMaxDamage() - hoe.getDamageValue();
            if (remaining < vegetation.size()) {
                return;
            }
        }

        for (BlockPos pos : vegetation) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.SHROOMLIGHT)
                    && !state.is(Blocks.NETHER_WART_BLOCK)
                    && !state.is(Blocks.WARPED_WART_BLOCK)) {
                continue;
            }

            boolean destroyed = level.destroyBlock(pos, true, player);
            if (destroyed && !player.isCreative()) {
                hoe.hurtAndBreak(1, player, player.getUsedItemHand());
            }
        }
    }

    public static void cutTree(Level level, Player player, BlockPos origin, Set<BlockPos> logs) {
        if (processingTree || logs.isEmpty()) {
            return;
        }

        ItemStack tool = player.getMainHandItem();
        if (!canUse(tool)) {
            return;
        }

        if (!player.isCreative()) {
            int remaining = tool.getMaxDamage() - tool.getDamageValue();
            if (remaining < logs.size()) {
                return;
            }
        }

        processingTree = true;
        try {
            BlockState originState = level.getBlockState(origin);
            boolean netherTree = TreeDetector.isCrimsonStem(originState)
                    || TreeDetector.isWarpedStem(originState);

            if (netherTree) {
                boolean crimson = TreeDetector.isCrimsonStem(originState);
                breakNetherVegetation(
                        level,
                        player,
                        TreeDetector.findNetherVegetation(level, logs, crimson)
                );
            }

            // The block originally mined by the player is left for vanilla to break.
            for (BlockPos log : logs) {
                if (log.equals(origin)) {
                    continue;
                }

                if (!TreeDetector.isTreeTrunk(level.getBlockState(log))) {
                    continue;
                }

                boolean destroyed = level.destroyBlock(log, true, player);
                if (destroyed && !player.isCreative()) {
                    tool.hurtAndBreak(1, player, player.getUsedItemHand());
                }
            }
        } finally {
            processingTree = false;
        }
    }

    public static void stripTree(Level level, Player player, BlockPos origin, Set<BlockPos> logs) {
        if (processingTree || logs.isEmpty()) {
            return;
        }

        ItemStack tool = player.getMainHandItem();
        if (!canUse(tool)) {
            return;
        }

        int blocksToStrip = 0;
        for (BlockPos log : logs) {
            BlockState state = level.getBlockState(log);
            if (TreeDetector.isTreeTrunk(state)
                    && !TreeDetector.isStrippedStem(state)
                    && getStrippedState(state) != null) {
                blocksToStrip++;
            }
        }

        if (!player.isCreative()) {
            int remaining = tool.getMaxDamage() - tool.getDamageValue();
            if (remaining < blocksToStrip) {
                return;
            }
        }

        BlockState originState = level.getBlockState(origin);
        boolean netherTree = TreeDetector.isCrimsonStem(originState)
                || TreeDetector.isWarpedStem(originState);
        Set<BlockPos> vegetation = netherTree
                ? TreeDetector.findNetherVegetation(
                        level, logs, TreeDetector.isCrimsonStem(originState))
                : Set.of();

        processingTree = true;
        try {
            for (BlockPos log : logs) {
                BlockState state = level.getBlockState(log);
                if (!TreeDetector.isTreeTrunk(state) || TreeDetector.isStrippedStem(state)) {
                    continue;
                }

                BlockState stripped = getStrippedState(state);
                if (stripped == null) {
                    continue;
                }

                level.setBlock(log, stripped, 3);
                if (!player.isCreative()) {
                    tool.hurtAndBreak(1, player, player.getUsedItemHand());
                }
            }

            if (!vegetation.isEmpty()) {
                breakNetherVegetation(level, player, vegetation);
            }
        } finally {
            processingTree = false;
        }
    }

    private static BlockState getStrippedState(BlockState state) {
        Block block = state.getBlock();
        Block strippedBlock = null;

        if (block == Blocks.OAK_LOG) strippedBlock = Blocks.STRIPPED_OAK_LOG;
        else if (block == Blocks.SPRUCE_LOG) strippedBlock = Blocks.STRIPPED_SPRUCE_LOG;
        else if (block == Blocks.BIRCH_LOG) strippedBlock = Blocks.STRIPPED_BIRCH_LOG;
        else if (block == Blocks.JUNGLE_LOG) strippedBlock = Blocks.STRIPPED_JUNGLE_LOG;
        else if (block == Blocks.ACACIA_LOG) strippedBlock = Blocks.STRIPPED_ACACIA_LOG;
        else if (block == Blocks.DARK_OAK_LOG) strippedBlock = Blocks.STRIPPED_DARK_OAK_LOG;
        else if (block == Blocks.MANGROVE_LOG) strippedBlock = Blocks.STRIPPED_MANGROVE_LOG;
        else if (block == Blocks.CHERRY_LOG) strippedBlock = Blocks.STRIPPED_CHERRY_LOG;
        else if (block == Blocks.PALE_OAK_LOG) strippedBlock = Blocks.STRIPPED_PALE_OAK_LOG;
        else if (block == Blocks.CRIMSON_STEM) strippedBlock = Blocks.STRIPPED_CRIMSON_STEM;
        else if (block == Blocks.WARPED_STEM) strippedBlock = Blocks.STRIPPED_WARPED_STEM;

        if (strippedBlock == null) {
            return null;
        }

        BlockState strippedState = strippedBlock.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS)
                && strippedState.hasProperty(RotatedPillarBlock.AXIS)) {
            Direction.Axis axis = state.getValue(RotatedPillarBlock.AXIS);
            strippedState = strippedState.setValue(RotatedPillarBlock.AXIS, axis);
        }
        return strippedState;
    }
}
