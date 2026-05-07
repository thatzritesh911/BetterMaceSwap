package com.ritesh;

import com.ritesh.config.ModConfig;
import com.ritesh.feature.AutoFeatures;
import com.ritesh.keybind.KeyBindings;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResult;

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

        UseItemCallback.EVENT.register((player, world, hand) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && player == client.player) {
                if (player.getItemInHand(hand).is(Items.ENDER_PEARL)) {
                    AutoFeatures.onPearlThrown(client.player);
                }
                if (player.getItemInHand(hand).is(Items.SPLASH_POTION)) {
                    AutoFeatures.onSplashPotThrown(client.player);
                }
            }
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
    if (client.player == null) return;
    AutoFeatures.onRender(client);
    ModConfig cfg = ModConfig.get();

            boolean breachPressed = KeyBindings.BREACH_SWAP_TOGGLE.isDown();
            if (!breachPressed && wasBreachSwapPressed) {
                cfg.autoBreachSwapEnabled = !cfg.autoBreachSwapEnabled;
                ModConfig.save();
                client.gui.setOverlayMessage(Component.literal("Attribute Swap: ")
                        .append(Component.literal(cfg.autoBreachSwapEnabled ? "ON" : "OFF")
                                .withStyle(cfg.autoBreachSwapEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
            }
            wasBreachSwapPressed = breachPressed;

            boolean stunPressed = KeyBindings.STUN_SLAM_TOGGLE.isDown();
            if (!stunPressed && wasStunSlamPressed) {
                cfg.autoStunSlamEnabled = !cfg.autoStunSlamEnabled;
                ModConfig.save();
                client.gui.setOverlayMessage(Component.literal("Stun Slam: ")
                        .append(Component.literal(cfg.autoStunSlamEnabled ? "ON" : "OFF")
                                .withStyle(cfg.autoStunSlamEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
            }
            wasStunSlamPressed = stunPressed;

            boolean cyclePressed = KeyBindings.MACE_MODE_CYCLE.isDown();
            if (!cyclePressed && wasMaceCyclePressed) {
                cfg.maceMode = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY) ? ModConfig.MaceEnchantMode.BREACH : ModConfig.MaceEnchantMode.DENSITY;
                ModConfig.save();
                boolean isDensity = cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY;
                client.gui.setOverlayMessage(Component.literal("Mace Mode: ")
                        .append(Component.literal(isDensity ? "Density" : "Breach")
                                .withStyle(isDensity ? ChatFormatting.YELLOW : ChatFormatting.DARK_PURPLE)), false);
            }
            wasMaceCyclePressed = cyclePressed;

            boolean pearlPressed = KeyBindings.PEARL_CATCH_TOGGLE.isDown();
            if (!pearlPressed && wasPearlCatchPressed) {
                cfg.pearlCatchEnabled = !cfg.pearlCatchEnabled;
                ModConfig.save();
                client.gui.setOverlayMessage(Component.literal("Pearl Catch: ")
                        .append(Component.literal(cfg.pearlCatchEnabled ? "ON" : "OFF")
                                .withStyle(cfg.pearlCatchEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
            }
            wasPearlCatchPressed = pearlPressed;

            boolean assistPressed = KeyBindings.AIM_ASSIST_TOGGLE.isDown();
            if (!assistPressed && wasAimAssistPressed) {
                cfg.aimAssistEnabled = !cfg.aimAssistEnabled;
                ModConfig.save();
                client.gui.setOverlayMessage(Component.literal("Aim Assist: ")
                        .append(Component.literal(cfg.aimAssistEnabled ? "ON" : "OFF")
                                .withStyle(cfg.aimAssistEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
            }
            wasAimAssistPressed = assistPressed;

            boolean windPressed = KeyBindings.WIND_CHARGE_TOGGLE.isDown();
            if (!windPressed && wasWindChargePressed) {
                cfg.windOnRightClickEnabled = !cfg.windOnRightClickEnabled;
                ModConfig.save();
                client.gui.setOverlayMessage(Component.literal("Wind Charge: ")
                        .append(Component.literal(cfg.windOnRightClickEnabled ? "ON" : "OFF")
                                .withStyle(cfg.windOnRightClickEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
            }
            wasWindChargePressed = windPressed;

            boolean lungePressed = KeyBindings.LUNGE_SWAP_TOGGLE.isDown();
            if (!lungePressed && wasLungeSwapPressed) {
                cfg.lungeSwapEnabled = !cfg.lungeSwapEnabled;
                ModConfig.save();
                client.gui.setOverlayMessage(Component.literal("Lunge Swap: ")
                        .append(Component.literal(cfg.lungeSwapEnabled ? "ON" : "OFF")
                                .withStyle(cfg.lungeSwapEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
            }
            wasLungeSwapPressed = lungePressed;

            boolean triggerPressed = KeyBindings.TRIGGER_BOT_TOGGLE.isDown();
            if (!triggerPressed && wasTriggerBotPressed) {
                cfg.triggerBotEnabled = !cfg.triggerBotEnabled;
                ModConfig.save();
                client.gui.setOverlayMessage(Component.literal("Trigger Bot: ")
                        .append(Component.literal(cfg.triggerBotEnabled ? "ON" : "OFF")
                                .withStyle(cfg.triggerBotEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
            }
            wasTriggerBotPressed = triggerPressed;

            boolean airPotsPressed = KeyBindings.AIR_POTS_TOGGLE.isDown();
            if (!airPotsPressed && wasAirPotsPressed) {
                cfg.airPotsEnabled = !cfg.airPotsEnabled;
                ModConfig.save();
                client.gui.setOverlayMessage(Component.literal("Air Pots: ")
                        .append(Component.literal(cfg.airPotsEnabled ? "ON" : "OFF")
                                .withStyle(cfg.airPotsEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
            }
            wasAirPotsPressed = airPotsPressed;

            boolean aimModePressed = KeyBindings.AIM_MODE_TOGGLE.isDown();
            if (!aimModePressed && wasAimModePressed) {
                cfg.aimMode = (cfg.aimMode == ModConfig.AimMode.WHILE_FALLING) ? ModConfig.AimMode.ALWAYS : ModConfig.AimMode.WHILE_FALLING;
                ModConfig.save();
                client.gui.setOverlayMessage(Component.literal("Aim Mode: ")
                        .append(Component.literal(cfg.aimMode.toString())
                                .withStyle(cfg.aimMode == ModConfig.AimMode.ALWAYS ? ChatFormatting.AQUA : ChatFormatting.YELLOW)), false);
            }
            wasAimModePressed = aimModePressed;

            AutoFeatures.tick(client);
        });
    }
}
