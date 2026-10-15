
package com.aetheris.aetherenchantments.mixin;

import com.aetheris.aetherenchantments.telekinesis.Telekinesis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(
            method = "addFreshEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aetherenchantments$collectItemEntity(
            Entity entity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(entity instanceof ItemEntity itemEntity)) {
            return;
        }

        ServerLevel level = (ServerLevel) (Object) this;

        if (Telekinesis.collectDrop(
                level,
                itemEntity.getItem()
        )) {
            cir.setReturnValue(false);
        }
    }
}
