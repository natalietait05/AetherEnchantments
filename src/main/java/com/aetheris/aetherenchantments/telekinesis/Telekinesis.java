
package com.aetheris.aetherenchantments.telekinesis;

import com.aetheris.aetherenchantments.AetherEnchantments;
import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class Telekinesis {

    private static final ThreadLocal<Tracking> ACTIVE_TRACKING =
            new ThreadLocal<>();

    private Telekinesis() {
    }

    private static final class Tracking {
        private final Player player;
        private int depth;

        private Tracking(Player player) {
            this.player = player;
            this.depth = 1;
        }
    }

    public static void beginBlockBreak(Level level, Player player) {
        if (level == null
                || player == null
                || level.isClientSide()
                || !(player instanceof ServerPlayer)) {
            return;
        }

        Tracking current = ACTIVE_TRACKING.get();

        if (current != null) {
            if (current.player == player) {
                current.depth++;
            }
            return;
        }

        ItemStack tool = player.getMainHandItem();

        if (!tool.is(AetherEnchantments.TELEKINESIS_TOOLS)) {
            return;
        }

        int enchantmentLevel = EnchantmentUtil.getLevel(
                level,
                tool,
                AetherEnchantments.TELEKINESIS
        );

        if (enchantmentLevel <= 0) {
            return;
        }

        ACTIVE_TRACKING.set(new Tracking(player));
    }

    public static void endBlockBreak() {
        Tracking current = ACTIVE_TRACKING.get();

        if (current == null) {
            return;
        }

        current.depth--;

        if (current.depth <= 0) {
            ACTIVE_TRACKING.remove();
        }
    }

    public static boolean collectDrop(Level level, ItemStack stack) {
        if (level == null
                || stack == null
                || stack.isEmpty()
                || level.isClientSide()) {
            return false;
        }

        Tracking current = ACTIVE_TRACKING.get();

        if (current == null) {
            return false;
        }

        Player player = current.player;

        if (player.isRemoved() || player.level() != level) {
            return false;
        }

        int originalCount = stack.getCount();
        ItemStack pickedUp = stack.copy();

        player.addItem(stack);

        int insertedCount = originalCount - stack.getCount();

        if (insertedCount > 0) {
            player.awardStat(
                    Stats.ITEM_PICKED_UP.get(pickedUp.getItem()),
                    insertedCount
            );
        }

        return stack.isEmpty();
    }
}
