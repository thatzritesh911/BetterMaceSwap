package com.ritesh.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

// Tick-based logic for AutoBreachSwap and AutoStunSlam is handled in
// BettermaceswapClient via ClientTickEvents. Nothing needed here.
@Mixin(Player.class)
public class ClientPlayerEntityMixin {
}
