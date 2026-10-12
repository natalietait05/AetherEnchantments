package com.aetheris.aetherenchantments.mixin;

import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class IndestructiblePlayerMixin {

    @Inject(
            method = "attack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherenchantments$preventAttack(
            Entity target,
            CallbackInfo ci
    ) {
        Player player = (Player) (Object) this;

        if (EnchantmentUtil.isIndestructibleUnusable(
                player.level(),
                player.getMainHandItem()
        )) {
            ci.cancel();
        }
    }
}