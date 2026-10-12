
package com.aetheris.aetherenchantments.mixin;

import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiConsumer;

@Mixin(LivingEntity.class)
public abstract class IndestructibleArmorAttributesMixin {

    @Redirect(
            method = "collectEquipmentChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"
            )
    )
    private void aetherenchantments$disableUnusableEquipmentAttributes(
            ItemStack stack,
            EquipmentSlot slot,
            BiConsumer<Holder<Attribute>, AttributeModifier> consumer
    ) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (EnchantmentUtil.isIndestructibleUnusable(entity.level(), stack)) {
            return;
        }

        stack.forEachModifier(slot, consumer);
    }
}
