package com.aetheris.aetherenchantments;

import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import com.aetheris.aetherenchantments.treecapitator.Treecapitator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class AetherEnchantments implements ModInitializer {
    public static final String MOD_ID = "aetherenchantments";
    public static final Identifier LUMBERJACK = id("lumberjack");
    public static final Identifier INDESTRUCTIBLE = id("indestructible");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("AetherEnchantments initialized");

        // Normal left-click mining cuts the connected tree. The enchantment level
        // determines the maximum number of trunk blocks processed.
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {

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

            int enchantmentLevel = EnchantmentUtil.getLevel(player.level(), tool, LUMBERJACK);
            if (enchantmentLevel <= 0) {
                return true;
            }

            Set<BlockPos> logs = Treecapitator.detectTree(
                    level,
                    pos,
                    maxTreeBlocks(enchantmentLevel)
            );

            if (!logs.isEmpty()) {
                Treecapitator.cutTree(level, player, pos, logs);
            }

            return true;
        });

        // Sneak + right-click with an enchanted axe strips the connected tree
        // without breaking any of its trunk blocks.
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (hand != InteractionHand.MAIN_HAND || !player.isCrouching()) {
                return InteractionResult.PASS;
            }

            ItemStack tool = player.getMainHandItem();
            if (!Treecapitator.canUse(tool)) {
                return InteractionResult.PASS;
            }

            int enchantmentLevel = EnchantmentUtil.getLevel(player.level(), tool, LUMBERJACK);
            if (enchantmentLevel <= 0) {
                return InteractionResult.PASS;
            }

            BlockPos target = hitResult.getBlockPos();
            Set<BlockPos> logs = Treecapitator.detectTree(
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

            Treecapitator.stripTree(level, player, target, logs);
            return InteractionResult.SUCCESS;
        });
    }

    private static int maxTreeBlocks(int level) {
        return switch (Math.min(level, 3)) {
            case 1 -> 8;
            case 2 -> 40;
            default -> 256;
        };
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
