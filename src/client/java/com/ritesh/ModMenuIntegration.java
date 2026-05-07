package com.ritesh;

import com.ritesh.config.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ModConfig config = ModConfig.get();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("text.autoconfig.bettermaceswap.title"))
                    .setSavingRunnable(() -> AutoConfig.getConfigHolder(ModConfig.class).save());

            ConfigEntryBuilder eb = builder.entryBuilder();

            ConfigCategory combat = builder.getOrCreateCategory(Component.translatable("text.autoconfig.bettermaceswap.category.Combat"));
            combat.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.autoBreachSwapEnabled"), config.autoBreachSwapEnabled).setDefaultValue(true).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.autoBreachSwapEnabled.@Tooltip")).setSaveConsumer(v -> config.autoBreachSwapEnabled = v).build());
            combat.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.smartSwitchEnabled"), config.smartSwitchEnabled).setDefaultValue(false).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.smartSwitchEnabled.@Tooltip")).setSaveConsumer(v -> config.smartSwitchEnabled = v).build());
            combat.addEntry(eb.startIntSlider(Component.translatable("text.autoconfig.bettermaceswap.option.breachSwapBackTicks"), config.breachSwapBackTicks, 1, 5).setDefaultValue(3).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.breachSwapBackTicks.@Tooltip")).setSaveConsumer(v -> config.breachSwapBackTicks = v).build());
            combat.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.autoStunSlamEnabled"), config.autoStunSlamEnabled).setDefaultValue(true).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.autoStunSlamEnabled.@Tooltip")).setSaveConsumer(v -> config.autoStunSlamEnabled = v).build());
            combat.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.combatTargetPlayers"), config.combatTargetPlayers).setDefaultValue(true).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.combatTargetPlayers.@Tooltip")).setSaveConsumer(v -> config.combatTargetPlayers = v).build());
            combat.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.combatTargetMobs"), config.combatTargetMobs).setDefaultValue(true).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.combatTargetMobs.@Tooltip")).setSaveConsumer(v -> config.combatTargetMobs = v).build());
            combat.addEntry(eb.startSelector(Component.translatable("text.autoconfig.bettermaceswap.option.maceMode"), ModConfig.MaceEnchantMode.values(), config.maceMode).setDefaultValue(ModConfig.MaceEnchantMode.DENSITY).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.maceMode.@Tooltip")).setSaveConsumer(v -> config.maceMode = (ModConfig.MaceEnchantMode) v).build());
            combat.addEntry(eb.startSelector(Component.translatable("text.autoconfig.bettermaceswap.option.attributeSwapMode"), ModConfig.AttributeSwapMode.values(), config.attributeSwapMode).setDefaultValue(ModConfig.AttributeSwapMode.ALL).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.attributeSwapMode.@Tooltip")).setSaveConsumer(v -> config.attributeSwapMode = (ModConfig.AttributeSwapMode) v).build());
            combat.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.windOnRightClickEnabled"), config.windOnRightClickEnabled).setDefaultValue(true).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.windOnRightClickEnabled.@Tooltip")).setSaveConsumer(v -> config.windOnRightClickEnabled = v).build());

            ConfigCategory pearl = builder.getOrCreateCategory(Component.translatable("text.autoconfig.bettermaceswap.category.Pearl"));
            pearl.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.pearlCatchEnabled"), config.pearlCatchEnabled).setDefaultValue(true).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.pearlCatchEnabled.@Tooltip")).setSaveConsumer(v -> config.pearlCatchEnabled = v).build());
            pearl.addEntry(eb.startIntSlider(Component.translatable("text.autoconfig.bettermaceswap.option.pearlCatchDelayTicks"), config.pearlCatchDelayTicks, 1, 5).setDefaultValue(2).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.pearlCatchDelayTicks.@Tooltip")).setSaveConsumer(v -> config.pearlCatchDelayTicks = v).build());
            pearl.addEntry(eb.startIntSlider(Component.translatable("text.autoconfig.bettermaceswap.option.pearlCatchAngle"), config.pearlCatchAngle, 60, 90).setDefaultValue(60).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.pearlCatchAngle.@Tooltip")).setSaveConsumer(v -> config.pearlCatchAngle = v).build());
            pearl.addEntry(eb.startSelector(Component.translatable("text.autoconfig.bettermaceswap.option.pearlReturnMode"), ModConfig.ReturnSlotMode.values(), config.pearlReturnMode).setDefaultValue(ModConfig.ReturnSlotMode.PREVIOUS).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.pearlReturnMode.@Tooltip")).setSaveConsumer(v -> config.pearlReturnMode = (ModConfig.ReturnSlotMode) v).build());

            ConfigCategory aim = builder.getOrCreateCategory(Component.translatable("text.autoconfig.bettermaceswap.category.Aim Assist"));
            aim.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.aimAssistEnabled"), config.aimAssistEnabled).setDefaultValue(false).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.aimAssistEnabled.@Tooltip")).setSaveConsumer(v -> config.aimAssistEnabled = v).build());
            aim.addEntry(eb.startIntSlider(Component.translatable("text.autoconfig.bettermaceswap.option.aimSpeed"), config.aimSpeed, 20, 100).setDefaultValue(15).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.aimSpeed.@Tooltip")).setSaveConsumer(v -> config.aimSpeed = v).build());
            aim.addEntry(eb.startIntSlider(Component.translatable("text.autoconfig.bettermaceswap.option.aimIntensity"), config.aimIntensity, 1, 25).setDefaultValue(15).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.aimIntensity.@Tooltip")).setSaveConsumer(v -> config.aimIntensity = v).build());
            aim.addEntry(eb.startIntSlider(Component.translatable("text.autoconfig.bettermaceswap.option.aimFallBlocks"), config.aimFallBlocks, 1, 5).setDefaultValue(2).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.aimFallBlocks.@Tooltip")).setSaveConsumer(v -> config.aimFallBlocks = v).build());
            aim.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.aimTargetMobs"), config.aimTargetMobs).setDefaultValue(true).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.aimTargetMobs.@Tooltip")).setSaveConsumer(v -> config.aimTargetMobs = v).build());
            aim.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.aimTargetPlayers"), config.aimTargetPlayers).setDefaultValue(true).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.aimTargetPlayers.@Tooltip")).setSaveConsumer(v -> config.aimTargetPlayers = v).build());
            aim.addEntry(eb.startSelector(Component.translatable("text.autoconfig.bettermaceswap.option.aimBone"), ModConfig.AimBone.values(), config.aimBone).setDefaultValue(ModConfig.AimBone.EYE).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.aimBone.@Tooltip")).setSaveConsumer(v -> config.aimBone = (ModConfig.AimBone) v).build());
            aim.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.triggerBotEnabled"), config.triggerBotEnabled).setDefaultValue(false).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.triggerBotEnabled.@Tooltip")).setSaveConsumer(v -> config.triggerBotEnabled = v).build());
            aim.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.triggerBotFallingOnly"), config.triggerBotFallingOnly).setDefaultValue(false).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.triggerBotFallingOnly.@Tooltip")).setSaveConsumer(v -> config.triggerBotFallingOnly = v).build());
            aim.addEntry(eb.startSelector(Component.translatable("text.autoconfig.bettermaceswap.option.aimMode"), ModConfig.AimMode.values(), config.aimMode).setDefaultValue(ModConfig.AimMode.WHILE_FALLING).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.aimMode.@Tooltip")).setSaveConsumer(v -> config.aimMode = (ModConfig.AimMode) v).build());

            ConfigCategory misc = builder.getOrCreateCategory(Component.translatable("text.autoconfig.bettermaceswap.category.Misc"));
            misc.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.lungeSwapEnabled"), config.lungeSwapEnabled).setDefaultValue(false).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.lungeSwapEnabled.@Tooltip")).setSaveConsumer(v -> config.lungeSwapEnabled = v).build());
            misc.addEntry(eb.startIntSlider(Component.translatable("text.autoconfig.bettermaceswap.option.lungeSwapDelay"), config.lungeSwapDelay, 0, 3).setDefaultValue(2).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.lungeSwapDelay.@Tooltip")).setSaveConsumer(v -> config.lungeSwapDelay = v).build());
            misc.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.airPotsEnabled"), config.airPotsEnabled).setDefaultValue(false).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.airPotsEnabled.@Tooltip")).setSaveConsumer(v -> config.airPotsEnabled = v).build());
            misc.addEntry(eb.startBooleanToggle(Component.translatable("text.autoconfig.bettermaceswap.option.autoChestplateOnTrigger"), config.autoChestplateOnTrigger).setDefaultValue(false).setTooltip(Component.translatable("text.autoconfig.bettermaceswap.option.autoChestplateOnTrigger.@Tooltip")).setSaveConsumer(v -> config.autoChestplateOnTrigger = v).build());

            return builder.build();
        };
    }
}
