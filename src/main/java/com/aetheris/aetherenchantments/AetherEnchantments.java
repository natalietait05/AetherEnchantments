
package com.aetheris.aetherenchantments;

import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import com.aetheris.aetherenchantments.harvester.Harvester;
import com.aetheris.aetherenchantments.treecapitator.Treecapitator;
import com.aetheris.aetherenchantments.prospector.Prospector;



import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class AetherEnchantments implements ModInitializer {

    public static final String MOD_ID = "aetherenchantments";

    public static final Identifier LUMBERJACK =
            id("lumberjack");

    public static final Identifier INDESTRUCTIBLE =
            id("indestructible");

    public static final Identifier TELEKINESIS =
            id("telekinesis");

    public static final Identifier HARVESTER =
            id("harvester");

    public static final TagKey<Item> TELEKINESIS_TOOLS =
            TagKey.create(
                    Registries.ITEM,
                    id("telekinesis_tools")
            );

    public static final TagKey<Item> HARVESTER_TOOLS =
            TagKey.create(
                    Registries.ITEM,
                    id("harvester_tools")
            );

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);


    public static final Identifier PROSPECTOR =
            id("prospector");

    public static final TagKey<Item> PROSPECTOR_TOOLS =
            TagKey.create(
                    Registries.ITEM,
                    id("prospector_tools")
            );

    public static final TagKey<Block> PROSPECTOR_ORES =
            TagKey.create(
                    Registries.BLOCK,
                    id("prospector_ores")
            );



    @Override
    public void onInitialize() {

        LOGGER.info("AetherEnchantments initialized");

        /*
         * ============================================================
         * INDESTRUCTIBLE
         * ============================================================
         */

        /*
         * ============================================================
         * LUMBERJACK - TREE BREAKING
         * ============================================================
         */

        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) -> {

                    if (EnchantmentUtil.isIndestructibleUnusable(
                            player.level(),
                            player.getMainHandItem()
                    )) {
                        return false;
                    }

                    if (Treecapitator.isProcessingTree()) {
                        return true;
                    }

                    ItemStack tool = player.getMainHandItem();

                    if (!Treecapitator.canUse(tool)) {
                        return true;
                    }

                    int enchantmentLevel =
                            EnchantmentUtil.getLevel(
                                    player.level(),
                                    tool,
                                    LUMBERJACK
                            );

                    if (enchantmentLevel <= 0) {
                        return true;
                    }

                    Set<BlockPos> logs =
                            Treecapitator.detectTree(
                                    level,
                                    pos,
                                    maxTreeBlocks(enchantmentLevel)
                            );

                    if (!logs.isEmpty()) {
                        Treecapitator.cutTree(
                                level,
                                player,
                                pos,
                                logs
                        );
                    }

                    return true;
                }
        );


        /*
         * ============================================================
         * PROSPECTOR - ORE VEIN MINING
         * ============================================================
         */

        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) -> {

                    if (Treecapitator.isProcessingTree()
                            || Prospector.isProcessing()) {
                        return true;
                    }

                    Prospector.beforeBreak(
                            level,
                            player,
                            pos
                    );

                    return true;
                }
        );


        /*
         * ============================================================
         * HARVESTER - CROP HARVESTING
         * ============================================================
         */

        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) -> {

                    if (Treecapitator.isProcessingTree()) {
                        return true;
                    }

                    Harvester.beforeBreak(
                            level,
                            player,
                            pos,
                            state
                    );

                    return true;
                }
        );

        /*
         * Replant the original crop after its drops have been generated.
         */

        PlayerBlockBreakEvents.AFTER.register(
                (level, player, pos, state, blockEntity) -> {

                    Harvester.afterBreak(
                            level,
                            player,
                            pos
                    );
                }
        );

        /*
         * ============================================================
         * RIGHT-CLICK INTERACTIONS
         * ============================================================
         */

        UseBlockCallback.EVENT.register(
                (player, level, hand, hitResult) -> {

                    if (hand != InteractionHand.MAIN_HAND) {
                        return InteractionResult.PASS;
                    }

                    /*
                     * HARVESTER - FERTILIZE
                     */

                    if (Harvester.plantArea(
                    level,
                    player,
                    hitResult.getBlockPos())) {
                        return InteractionResult.SUCCESS;
                    }

                    if (Harvester.fertilize(
                            level,
                            player,
                            hitResult.getBlockPos()
                    )) {
                        return InteractionResult.SUCCESS;
                    }

                    /*
                     * LUMBERJACK - TREE STRIPPING
                     */

                    if (!player.isCrouching()) {
                        return InteractionResult.PASS;
                    }

                    ItemStack tool = player.getMainHandItem();

                    if (!Treecapitator.canUse(tool)) {
                        return InteractionResult.PASS;
                    }

                    int enchantmentLevel =
                            EnchantmentUtil.getLevel(
                                    player.level(),
                                    tool,
                                    LUMBERJACK
                            );

                    if (enchantmentLevel <= 0) {
                        return InteractionResult.PASS;
                    }

                    BlockPos target = hitResult.getBlockPos();

                    Set<BlockPos> logs =
                            Treecapitator.detectTree(
                                    level,
                                    target,
                                    maxTreeBlocks(enchantmentLevel)
                            );

                    if (logs.isEmpty()) {
                        return InteractionResult.PASS;
                    }

                    if (level.isClientSide()) {
                        return InteractionResult.SUCCESS;
                    }

                    Treecapitator.stripTree(
                            level,
                            player,
                            target,
                            logs
                    );

                    return InteractionResult.SUCCESS;
                }
        );
    }

    /*
     * ============================================================
     * LUMBERJACK CONFIGURATION
     * ============================================================
     */

    private static int maxTreeBlocks(int level) {

        return switch (Math.min(level, 3)) {
            case 1 -> 8;
            case 2 -> 40;
            default -> 256;
        };
    }

    /*
     * ============================================================
     * IDENTIFIER
     * ============================================================
     */

    public static Identifier id(String path) {

        return Identifier.fromNamespaceAndPath(
                MOD_ID,
                path
        );
    }
}
