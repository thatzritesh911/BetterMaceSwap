package com.ritesh.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ritesh.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BmsCommand {

    private static final Map<String, Consumer<ModConfig>> TOGGLES = new HashMap<>();
    private static final Map<String, String> TOGGLE_NAMES = new HashMap<>();

    static {
        TOGGLES.put("combat_attributeswap",        c -> c.autoBreachSwapEnabled   = !c.autoBreachSwapEnabled);
        TOGGLES.put("combat_lungeswap",             c -> c.lungeSwapEnabled         = !c.lungeSwapEnabled);
        TOGGLES.put("combat_smartswitch",           c -> c.smartSwitchEnabled       = !c.smartSwitchEnabled);
        TOGGLES.put("combat_airpots",               c -> c.airPotsEnabled           = !c.airPotsEnabled);
        TOGGLES.put("combat_target_players",        c -> c.combatTargetPlayers      = !c.combatTargetPlayers);
        TOGGLES.put("combat_target_mobs",           c -> c.combatTargetMobs         = !c.combatTargetMobs);
        TOGGLES.put("stunslam_stun_slam",           c -> c.autoStunSlamEnabled      = !c.autoStunSlamEnabled);
        TOGGLES.put("stunslam_return_slot",         c -> c.stunSlamReturnSlot       = !c.stunSlamReturnSlot);
        TOGGLES.put("stunslam_safe_mode",           c -> c.safeStunSlam             = !c.safeStunSlam);
        TOGGLES.put("pearl_pearl_catch",            c -> c.pearlCatchEnabled        = !c.pearlCatchEnabled);
        TOGGLES.put("pearl_return_angle",           c -> c.pearlCatchReturnAngle    = !c.pearlCatchReturnAngle);
        TOGGLES.put("aim_aim_assist",               c -> c.aimAssistEnabled         = !c.aimAssistEnabled);
        TOGGLES.put("aim_triggerbot",               c -> c.triggerBotEnabled        = !c.triggerBotEnabled);
        TOGGLES.put("aim_triggerbot_falling_only",  c -> c.triggerBotFallingOnly    = !c.triggerBotFallingOnly);
        TOGGLES.put("aim_auto_chestplate",          c -> c.autoChestplateOnTrigger  = !c.autoChestplateOnTrigger);
        TOGGLES.put("aim_target_players",           c -> c.aimTargetPlayers         = !c.aimTargetPlayers);
        TOGGLES.put("aim_target_mobs",              c -> c.aimTargetMobs            = !c.aimTargetMobs);
        TOGGLES.put("misc_wind",                    c -> c.windOnRightClickEnabled  = !c.windOnRightClickEnabled);
        TOGGLES.put("misc_rocketboost",             c -> c.rocketBoostEnabled       = !c.rocketBoostEnabled);
        TOGGLES.put("misc_autojump_wind",           c -> c.autoJumpWindCharge       = !c.autoJumpWindCharge);
        TOGGLES.put("misc_experimental_pearl",      c -> c.experimentalPearlCatch   = !c.experimentalPearlCatch);

        TOGGLE_NAMES.put("combat_attributeswap",        "Attribute Swap");
        TOGGLE_NAMES.put("combat_lungeswap",            "Lunge Swap");
        TOGGLE_NAMES.put("combat_smartswitch",          "Smart Switch");
        TOGGLE_NAMES.put("combat_airpots",              "Air Pots");
        TOGGLE_NAMES.put("combat_target_players",       "Combat Target Players");
        TOGGLE_NAMES.put("combat_target_mobs",          "Combat Target Mobs");
        TOGGLE_NAMES.put("stunslam_stun_slam",          "Stun Slam");
        TOGGLE_NAMES.put("stunslam_return_slot",        "Stun Return Slot");
        TOGGLE_NAMES.put("stunslam_safe_mode",          "Safe Stun Slam");
        TOGGLE_NAMES.put("pearl_pearl_catch",           "Pearl Catch");
        TOGGLE_NAMES.put("pearl_return_angle",          "Pearl Return Angle");
        TOGGLE_NAMES.put("aim_aim_assist",              "Aim Assist");
        TOGGLE_NAMES.put("aim_triggerbot",              "Trigger Bot");
        TOGGLE_NAMES.put("aim_triggerbot_falling_only", "Triggerbot Falling Only");
        TOGGLE_NAMES.put("aim_auto_chestplate",         "Auto Chestplate");
        TOGGLE_NAMES.put("aim_target_players",          "Aim Target Players");
        TOGGLE_NAMES.put("aim_target_mobs",             "Aim Target Mobs");
        TOGGLE_NAMES.put("misc_wind",                   "Wind Right Click");
        TOGGLE_NAMES.put("misc_rocketboost",            "Rocket Boost");
        TOGGLE_NAMES.put("misc_autojump_wind",          "Auto Jump Wind");
        TOGGLE_NAMES.put("misc_experimental_pearl",     "Experimental Pearl Catch");
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var root = ClientCommands.literal("bms")

            // ┌─ combat
            .then(ClientCommands.literal("combat")
                .then(ClientCommands.literal("attributeswap")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().autoBreachSwapEnabled = true; ModConfig.save(); send(ctx.getSource(), "Attribute Swap", true, "combat_attributeswap"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().autoBreachSwapEnabled = false; ModConfig.save(); send(ctx.getSource(), "Attribute Swap", false, "combat_attributeswap"); return 1; })))
                .then(ClientCommands.literal("lungeswap")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().lungeSwapEnabled = true; ModConfig.save(); send(ctx.getSource(), "Lunge Swap", true, "combat_lungeswap"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().lungeSwapEnabled = false; ModConfig.save(); send(ctx.getSource(), "Lunge Swap", false, "combat_lungeswap"); return 1; })))
                .then(ClientCommands.literal("smartswitch")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().smartSwitchEnabled = true; ModConfig.save(); send(ctx.getSource(), "Smart Switch", true, "combat_smartswitch"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().smartSwitchEnabled = false; ModConfig.save(); send(ctx.getSource(), "Smart Switch", false, "combat_smartswitch"); return 1; })))
                .then(ClientCommands.literal("airpots")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().airPotsEnabled = true; ModConfig.save(); send(ctx.getSource(), "Air Pots", true, "combat_airpots"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().airPotsEnabled = false; ModConfig.save(); send(ctx.getSource(), "Air Pots", false, "combat_airpots"); return 1; })))
                .then(ClientCommands.literal("attribute_swap_ticks")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(1, 5)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().breachSwapBackTicks = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Attribute Swap Ticks", v); return 1;
                    })))
                .then(ClientCommands.literal("lunge_delay")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(0, 3)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().lungeSwapDelay = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Lunge Delay", v); return 1;
                    })))
                .then(ClientCommands.literal("mace_mode")
                    .then(ClientCommands.literal("density").executes(ctx -> {
                        ModConfig.get().maceMode = ModConfig.MaceEnchantMode.DENSITY; ModConfig.save();
                        sendEnum(ctx.getSource(), "Mace Mode", "Density"); return 1;
                    }))
                    .then(ClientCommands.literal("breach").executes(ctx -> {
                        ModConfig.get().maceMode = ModConfig.MaceEnchantMode.BREACH; ModConfig.save();
                        sendEnum(ctx.getSource(), "Mace Mode", "Breach"); return 1;
                    })))
                .then(ClientCommands.literal("attribute_swap_mode")
                    .then(ClientCommands.literal("all").executes(ctx -> {
                        ModConfig.get().attributeSwapMode = ModConfig.AttributeSwapMode.ALL; ModConfig.save();
                        sendEnum(ctx.getSource(), "Attribute Swap Mode", "All"); return 1;
                    }))
                    .then(ClientCommands.literal("weapons_only").executes(ctx -> {
                        ModConfig.get().attributeSwapMode = ModConfig.AttributeSwapMode.WEAPONS_ONLY; ModConfig.save();
                        sendEnum(ctx.getSource(), "Attribute Swap Mode", "Weapons Only"); return 1;
                    })))
                .then(ClientCommands.literal("attribute_trigger_mode")
                    .then(ClientCommands.literal("weapons").executes(ctx -> {
                        ModConfig.get().breachSwapTriggerMode = ModConfig.BreachSwapTriggerMode.WEAPONS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Attribute Trigger Mode", "Weapons"); return 1;
                    }))
                    .then(ClientCommands.literal("all").executes(ctx -> {
                        ModConfig.get().breachSwapTriggerMode = ModConfig.BreachSwapTriggerMode.ALL; ModConfig.save();
                        sendEnum(ctx.getSource(), "Attribute Trigger Mode", "All"); return 1;
                    })))
                .then(ClientCommands.literal("target_players")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().combatTargetPlayers = true; ModConfig.save(); send(ctx.getSource(), "Combat Target Players", true, "combat_target_players"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().combatTargetPlayers = false; ModConfig.save(); send(ctx.getSource(), "Combat Target Players", false, "combat_target_players"); return 1; })))
                .then(ClientCommands.literal("target_mobs")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().combatTargetMobs = true; ModConfig.save(); send(ctx.getSource(), "Combat Target Mobs", true, "combat_target_mobs"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().combatTargetMobs = false; ModConfig.save(); send(ctx.getSource(), "Combat Target Mobs", false, "combat_target_mobs"); return 1; }))))

            // ┌─ stunslam
            .then(ClientCommands.literal("stunslam")
                .then(ClientCommands.literal("stun_slam")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().autoStunSlamEnabled = true; ModConfig.save(); send(ctx.getSource(), "Stun Slam", true, "stunslam_stun_slam"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().autoStunSlamEnabled = false; ModConfig.save(); send(ctx.getSource(), "Stun Slam", false, "stunslam_stun_slam"); return 1; })))
                .then(ClientCommands.literal("trigger_mode")
                    .then(ClientCommands.literal("weapons").executes(ctx -> {
                        ModConfig.get().stunSlamTriggerMode = ModConfig.StunSlamTriggerMode.WEAPONS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Stun Trigger Mode", "Weapons"); return 1;
                    }))
                    .then(ClientCommands.literal("all").executes(ctx -> {
                        ModConfig.get().stunSlamTriggerMode = ModConfig.StunSlamTriggerMode.ALL; ModConfig.save();
                        sendEnum(ctx.getSource(), "Stun Trigger Mode", "All"); return 1;
                    })))
                .then(ClientCommands.literal("return_slot")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().stunSlamReturnSlot = true; ModConfig.save(); send(ctx.getSource(), "Stun Return Slot", true, "stunslam_return_slot"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().stunSlamReturnSlot = false; ModConfig.save(); send(ctx.getSource(), "Stun Return Slot", false, "stunslam_return_slot"); return 1; })))
                .then(ClientCommands.literal("safe_mode")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().safeStunSlam = true; ModConfig.save(); send(ctx.getSource(), "Safe Stun Slam", true, "stunslam_safe_mode"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().safeStunSlam = false; ModConfig.save(); send(ctx.getSource(), "Safe Stun Slam", false, "stunslam_safe_mode"); return 1; }))))

            // ┌─ pearl
            .then(ClientCommands.literal("pearl")
                .then(ClientCommands.literal("pearl_catch")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().pearlCatchEnabled = true; ModConfig.save(); send(ctx.getSource(), "Pearl Catch", true, "pearl_pearl_catch"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().pearlCatchEnabled = false; ModConfig.save(); send(ctx.getSource(), "Pearl Catch", false, "pearl_pearl_catch"); return 1; })))
                .then(ClientCommands.literal("catch_angle")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(60, 90)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().pearlCatchAngle = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Pearl Catch Angle", v); return 1;
                    })))
                .then(ClientCommands.literal("catch_delay")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(1, 5)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().pearlCatchDelayTicks = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Pearl Catch Delay", v); return 1;
                    })))
                .then(ClientCommands.literal("return_angle")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().pearlCatchReturnAngle = true; ModConfig.save(); send(ctx.getSource(), "Pearl Return Angle", true, "pearl_return_angle"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().pearlCatchReturnAngle = false; ModConfig.save(); send(ctx.getSource(), "Pearl Return Angle", false, "pearl_return_angle"); return 1; })))
                .then(ClientCommands.literal("return_angle_value")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(-90, 90)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().pearlReturnAngle = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Pearl Return Angle Value", v); return 1;
                    })))
                .then(ClientCommands.literal("return_mode")
                    .then(ClientCommands.literal("previous").executes(ctx -> {
                        ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.PREVIOUS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Pearl Return Mode", "Previous"); return 1;
                    }))
                    .then(ClientCommands.literal("sword").executes(ctx -> {
                        ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.SWORD; ModConfig.save();
                        sendEnum(ctx.getSource(), "Pearl Return Mode", "Sword"); return 1;
                    }))
                    .then(ClientCommands.literal("axe").executes(ctx -> {
                        ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.AXE; ModConfig.save();
                        sendEnum(ctx.getSource(), "Pearl Return Mode", "Axe"); return 1;
                    }))
                    .then(ClientCommands.literal("elytra").executes(ctx -> {
                        ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.ELYTRA; ModConfig.save();
                        sendEnum(ctx.getSource(), "Pearl Return Mode", "Elytra"); return 1;
                    }))))

            // ┌─ aim
            .then(ClientCommands.literal("aim")
                .then(ClientCommands.literal("aim_assist")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().aimAssistEnabled = true; ModConfig.save(); send(ctx.getSource(), "Aim Assist", true, "aim_aim_assist"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().aimAssistEnabled = false; ModConfig.save(); send(ctx.getSource(), "Aim Assist", false, "aim_aim_assist"); return 1; })))
                .then(ClientCommands.literal("triggerbot")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().triggerBotEnabled = true; ModConfig.save(); send(ctx.getSource(), "Trigger Bot", true, "aim_triggerbot"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().triggerBotEnabled = false; ModConfig.save(); send(ctx.getSource(), "Trigger Bot", false, "aim_triggerbot"); return 1; })))
                .then(ClientCommands.literal("triggerbot_falling_only")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().triggerBotFallingOnly = true; ModConfig.save(); send(ctx.getSource(), "Triggerbot Falling Only", true, "aim_triggerbot_falling_only"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().triggerBotFallingOnly = false; ModConfig.save(); send(ctx.getSource(), "Triggerbot Falling Only", false, "aim_triggerbot_falling_only"); return 1; })))
                .then(ClientCommands.literal("auto_chestplate")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().autoChestplateOnTrigger = true; ModConfig.save(); send(ctx.getSource(), "Auto Chestplate", true, "aim_auto_chestplate"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().autoChestplateOnTrigger = false; ModConfig.save(); send(ctx.getSource(), "Auto Chestplate", false, "aim_auto_chestplate"); return 1; })))
                .then(ClientCommands.literal("aim_speed")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(20, 100)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().aimSpeed = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Aim Speed", v); return 1;
                    })))
                .then(ClientCommands.literal("range")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(1, 25)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().aimIntensity = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Aim Range", v); return 1;
                    })))
                .then(ClientCommands.literal("mode")
                    .then(ClientCommands.literal("while_falling").executes(ctx -> {
                        ModConfig.get().aimMode = ModConfig.AimMode.WHILE_FALLING; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Mode", "While Falling"); return 1;
                    }))
                    .then(ClientCommands.literal("always").executes(ctx -> {
                        ModConfig.get().aimMode = ModConfig.AimMode.ALWAYS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Mode", "Always"); return 1;
                    })))
                .then(ClientCommands.literal("bone")
                    .then(ClientCommands.literal("eye").executes(ctx -> {
                        ModConfig.get().aimBone = ModConfig.AimBone.EYE; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Bone", "Eye"); return 1;
                    }))
                    .then(ClientCommands.literal("chest").executes(ctx -> {
                        ModConfig.get().aimBone = ModConfig.AimBone.CHEST; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Bone", "Chest"); return 1;
                    }))
                    .then(ClientCommands.literal("legs").executes(ctx -> {
                        ModConfig.get().aimBone = ModConfig.AimBone.LEGS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Bone", "Legs"); return 1;
                    })))
                .then(ClientCommands.literal("target_players")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().aimTargetPlayers = true; ModConfig.save(); send(ctx.getSource(), "Aim Target Players", true, "aim_target_players"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().aimTargetPlayers = false; ModConfig.save(); send(ctx.getSource(), "Aim Target Players", false, "aim_target_players"); return 1; })))
                .then(ClientCommands.literal("target_mobs")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().aimTargetMobs = true; ModConfig.save(); send(ctx.getSource(), "Aim Target Mobs", true, "aim_target_mobs"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().aimTargetMobs = false; ModConfig.save(); send(ctx.getSource(), "Aim Target Mobs", false, "aim_target_mobs"); return 1; }))))

            // ┌─ misc
            .then(ClientCommands.literal("misc")
                .then(ClientCommands.literal("wind")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().windOnRightClickEnabled = true; ModConfig.save(); send(ctx.getSource(), "Wind Right Click", true, "misc_wind"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().windOnRightClickEnabled = false; ModConfig.save(); send(ctx.getSource(), "Wind Right Click", false, "misc_wind"); return 1; })))
                .then(ClientCommands.literal("rocketboost")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().rocketBoostEnabled = true; ModConfig.save(); send(ctx.getSource(), "Rocket Boost", true, "misc_rocketboost"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().rocketBoostEnabled = false; ModConfig.save(); send(ctx.getSource(), "Rocket Boost", false, "misc_rocketboost"); return 1; })))
                .then(ClientCommands.literal("autojump_wind")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().autoJumpWindCharge = true; ModConfig.save(); send(ctx.getSource(), "Auto Jump Wind", true, "misc_autojump_wind"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().autoJumpWindCharge = false; ModConfig.save(); send(ctx.getSource(), "Auto Jump Wind", false, "misc_autojump_wind"); return 1; })))
                .then(ClientCommands.literal("experimental_pearl")
                    .then(ClientCommands.literal("on").executes(ctx -> { ModConfig.get().experimentalPearlCatch = true; ModConfig.save(); send(ctx.getSource(), "Experimental Pearl Catch", true, "misc_experimental_pearl"); return 1; }))
                    .then(ClientCommands.literal("off").executes(ctx -> { ModConfig.get().experimentalPearlCatch = false; ModConfig.save(); send(ctx.getSource(), "Experimental Pearl Catch", false, "misc_experimental_pearl"); return 1; }))))

            // ┌─ menu
            .then(ClientCommands.literal("menu").executes(ctx -> {
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> mc.setScreen(
    me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen(ModConfig.class, mc.screen).get()
));
                return 1;
            }))

            // ┌─ hidden toggle node
            .then(ClientCommands.literal("\u00a70t")
                .then(ClientCommands.argument("k", StringArgumentType.word())
                    .suggests((ctx, builder) -> builder.buildFuture())
                    .executes(ctx -> {
                        String key = StringArgumentType.getString(ctx, "k");
                        Consumer<ModConfig> action = TOGGLES.get(key);
                        if (action == null) return 0;
                        ModConfig cfg = ModConfig.get();
                        action.accept(cfg);
                        ModConfig.save();
                        boolean newState = getState(cfg, key);
                        String name = TOGGLE_NAMES.getOrDefault(key, key);
                        send(ctx.getSource(), name, newState, key);
                        return 1;
                    })))

            // ┌─ status
            .then(ClientCommands.literal("status").executes(ctx -> {
                ModConfig cfg = ModConfig.get();
                FabricClientCommandSource src = ctx.getSource();

                src.sendFeedback(Component.literal("§6§l━━━ BetterMaceSwap Status ━━━"));
                src.sendFeedback(Component.literal("§7Click any toggle to flip it  •  ")
                    .append(clickable("§e[Open Menu]", "/bms menu", "Open mod config screen")));

                src.sendFeedback(Component.literal("§e§lCOMBAT"));
                src.sendFeedback(toggleLine("  Attribute Swap",     cfg.autoBreachSwapEnabled,    "combat_attributeswap"));
                src.sendFeedback(toggleLine("  Lunge Swap",         cfg.lungeSwapEnabled,          "combat_lungeswap"));
                src.sendFeedback(toggleLine("  Smart Switch",       cfg.smartSwitchEnabled,        "combat_smartswitch"));
                src.sendFeedback(toggleLine("  Air Pots",           cfg.airPotsEnabled,            "combat_airpots"));
                src.sendFeedback(toggleLine("  Target Players",     cfg.combatTargetPlayers,       "combat_target_players"));
                src.sendFeedback(toggleLine("  Target Mobs",        cfg.combatTargetMobs,          "combat_target_mobs"));
                src.sendFeedback(enumLine("  Mace Mode",            cfg.maceMode.toString(),
                    cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY ? "/bms combat mace_mode breach" : "/bms combat mace_mode density",
                    "Click to switch"));
                src.sendFeedback(enumLine("  Attr Swap Mode",       cfg.attributeSwapMode.toString(),
                    cfg.attributeSwapMode == ModConfig.AttributeSwapMode.ALL ? "/bms combat attribute_swap_mode weapons_only" : "/bms combat attribute_swap_mode all",
                    "Click to switch"));
                src.sendFeedback(enumLine("  Attr Trigger Mode",    cfg.breachSwapTriggerMode.toString(),
                    cfg.breachSwapTriggerMode == ModConfig.BreachSwapTriggerMode.WEAPONS ? "/bms combat attribute_trigger_mode all" : "/bms combat attribute_trigger_mode weapons",
                    "Click to switch"));
                src.sendFeedback(Component.literal("  §7Swap Ticks: §f" + cfg.breachSwapBackTicks + "  Lunge Delay: §f" + cfg.lungeSwapDelay));

                src.sendFeedback(Component.literal("§e§lSTUN SLAM"));
                src.sendFeedback(toggleLine("  Stun Slam",          cfg.autoStunSlamEnabled,       "stunslam_stun_slam"));
                src.sendFeedback(toggleLine("  Return Slot",        cfg.stunSlamReturnSlot,         "stunslam_return_slot"));
                src.sendFeedback(toggleLine("  Safe Mode",          cfg.safeStunSlam,               "stunslam_safe_mode"));
                src.sendFeedback(enumLine("  Trigger Mode",         cfg.stunSlamTriggerMode.toString(),
                    cfg.stunSlamTriggerMode == ModConfig.StunSlamTriggerMode.WEAPONS ? "/bms stunslam trigger_mode all" : "/bms stunslam trigger_mode weapons",
                    "Click to switch"));

                src.sendFeedback(Component.literal("§e§lPEARL"));
                src.sendFeedback(toggleLine("  Pearl Catch",        cfg.pearlCatchEnabled,          "pearl_pearl_catch"));
                src.sendFeedback(toggleLine("  Return Angle",       cfg.pearlCatchReturnAngle,      "pearl_return_angle"));
                src.sendFeedback(enumLine("  Return Mode",          cfg.pearlReturnMode.toString(),
                    "/bms pearl return_mode " + nextReturnMode(cfg.pearlReturnMode),
                    "Click to cycle"));
                src.sendFeedback(Component.literal("  §7Catch Angle: §f" + cfg.pearlCatchAngle + "  Delay: §f" + cfg.pearlCatchDelayTicks + "  Return Angle: §f" + cfg.pearlReturnAngle));

                src.sendFeedback(Component.literal("§e§lAIM"));
                src.sendFeedback(toggleLine("  Aim Assist",         cfg.aimAssistEnabled,           "aim_aim_assist"));
                src.sendFeedback(toggleLine("  Trigger Bot",        cfg.triggerBotEnabled,          "aim_triggerbot"));
                src.sendFeedback(toggleLine("  TB Falling Only",    cfg.triggerBotFallingOnly,      "aim_triggerbot_falling_only"));
                src.sendFeedback(toggleLine("  Auto Chestplate",    cfg.autoChestplateOnTrigger,    "aim_auto_chestplate"));
                src.sendFeedback(toggleLine("  Target Players",     cfg.aimTargetPlayers,           "aim_target_players"));
                src.sendFeedback(toggleLine("  Target Mobs",        cfg.aimTargetMobs,              "aim_target_mobs"));
                src.sendFeedback(enumLine("  Mode",                 cfg.aimMode.toString(),
                    cfg.aimMode == ModConfig.AimMode.WHILE_FALLING ? "/bms aim mode always" : "/bms aim mode while_falling",
                    "Click to switch"));
                src.sendFeedback(enumLine("  Bone",                 cfg.aimBone.toString(),
                    "/bms aim bone " + nextBone(cfg.aimBone),
                    "Click to cycle"));
                src.sendFeedback(Component.literal("  §7Speed: §f" + cfg.aimSpeed + "  Range: §f" + cfg.aimIntensity));

                src.sendFeedback(Component.literal("§e§lMISC"));
                src.sendFeedback(toggleLine("  Wind RC",            cfg.windOnRightClickEnabled,    "misc_wind"));
                src.sendFeedback(toggleLine("  Rocket Boost",       cfg.rocketBoostEnabled,         "misc_rocketboost"));
                src.sendFeedback(toggleLine("  Auto Jump Wind",     cfg.autoJumpWindCharge,         "misc_autojump_wind"));
                src.sendFeedback(toggleLine("  Experimental Pearl", cfg.experimentalPearlCatch,     "misc_experimental_pearl"));

                src.sendFeedback(Component.literal("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                return 1;
            }));

        dispatcher.register(root);
        dispatcher.register(ClientCommands.literal("bettermaceswap").redirect(dispatcher.getRoot().getChild("bms")));
    }

    private static MutableComponent toggleLine(String label, boolean on, String key) {
        MutableComponent badge = Component.literal(on ? " [ON]" : " [OFF]")
            .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED)
            .withStyle(s -> s
                .withClickEvent(new ClickEvent.RunCommand( "/bms \u00a70t " + key))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to toggle"))));
        return Component.literal(label + ":").withStyle(ChatFormatting.WHITE).append(badge);
    }

    private static MutableComponent enumLine(String label, String currentVal, String cmd, String hoverMsg) {
        MutableComponent badge = Component.literal(" [" + currentVal + "]")
            .withStyle(ChatFormatting.YELLOW)
            .withStyle(s -> s
                .withClickEvent(new ClickEvent.RunCommand( cmd))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverMsg))));
        return Component.literal(label + ":").withStyle(ChatFormatting.WHITE).append(badge);
    }

    private static MutableComponent clickable(String display, String cmd, String hover) {
        return Component.literal(display)
            .withStyle(s -> s
                .withClickEvent(new ClickEvent.RunCommand( cmd))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(hover))));
    }

    private static boolean getState(ModConfig cfg, String key) {
        return switch (key) {
            case "combat_attributeswap"        -> cfg.autoBreachSwapEnabled;
            case "combat_lungeswap"            -> cfg.lungeSwapEnabled;
            case "combat_smartswitch"          -> cfg.smartSwitchEnabled;
            case "combat_airpots"              -> cfg.airPotsEnabled;
            case "combat_target_players"       -> cfg.combatTargetPlayers;
            case "combat_target_mobs"          -> cfg.combatTargetMobs;
            case "stunslam_stun_slam"          -> cfg.autoStunSlamEnabled;
            case "stunslam_return_slot"        -> cfg.stunSlamReturnSlot;
            case "stunslam_safe_mode"          -> cfg.safeStunSlam;
            case "pearl_pearl_catch"           -> cfg.pearlCatchEnabled;
            case "pearl_return_angle"          -> cfg.pearlCatchReturnAngle;
            case "aim_aim_assist"              -> cfg.aimAssistEnabled;
            case "aim_triggerbot"              -> cfg.triggerBotEnabled;
            case "aim_triggerbot_falling_only" -> cfg.triggerBotFallingOnly;
            case "aim_auto_chestplate"         -> cfg.autoChestplateOnTrigger;
            case "aim_target_players"          -> cfg.aimTargetPlayers;
            case "aim_target_mobs"             -> cfg.aimTargetMobs;
            case "misc_wind"                   -> cfg.windOnRightClickEnabled;
            case "misc_rocketboost"            -> cfg.rocketBoostEnabled;
            case "misc_autojump_wind"          -> cfg.autoJumpWindCharge;
            case "misc_experimental_pearl"     -> cfg.experimentalPearlCatch;
            default -> false;
        };
    }

    private static String nextReturnMode(ModConfig.ReturnSlotMode current) {
        return switch (current) {
            case PREVIOUS -> "sword";
            case SWORD    -> "axe";
            case AXE      -> "elytra";
            case ELYTRA   -> "previous";
        };
    }

    private static String nextBone(ModConfig.AimBone current) {
        return switch (current) {
            case EYE   -> "chest";
            case CHEST -> "legs";
            case LEGS  -> "eye";
        };
    }

    private static void send(FabricClientCommandSource src, String feature, boolean on, String toggleKey) {
        MutableComponent badge = Component.literal(on ? "ON" : "OFF")
            .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED)
            .withStyle(s -> s
                .withClickEvent(new ClickEvent.RunCommand( "/bms \u00a70t " + toggleKey))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to toggle"))));
        src.sendFeedback(Component.literal("[BMS] " + feature + ": ").append(badge));
    }

    private static void sendVal(FabricClientCommandSource src, String feature, int value) {
        src.sendFeedback(Component.literal("[BMS] " + feature + ": ")
            .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.AQUA)));
    }

    private static void sendEnum(FabricClientCommandSource src, String feature, String value) {
        src.sendFeedback(Component.literal("[BMS] " + feature + ": ")
            .append(Component.literal(value).withStyle(ChatFormatting.YELLOW)));
    }
}
