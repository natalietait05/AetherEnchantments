package com.aetheris.aetherenchantments.mixin;

import com.aetheris.aetherenchantments.AetherEnchantments;
import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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

        if (level <= 0) {
            return;
        }

        int maxDamage = stack.getMaxDamage();

        // Damage value that leaves exactly one durability point.
        int lastDurabilityDamage = maxDamage - 1;

        // The item has not reached its last durability point yet.
        if (newDamage < lastDurabilityDamage) {
            return;
        }

        // Check whether this is the first time reaching one durability.
        boolean justReachedLastDurability =
                stack.getDamageValue() < lastDurabilityDamage;

        // Keep the item at one durability point.
        stack.setDamageValue(lastDurabilityDamage);

        // Play the break sound only once, when reaching one durability.
        if (justReachedLastDurability) {

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ITEM_BREAK,
                    player.getSoundSource(),
                    1.0F,
                    1.0F
            );
        }

        ci.cancel();
    }
}