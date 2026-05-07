package com.ritesh.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;

@Config(name = "bettermaceswap")
public class ModConfig implements ConfigData {

    public boolean autoBreachSwapEnabled = true;
    public boolean smartSwitchEnabled = false;

    @BoundedDiscrete(min = 1, max = 5)
    public int breachSwapBackTicks = 3;

    public boolean autoStunSlamEnabled = true;
    public boolean combatTargetMobs = true;
    public boolean combatTargetPlayers = true;

    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public MaceEnchantMode maceMode = MaceEnchantMode.DENSITY;

    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AttributeSwapMode attributeSwapMode = AttributeSwapMode.ALL;

    public boolean windOnRightClickEnabled = true;

    public boolean pearlCatchEnabled = true;

    @BoundedDiscrete(min = 1, max = 5)
    public int pearlCatchDelayTicks = 2;

    @BoundedDiscrete(min = 60, max = 90)
    public int pearlCatchAngle = 60;

    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public ReturnSlotMode pearlReturnMode = ReturnSlotMode.PREVIOUS;

    public boolean aimAssistEnabled = false;

    @BoundedDiscrete(min = 20, max = 100)
    public int aimSpeed = 15;

    @BoundedDiscrete(min = 1, max = 25)
    public int aimIntensity = 15;

    @BoundedDiscrete(min = 1, max = 5)
    public int aimFallBlocks = 2;

    public boolean aimTargetMobs = true;
    public boolean aimTargetPlayers = true;

    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AimBone aimBone = AimBone.EYE;

    public boolean triggerBotEnabled = false;
    public boolean triggerBotFallingOnly = false;

    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AimMode aimMode = AimMode.WHILE_FALLING;

    public boolean lungeSwapEnabled = false;

    @BoundedDiscrete(min = 0, max = 3)
    public int lungeSwapDelay = 2;

    public boolean airPotsEnabled = false;
    public boolean autoChestplateOnTrigger = false;

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    public enum MaceEnchantMode {
        DENSITY, BREACH;
        @Override public String toString() { return this == DENSITY ? "Density" : "Breach"; }
    }

    public enum AimMode {
        WHILE_FALLING, ALWAYS;
        @Override public String toString() { return this == WHILE_FALLING ? "While Falling" : "Always"; }
    }

    public enum AttributeSwapMode {
        ALL("All"), WEAPONS_ONLY("Weapons Only");
        private final String label;
        AttributeSwapMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public enum ReturnSlotMode { PREVIOUS, SWORD, AXE, ELYTRA }

    public enum AimBone {
        EYE, CHEST, LEGS;
        @Override public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    }
}
