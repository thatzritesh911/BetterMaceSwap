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
    public MaceEnchantMode maceMode;

    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AttributeSwapMode attributeSwapMode;

    public boolean windOnRightClickEnabled;

    public boolean pearlCatchEnabled;

    @BoundedDiscrete(min = 1, max = 5)
    public int pearlCatchDelayTicks;

    @BoundedDiscrete(min = 60, max = 90)
    public int pearlCatchAngle;

    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public ReturnSlotMode pearlReturnMode;

    public boolean aimAssistEnabled;

    @BoundedDiscrete(min = 20, max = 100)
    public int aimSpeed;

    @BoundedDiscrete(min = 20, max = 100)
    public int aimIntensity;

    @BoundedDiscrete(min = 1, max = 5)
    public int aimFallBlocks;

    public boolean aimTargetMobs = true;
    public boolean aimTargetPlayers = true;

    public ModConfig() {
        this.maceMode = MaceEnchantMode.DENSITY;
        this.attributeSwapMode = AttributeSwapMode.ALL;
        this.autoBreachSwapEnabled = true;
        this.autoStunSlamEnabled = true;
        this.combatTargetMobs = true;
        this.combatTargetPlayers = true;
        this.pearlCatchEnabled = true;
        this.pearlCatchDelayTicks = 2;
        this.pearlCatchAngle = 60;
        this.pearlReturnMode = ReturnSlotMode.PREVIOUS;
        this.windOnRightClickEnabled = true;
        this.aimAssistEnabled = false;
        this.aimSpeed = 15;
        this.aimIntensity = 25;
        this.aimFallBlocks = 2;
        this.aimTargetMobs = true;
        this.aimTargetPlayers = true;
    }

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    public static enum MaceEnchantMode {
        DENSITY,
        BREACH;

        @Override
        public String toString() {
            return this == DENSITY ? "Density" : "Breach";
        }
    }

    public static enum AttributeSwapMode {
        ALL("All"),
        WEAPONS_ONLY("Weapons Only");

        private final String label;

        AttributeSwapMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static enum ReturnSlotMode {
        PREVIOUS,
        SWORD,
        AXE
    }
}