package com.ritesh.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Config(name = "bettermaceswap")
public class ModConfig implements ConfigData {

    public boolean autoBreachSwapEnabled = false;

    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int breachSwapBackTicks = 3;

    public boolean autoStunSlamEnabled = false;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public MaceEnchantMode maceMode = MaceEnchantMode.DENSITY;

    public boolean attackPlayers = true;
    public boolean attackMobs = true;
    public boolean attackAnimals = false;

    public boolean pearlCatchEnabled = false;

    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int pearlCatchDelayTicks = 10;

    public enum MaceEnchantMode {
        DENSITY,
        BREACH;

        @Override
        public String toString() {
            return this == DENSITY ? "Density" : "Breach";
        }

        public Text asText() {
            if (this == DENSITY) {
                return Text.literal("Density").formatted(Formatting.YELLOW);
            } else {
                return Text.literal("Breach").formatted(Formatting.DARK_PURPLE);
            }
        }
    }

    public static ModConfig get() {
        return me.shedaniel.autoconfig.AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}