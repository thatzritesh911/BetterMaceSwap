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
                GLFW.GLFW_KEY_M,
                CATEGORY
        ));
    }
}