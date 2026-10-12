
package com.aetheris.aetherenchantments.enchantment;

import com.aetheris.aetherenchantments.AetherEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public final class EnchantmentUtil {

    private EnchantmentUtil() {
    }

    public static int getLevel(
            Level level,
            ItemStack stack,
            Identifier enchantmentId
    ) {
        if (level == null || stack == null || stack.isEmpty()) {
            return 0;
        }

        var enchantment = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(enchantmentId);

        return enchantment
                .map(holder ->
                        EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }

    /**
     * Checks whether an item has the Indestructible enchantment.
     */
    public static boolean hasIndestructible(
            Level level,
            ItemStack stack
    ) {
        return getLevel(
                level,
                stack,
                AetherEnchantments.INDESTRUCTIBLE
        ) > 0;
    }

    /**
     * Checks whether an Indestructible item has reached
     * its last durability point and must no longer be usable.
     */
    public static boolean isIndestructibleUnusable(
            Level level,
            ItemStack stack
    ) {
        if (level == null
                || stack == null
                || stack.isEmpty()
                || !stack.isDamageableItem()) {
            return false;
        }

        if (!hasIndestructible(level, stack)) {
            return false;
        }

        return stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }
}
