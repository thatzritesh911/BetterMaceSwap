package com.ritesh.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.ritesh.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BmsCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var root = ClientCommandManager.literal("bms")

            // ┌─ combat
            // │   ├─ attributeswap <on/off>
            // │   ├─ lungeswap <on/off>
            // │   ├─ smartswitch <on/off>
            // │   ├─ airpots <on/off>
            // │   ├─ attribute_swap_ticks <1-5>
            // │   ├─ lunge_delay <0-3>
            // │   ├─ mace_mode <density/breach>
            // │   ├─ attribute_swap_mode <all/weapons_only>
            // │   ├─ attribute_trigger_mode <weapons/all>
            // │   ├─ target_players <on/off>
            // │   └─ target_mobs <on/off>
            .then(ClientCommandManager.literal("combat")
                .then(ClientCommandManager.literal("attributeswap")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().autoBreachSwapEnabled = true; ModConfig.save(); send(ctx.getSource(), "Attribute Swap", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().autoBreachSwapEnabled = false; ModConfig.save(); send(ctx.getSource(), "Attribute Swap", false); return 1; })))
                .then(ClientCommandManager.literal("lungeswap")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().lungeSwapEnabled = true; ModConfig.save(); send(ctx.getSource(), "Lunge Swap", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().lungeSwapEnabled = false; ModConfig.save(); send(ctx.getSource(), "Lunge Swap", false); return 1; })))
                .then(ClientCommandManager.literal("smartswitch")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().smartSwitchEnabled = true; ModConfig.save(); send(ctx.getSource(), "Smart Switch", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().smartSwitchEnabled = false; ModConfig.save(); send(ctx.getSource(), "Smart Switch", false); return 1; })))
                .then(ClientCommandManager.literal("airpots")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().airPotsEnabled = true; ModConfig.save(); send(ctx.getSource(), "Air Pots", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().airPotsEnabled = false; ModConfig.save(); send(ctx.getSource(), "Air Pots", false); return 1; })))
                .then(ClientCommandManager.literal("attribute_swap_ticks")
                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 5)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().breachSwapBackTicks = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Attribute Swap Ticks", v); return 1;
                    })))
                .then(ClientCommandManager.literal("lunge_delay")
                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(0, 3)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().lungeSwapDelay = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Lunge Delay", v); return 1;
                    })))
                .then(ClientCommandManager.literal("mace_mode")
                    .then(ClientCommandManager.literal("density").executes(ctx -> {
                        ModConfig.get().maceMode = ModConfig.MaceEnchantMode.DENSITY; ModConfig.save();
                        sendEnum(ctx.getSource(), "Mace Mode", "Density"); return 1;
                    }))
                    .then(ClientCommandManager.literal("breach").executes(ctx -> {
                        ModConfig.get().maceMode = ModConfig.MaceEnchantMode.BREACH; ModConfig.save();
                        sendEnum(ctx.getSource(), "Mace Mode", "Breach"); return 1;
                    })))
                .then(ClientCommandManager.literal("attribute_swap_mode")
                    .then(ClientCommandManager.literal("all").executes(ctx -> {
                        ModConfig.get().attributeSwapMode = ModConfig.AttributeSwapMode.ALL; ModConfig.save();
                        sendEnum(ctx.getSource(), "Attribute Swap Mode", "All"); return 1;
                    }))
                    .then(ClientCommandManager.literal("weapons_only").executes(ctx -> {
                        ModConfig.get().attributeSwapMode = ModConfig.AttributeSwapMode.WEAPONS_ONLY; ModConfig.save();
                        sendEnum(ctx.getSource(), "Attribute Swap Mode", "Weapons Only"); return 1;
                    })))
                .then(ClientCommandManager.literal("attribute_trigger_mode")
                    .then(ClientCommandManager.literal("weapons").executes(ctx -> {
                        ModConfig.get().breachSwapTriggerMode = ModConfig.BreachSwapTriggerMode.WEAPONS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Attribute Trigger Mode", "Weapons"); return 1;
                    }))
                    .then(ClientCommandManager.literal("all").executes(ctx -> {
                        ModConfig.get().breachSwapTriggerMode = ModConfig.BreachSwapTriggerMode.ALL; ModConfig.save();
                        sendEnum(ctx.getSource(), "Attribute Trigger Mode", "All"); return 1;
                    })))
                .then(ClientCommandManager.literal("target_players")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().combatTargetPlayers = true; ModConfig.save(); send(ctx.getSource(), "Combat Target Players", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().combatTargetPlayers = false; ModConfig.save(); send(ctx.getSource(), "Combat Target Players", false); return 1; })))
                .then(ClientCommandManager.literal("target_mobs")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().combatTargetMobs = true; ModConfig.save(); send(ctx.getSource(), "Combat Target Mobs", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().combatTargetMobs = false; ModConfig.save(); send(ctx.getSource(), "Combat Target Mobs", false); return 1; }))))

            // ┌─ stunslam
            // │   ├─ stun_slam <on/off>
            // │   ├─ trigger_mode <weapons/all>
            // │   ├─ return_slot <on/off>
            // │   └─ safe_mode <on/off>
            .then(ClientCommandManager.literal("stunslam")
                .then(ClientCommandManager.literal("stun_slam")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().autoStunSlamEnabled = true; ModConfig.save(); send(ctx.getSource(), "Stun Slam", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().autoStunSlamEnabled = false; ModConfig.save(); send(ctx.getSource(), "Stun Slam", false); return 1; })))
                .then(ClientCommandManager.literal("trigger_mode")
                    .then(ClientCommandManager.literal("weapons").executes(ctx -> {
                        ModConfig.get().stunSlamTriggerMode = ModConfig.StunSlamTriggerMode.WEAPONS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Stun Trigger Mode", "Weapons"); return 1;
                    }))
                    .then(ClientCommandManager.literal("all").executes(ctx -> {
                        ModConfig.get().stunSlamTriggerMode = ModConfig.StunSlamTriggerMode.ALL; ModConfig.save();
                        sendEnum(ctx.getSource(), "Stun Trigger Mode", "All"); return 1;
                    })))
                .then(ClientCommandManager.literal("return_slot")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().stunSlamReturnSlot = true; ModConfig.save(); send(ctx.getSource(), "Stun Return Slot", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().stunSlamReturnSlot = false; ModConfig.save(); send(ctx.getSource(), "Stun Return Slot", false); return 1; })))
                .then(ClientCommandManager.literal("safe_mode")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().safeStunSlam = true; ModConfig.save(); send(ctx.getSource(), "Safe Stun Slam", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().safeStunSlam = false; ModConfig.save(); send(ctx.getSource(), "Safe Stun Slam", false); return 1; }))))

            // ┌─ pearl
            // │   ├─ pearl_catch <on/off>
            // │   ├─ catch_angle <60-90>
            // │   ├─ catch_delay <1-5>
            // │   ├─ return_angle <on/off>
            // │   ├─ return_angle_value <-90-90>
            // │   └─ return_mode <previous/sword/axe/elytra>
            .then(ClientCommandManager.literal("pearl")
                .then(ClientCommandManager.literal("pearl_catch")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().pearlCatchEnabled = true; ModConfig.save(); send(ctx.getSource(), "Pearl Catch", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().pearlCatchEnabled = false; ModConfig.save(); send(ctx.getSource(), "Pearl Catch", false); return 1; })))
                .then(ClientCommandManager.literal("catch_angle")
                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(60, 90)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().pearlCatchAngle = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Pearl Catch Angle", v); return 1;
                    })))
                .then(ClientCommandManager.literal("catch_delay")
                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 5)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().pearlCatchDelayTicks = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Pearl Catch Delay", v); return 1;
                    })))
                .then(ClientCommandManager.literal("return_angle")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().pearlCatchReturnAngle = true; ModConfig.save(); send(ctx.getSource(), "Pearl Return Angle", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().pearlCatchReturnAngle = false; ModConfig.save(); send(ctx.getSource(), "Pearl Return Angle", false); return 1; })))
                .then(ClientCommandManager.literal("return_angle_value")
                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(-90, 90)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().pearlReturnAngle = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Pearl Return Angle Value", v); return 1;
                    })))
                .then(ClientCommandManager.literal("return_mode")
                    .then(ClientCommandManager.literal("previous").executes(ctx -> {
                        ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.PREVIOUS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Pearl Return Mode", "Previous"); return 1;
                    }))
                    .then(ClientCommandManager.literal("sword").executes(ctx -> {
                        ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.SWORD; ModConfig.save();
                        sendEnum(ctx.getSource(), "Pearl Return Mode", "Sword"); return 1;
                    }))
                    .then(ClientCommandManager.literal("axe").executes(ctx -> {
                        ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.AXE; ModConfig.save();
                        sendEnum(ctx.getSource(), "Pearl Return Mode", "Axe"); return 1;
                    }))
                    .then(ClientCommandManager.literal("elytra").executes(ctx -> {
                        ModConfig.get().pearlReturnMode = ModConfig.ReturnSlotMode.ELYTRA; ModConfig.save();
                        sendEnum(ctx.getSource(), "Pearl Return Mode", "Elytra"); return 1;
                    }))))

            // ┌─ aim
            // │   ├─ aim_assist <on/off>
            // │   ├─ triggerbot <on/off>
            // │   ├─ triggerbot_falling_only <on/off>
            // │   ├─ auto_chestplate <on/off>
            // │   ├─ aim_speed <20-100>
            // │   ├─ range <1-25>
            // │   ├─ mode <while_falling/always>
            // │   ├─ bone <eye/chest/legs>
            // │   ├─ target_players <on/off>
            // │   └─ target_mobs <on/off>
            .then(ClientCommandManager.literal("aim")
                .then(ClientCommandManager.literal("aim_assist")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().aimAssistEnabled = true; ModConfig.save(); send(ctx.getSource(), "Aim Assist", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().aimAssistEnabled = false; ModConfig.save(); send(ctx.getSource(), "Aim Assist", false); return 1; })))
                .then(ClientCommandManager.literal("triggerbot")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().triggerBotEnabled = true; ModConfig.save(); send(ctx.getSource(), "Trigger Bot", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().triggerBotEnabled = false; ModConfig.save(); send(ctx.getSource(), "Trigger Bot", false); return 1; })))
                .then(ClientCommandManager.literal("triggerbot_falling_only")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().triggerBotFallingOnly = true; ModConfig.save(); send(ctx.getSource(), "Triggerbot Falling Only", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().triggerBotFallingOnly = false; ModConfig.save(); send(ctx.getSource(), "Triggerbot Falling Only", false); return 1; })))
                .then(ClientCommandManager.literal("auto_chestplate")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().autoChestplateOnTrigger = true; ModConfig.save(); send(ctx.getSource(), "Auto Chestplate", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().autoChestplateOnTrigger = false; ModConfig.save(); send(ctx.getSource(), "Auto Chestplate", false); return 1; })))
                .then(ClientCommandManager.literal("aim_speed")
                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(20, 100)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().aimSpeed = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Aim Speed", v); return 1;
                    })))
                .then(ClientCommandManager.literal("range")
                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 25)).executes(ctx -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.get().aimIntensity = v; ModConfig.save();
                        sendVal(ctx.getSource(), "Aim Range", v); return 1;
                    })))
                .then(ClientCommandManager.literal("mode")
                    .then(ClientCommandManager.literal("while_falling").executes(ctx -> {
                        ModConfig.get().aimMode = ModConfig.AimMode.WHILE_FALLING; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Mode", "While Falling"); return 1;
                    }))
                    .then(ClientCommandManager.literal("always").executes(ctx -> {
                        ModConfig.get().aimMode = ModConfig.AimMode.ALWAYS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Mode", "Always"); return 1;
                    })))
                .then(ClientCommandManager.literal("bone")
                    .then(ClientCommandManager.literal("eye").executes(ctx -> {
                        ModConfig.get().aimBone = ModConfig.AimBone.EYE; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Bone", "Eye"); return 1;
                    }))
                    .then(ClientCommandManager.literal("chest").executes(ctx -> {
                        ModConfig.get().aimBone = ModConfig.AimBone.CHEST; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Bone", "Chest"); return 1;
                    }))
                    .then(ClientCommandManager.literal("legs").executes(ctx -> {
                        ModConfig.get().aimBone = ModConfig.AimBone.LEGS; ModConfig.save();
                        sendEnum(ctx.getSource(), "Aim Bone", "Legs"); return 1;
                    })))
                .then(ClientCommandManager.literal("target_players")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().aimTargetPlayers = true; ModConfig.save(); send(ctx.getSource(), "Aim Target Players", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().aimTargetPlayers = false; ModConfig.save(); send(ctx.getSource(), "Aim Target Players", false); return 1; })))
                .then(ClientCommandManager.literal("target_mobs")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().aimTargetMobs = true; ModConfig.save(); send(ctx.getSource(), "Aim Target Mobs", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().aimTargetMobs = false; ModConfig.save(); send(ctx.getSource(), "Aim Target Mobs", false); return 1; }))))

            // ┌─ misc
            // │   ├─ wind <on/off>
            // │   ├─ rocketboost <on/off>
            // │   ├─ autojump_wind <on/off>
            // │   └─ experimental_pearl <on/off>
            .then(ClientCommandManager.literal("misc")
                .then(ClientCommandManager.literal("wind")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().windOnRightClickEnabled = true; ModConfig.save(); send(ctx.getSource(), "Wind Right Click", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().windOnRightClickEnabled = false; ModConfig.save(); send(ctx.getSource(), "Wind Right Click", false); return 1; })))
                .then(ClientCommandManager.literal("rocketboost")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().rocketBoostEnabled = true; ModConfig.save(); send(ctx.getSource(), "Rocket Boost", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().rocketBoostEnabled = false; ModConfig.save(); send(ctx.getSource(), "Rocket Boost", false); return 1; })))
                .then(ClientCommandManager.literal("autojump_wind")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().autoJumpWindCharge = true; ModConfig.save(); send(ctx.getSource(), "Auto Jump Wind", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().autoJumpWindCharge = false; ModConfig.save(); send(ctx.getSource(), "Auto Jump Wind", false); return 1; })))
                .then(ClientCommandManager.literal("experimental_pearl")
                    .then(ClientCommandManager.literal("on").executes(ctx -> { ModConfig.get().experimentalPearlCatch = true; ModConfig.save(); send(ctx.getSource(), "Experimental Pearl Catch", true); return 1; }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> { ModConfig.get().experimentalPearlCatch = false; ModConfig.save(); send(ctx.getSource(), "Experimental Pearl Catch", false); return 1; }))))

            // ┌─ menu  (opens AutoConfig GUI)
            .then(ClientCommandManager.literal("menu").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.send(() -> mc.setScreen(
                    AutoConfig.getConfigScreen(ModConfig.class, mc.currentScreen).get()
                ));
                return 1;
            }))

            // ┌─ status  (clickable toggles)
            .then(ClientCommandManager.literal("status").executes(ctx -> {
                ModConfig cfg = ModConfig.get();
                FabricClientCommandSource src = ctx.getSource();

                src.sendFeedback(Text.literal("§6§l━━━ BetterMaceSwap Status ━━━"));

                // helper hint
                src.sendFeedback(Text.literal("§7Click any toggle to flip it  •  ")
                    .append(clickable("§e[Open Menu]", "/bms menu", "Open mod config screen")));

                src.sendFeedback(Text.literal("§e§lCOMBAT"));
                src.sendFeedback(toggleLine("  Attribute Swap",     cfg.autoBreachSwapEnabled,   "/bms combat attributeswap"));
                src.sendFeedback(toggleLine("  Lunge Swap",         cfg.lungeSwapEnabled,         "/bms combat lungeswap"));
                src.sendFeedback(toggleLine("  Smart Switch",       cfg.smartSwitchEnabled,       "/bms combat smartswitch"));
                src.sendFeedback(toggleLine("  Air Pots",           cfg.airPotsEnabled,           "/bms combat airpots"));
                src.sendFeedback(toggleLine("  Target Players",     cfg.combatTargetPlayers,      "/bms combat target_players"));
                src.sendFeedback(toggleLine("  Target Mobs",        cfg.combatTargetMobs,         "/bms combat target_mobs"));
                src.sendFeedback(enumLine( "  Mace Mode",           cfg.maceMode.toString(),
                    cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY ? "/bms combat mace_mode breach" : "/bms combat mace_mode density",
                    "Click to switch"));
                src.sendFeedback(enumLine( "  Attr Swap Mode",      cfg.attributeSwapMode.toString(),
                    cfg.attributeSwapMode == ModConfig.AttributeSwapMode.ALL ? "/bms combat attribute_swap_mode weapons_only" : "/bms combat attribute_swap_mode all",
                    "Click to switch"));
                src.sendFeedback(enumLine( "  Attr Trigger Mode",   cfg.breachSwapTriggerMode.toString(),
                    cfg.breachSwapTriggerMode == ModConfig.BreachSwapTriggerMode.WEAPONS ? "/bms combat attribute_trigger_mode all" : "/bms combat attribute_trigger_mode weapons",
                    "Click to switch"));
                src.sendFeedback(Text.literal("  §7Swap Ticks: §f" + cfg.breachSwapBackTicks + "  Lunge Delay: §f" + cfg.lungeSwapDelay));

                src.sendFeedback(Text.literal("§e§lSTUN SLAM"));
                src.sendFeedback(toggleLine("  Stun Slam",          cfg.autoStunSlamEnabled,      "/bms stunslam stun_slam"));
                src.sendFeedback(toggleLine("  Return Slot",        cfg.stunSlamReturnSlot,        "/bms stunslam return_slot"));
                src.sendFeedback(toggleLine("  Safe Mode",          cfg.safeStunSlam,              "/bms stunslam safe_mode"));
                src.sendFeedback(enumLine( "  Trigger Mode",        cfg.stunSlamTriggerMode.toString(),
                    cfg.stunSlamTriggerMode == ModConfig.StunSlamTriggerMode.WEAPONS ? "/bms stunslam trigger_mode all" : "/bms stunslam trigger_mode weapons",
                    "Click to switch"));

                src.sendFeedback(Text.literal("§e§lPEARL"));
                src.sendFeedback(toggleLine("  Pearl Catch",        cfg.pearlCatchEnabled,         "/bms pearl pearl_catch"));
                src.sendFeedback(toggleLine("  Return Angle",       cfg.pearlCatchReturnAngle,     "/bms pearl return_angle"));
                src.sendFeedback(enumLine( "  Return Mode",         cfg.pearlReturnMode.toString(),
                    "/bms pearl return_mode " + nextReturnMode(cfg.pearlReturnMode),
                    "Click to cycle"));
                src.sendFeedback(Text.literal("  §7Catch Angle: §f" + cfg.pearlCatchAngle + "  Delay: §f" + cfg.pearlCatchDelayTicks + "  Return Angle: §f" + cfg.pearlReturnAngle));

                src.sendFeedback(Text.literal("§e§lAIM"));
                src.sendFeedback(toggleLine("  Aim Assist",         cfg.aimAssistEnabled,          "/bms aim aim_assist"));
                src.sendFeedback(toggleLine("  Trigger Bot",        cfg.triggerBotEnabled,         "/bms aim triggerbot"));
                src.sendFeedback(toggleLine("  TB Falling Only",    cfg.triggerBotFallingOnly,     "/bms aim triggerbot_falling_only"));
                src.sendFeedback(toggleLine("  Auto Chestplate",    cfg.autoChestplateOnTrigger,   "/bms aim auto_chestplate"));
                src.sendFeedback(toggleLine("  Target Players",     cfg.aimTargetPlayers,          "/bms aim target_players"));
                src.sendFeedback(toggleLine("  Target Mobs",        cfg.aimTargetMobs,             "/bms aim target_mobs"));
                src.sendFeedback(enumLine( "  Mode",                cfg.aimMode.toString(),
                    cfg.aimMode == ModConfig.AimMode.WHILE_FALLING ? "/bms aim mode always" : "/bms aim mode while_falling",
                    "Click to switch"));
                src.sendFeedback(enumLine( "  Bone",                cfg.aimBone.toString(),
                    "/bms aim bone " + nextBone(cfg.aimBone),
                    "Click to cycle"));
                src.sendFeedback(Text.literal("  §7Speed: §f" + cfg.aimSpeed + "  Range: §f" + cfg.aimIntensity));

                src.sendFeedback(Text.literal("§e§lMISC"));
                src.sendFeedback(toggleLine("  Wind RC",            cfg.windOnRightClickEnabled,   "/bms misc wind"));
                src.sendFeedback(toggleLine("  Rocket Boost",       cfg.rocketBoostEnabled,        "/bms misc rocketboost"));
                src.sendFeedback(toggleLine("  Auto Jump Wind",     cfg.autoJumpWindCharge,        "/bms misc autojump_wind"));
                src.sendFeedback(toggleLine("  Experimental Pearl", cfg.experimentalPearlCatch,    "/bms misc experimental_pearl"));

                src.sendFeedback(Text.literal("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                return 1;
            }));

        dispatcher.register(root);
        dispatcher.register(ClientCommandManager.literal("bettermaceswap").redirect(dispatcher.getRoot().getChild("bms")));
    }

    // ── clickable toggle line: "  Feature Name: [ON]" or "[OFF]", click flips it ──
    private static MutableText toggleLine(String label, boolean on, String baseCmd) {
        String nextState = on ? "off" : "on";
        String cmd = baseCmd + " " + nextState;
        String hoverMsg = "Click to turn " + (on ? "OFF" : "ON");

        MutableText badge = Text.literal(on ? " [ON]" : " [OFF]")
            .formatted(on ? Formatting.GREEN : Formatting.RED)
            .styled(s -> s
                .withClickEvent(new ClickEvent.RunCommand(cmd))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverMsg))));

        return Text.literal(label + ":").formatted(Formatting.WHITE).append(badge);
    }

    // ── clickable enum line: shows current value, click cycles/switches it ──
    private static MutableText enumLine(String label, String currentVal, String cmd, String hoverMsg) {
        MutableText badge = Text.literal(" [" + currentVal + "]")
            .formatted(Formatting.YELLOW)
            .styled(s -> s
                .withClickEvent(new ClickEvent.RunCommand(cmd))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverMsg))));

        return Text.literal(label + ":").formatted(Formatting.WHITE).append(badge);
    }

    // ── plain clickable text helper ──
    private static MutableText clickable(String display, String cmd, String hover) {
        return Text.literal(display)
            .styled(s -> s
                .withClickEvent(new ClickEvent.RunCommand(cmd))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(hover))));
    }

    // ── cycle helpers ──
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

    // ── feedback helpers (used by non-status commands) ──
    private static void send(FabricClientCommandSource src, String feature, boolean on) {
        src.sendFeedback(Text.literal("[BMS] " + feature + ": ")
            .append(Text.literal(on ? "ON" : "OFF").formatted(on ? Formatting.GREEN : Formatting.RED)));
    }

    private static void sendVal(FabricClientCommandSource src, String feature, int value) {
        src.sendFeedback(Text.literal("[BMS] " + feature + ": ")
            .append(Text.literal(String.valueOf(value)).formatted(Formatting.AQUA)));
    }

    private static void sendEnum(FabricClientCommandSource src, String feature, String value) {
        src.sendFeedback(Text.literal("[BMS] " + feature + ": ")
            .append(Text.literal(value).formatted(Formatting.YELLOW)));
    }
}