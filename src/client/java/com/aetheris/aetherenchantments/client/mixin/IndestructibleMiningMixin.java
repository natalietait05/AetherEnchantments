package com.aetheris.aetherenchantments.client.mixin;

import com.aetheris.aetherenchantments.enchantment.EnchantmentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.aetheris.aetherenchantments.client.AetherEnchantmentsToast;

@Mixin(MultiPlayerGameMode.class)
public abstract class IndestructibleMiningMixin {

    @Inject(
            method = "startDestroyBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherenchantments$preventMining(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (EnchantmentUtil.isIndestructibleUnusable(
                player.level(),
                player.getMainHandItem()
        )) {
            AetherEnchantmentsToast.showUnusable(
                    player.getMainHandItem()
            );

            cir.setReturnValue(false);
        }

        if (EnchantmentUtil.isIndestructibleUnusable(
                player.level(),
                player.getMainHandItem()
        )) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "continueDestroyBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherenchantments$preventContinuingMining(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (EnchantmentUtil.isIndestructibleUnusable(
                player.level(),
                player.getMainHandItem()
        )) {
            cir.setReturnValue(false);
        }
    }
}