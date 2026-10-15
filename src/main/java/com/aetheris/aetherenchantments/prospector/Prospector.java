
package com.aetheris.aetherenchantments.prospector;

import com.aetheris.aetherenchantments.AetherEnchantments;
import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class Prospector {

    public static final int MAX_BLOCKS = 256;

    private static final ThreadLocal<Boolean> PROCESSING =
            ThreadLocal.withInitial(() -> false);

    private static final TagKey<Block> ORES =
            AetherEnchantments.PROSPECTOR_ORES;

    private Prospector() {
    }

    /*
     * ============================================================
     * PROCESSING STATE
     * ============================================================
     */

    public static boolean isProcessing() {
        return PROCESSING.get();
    }

    /*
     * ============================================================
     * MAIN ENTRY POINT
     * ============================================================
     */

    public static void beforeBreak(
            Level level,
            Player player,
            BlockPos origin
    ) {

        if (level.isClientSide()
                || !(player instanceof ServerPlayer serverPlayer)
                || PROCESSING.get()
                || player.isRemoved()) {
            return;
        }

        ItemStack tool = player.getMainHandItem();

        /*
         * PROSPECTOR TOOL CHECK
         */

        if (!tool.is(AetherEnchantments.PROSPECTOR_TOOLS)) {
            return;
        }

        /*
         * ENCHANTMENT LEVEL
         */

        int enchantmentLevel =
                EnchantmentUtil.getLevel(
                        level,
                        tool,
                        AetherEnchantments.PROSPECTOR
                );

        if (enchantmentLevel <= 0) {
            return;
        }

        /*
         * ORIGIN BLOCK CHECK
         */

        BlockState originState = level.getBlockState(origin);

        if (!originState.is(ORES)) {
            return;
        }

        /*
         * TOOL TIER CHECK
         *
         * If the tool cannot correctly mine the original ore,
         * Prospector will not activate.
         */

        if (!canMineOre(tool, originState)) {
            return;
        }

        /*
         * FIND CONNECTED ORES
         */

        Set<BlockPos> vein =
                findVein(
                        level,
                        origin,
                        originState,
                        enchantmentLevel,
                        tool
                );

        if (vein.size() <= 1) {
            return;
        }

        /*
         * BREAK ADDITIONAL ORES
         */

        PROCESSING.set(true);

        try {

            for (BlockPos pos : vein) {

                // The original block is left to vanilla.
                if (pos.equals(origin)) {
                    continue;
                }

                level.getChunkSource().hasChunk(
                        pos.getX() >> 4,
                        pos.getZ() >> 4
                );

                BlockState currentState =
                        level.getBlockState(pos);

                if (!currentState.is(ORES)) {
                    continue;
                }

                // Recheck the tool requirement before breaking.
                if (!canMineOre(tool, currentState)) {
                    continue;
                }

                /*
                 * Use the player's normal breaking method.
                 *
                 * This preserves:
                 * - Fortune
                 * - Silk Touch
                 * - Tool durability
                 * - Indestructible
                 * - Telekinesis
                 */

                serverPlayer.gameMode.destroyBlock(pos);
            }

        } finally {

            PROCESSING.set(false);
        }
    }

    /*
     * ============================================================
     * TOOL TIER VALIDATION
     * ============================================================
     */

    private static boolean canMineOre(
            ItemStack tool,
            BlockState state
    ) {

        /*
         * Some blocks do not require a specific tool tier.
         * For blocks that do, use vanilla's correct-tool check.
         */

        if (!state.requiresCorrectToolForDrops()) {
            return true;
        }

        return tool.isCorrectToolForDrops(state);
    }

    /*
     * ============================================================
     * VEIN DETECTION
     * ============================================================
     */

    private static Set<BlockPos> findVein(
            Level level,
            BlockPos origin,
            BlockState originState,
            int enchantmentLevel,
            ItemStack tool
    ) {

        Set<BlockPos> found = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        String originFamily =
                getOreFamily(originState);

        found.add(origin.immutable());
        queue.add(origin.immutable());

        while (!queue.isEmpty()
                && found.size() < MAX_BLOCKS) {

            BlockPos current = queue.removeFirst();

            for (Direction direction : Direction.values()) {

                if (found.size() >= MAX_BLOCKS) {
                    break;
                }

                BlockPos neighbor =
                        current.relative(direction);

                if (found.contains(neighbor)) {
                    continue;
                }

                /*
                 * Do not load new chunks during detection.
                 */

                level.getChunkSource().hasChunk(
                        neighbor.getX() >> 4,
                        neighbor.getZ() >> 4
                );

                BlockState neighborState =
                        level.getBlockState(neighbor);

                /*
                 * Only registered ores are considered.
                 */

                if (!neighborState.is(ORES)) {
                    continue;
                }

                /*
                 * Respect the mining tier of the current tool.
                 *
                 * An inaccessible ore is ignored, but does not
                 * prevent Prospector from finding other ores.
                 */

                if (!canMineOre(tool, neighborState)) {
                    continue;
                }

                /*
                 * LEVEL I:
                 * Only ores belonging to the same family.
                 *
                 * LEVEL II:
                 * Any connected registered ore.
                 */

                if (enchantmentLevel == 1) {

                    String neighborFamily =
                            getOreFamily(neighborState);

                    if (!originFamily.equals(neighborFamily)) {
                        continue;
                    }
                }

                found.add(neighbor.immutable());
                queue.addLast(neighbor.immutable());
            }
        }

        return found;
    }

    /*
     * ============================================================
     * ORE FAMILY DETECTION
     * ============================================================
     */

    private static String getOreFamily(BlockState state) {

        Block block = state.getBlock();

        if (block == Blocks.COAL_ORE
                || block == Blocks.DEEPSLATE_COAL_ORE) {
            return "coal";
        }

        if (block == Blocks.IRON_ORE
                || block == Blocks.DEEPSLATE_IRON_ORE) {
            return "iron";
        }

        if (block == Blocks.COPPER_ORE
                || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return "copper";
        }

        if (block == Blocks.GOLD_ORE
                || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.NETHER_GOLD_ORE) {
            return "gold";
        }

        if (block == Blocks.REDSTONE_ORE
                || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return "redstone";
        }

        if (block == Blocks.LAPIS_ORE
                || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            return "lapis";
        }

        if (block == Blocks.DIAMOND_ORE
                || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return "diamond";
        }

        if (block == Blocks.EMERALD_ORE
                || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return "emerald";
        }

        if (block == Blocks.NETHER_QUARTZ_ORE) {
            return "quartz";
        }

        if (block == Blocks.ANCIENT_DEBRIS) {
            return "ancient_debris";
        }

        /*
         * Unknown registered ores are treated as their own family.
         */

        return block.toString();
    }
}
