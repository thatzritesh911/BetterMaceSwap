package com.ritesh;

import com.ritesh.config.ModConfig;
import com.ritesh.feature.AutoFeatures;
import com.ritesh.keybind.KeyBindings;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

public class BettermaceswapClient implements ClientModInitializer {

    private boolean wasBreachSwapPressed = false;
    private boolean wasStunSlamPressed = false;
    private boolean wasMaceCyclePressed = false;
    private boolean wasPearlCatchPressed = false;
    private boolean wasAimAssistPressed = false;
    private boolean wasWindChargePressed = false;
    private boolean wasLungeSwapPressed = false;
    private boolean wasTriggerBotPressed = false;
    private boolean wasAirPotsPressed = false;
    private boolean wasAimModePressed = false;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        KeyBindings.register();

        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            AutoFeatures.onRender(MinecraftClient.getInstance());
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && player == client.player) {
                if (player.getStackInHand(hand).isOf(Items.ENDER_PEARL)) {
                    AutoFeatures.onPearlThrown((ClientPlayerEntity) player);
                }
                if (player.getStackInHand(hand).isOf(Items.SPLASH_POTION)) {
                    AutoFeatures.onSplashPotThrown((ClientPlayerEntity) player);
                }
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            ModConfig cfg = ModConfig.get();

            boolean breachPressed = KeyBindings.BREACH_SWAP_TOGGLE.isPressed();
            if (!breachPressed && wasBreachSwapPressed) {
                cfg.autoBreachSwapEnabled = !cfg.autoBreachSwapEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Attribute Swap: ").append(Text.literal(cfg.autoBreachSwapEnabled ? "ON" : "OFF").formatted(cfg.autoBreachSwapEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }
            wasBreachSwapPressed = breachPressed;

            boolean stunPressed = KeyBindings.STUN_SLAM_TOGGLE.isPressed();
            if (!stunPressed && wasStunSlamPressed) {
                cfg.autoStunSlamEnabled = !cfg.autoStunSlamEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Stun Slam: ").append(Text.literal(cfg.autoStunSlamEnabled ? "ON" : "OFF").formatted(cfg.autoStunSlamEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }
            wasStunSlamPressed = stunPressed;

            boolean cyclePressed = KeyBindings.MACE_MODE_CYCLE.isPressed();
            if (!cyclePressed && wasMaceCyclePressed) {
                cfg.maceMode = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY) ? ModConfig.MaceEnchantMode.BREACH : ModConfig.MaceEnchantMode.DENSITY;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Mace Mode: ").append(cfg.maceMode.asText()), true);
            }
            wasMaceCyclePressed = cyclePressed;

            boolean pearlPressed = KeyBindings.PEARL_CATCH_TOGGLE.isPressed();
            if (!pearlPressed && wasPearlCatchPressed) {
                cfg.pearlCatchEnabled = !cfg.pearlCatchEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Pearl Catch: ").append(Text.literal(cfg.pearlCatchEnabled ? "ON" : "OFF").formatted(cfg.pearlCatchEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }
            wasPearlCatchPressed = pearlPressed;

            boolean assistPressed = KeyBindings.AIM_ASSIST_TOGGLE.isPressed();
            if (!assistPressed && wasAimAssistPressed) {
                cfg.aimAssistEnabled = !cfg.aimAssistEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Aim Assist: ").append(Text.literal(cfg.aimAssistEnabled ? "ON" : "OFF").formatted(cfg.aimAssistEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }
            wasAimAssistPressed = assistPressed;

            boolean windPressed = KeyBindings.WIND_CHARGE_TOGGLE.isPressed();
            if (!windPressed && wasWindChargePressed) {
                cfg.windOnRightClickEnabled = !cfg.windOnRightClickEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Wind Charge: ").append(Text.literal(cfg.windOnRightClickEnabled ? "ON" : "OFF").formatted(cfg.windOnRightClickEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }
            wasWindChargePressed = windPressed;

            boolean lungePressed = KeyBindings.LUNGE_SWAP_TOGGLE.isPressed();
            if (!lungePressed && wasLungeSwapPressed) {
                cfg.lungeSwapEnabled = !cfg.lungeSwapEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Lunge Swap: ").append(Text.literal(cfg.lungeSwapEnabled ? "ON" : "OFF").formatted(cfg.lungeSwapEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }
            wasLungeSwapPressed = lungePressed;

            boolean triggerPressed = KeyBindings.TRIGGER_BOT_TOGGLE.isPressed();
            if (!triggerPressed && wasTriggerBotPressed) {
                cfg.triggerBotEnabled = !cfg.triggerBotEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Trigger Bot: ").append(Text.literal(cfg.triggerBotEnabled ? "ON" : "OFF").formatted(cfg.triggerBotEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }
            wasTriggerBotPressed = triggerPressed;

            boolean airPotsPressed = KeyBindings.AIR_POTS_TOGGLE.isPressed();
            if (!airPotsPressed && wasAirPotsPressed) {
                cfg.airPotsEnabled = !cfg.airPotsEnabled;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Air Pots: ").append(Text.literal(cfg.airPotsEnabled ? "ON" : "OFF").formatted(cfg.airPotsEnabled ? Formatting.GREEN : Formatting.RED)), true);
            }
            wasAirPotsPressed = airPotsPressed;

            boolean aimModePressed = KeyBindings.AIM_MODE_TOGGLE.isPressed();
            if (!aimModePressed && wasAimModePressed) {
                cfg.aimMode = (cfg.aimMode == ModConfig.AimMode.WHILE_FALLING) ? ModConfig.AimMode.ALWAYS : ModConfig.AimMode.WHILE_FALLING;
                ModConfig.save();
                client.player.sendMessage(Text.literal("Aim Mode: ").append(cfg.aimMode.asText()), true);
            }
            wasAimModePressed = aimModePressed;

            AutoFeatures.tick(client);
        }); // Closes END_CLIENT_TICK
    } // Closes onInitializeClient
} // Closes class