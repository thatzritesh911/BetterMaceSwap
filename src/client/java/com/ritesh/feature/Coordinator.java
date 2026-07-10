package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.ritesh.feature.AutoFeaturesState.*;

public class Coordinator {

    public static void onRender(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        if (!cfg.aimAssistEnabled) return;
        AimAssist.applyRotation(p, cfg);
    }

    public static boolean isLungeSwapPending() { return lungeSwapPending; }

    public static void startLungeSwap(int returnSlot) {
        Combat.startLungeSwap(returnSlot);
    }

    public static void onBlockHit(LocalPlayer p) {
        Combat.onBlockHit(p);
    }

    public static void onAttack(Player player) {
        Combat.onAttack(player);
    }

    public static void onHit(Player player, Entity target) {
        if (player == null || target == null) return;
        if (isInSlamCombo) return;
        if (isAttacking) return;
        ModConfig cfg = ModConfig.get();
        if (lungeSwapPending && lungeReturnSlot != -1) return;

        boolean isPlayer = target instanceof Player;
        boolean isMob = target instanceof Mob || target instanceof Animal;
        if (isPlayer && !cfg.combatTargetPlayers) return;
        if (isMob && !cfg.combatTargetMobs) return;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        int startingSlot = inv.getSelectedSlot();
        ItemStack held = player.getMainHandItem();

        boolean isWeapon = Combat.isSword(held) || StunSlam.isAxe(held) || held.is(Items.MACE);
        boolean allowedByMode = isWeapon || (cfg.attributeSwapMode == ModConfig.AttributeSwapMode.ALL);
        boolean slamAllowed = cfg.autoStunSlamEnabled && (cfg.stunSlamTriggerMode == ModConfig.StunSlamTriggerMode.ALL || isWeapon);
        if (!allowedByMode && !slamAllowed) return;

        if (slamAllowed && target instanceof LivingEntity tp) {
            if (tp.isBlocking() && player.distanceTo(target) <= 3.0) {
                int axeSlot = Combat.findItem(player, "axe");
                boolean wantDensity = cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY;
                int maceSlot = StunSlam.findBestMace(player, wantDensity);
                int returnAfterSlam = (swordSlotBeforeAxe != -1) ? swordSlotBeforeAxe : startingSlot;
                swordSlotBeforeAxe = -1;

                if (axeSlot != -1 && maceSlot != -1) {
                    StunSlam.handleSlamWithMace(player, target, axeSlot, maceSlot, returnAfterSlam, cfg);
                    return;
                }
                if (axeSlot != -1) {
                    StunSlam.handleSlamNoMace(player, target, axeSlot, maceSlot, returnAfterSlam);
                    return;
                }
                if (StunSlam.isAxe(held)) {
                    StunSlam.handleSlamDirectAxe(target, maceSlot, returnAfterSlam, cfg);
                    return;
                }
            }
        }

        if (!allowedByMode) return;
        if (lungeSwapPending) return;

        if (isBreachSwapped && swapBackTimer <= 0) {
            Combat.forceReset((LocalPlayer) player);
        }

        Combat.applyBreachSwap(player, startingSlot, held, cfg);
    }

    public static void tick(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;

        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        int currentSlot = inv.getSelectedSlot();
        if (!p.getInventory().getItem(currentSlot).is(Items.ENDER_PEARL)) {
            lastSelectedSlot = currentSlot;
        }

        boolean inAir = !p.onGround();
        if (wasInAir && !inAir) {
            triggerBotHitsThisFall = 0;
            chestplateEquipped = false;
        }
        wasInAir = inAir;

        if (triggerBotCooldown > 0) triggerBotCooldown--;

        Combat.tickLungeSwap(client);

        wasAttackPressed = client.options.keyAttack.isDown();

        if (cfg.aimAssistEnabled) {
            AimAssist.tick(client, cfg);
            AimAssist.applyRotation(p, cfg);
            Combat.tickTriggerBot(client);
        }

        StunSlam.tick(client);
        Pearl.tick(client);
        Misc.tick(client);

        Combat.tickBreachSwap(p);
    }

    public static void onSplashPotThrown(LocalPlayer player) {
        Misc.onSplashPotThrown(player);
    }

    public static void onPearlThrown(LocalPlayer player) {
        Pearl.onPearlThrown(player);
    }

    public static void fullReset(LocalPlayer player) {
        Combat.forceReset(player);
        StunSlam.reset();
        Pearl.reset();
        Misc.reset();
        Combat.reset();
        AimAssist.reset();
        lastSelectedSlot = -1;
        isBreachSwapped = false;
        originalSlot = -1;
        swapBackTimer = 0;
    }

    public static void onPearlHitEntity(Entity target) {}
    public static void toggleAimLock(LocalPlayer player, Minecraft client) {}
    public static boolean isAimLockActive() { return false; }
}
