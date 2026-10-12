
package com.aetheris.aetherenchantments.mixin;

import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentHelper.class)
public abstract class IndestructibleArmorEnchantmentsMixin {

    @Inject(
            method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void aetherenchantments$disableUnusableEquipmentEnchantments(
            ItemStack piece,
            EquipmentSlot slot,
            LivingEntity owner,
            EnchantmentHelper.EnchantmentInSlotVisitor method,
            CallbackInfo ci
    ) {
        if (EnchantmentUtil.isIndestructibleUnusable(owner.level(), piece)) {
            ci.cancel();
        }
    }
}
