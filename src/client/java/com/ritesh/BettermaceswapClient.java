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
                ModConfig.save();
                client.player.sendMessage(Text.literal("Breach Swap: ").append(Text.literal(cfg.autoBreachSwapEnabled ? "ON" : "OFF").formatted(cfg.autoBreachSwapEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }

            if (KeyBindings.STUN_SLAM_TOGGLE.wasPressed()) {
                cfg.autoStunSlamEnabled = !cfg.autoStunSlamEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Stun Slam: ").append(Text.literal(cfg.autoStunSlamEnabled ? "ON" : "OFF").formatted(cfg.autoStunSlamEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }

            if (KeyBindings.MACE_MODE_CYCLE.wasPressed()) {
                cfg.maceMode = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY)
                        ? ModConfig.MaceEnchantMode.BREACH
                        : ModConfig.MaceEnchantMode.DENSITY;
                ModConfig.save();
                boolean isDensity = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY);
                client.player.sendMessage(Text.literal("Mace Mode: ").append(Text.literal(isDensity ? "Density" : "Breach").formatted(isDensity ? Formatting.YELLOW : Formatting.DARK_PURPLE)), true);
            }

            if (KeyBindings.PEARL_CATCH_TOGGLE.wasPressed()) {
                cfg.pearlCatchEnabled = !cfg.pearlCatchEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Pearl Catch: ").append(Text.literal(cfg.pearlCatchEnabled ? "ON" : "OFF").formatted(cfg.pearlCatchEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }

            if (KeyBindings.AIM_LOCK_TOGGLE.wasPressed()) {
                AutoFeatures.toggleAimLock(client.player, client);
                boolean active = AutoFeatures.isAimLockActive();
                client.player.sendMessage(Text.literal("Aim Lock: ").append(Text.literal(active ? "ON" : "OFF").formatted(active ? Formatting.GREEN : Formatting.RED)), true);
            }

            if (KeyBindings.AIM_ASSIST_TOGGLE.wasPressed()) {
                cfg.aimAssistEnabled = !cfg.aimAssistEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Aim Assist: ").append(Text.literal(cfg.aimAssistEnabled ? "ON" : "OFF").formatted(cfg.aimAssistEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }

            if (KeyBindings.WIND_CHARGE_TOGGLE.wasPressed()) {
                cfg.windOnRightClickEnabled = !cfg.windOnRightClickEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Wind Charge: ").append(Text.literal(cfg.windOnRightClickEnabled ? "ON" : "OFF").formatted(cfg.windOnRightClickEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }

            AutoFeatures.tick(client);
        });
    }
}