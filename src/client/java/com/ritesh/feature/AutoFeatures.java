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
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

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
    private static boolean wasAttackPressed = false;
    private static int windActionCooldown = 0;
    private static int lastSelectedSlot = -1;
    static boolean lungeSwapPending = false;
    static int lungeReturnSlot = -1;
    static int lungeSwapDelay = 0;
    private static boolean airPotThrown = false;
    private static int airPotWindDelay = 0;
    private static boolean slamFollowUpPending = false;
    private static Entity slamFollowUpTarget = null;
    private static int slamFollowUpSlot = -1;
    private static int slamFollowUpDelay = 0;
    private static boolean isAttacking = false;
    private static int triggerBotHitsThisFall = 0;
    private static int triggerBotCooldown = 0;
    private static boolean triggerBotArmed = false;
    private static int triggerBotFireDelay = 0;
    private static boolean wasInAir = false;
    private static boolean chestplateEquipped = false;

    public static void onRender(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        if (!cfg.aimAssistEnabled) return;
        AimAssist.applyRotation(p, cfg);
    }

    public static boolean isLungeSwapPending() {
        return lungeSwapPending;
    }

    public static void startLungeSwap(int returnSlot) {
        lungeReturnSlot = returnSlot;
        lungeSwapPending = true;
        lungeSwapDelay = ModConfig.get().lungeSwapDelay;
    }

    public static void onBlockHit(ClientPlayerEntity p) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.lungeSwapEnabled || lungeSwapPending) return;
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();
        ItemStack held = p.getMainHandStack();
        if (!isSword(held) && !isAxe(held) && !held.isOf(Items.MACE) && !isSpear(held)) {
            int spearSlot = findSpear(p);
            MinecraftClient client = MinecraftClient.getInstance();
            if (spearSlot != -1 && client.getNetworkHandler() != null) {
                int returnSlot = inv.getSelectedSlot();
                inv.setSelectedSlot(spearSlot);
                client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(spearSlot));
                startLungeSwap(returnSlot);
            }
        }
    }


    public static void onAttack(PlayerEntity player) {
        ModConfig cfg = ModConfig.get();
        MinecraftClient client = MinecraftClient.getInstance();
        if (player == null) return;

        // Lunge logic for air/mobs
        if (cfg.lungeSwapEnabled && !lungeSwapPending && player instanceof ClientPlayerEntity) {
            InventoryAccessor inv = (InventoryAccessor) player.getInventory();
            ItemStack held = player.getMainHandStack();
            if (!isSword(held) && !isAxe(held) && !held.isOf(Items.MACE) && !isSpear(held)) {
                int spearSlot = findSpear((ClientPlayerEntity) player);
                if (spearSlot != -1 && client.getNetworkHandler() != null) {
                    int returnSlot = inv.getSelectedSlot();
                    inv.setSelectedSlot(spearSlot);
                    client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(spearSlot));
                    startLungeSwap(returnSlot);
                }
            }
        }
    }

    /**
     * Called specifically when an entity is successfully hit
     */
    public static void onHit(PlayerEntity player, Entity target) {
        if (player == null || target == null) return;
        ModConfig cfg = ModConfig.get();
        if (lungeSwapPending && lungeReturnSlot != -1) return;

        boolean isPlayer = target instanceof PlayerEntity;
        boolean isMob = target instanceof MobEntity || target instanceof AnimalEntity;
        if (isPlayer && !cfg.combatTargetPlayers) return;
        if (isMob && !cfg.combatTargetMobs) return;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        int startingSlot = inv.getSelectedSlot();
        ItemStack held = player.getMainHandStack();

        boolean isWeapon = isSword(held) || isAxe(held) || held.isOf(Items.MACE);
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
                        if (swordSlot != -1) {
                            queueSlamFollowUp(target, swordSlot, 3);
                        }
                    }
                    return;
                }
            }
        }

        if (!allowedByMode) return;

        if (cfg.autoBreachSwapEnabled && !isBreachSwapped) {
            boolean wantDensity;
            if (cfg.smartSwitchEnabled) {
                wantDensity = !player.isOnGround() && player.getVelocity().y < -0.3;
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

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;

        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        int currentSlot = inv.getSelectedSlot();
        if (!p.getInventory().getStack(currentSlot).isOf(Items.ENDER_PEARL)) {
            lastSelectedSlot = currentSlot;
        }

        boolean inAir = !p.isOnGround();
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
                    client.getNetworkHandler().sendPacket(
                        new UpdateSelectedSlotC2SPacket(lungeReturnSlot)
                    );
                    lungeReturnSlot = -1;
                }
            }
        }

        boolean attackPressed = client.options.attackKey.isPressed();
        wasAttackPressed = attackPressed;

        if (cfg.aimAssistEnabled) {
            AimAssist.tick(client, cfg);
            AimAssist.applyRotation(p, cfg);

            if (cfg.triggerBotEnabled && AimAssist.hasTarget()
                    && triggerBotHitsThisFall < 2
                    && triggerBotCooldown == 0
                    && !triggerBotArmed) {

                boolean fallingCheck = !cfg.triggerBotFallingOnly ||
                        (!p.isOnGround() && p.getVelocity().y < -0.1);

                if (fallingCheck) {
                 if (cfg.autoChestplateOnTrigger && !chestplateEquipped) {
    double dist = p.distanceTo(AimAssist.getLockedTarget());
    double fallSpeed = -p.getVelocity().y; // positive when falling
    if (dist <= 4.5 && fallSpeed > 0.5) { // only if falling fast
        int chestSlot = findChestplate(p);
        if (chestSlot != -1 && client.interactionManager != null) {
            client.interactionManager.clickSlot(
                p.playerScreenHandler.syncId,
                6,
                chestSlot,
                net.minecraft.screen.slot.SlotActionType.SWAP,
                p
            );
            chestplateEquipped = true;
        }
    }
}

                    HitResult hit = client.crosshairTarget;
                    if (hit instanceof EntityHitResult ehr && ehr.getEntity() == AimAssist.getLockedTarget()) {
                        triggerBotArmed = true;
                        triggerBotFireDelay = 1 + (int)(Math.random() * 3);
                    }
                }
            }

           if (triggerBotArmed && --triggerBotFireDelay <= 0) {
    triggerBotArmed = false;
    HitResult hit = client.crosshairTarget;
    if (hit instanceof EntityHitResult ehr && ehr.getEntity() == AimAssist.getLockedTarget()) {
        client.interactionManager.attackEntity(p, AimAssist.getLockedTarget());
        p.swingHand(Hand.MAIN_HAND);
        triggerBotHitsThisFall++;
        triggerBotCooldown = 9 + (int)(Math.random() * 2);
    }
  }
}

        if (cameraLocked) p.setPitch(-90.0F);
        if (windActionCooldown > 0) windActionCooldown--;
        if (cfg.windOnRightClickEnabled) tickWindRightClick(p, client, inv);

        if (slamPending && slamTarget != null) {
            slamPending = false;
            int prev = inv.getSelectedSlot();
            inv.setSelectedSlot(slamMaceSlot);
            if (!isAttacking) {
                isAttacking = true;
                client.interactionManager.attackEntity(p, slamTarget);
                p.swingHand(Hand.MAIN_HAND);
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
                    client.interactionManager.attackEntity(p, slamFollowUpTarget);
                    p.swingHand(Hand.MAIN_HAND);
                    isAttacking = false;
                }
                slamFollowUpTarget = null;
            }
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

        if (airPotThrown && --airPotWindDelay <= 0) {
            airPotThrown = false;
            fireAirPotWindCharge(client);
        }
    }

    private static void fireAirPotWindCharge(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;
        if (p.getOffHandStack().isOf(Items.WIND_CHARGE)) {
            client.interactionManager.interactItem(p, Hand.OFF_HAND);
            p.swingHand(Hand.OFF_HAND);
            return;
        }
        int slot = findItem(p, "wind_charge");
        if (slot == -1) return;
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();
        int returnSlot = inv.getSelectedSlot();
        inv.setSelectedSlot(slot);
        client.interactionManager.interactItem(p, Hand.MAIN_HAND);
        p.swingHand(Hand.MAIN_HAND);
        client.execute(() -> inv.setSelectedSlot(returnSlot));
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
        lastSelectedSlot = -1;
        lungeSwapPending = false;
        lungeReturnSlot = -1;
        lungeSwapDelay = 0;
        airPotThrown = false;
        airPotWindDelay = 0;
        slamFollowUpPending = false;
        slamFollowUpTarget = null;
        slamFollowUpDelay = 0;
        wasAttackPressed = false;
        isAttacking = false;
        triggerBotHitsThisFall = 0;
        triggerBotCooldown = 0;
        triggerBotArmed = false;
        triggerBotFireDelay = 0;
        wasInAir = false;
        chestplateEquipped = false;
        AimAssist.reset();
    }

    public static void onPearlThrown(ClientPlayerEntity player) {
        ModConfig cfg = ModConfig.get();
        if (cfg.pearlCatchEnabled) {
            pearlThrown = true;
            pearlCatchTimer = cfg.pearlCatchDelayTicks;
            pearlReturnSlot = switch (cfg.pearlReturnMode) {
                case SWORD -> findItem(player, "sword");
                case AXE -> findItem(player, "axe");
                case ELYTRA -> findItem(player, "elytra");
                default -> lastSelectedSlot != -1 ? lastSelectedSlot : ((InventoryAccessor) player.getInventory()).getSelectedSlot();
            };
            if (pearlReturnSlot == -1) {
                pearlReturnSlot = lastSelectedSlot != -1 ? lastSelectedSlot : ((InventoryAccessor) player.getInventory()).getSelectedSlot();
            }
            boolean hasWindCharge = findItem(player, "wind_charge") != -1 || player.getOffHandStack().isOf(Items.WIND_CHARGE);
            if (hasWindCharge && player.getPitch() <= -60.0F) {
                player.setPitch(-90.0F);
                cameraLocked = true;
            }
        }
    }

    public static void onSplashPotThrown(ClientPlayerEntity player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.airPotsEnabled) return;
        if (player.isOnGround()) return;
        airPotThrown = true;
        airPotWindDelay = 2;
    }

    private static void queueMaceSlam(Entity target, int maceSlot, int returnSlot) {
        slamTarget = target;
        slamMaceSlot = maceSlot;
        slamReturnSlot = returnSlot;
        slamPending = true;
    }

    private static void queueSlamFollowUp(Entity target, int returnSlot, int delayTicks) {
        slamFollowUpTarget = target;
        slamFollowUpSlot = returnSlot;
        slamFollowUpDelay = delayTicks;
        slamFollowUpPending = true;
    }

    private static boolean isSword(ItemStack s) {
        return Registries.ITEM.getId(s.getItem()).toString().contains("sword");
    }

    private static boolean isAxe(ItemStack s) {
        return Registries.ITEM.getId(s.getItem()).toString().contains("axe");
    }

    private static boolean isSpear(ItemStack s) {
        return Registries.ITEM.getId(s.getItem()).toString().contains("spear");
    }

    private static int findSpear(ClientPlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            if (Registries.ITEM.getId(s.getItem()).toString().contains("spear")) return i;
        }
        return -1;
    }

    private static int findItem(PlayerEntity p, String name) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            if (Registries.ITEM.getId(s.getItem()).toString().contains(name)) return i;
        }
        return -1;
    }

    private static int findChestplate(ClientPlayerEntity p) {
    // removed slot 38 check temporarily to debug
    for (int i = 0; i < 9; i++) {
        ItemStack s = p.getInventory().getStack(i);
        if (s.isEmpty()) continue;
        if (Registries.ITEM.getId(s.getItem()).toString().contains("chestplate")) return i;
    }
    return -1;
}

    private static int findBestMace(PlayerEntity p, boolean wantDensity) {
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

    public static void onPearlHitEntity(Entity target) {}
    public static void toggleAimLock(ClientPlayerEntity player, MinecraftClient client) {}
    public static boolean isAimLockActive() { return false; }
}