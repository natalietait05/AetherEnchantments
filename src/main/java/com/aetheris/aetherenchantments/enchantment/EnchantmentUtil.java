package com.aetheris.aetherenchantments.enchantment;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public final class EnchantmentUtil {
    private EnchantmentUtil() {
    }

    public static int getLevel(Level level, ItemStack stack, Identifier enchantmentId) {
        if (level == null || stack == null || stack.isEmpty()) {
            return 0;
        }

        var enchantment = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(enchantmentId);

        return enchantment
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }
}
