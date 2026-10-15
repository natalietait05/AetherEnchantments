
package com.aetheris.aetherenchantments.mixin;

import com.aetheris.aetherenchantments.telekinesis.Telekinesis;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(
            method = "destroyBlock",
            at = @At("HEAD")
    )
    private void aetherenchantments$beginBlockBreak(
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Telekinesis.beginBlockBreak(player.level(), player);
    }

    @Inject(
            method = "destroyBlock",
            at = @At("RETURN")
    )
    private void aetherenchantments$endBlockBreak(
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Telekinesis.endBlockBreak();
    }
}
