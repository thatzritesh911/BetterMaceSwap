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

    // NEW: Target filters specifically for Combat/Mace features
    @Category("Combat")
    public boolean combatTargetMobs = true;

    @Category("Combat")
    public boolean combatTargetPlayers = true;

    @Category("Combat")
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public MaceEnchantMode maceMode;

    @Category("Combat")
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public AttributeSwapMode attributeSwapMode;

    @Category("Combat")
    public boolean windOnRightClickEnabled;

    @Category("Pearl")
    public boolean pearlCatchEnabled;

    @Category("Pearl")
    @BoundedDiscrete(min = 1, max = 5)
    public int pearlCatchDelayTicks;

    @Category("Pearl")
    @BoundedDiscrete(min = 60, max = 90)
    public int pearlCatchAngle;

    @Category("Pearl")
    @EnumHandler(option = EnumDisplayOption.BUTTON)
    public ReturnSlotMode pearlReturnMode;

    @Category("Aim Assist")
    public boolean aimAssistEnabled;

    @Category("Aim Assist")
    @BoundedDiscrete(min = 20, max = 100)
    public int aimSpeed;

    @Category("Aim Assist")
    @BoundedDiscrete(min = 20, max = 100)
    public int aimIntensity;

    @Category("Aim Assist")
    @BoundedDiscrete(min = 1, max = 5)
    public int aimFallBlocks;

    @Category("Aim Assist")
    public boolean aimTargetMobs = true;

    @Category("Aim Assist")
    public boolean aimTargetPlayers = true;

    public ModConfig() {
        this.maceMode = MaceEnchantMode.DENSITY;
        this.attributeSwapMode = AttributeSwapMode.ALL;
        
        // Combat category defaults
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

    @Environment(EnvType.CLIENT)
    public static enum MaceEnchantMode {
        DENSITY,
        BREACH;

        @Override
        public String toString() {
            return this == DENSITY ? "Density" : "Breach";
        }

        public Text asText() {
            return this == DENSITY
                    ? Text.literal("Density").formatted(Formatting.GREEN)
                    : Text.literal("Breach").formatted(Formatting.GOLD);
        }
    }

    @Environment(EnvType.CLIENT)
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

    @Environment(EnvType.CLIENT)
    public static enum ReturnSlotMode {
        PREVIOUS,
        SWORD,
        AXE
    }
}