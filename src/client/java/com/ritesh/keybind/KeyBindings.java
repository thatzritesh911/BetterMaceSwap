package com.ritesh.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyBinding BREACH_SWAP_TOGGLE;
    public static KeyBinding STUN_SLAM_TOGGLE;
    public static KeyBinding MACE_MODE_CYCLE;
    public static KeyBinding PEARL_CATCH_TOGGLE;
    public static KeyBinding AIM_LOCK_TOGGLE;
    public static KeyBinding AIM_ASSIST_TOGGLE;
    public static KeyBinding WIND_CHARGE_TOGGLE;
    public static KeyBinding LUNGE_SWAP_TOGGLE;
    public static KeyBinding TRIGGER_BOT_TOGGLE;
    public static KeyBinding AIR_POTS_TOGGLE;
    public static KeyBinding AIM_MODE_TOGGLE;

    private static final KeyBinding.Category CATEGORY =
        KeyBinding.Category.create(Identifier.of("bettermaceswap", "general"));

    public static void register() {
        BREACH_SWAP_TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.breach_swap_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY
        ));

        STUN_SLAM_TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.stun_slam_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                CATEGORY
        ));

        MACE_MODE_CYCLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.mace_mode_cycle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                CATEGORY
        ));

        PEARL_CATCH_TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.pearl_catch_toggle",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                CATEGORY
        ));

        AIM_ASSIST_TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.aim_assist_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                CATEGORY
        ));

        WIND_CHARGE_TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.wind_charge_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                CATEGORY
        ));

        LUNGE_SWAP_TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.lunge_swap_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));

        TRIGGER_BOT_TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.trigger_bot_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                CATEGORY
        ));

        AIR_POTS_TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.air_pots_toggle",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                CATEGORY
        ));

        AIM_MODE_TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bettermaceswap.aim_mode_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));
    }
}