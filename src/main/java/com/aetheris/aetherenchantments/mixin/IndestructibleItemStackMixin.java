package com.aetheris.aetherenchantments.mixin;

import com.aetheris.aetherenchantments.AetherEnchantments;
import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class IndestructibleItemStackMixin {
    @Inject(
            method = "applyDamage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherenchantments$keepItemAtOneDurability(
            int newDamage,
            ServerPlayer player,
            Consumer<ItemStack> onBreak,
            CallbackInfo ci
    ) {
        ItemStack stack = (ItemStack) (Object) this;
        if (player == null || !stack.isDamageableItem()) {
            return;
        }

        int level = EnchantmentUtil.getLevel(
                player.level(),
                stack,
                AetherEnchantments.INDESTRUCTIBLE
        );
        if (level <= 0 || newDamage < stack.getMaxDamage()) {
            return;
        }

        // applyDamage receives the resulting damage value, not the damage amount.
        // Keep the item one point below its break threshold so it remains usable
        // and can still be repaired normally.
        stack.setDamageValue(Math.max(0, stack.getMaxDamage() - 1));
        ci.cancel();
    }
}
