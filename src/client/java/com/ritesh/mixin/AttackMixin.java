package com.ritesh.mixin;

import com.ritesh.feature.AutoFeatures; // Corrected import
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class AttackMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        // Ensure we only run logic if the player is the local client player
        if (player instanceof ClientPlayerEntity clientPlayer) {
            // This now triggers the single manager that handles 
            // Stun Slam first, then Breach Swap if Stun Slam fails.
            AutoFeatures.onHit(clientPlayer, target);
        }
    }
}