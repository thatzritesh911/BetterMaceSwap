package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
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

    public static void onHit(ClientPlayerEntity player, Entity target) {
        ModConfig cfg = ModConfig.get();
        if (player == null || target == null) return;

        boolean isPlayer = target instanceof PlayerEntity;
        boolean isMob = target instanceof MobEntity || target instanceof AnimalEntity;
        if (isPlayer && !cfg.combatTargetPlayers) return;
        if (isMob && !cfg.combatTargetMobs) return;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        int startingSlot = inv.getSelectedSlot();
        ItemStack held = player.getMainHandStack();

        boolean isWeapon = isSword(held) || isAxe(held);
        boolean allowedByMode = isWeapon || (cfg.attributeSwapMode == ModConfig.AttributeSwapMode.ALL);

        if (!allowedByMode && !cfg.autoStunSlamEnabled) return;

        if (cfg.autoStunSlamEnabled && target instanceof LivingEntity tp) {
            if (tp.isBlocking()) {
                boolean wantDensity = cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY;
                int maceSlot = findBestMace(player, wantDensity);

                if (isSword(held)) {
                    int axeSlot = findItem(player, "axe");
                    if (axeSlot != -1) {
                        inv.setSelectedSlot(axeSlot);
                        MinecraftClient.getInstance().interactionManager.attackEntity(player, target);
                        player.swingHand(Hand.MAIN_HAND);

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
                wantDensity = !player.isOnGround();
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

    public static void onPearlThrown(ClientPlayerEntity player) {
        ModConfig cfg = ModConfig.get();
        if (cfg.pearlCatchEnabled) {
            pearlThrown = true;
            pearlCatchTimer = cfg.pearlCatchDelayTicks;
            pearlReturnSlot = lastSelectedSlot != -1 ? lastSelectedSlot : ((InventoryAccessor) player.getInventory()).getSelectedSlot();

            if (player.getPitch() <= -60.0F) {
                player.setPitch(-90.0F);
                cameraLocked = true;
            }
        }
    }

    public static void onPearlHitEntity(Entity target) {}

    public static void toggleAimLock(ClientPlayerEntity player, MinecraftClient client) {
        aimLockActive = !aimLockActive;
    }

    public static boolean isAimLockActive() {
        return aimLockActive;
    }

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;

        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        int currentSlot = inv.getSelectedSlot();
        if (!p.getInventory().getStack(currentSlot).isOf(Items.ENDER_PEARL)) {
            lastSelectedSlot = currentSlot;
        }

        if (cfg.aimAssistEnabled) {
            double velocityY = p.getVelocity().y;
            if (!p.isOnGround() && velocityY < -0.1 && velocityY < -(cfg.aimFallBlocks * 0.08)) {
                assistAim(p, client);
            }
        }

        if (cameraLocked) p.setPitch(-90.0F);
        if (windActionCooldown > 0) windActionCooldown--;
        if (cfg.windOnRightClickEnabled) tickWindRightClick(p, client, inv);

        if (slamPending && slamTarget != null) {
            slamPending = false;
            inv.setSelectedSlot(slamMaceSlot);
            client.interactionManager.attackEntity(p, slamTarget);
            p.swingHand(Hand.MAIN_HAND);
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

    private static void tickWindRightClick(ClientPlayerEntity p, MinecraftClient mc, InventoryAccessor inv) {
        boolean usePressed = mc.options.useKey.isPressed();
        boolean justPressed = usePressed && !wasUsePressed;
        wasUsePressed = usePressed;

        if (justPressed && windActionCooldown <= 0) {
            ItemStack held = p.getMainHandStack();
            if (isSword(held) || isAxe(held) || held.isOf(Items.MACE)) {
                if (p.getOffHandStack().isOf(Items.WIND_CHARGE)) {
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

    private static void fireWindCharge(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;
        Hand fireHand = (windSlot == -1) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        client.interactionManager.interactItem(p, fireHand);
        p.swingHand(fireHand);

        client.execute(() -> {
            if (windReturnSlot != -1) {
                ((InventoryAccessor) p.getInventory()).setSelectedSlot(windReturnSlot);
            }
            cameraLocked = false;
            windSlot = -1;
            windReturnSlot = -1;
        });
    }

    private static void assistAim(ClientPlayerEntity p, MinecraftClient mc) {
        ModConfig cfg = ModConfig.get();
        if (mc.world == null || p.isSpectator()) return;

        Entity target = null;
        double bestDist = 36.0;

        for (Entity e : mc.world.getEntities()) {
            if (e == p || !e.isAlive()) continue;
            boolean isPlayer = e instanceof PlayerEntity;
            boolean isMob = e instanceof MobEntity || e instanceof AnimalEntity;

            if (isPlayer && !cfg.aimTargetPlayers) continue;
            if (isMob && !cfg.aimTargetMobs) continue;
            if (!isPlayer && !isMob) continue;

            double d = p.squaredDistanceTo(e);
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
            p.setYaw(p.getYaw() + MathHelper.clamp(MathHelper.wrapDegrees(targetYaw - p.getYaw()) * 0.25F, -maxStep, maxStep));
            p.setPitch(p.getPitch() + MathHelper.clamp(MathHelper.wrapDegrees(targetPitch - p.getPitch()) * 0.25F, -maxStep, maxStep));
        }
    }

    private static void forceReset(ClientPlayerEntity player) {
        if (originalSlot != -1) {
            ((InventoryAccessor) player.getInventory()).setSelectedSlot(originalSlot);
        }
        isBreachSwapped = false;
        originalSlot = -1;
    }

    public static void fullReset(ClientPlayerEntity player) {
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
    private static boolean isAxe(ItemStack s) { return s.getItem().toString().contains("axe"); }

    private static int findItem(ClientPlayerEntity p, String name) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).getItem().toString().contains(name)) return i;
        }
        return -1;
    }

    private static int findBestMace(ClientPlayerEntity p, boolean wantDensity) {
        int fallback = -1;
        String search = wantDensity ? "density" : "breach";
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(Items.MACE)) {
                if (fallback == -1) fallback = i;
                if (hasEnchantByName(s, search)) return i;
            }
        }
        return fallback;
    }

    private static boolean hasEnchantByName(ItemStack s, String name) {
        return EnchantmentHelper.getEnchantments(s).getEnchantments().stream()
                .anyMatch(entry -> entry.getKey().isPresent() &&
                    entry.getKey().get().getValue().getPath().contains(name.toLowerCase()));
    }
}