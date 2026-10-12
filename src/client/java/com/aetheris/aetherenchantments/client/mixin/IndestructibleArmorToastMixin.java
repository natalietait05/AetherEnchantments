
package com.aetheris.aetherenchantments.client.mixin;

import com.aetheris.aetherenchantments.client.AetherEnchantmentsToast;
import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class IndestructibleArmorToastMixin {

    @Unique
    private static final EquipmentSlot[] AETHERENCHANTMENTS_ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    @Unique
    private final ItemStack[] aetherenchantments$previousArmorStacks =
            new ItemStack[4];

    @Unique
    private final boolean[] aetherenchantments$previousUnusable =
            new boolean[4];

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void aetherenchantments$checkArmorToast(CallbackInfo ci) {

        LivingEntity entity = (LivingEntity) (Object) this;

        // Solo comprobar al jugador local.
        if (entity != Minecraft.getInstance().player) {
            return;
        }

        if (!entity.level().isClientSide()) {
            return;
        }

        for (int i = 0; i < AETHERENCHANTMENTS_ARMOR_SLOTS.length; i++) {

            EquipmentSlot slot = AETHERENCHANTMENTS_ARMOR_SLOTS[i];

            ItemStack currentStack = entity.getItemBySlot(slot);

            boolean unusable =
                    EnchantmentUtil.isIndestructibleUnusable(
                            entity.level(),
                            currentStack
                    );

            boolean changedStack =
                    aetherenchantments$previousArmorStacks[i] != currentStack;

            boolean becameUnusable =
                    unusable && !aetherenchantments$previousUnusable[i];

            if (unusable && (changedStack || becameUnusable)) {
                AetherEnchantmentsToast.showUnusable(currentStack);
            }

            aetherenchantments$previousArmorStacks[i] = currentStack;
            aetherenchantments$previousUnusable[i] = unusable;
        }
    }
}
