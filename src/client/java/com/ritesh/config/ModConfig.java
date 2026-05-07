package com.ritesh.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Config(name = "bettermaceswap")
@Environment(EnvType.CLIENT)
public class ModConfig implements ConfigData {

    @Category("Combat")
    public boolean autoBreachSwapEnabled = true;
    @Category("Combat")
    public boolean smartSwitchEnabled = false;
    @Category("Combat")
    @BoundedDiscrete(min = 1, max = 5)
    public int breachSwapBackTicks = 3;
    @Category("Combat")
    public boolean autoStunSlamEnabled = true;
    @Category("Combat")
    public boolean combatTargetMobs = true;
    @Category("Combat")
    public boolean combatTargetPlayers = true;
    @Category("Combat")
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public MaceEnchantMode maceMode = MaceEnchantMode.DENSITY;
    @Category("Combat")
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AttributeSwapMode attributeSwapMode = AttributeSwapMode.ALL;

    @Category("Pearl")
    public boolean pearlCatchEnabled = true;
    @Category("Pearl")
    @BoundedDiscrete(min = 1, max = 5)
    public int pearlCatchDelayTicks = 2;
    @Category("Pearl")
    @BoundedDiscrete(min = 60, max = 90)
    public int pearlCatchAngle = 60;
    @Category("Pearl")
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public ReturnSlotMode pearlReturnMode = ReturnSlotMode.PREVIOUS;

    @Category("Aim Assist")
    public boolean aimAssistEnabled = false;
    @Category("Aim Assist")
    @BoundedDiscrete(min = 20, max = 100)
    public int aimSpeed = 15;
    @Category("Aim Assist")
    @BoundedDiscrete(min = 1, max = 25)
    public int aimIntensity = 15;
    @Category("Aim Assist")
    public boolean aimTargetMobs = true;
    @Category("Aim Assist")
    public boolean aimTargetPlayers = true;
    @Category("Aim Assist")
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AimBone aimBone = AimBone.EYE;
    @Category("Aim Assist")
    public boolean triggerBotEnabled = false;
    @Category("Aim Assist")
    public boolean triggerBotFallingOnly = false;
    @Category("Aim Assist")
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AimMode aimMode = AimMode.WHILE_FALLING;

    @Category("Misc")
    public boolean lungeSwapEnabled = false;
    @Category("Misc")
    @BoundedDiscrete(min = 0, max = 3)
    public int lungeSwapDelay = 2;
    @Category("Misc")
    public boolean windOnRightClickEnabled = true;
    @Category("Misc")
    public boolean airPotsEnabled = false;
    @Category("Misc")
    public boolean autoChestplateOnTrigger = false;

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    @Environment(EnvType.CLIENT)
    public enum MaceEnchantMode {
        DENSITY, BREACH;
        @Override public String toString() { return this == DENSITY ? "Density" : "Breach"; }
        public Text asText() {
            return this == DENSITY ? Text.literal("Density").formatted(Formatting.GREEN) : Text.literal("Breach").formatted(Formatting.GOLD);
        }
    }

    @Environment(EnvType.CLIENT)
    public enum AimMode {
        WHILE_FALLING, ALWAYS;
        @Override public String toString() { return this == WHILE_FALLING ? "While Falling" : "Always"; }
        public Text asText() {
            return this == WHILE_FALLING ? Text.literal("While Falling").formatted(Formatting.YELLOW) : Text.literal("Always").formatted(Formatting.AQUA);
        }
    }

    @Environment(EnvType.CLIENT)
    public enum AttributeSwapMode {
        ALL("All"), WEAPONS_ONLY("Weapons Only");
        private final String label;
        AttributeSwapMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    @Environment(EnvType.CLIENT)
    public enum ReturnSlotMode { PREVIOUS, SWORD, AXE, ELYTRA }

    @Environment(EnvType.CLIENT)
    public enum AimBone {
        EYE, CHEST, LEGS;
        @Override public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    }
}