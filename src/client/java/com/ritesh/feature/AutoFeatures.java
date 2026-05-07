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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public class AutoFeatures {

    private static int originalSlot = -1;
    private static int swapBackTimer = 0;
    private static boolean isBreachSwapped = false;
    private static Entity slamTarget = null;
    private static int slamMaceSlot = -1;
    private static int slamReturnSlot = -1;
    private static boolean slamPending = false;
    private static boolean slamFollowUpPending = false;
    private static Entity slamFollowUpTarget = null;
    private static int slamFollowUpSlot = -1;
    private static int slamFollowUpDelay = 0;
    private static boolean pearlThrown = false;
    private static int pearlCatchTimer = 0;
    private static int pearlReturnSlot = -1;
    private static boolean cameraLocked = false;
    private static int windFireDelay = 0;
    private static int windSlot = -1;
    private static int windReturnSlot = -1;
    private static boolean wasUsePressed = false;
    private static boolean wasAttackPressed = false;
    private static int windActionCooldown = 0;
    private static int lastSelectedSlot = -1;
    static boolean lungeSwapPending = false;
    static int lungeReturnSlot = -1;
    static int lungeSwapDelay = 0;
    private static boolean airPotThrown = false;
    private static int airPotWindDelay = 0;
    private static boolean isAttacking = false;
    private static int triggerBotHitsThisFall = 0;
    private static int triggerBotCooldown = 0;
    private static boolean triggerBotArmed = false;
    private static int triggerBotFireDelay = 0;
    private static boolean wasInAir = false;
    private static boolean chestplateEquipped = false;
    private static boolean aimLockActive = false;

    public static void onRender(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        if (!cfg.aimAssistEnabled) return;
        AimAssist.applyRotation(p, cfg);
    }

    public static boolean isLungeSwapPending() { return lungeSwapPending; }

    public static void startLungeSwap(int returnSlot) {
    lungeReturnSlot = returnSlot;
    lungeSwapPending = true;
    lungeSwapDelay = ModConfig.get().lungeSwapDelay;
}

// ADD HERE
public static void onDoAttack(LocalPlayer player) {
    ModConfig cfg = ModConfig.get();
    if (!cfg.lungeSwapEnabled || lungeSwapPending) return;
    Minecraft client = Minecraft.getInstance();
    InventoryAccessor inv = (InventoryAccessor) player.getInventory();
    ItemStack held = player.getMainHandItem();
    if (!isSword(held) && !isAxe(held) && !held.is(Items.MACE) && !isSpear(held)) {
        int spearSlot = findSpear(player);
        if (spearSlot != -1 && client.getConnection() != null) {
            int returnSlot = inv.getSelectedSlot();
            inv.setSelectedSlot(spearSlot);
            client.getConnection().send(new ServerboundSetCarriedItemPacket(spearSlot));
            startLungeSwap(returnSlot);
        }
    }
}

    public static boolean isSwordHeld(ItemStack s) { return isSword(s); }
    public static boolean isAxeHeld(ItemStack s) { return isAxe(s); }
    public static boolean isSpearHeld(ItemStack s) { return isSpear(s); }
    public static int findSpearSlot(LocalPlayer p) { return findSpear(p); }

    public static void onHit(LocalPlayer player, Entity target) {
        if (player == null || target == null) return;
        ModConfig cfg = ModConfig.get();
        if (lungeSwapPending && lungeReturnSlot != -1) return;

        boolean isPlayer = target instanceof Player;
        boolean isMob = target instanceof Mob || target instanceof Animal;
        if (isPlayer && !cfg.combatTargetPlayers) return;
        if (isMob && !cfg.combatTargetMobs) return;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        int startingSlot = inv.getSelectedSlot();
        ItemStack held = player.getMainHandItem();

        boolean isWeapon = isSword(held) || isAxe(held) || held.is(Items.MACE);
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
                            queueSlamFollowUp(target, startingSlot, 3);
                        } else {
                            queueSlamFollowUp(target, startingSlot, 2);
                        }
                        return;
                    }
                } else if (isAxe(held)) {
                    if (maceSlot != -1) {
                        queueMaceSlam(target, maceSlot, startingSlot);
                        queueSlamFollowUp(target, startingSlot, 3);
                    } else {
                        int swordSlot = findItem(player, "sword");
                        if (swordSlot != -1) queueSlamFollowUp(target, swordSlot, 3);
                    }
                    return;
                }
            }
        }

        if (!allowedByMode) return;

        if (cfg.autoBreachSwapEnabled && !isBreachSwapped) {
            boolean wantDensity;
            if (cfg.smartSwitchEnabled) {
                wantDensity = !player.onGround() && player.getDeltaMovement().y < -0.3;
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

        if (lungeSwapPending) {
            if (lungeSwapDelay > 0) {
                lungeSwapDelay--;
            } else {
                lungeSwapPending = false;
                if (lungeReturnSlot != -1) {
                    inv.setSelectedSlot(lungeReturnSlot);
                    if (client.getConnection() != null) {
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(lungeReturnSlot));
                    }
                    lungeReturnSlot = -1;
                }
            }
        }

        boolean attackPressed = client.options.keyAttack.isDown();
        boolean justAttacked = attackPressed && !wasAttackPressed;

        wasAttackPressed = attackPressed;

        if (cfg.aimAssistEnabled) {
            AimAssist.tick(client, cfg);
            AimAssist.applyRotation(p, cfg);

            if (cfg.triggerBotEnabled && AimAssist.hasTarget()
                    && triggerBotHitsThisFall < 2
                    && triggerBotCooldown == 0
                    && !triggerBotArmed) {

                boolean fallingCheck = !cfg.triggerBotFallingOnly ||
                        (!p.onGround() && p.getDeltaMovement().y < -0.1);

                if (fallingCheck) {
                    if (cfg.autoChestplateOnTrigger && !chestplateEquipped) {
    double dist = p.distanceTo(AimAssist.getLockedTarget());
    double fallSpeed = -p.getDeltaMovement().y;
    if (dist <= 4.5 && fallSpeed > 0.5) {
        int chestSlot = findChestplate(p);
        if (chestSlot != -1) {
            inv.setSelectedSlot(chestSlot);
            client.gameMode.useItem(p, net.minecraft.world.InteractionHand.MAIN_HAND);
            chestplateEquipped = true;
        }
    }
}
                    HitResult hit = client.hitResult;
                    if (hit instanceof EntityHitResult ehr && ehr.getEntity() == AimAssist.getLockedTarget()) {
                        triggerBotArmed = true;
                        triggerBotFireDelay = 1 + (int)(Math.random() * 3);
                    }
                }
            }

            if (triggerBotArmed && --triggerBotFireDelay <= 0) {
                triggerBotArmed = false;
                HitResult hit = client.hitResult;
                if (hit instanceof EntityHitResult ehr && ehr.getEntity() == AimAssist.getLockedTarget()) {
                    client.gameMode.attack(p, AimAssist.getLockedTarget());
                    p.swing(InteractionHand.MAIN_HAND);
                    triggerBotHitsThisFall++;
                    triggerBotCooldown = 9 + (int)(Math.random() * 2);
                }
            }
        }

        if (cameraLocked) p.setXRot(-90.0F);
        if (windActionCooldown > 0) windActionCooldown--;
        if (cfg.windOnRightClickEnabled) tickWindRightClick(p, client, inv);

        if (slamPending && slamTarget != null) {
            slamPending = false;
            int prev = inv.getSelectedSlot();
            inv.setSelectedSlot(slamMaceSlot);
            if (!isAttacking) {
                isAttacking = true;
                client.gameMode.attack(p, slamTarget);
                p.swing(InteractionHand.MAIN_HAND);
                isAttacking = false;
            }
            inv.setSelectedSlot(slamReturnSlot != -1 ? slamReturnSlot : prev);
            slamTarget = null;
        }

        if (slamFollowUpPending && slamFollowUpDelay > 0 && --slamFollowUpDelay == 0) {
            slamFollowUpPending = false;
            if (slamFollowUpTarget != null) {
                inv.setSelectedSlot(slamFollowUpSlot);
                if (!isAttacking) {
                    isAttacking = true;
                    client.gameMode.attack(p, slamFollowUpTarget);
                    p.swing(InteractionHand.MAIN_HAND);
                    isAttacking = false;
                }
                slamFollowUpTarget = null;
            }
        }

        if (isBreachSwapped && --swapBackTimer <= 0) forceReset(p);

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

        if (windFireDelay > 0 && --windFireDelay == 0) fireWindCharge(client);
        if (airPotThrown && --airPotWindDelay <= 0) { airPotThrown = false; fireAirPotWindCharge(client); }
    }

    private static void fireAirPotWindCharge(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        if (p.getOffhandItem().is(Items.WIND_CHARGE)) {
            client.gameMode.useItem(p, InteractionHand.OFF_HAND);
            p.swing(InteractionHand.OFF_HAND);
            return;
        }
        int slot = findItem(p, "wind_charge");
        if (slot == -1) return;
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();
        int returnSlot = inv.getSelectedSlot();
        inv.setSelectedSlot(slot);
        client.gameMode.useItem(p, InteractionHand.MAIN_HAND);
        p.swing(InteractionHand.MAIN_HAND);
        client.execute(() -> inv.setSelectedSlot(returnSlot));
    }

    private static void tickWindRightClick(LocalPlayer p, Minecraft mc, InventoryAccessor inv) {
        boolean usePressed = mc.options.keyUse.isDown();
        boolean justPressed = usePressed && !wasUsePressed;
        wasUsePressed = usePressed;
        if (justPressed && windActionCooldown <= 0) {
            ItemStack held = p.getMainHandItem();
            if (isSword(held) || isAxe(held) || held.is(Items.MACE)) {
                if (p.getOffhandItem().is(Items.WIND_CHARGE)) {
                    windReturnSlot = -1; windSlot = -1; windFireDelay = 2; windActionCooldown = 10;
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
        client.execute(() -> {
            if (windReturnSlot != -1) ((InventoryAccessor) p.getInventory()).setSelectedSlot(windReturnSlot);
            cameraLocked = false; windSlot = -1; windReturnSlot = -1;
        });
    }

    private static void forceReset(LocalPlayer player) {
        if (originalSlot != -1) ((InventoryAccessor) player.getInventory()).setSelectedSlot(originalSlot);
        isBreachSwapped = false;
        originalSlot = -1;
    }

    public static void fullReset(LocalPlayer player) {
        forceReset(player);
        slamPending = false; slamTarget = null; pearlThrown = false; cameraLocked = false;
        windFireDelay = 0; windSlot = -1; windReturnSlot = -1; lastSelectedSlot = -1;
        lungeSwapPending = false; lungeReturnSlot = -1; lungeSwapDelay = 0;
        airPotThrown = false; airPotWindDelay = 0;
        slamFollowUpPending = false; slamFollowUpTarget = null; slamFollowUpDelay = 0;
        wasAttackPressed = false; isAttacking = false;
        triggerBotHitsThisFall = 0; triggerBotCooldown = 0;
        triggerBotArmed = false; triggerBotFireDelay = 0;
        wasInAir = false; chestplateEquipped = false; aimLockActive = false;
        AimAssist.reset();
    }

    public static void onPearlThrown(LocalPlayer player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.pearlCatchEnabled) return;
        pearlThrown = true;
        pearlCatchTimer = cfg.pearlCatchDelayTicks;
        pearlReturnSlot = switch (cfg.pearlReturnMode) {
            case SWORD -> findItem(player, "sword");
            case AXE -> findItem(player, "_axe");
            case ELYTRA -> findItem(player, "elytra");
            default -> lastSelectedSlot != -1 ? lastSelectedSlot : ((InventoryAccessor) player.getInventory()).getSelectedSlot();
        };
        if (pearlReturnSlot == -1) pearlReturnSlot = lastSelectedSlot != -1 ? lastSelectedSlot : ((InventoryAccessor) player.getInventory()).getSelectedSlot();
        boolean hasWindCharge = findItem(player, "wind_charge") != -1 || player.getOffhandItem().is(Items.WIND_CHARGE);
        if (hasWindCharge && player.getXRot() <= -60.0F) { player.setXRot(-90.0F); cameraLocked = true; }
    }

    public static void onSplashPotThrown(LocalPlayer player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.airPotsEnabled) return;
        if (player.onGround()) return;
        airPotThrown = true;
        airPotWindDelay = 2;
    }

    public static void toggleAimLock(LocalPlayer player, Minecraft client) { aimLockActive = !aimLockActive; }
    public static boolean isAimLockActive() { return aimLockActive; }
    public static void onPearlHitEntity(Entity target) {}

    private static void queueMaceSlam(Entity target, int maceSlot, int returnSlot) {
        slamTarget = target; slamMaceSlot = maceSlot; slamReturnSlot = returnSlot; slamPending = true;
    }

    private static void queueSlamFollowUp(Entity target, int returnSlot, int delayTicks) {
        slamFollowUpTarget = target; slamFollowUpSlot = returnSlot; slamFollowUpDelay = delayTicks; slamFollowUpPending = true;
    }

    private static boolean isSword(ItemStack s) { return s.getItem().toString().contains("sword"); }
    private static boolean isAxe(ItemStack s) { return s.getItem().toString().contains("_axe"); }
    private static boolean isSpear(ItemStack s) { return s.getItem().toString().contains("spear"); }

    private static int findSpear(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem().toString().contains("spear")) return i;
        }
        return -1;
    }

    private static int findItem(LocalPlayer p, String name) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem().toString().contains(name)) return i;
        }
        return -1;
    }

    private static int findChestplate(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem().toString().contains("chestplate")) return i;
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
        for (Holder<Enchantment> entry : enc.keySet()) { if (entry.is(key)) return true; }
        return false;
    }
}
