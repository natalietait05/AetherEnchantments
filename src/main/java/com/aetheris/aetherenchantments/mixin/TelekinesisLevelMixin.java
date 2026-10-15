
package com.aetheris.aetherenchantments.mixin;

import com.aetheris.aetherenchantments.telekinesis.Telekinesis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class TelekinesisLevelMixin {

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

        Level level = (Level) (Object) this;

        if (Telekinesis.collectDrop(
                level,
                itemEntity.getItem()
        )) {
            cir.setReturnValue(true);
        }
    }
}
