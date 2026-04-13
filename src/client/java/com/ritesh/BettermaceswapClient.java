package com.ritesh;

import com.ritesh.config.ModConfig;
import com.ritesh.feature.AutoFeatures;
import com.ritesh.keybind.KeyBindings;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

public class BettermaceswapClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        KeyBindings.register();

        UseItemCallback.EVENT.register((player, world, hand) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && player == client.player) {
                if (player.getStackInHand(hand).isOf(Items.ENDER_PEARL)) {
                    AutoFeatures.onPearlThrown((ClientPlayerEntity) player);
                }
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            ModConfig cfg = ModConfig.get();

            if (KeyBindings.BREACH_SWAP_TOGGLE.wasPressed()) {
                cfg.autoBreachSwapEnabled = !cfg.autoBreachSwapEnabled;
                AutoConfig.getConfigHolder(ModConfig.class).save();
                client.player.sendMessage(Text.literal("Attribute Swap: ").append(Text.literal(cfg.autoBreachSwapEnabled ? "ON" : "OFF").formatted(cfg.autoBreachSwapEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }

            if (KeyBindings.STUN_SLAM_TOGGLE.wasPressed()) {
                cfg.autoStunSlamEnabled = !cfg.autoStunSlamEnabled;
                AutoConfig.getConfigHolder(ModConfig.class).save();
                client.player.sendMessage(Text.literal("Stun Slam: ").append(Text.literal(cfg.autoStunSlamEnabled ? "ON" : "OFF").formatted(cfg.autoStunSlamEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }

            if (KeyBindings.MACE_MODE_CYCLE.wasPressed()) {
                cfg.maceMode = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY) ? ModConfig.MaceEnchantMode.BREACH : ModConfig.MaceEnchantMode.DENSITY;
                AutoConfig.getConfigHolder(ModConfig.class).save();

                boolean isDensity = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY);
                client.player.sendMessage(
                        Text.literal("Mode: ")
                                .append(Text.literal(isDensity ? "Density" : "Breach")
                                        .formatted(isDensity ? Formatting.YELLOW : Formatting.DARK_PURPLE)),
                        true
                );
            }

            if (KeyBindings.PEARL_CATCH_TOGGLE.wasPressed()) {
                cfg.pearlCatchEnabled = !cfg.pearlCatchEnabled;
                AutoConfig.getConfigHolder(ModConfig.class).save();
                client.player.sendMessage(Text.literal("Pearl Catch: ").append(Text.literal(cfg.pearlCatchEnabled ? "ON" : "OFF").formatted(cfg.pearlCatchEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }

            AutoFeatures.tick(client);
        });
    }
}