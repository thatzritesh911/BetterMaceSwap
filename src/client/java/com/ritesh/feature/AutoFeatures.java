package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.InteractionHand;

public class AutoFeatures {

    private static int originalSlot = -1;
    private static int swapBackTimer = 0;
    private static boolean isBreachSwapped = false;

    private static Entity slamTarget = null;
    private static int slamMaceSlot = -1;
    private static int slamReturnSlot = -1;
    private static boolean slamPending = false;

    private static boolean pearlThrown = false;
    private static int pearlCatchTimer = 0;
    private static int pearlReturnSlot = -1;
    private static boolean cameraLocked = false;
    private static int windFireDelay = 0;
    private static int windSlot = -1;
    private static int windReturnSlot = -1;

    private static boolean wasUsePressed = false;
    private static int windActionCooldown = 0;

    private static boolean aimLockActive = false;
    private static int lastSelectedSlot = -1;

    public static void onHit(LocalPlayer player, Entity target) {
        ModConfig cfg = ModConfig.get();
        if (player == null || target == null) return;

        boolean isPlayer = target instanceof Player;
        boolean isMob = target instanceof Mob || target instanceof Animal;
        if (isPlayer && !cfg.combatTargetPlayers) return;
        if (isMob && !cfg.combatTargetMobs) return;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        int startingSlot = inv.getSelectedSlot();
        ItemStack held = player.getMainHandItem();

        boolean isWeapon = isSword(held) || isAxe(held);
        boolean allowedByMode = isWeapon || (cfg.attributeSwapMode == ModConfig.AttributeSwapMode.ALL);

        if (!allowedByMode && !cfg.autoStunSlamEnabled) return;

        if (cfg.autoStunSlamEnabled && target instanceof LivingEntity tp) {
            if (tp.isBlocking()) {
                boolean wantDensity = cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY;
                int maceSlot = findBestMace(player, wantDensity);

                if (isSword(held)) {
                    int axeSlot = findItem(player, "_axe");
                    if (axeSlot != -1) {
                        inv.setSelectedSlot(axeSlot);
                        Minecraft.getInstance().gameMode.attack(player, target);
                        player.swing(InteractionHand.MAIN_HAND);
                        if (maceSlot != -1) {
                            queueMaceSlam(target, maceSlot, startingSlot);
                        } else {
                            inv.setSelectedSlot(startingSlot);
                        }
                        return;
                    }
                } else if (isAxe(held)) {
                    if (maceSlot != -1) {
                        queueMaceSlam(target, maceSlot, startingSlot);
                        return;
                    }
                }
            }
        }

        if (!allowedByMode) return;

        if (cfg.autoBreachSwapEnabled && !isBreachSwapped) {
            boolean wantDensity;
            if (cfg.smartSwitchEnabled) {
                wantDensity = !player.onGround();
            } else {
                wantDensity = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY);
            }

            int maceSlot = findBestMace(player, wantDensity);
            if (maceSlot != -1 && maceSlot != startingSlot) {
                originalSlot = startingSlot;
                inv.setSelectedSlot(maceSlot);
                isBreachSwapped = true;
                swapBackTimer = cfg.breachSwapBackTicks;
            }
        }
    }

    public static void onPearlThrown(LocalPlayer player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.pearlCatchEnabled) return;

        pearlThrown = true;
        pearlCatchTimer = cfg.pearlCatchDelayTicks;
        pearlReturnSlot = lastSelectedSlot != -1 ? lastSelectedSlot : ((InventoryAccessor) player.getInventory()).getSelectedSlot();

        if (player.getXRot() <= -60.0F) {
            player.setXRot(-90.0F);
            cameraLocked = true;
        }
    }

    public static void toggleAimLock(LocalPlayer player, Minecraft client) {
        aimLockActive = !aimLockActive;
    }

    public static boolean isAimLockActive() {
        return aimLockActive;
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

        if (cfg.aimAssistEnabled) {
            double velocityY = p.getDeltaMovement().y;
            if (!p.onGround() && velocityY < -0.1 && velocityY < -(cfg.aimFallBlocks * 0.08)) {
                assistAim(p, client);
            }
        }

        if (cameraLocked) p.setXRot(-90.0F);
        if (windActionCooldown > 0) windActionCooldown--;
        if (cfg.windOnRightClickEnabled) tickWindRightClick(p, client, inv);

        if (slamPending && slamTarget != null) {
            slamPending = false;
            inv.setSelectedSlot(slamMaceSlot);
            client.gameMode.attack(p, slamTarget);
            p.swing(InteractionHand.MAIN_HAND);
            inv.setSelectedSlot(slamReturnSlot);
            slamTarget = null;
        }

        if (isBreachSwapped && --swapBackTimer <= 0) {
            forceReset(p);
        }

        if (pearlThrown && --pearlCatchTimer <= 0) {
            pearlThrown = false;
            int slot = findItem(p, "wind_charge");
            if (slot != -1 && cameraLocked) {
                windSlot = slot;
                windReturnSlot = pearlReturnSlot;
                inv.setSelectedSlot(windSlot);
                windFireDelay = 2;
            } else {
                cameraLocked = false;
                windSlot = -1;
                windReturnSlot = -1;
                windFireDelay = 0;
            }
        }

        if (windFireDelay > 0 && --windFireDelay == 0) {
            fireWindCharge(client);
        }
    }

    private static void tickWindRightClick(LocalPlayer p, Minecraft mc, InventoryAccessor inv) {
        boolean usePressed = mc.options.keyUse.isDown();
        boolean justPressed = usePressed && !wasUsePressed;
        wasUsePressed = usePressed;

        if (justPressed && windActionCooldown <= 0) {
            ItemStack held = p.getMainHandItem();
            if (isSword(held) || isAxe(held) || held.is(Items.MACE)) {
                if (p.getOffhandItem().is(Items.WIND_CHARGE)) {
                    windReturnSlot = -1;
                    windSlot = -1;
                    windFireDelay = 2;
                    windActionCooldown = 10;
                } else {
                    int slot = findItem(p, "wind_charge");
                    if (slot != -1) {
                        windReturnSlot = inv.getSelectedSlot();
                        windSlot = slot;
                        inv.setSelectedSlot(windSlot);
                        windFireDelay = 2;
                        windActionCooldown = 15;
                    }
                }
            }
        }
    }

    private static void fireWindCharge(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        InteractionHand fireHand = (windSlot == -1) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        client.gameMode.useItem(p, fireHand);
        p.swing(fireHand);

        final int returnSlot = windReturnSlot;
        client.execute(() -> {
            if (returnSlot != -1) {
                ((InventoryAccessor) p.getInventory()).setSelectedSlot(returnSlot);
            }
            cameraLocked = false;
            windSlot = -1;
            windReturnSlot = -1;
        });
    }

    private static void assistAim(LocalPlayer p, Minecraft mc) {
        ModConfig cfg = ModConfig.get();
        if (mc.level == null || p.isSpectator()) return;

        Entity target = null;
        double bestDist = 36.0;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == p || !e.isAlive()) continue;
            boolean isPlayer = e instanceof Player;
            boolean isMob = e instanceof Mob || e instanceof Animal;

            if (isPlayer && !cfg.aimTargetPlayers) continue;
            if (isMob && !cfg.aimTargetMobs) continue;
            if (!isPlayer && !isMob) continue;

            double d = p.distanceToSqr(e);
            if (d < bestDist) {
                bestDist = d;
                target = e;
            }
        }

        if (target != null) {
            double dx = target.getX() - p.getX();
            double dy = target.getY() - p.getY();
            double dz = target.getZ() - p.getZ();
            float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
            float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
            float maxStep = (float) cfg.aimSpeed / 10.0F;
            p.setYRot(p.getYRot() + Mth.clamp(Mth.wrapDegrees(targetYaw - p.getYRot()) * 0.25F, -maxStep, maxStep));
            p.setXRot(p.getXRot() + Mth.clamp(Mth.wrapDegrees(targetPitch - p.getXRot()) * 0.25F, -maxStep, maxStep));
        }
    }

    private static void forceReset(LocalPlayer player) {
        if (originalSlot != -1) {
            ((InventoryAccessor) player.getInventory()).setSelectedSlot(originalSlot);
        }
        isBreachSwapped = false;
        originalSlot = -1;
    }

    public static void fullReset(LocalPlayer player) {
        forceReset(player);
        slamPending = false;
        slamTarget = null;
        pearlThrown = false;
        cameraLocked = false;
        windFireDelay = 0;
        windSlot = -1;
        windReturnSlot = -1;
        aimLockActive = false;
        lastSelectedSlot = -1;
    }

    private static void queueMaceSlam(Entity target, int maceSlot, int returnSlot) {
        slamTarget = target;
        slamMaceSlot = maceSlot;
        slamReturnSlot = returnSlot;
        slamPending = true;
    }

    private static boolean isSword(ItemStack s) { return s.getItem().toString().contains("sword"); }
    private static boolean isAxe(ItemStack s) { return s.getItem().toString().contains("_axe"); }

    private static int findItem(LocalPlayer p, String name) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).getItem().toString().contains(name)) return i;
        }
        return -1;
    }

    private static int findBestMace(LocalPlayer p, boolean wantDensity) {
        ResourceKey<Enchantment> goal = wantDensity ? Enchantments.DENSITY : Enchantments.BREACH;
        int fallback = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(Items.MACE)) {
                if (fallback == -1) fallback = i;
                if (hasEnchant(s, goal)) return i;
            }
        }
        return fallback;
    }

    private static boolean hasEnchant(ItemStack s, ResourceKey<Enchantment> key) {
        ItemEnchantments enc = s.get(DataComponents.ENCHANTMENTS);
        if (enc == null) return false;
        for (Holder<Enchantment> entry : enc.keySet()) {
            if (entry.is(key)) return true;
        }
        return false;
    }
}
