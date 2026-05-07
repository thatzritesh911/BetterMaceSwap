package com.ritesh.mixin;

import com.ritesh.feature.AutoFeatures; 
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class LungeAttackMixin {

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        
        if (client.player != null) {
            // This should now recognize AutoFeatures
            AutoFeatures.onAttack(client.player);
        }
    }
}