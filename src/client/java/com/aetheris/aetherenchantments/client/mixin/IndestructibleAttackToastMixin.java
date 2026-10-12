package com.aetheris.aetherenchantments.client.mixin;

import com.aetheris.aetherenchantments.client.AetherEnchantmentsToast;
import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class IndestructibleAttackToastMixin {

    @Inject(
            method = "attack",
            at = @At("HEAD")
    )
    private void aetherenchantments$showAttackToast(
            Entity target,
            CallbackInfo ci
    ) {
        Player player = (Player) (Object) this;

        if (!player.level().isClientSide()) {
            return;
        }

        if (EnchantmentUtil.isIndestructibleUnusable(
                player.level(),
                player.getMainHandItem()
        )) {
            AetherEnchantmentsToast.showUnusable(
                    player.getMainHandItem()
            );
        }
    }
}