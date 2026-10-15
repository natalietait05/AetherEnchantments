
package com.aetheris.aetherenchantments.mixin;

import com.aetheris.aetherenchantments.telekinesis.Telekinesis;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(net.minecraft.world.level.block.Block.class)
public abstract class TelekinesisBlockMixin {

    @Inject(
            method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void aetherenchantments$collectBlockDrop(
            Level level,
            Supplier<ItemEntity> entityFactory,
            ItemStack stack,
            CallbackInfo ci
    ) {
        if (Telekinesis.collectDrop(level, stack)) {
            ci.cancel();
        }
    }
}
