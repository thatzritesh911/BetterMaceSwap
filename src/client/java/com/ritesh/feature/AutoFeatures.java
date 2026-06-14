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
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@Environment(EnvType.CLIENT)
public class AutoFeatures {
    private static int originalSlot = -1;
    private static int swapBackTimer = 0;
    private static boolean isBreachSwapped = false;
    private static Entity slamTarget = null;
    private static int slamMaceSlot = -1;
    private static boolean slamPending = false;
    private static boolean pearlThrown = false;
    private static int jumpDelay = 0;
    private static float pearlSavedYaw = 0f;
private static int pearlWindDelay = 0;
private static boolean pearlRedirecting = false;
// NEW
private static boolean pearlExpMode = false;
private static boolean pearlExpDropDetected = false;
private static net.minecraft.util.math.Vec3d pearlSpawnPos = null;
private static net.minecraft.util.math.Vec3d pearlInitialVel = null;
private static int pearlSimTick = 0;
    private static Entity triggerBotTarget = null;
    private static int pearlCatchTimer = 0;
    private static int pearlReturnSlot = -1;
    private static boolean cameraLocked = false;
    private static boolean isPearlCatchFire = false;
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
    private static int slamMaceDelay = 0;
    private static boolean isAttacking = false;
    private static boolean isInSlamCombo = false;
    private static boolean slamReturnPending = false;
private static int slamReturnSlotQueued = -1;
private static int slamReturnDelay = 0;
private static int axeSwingDelay = 0;
private static boolean axeSwingHasMace = false;
private static int windReturnDelay = 0;
    private static int triggerBotHitsThisFall = 0;
    private static int triggerBotCooldown = 0;
    private static boolean triggerBotArmed = false;
    private static int triggerBotFireDelay = 0;
    private static boolean wasInAir = false;
    private static boolean chestplateEquipped = false;
    private static int swordSlotBeforeAxe = -1;

    public static void onRender(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
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

        if (cfg.lungeSwapEnabled && !lungeSwapPending && player instanceof ClientPlayerEntity) {
            HitResult hit = client.crosshairTarget;
            if (hit instanceof EntityHitResult) return;

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

    public static void onHit(PlayerEntity player, Entity target) {
        if (player == null || target == null) return;
        if (isInSlamCombo) return;
        if (isAttacking) return;
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
        boolean slamAllowed = cfg.autoStunSlamEnabled && (cfg.stunSlamTriggerMode == ModConfig.StunSlamTriggerMode.ALL || isWeapon);
        if (!allowedByMode && !slamAllowed) return;

        if (slamAllowed && target instanceof LivingEntity tp) {
            if (tp.isBlocking() && player.distanceTo(target) <= 3.0) {
                int axeSlot = findItem(player, "axe");
                boolean wantDensity = cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY;
                int maceSlot = findBestMace(player, wantDensity);
                int returnAfterSlam = (swordSlotBeforeAxe != -1) ? swordSlotBeforeAxe : startingSlot;
                swordSlotBeforeAxe = -1;
                
if (axeSlot != -1 && maceSlot != -1) {
    MinecraftClient mc = MinecraftClient.getInstance();
    isInSlamCombo = true;
    try {
        if (!isAxe(held)) {
            inv.setSelectedSlot(axeSlot);
            mc.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
        }
    } finally {
        isInSlamCombo = false;
    }
    slamReturnSlotQueued = returnAfterSlam;
    queueMaceSlam(target, maceSlot, cfg.stunSlamReturnSlot ? returnAfterSlam : maceSlot, cfg.safeStunSlam ? 1 : 0);
    return;
}

if (axeSlot != -1) {
                    inv.setSelectedSlot(axeSlot);
                    axeSwingDelay = 3;
                    slamTarget = target;
                    slamReturnSlotQueued = returnAfterSlam;
                    if (maceSlot != -1) {
                        axeSwingHasMace = true;
                        slamMaceSlot = maceSlot;
                    } else {
                        axeSwingHasMace = false;
                    }
                    return;
                }

                if (isAxe(held)) {
                    if (maceSlot != -1) {
                        queueMaceSlam(target, maceSlot, cfg.stunSlamReturnSlot ? returnAfterSlam : slamMaceSlot, 2);
                    } else {
                        queueSlotReturn(cfg.stunSlamReturnSlot ? returnAfterSlam : -1, 3);
                    }
                    return;
                }
            }
        }
        if (!allowedByMode) return;
        if (lungeSwapPending) return;

        // Force reset breach swap state if enough time has passed
        if (isBreachSwapped && swapBackTimer <= 0) {
            forceReset((ClientPlayerEntity) player);
        }

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
                swapBackTimer = cfg.breachSwapBackTicks + (Math.random() < 0.5 ? 1 : 0);
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
                    client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(lungeReturnSlot));
                    lungeReturnSlot = -1;
                }
            }
        }

        boolean attackPressed = client.options.attackKey.isPressed();
        wasAttackPressed = attackPressed;

        if (cfg.aimAssistEnabled) {
            AimAssist.tick(client, cfg);
            AimAssist.applyRotation(p, cfg);

            if (cfg.triggerBotEnabled && cfg.autoChestplateOnTrigger && !chestplateEquipped) {
    Entity aimTarget = AimAssist.getLockedTarget();
    if (aimTarget != null) {
        double dist = p.distanceTo(aimTarget);
        boolean fallingCheck = !cfg.triggerBotFallingOnly ||
                (!p.isOnGround() && p.getVelocity().y < -0.1);
        if (fallingCheck && dist <= 8.0) {
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
}

            if (cfg.triggerBotEnabled
        && cfg.aimAssistEnabled  // AA must be enabled
        && triggerBotHitsThisFall < 2
        && triggerBotCooldown == 0
        && !triggerBotArmed) {

    boolean fallingCheck = !cfg.triggerBotFallingOnly ||
            (!p.isOnGround() && p.getVelocity().y < -0.1);

    if (fallingCheck) {
        // crosshair must be on an entity (manual or AA-assisted aim)
        if (client.crosshairTarget instanceof EntityHitResult ehr) {
            Entity t = ehr.getEntity();
            double dist = p.distanceTo(t);
            boolean inRange = dist <= cfg.aimIntensity;
            boolean valid = (t instanceof PlayerEntity && cfg.combatTargetPlayers)
                    || ((t instanceof MobEntity || t instanceof AnimalEntity) && cfg.combatTargetMobs);
            if (valid && inRange) {
                triggerBotArmed = true;
                triggerBotTarget = t;
                triggerBotFireDelay = 1 + (int)(Math.random() * 3);
            }
        }
    }
}

            if (triggerBotArmed && --triggerBotFireDelay <= 0) {
                triggerBotArmed = false;
                if (triggerBotTarget != null && triggerBotTarget.isAlive()) {
                    client.interactionManager.attackEntity(p, triggerBotTarget);
                    p.swingHand(Hand.MAIN_HAND);
                    triggerBotHitsThisFall++;
                    triggerBotCooldown = 3 + (int)(Math.random() * 2);
                    triggerBotTarget = null;
                }
            }
        }

        if (cameraLocked) p.setPitch(-90.0F);
        if (windActionCooldown > 0) windActionCooldown--;
        if (jumpDelay > 0 && --jumpDelay == 0) p.jump();

        if (cfg.autoJumpWindCharge && p.isOnGround() && windActionCooldown <= 0 && p.getPitch() > 70.0F) {
            int wcSlot = findItem(p, "wind_charge");
            if (wcSlot != -1 && client.options.useKey.isPressed()) {
    ItemStack wc = p.getInventory().getStack(wcSlot);
    if (!isValidWindItem(wc)) return;
    ItemStack held = p.getMainHandStack();
    if (isSword(held) || isAxe(held) || held.isOf(Items.MACE)) {
        int delay = 3 + (int)(Math.random() * 2);
        windReturnSlot = inv.getSelectedSlot();
        windSlot = wcSlot;
        inv.setSelectedSlot(wcSlot);
        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(wcSlot));
        windActionCooldown = 3;
        windFireDelay = delay;
        jumpDelay = delay;
    }
}
        }
        
        if (cfg.windOnRightClickEnabled) tickWindRightClick(p, client, inv, cfg);

    if (slamPending && slamTarget != null) {
    if (slamMaceDelay > 0) {
        slamMaceDelay--;
    } else {
        isAttacking = false; // force reset just in case
        slamPending = false;
        inv.setSelectedSlot(slamMaceSlot);
        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slamMaceSlot));
        if (!isAttacking && slamTarget != null) {
            isAttacking = true;
            client.interactionManager.attackEntity(p, slamTarget);
            p.swingHand(Hand.MAIN_HAND);
            isAttacking = false;
        }

        slamReturnDelay = 5;
        slamReturnPending = true;
        slamTarget = null;
    }
}

if (slamReturnPending && --slamReturnDelay <= 0) {
    slamReturnPending = false;
    if (slamReturnSlotQueued != -1) {
        inv.setSelectedSlot(slamReturnSlotQueued);
        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slamReturnSlotQueued));
    }
    slamReturnSlotQueued = -1;
}

if (axeSwingDelay > 0 && --axeSwingDelay == 0) {
    if (slamTarget != null && slamTarget.isAlive()) {
        if (axeSwingHasMace) {
            int captured = slamReturnSlotQueued;
            queueMaceSlam(slamTarget, slamMaceSlot, cfg.stunSlamReturnSlot ? captured : slamMaceSlot, 0);
        } else {
            if (cfg.stunSlamReturnSlot) queueSlotReturn(slamReturnSlotQueued, 4);
        }
    }
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
            } else {
                inv.setSelectedSlot(slamFollowUpSlot);
                client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slamFollowUpSlot));
            }
        }

        if (isBreachSwapped && --swapBackTimer <= 0) forceReset(p);

if (pearlRedirecting) {
    MinecraftClient mc = MinecraftClient.getInstance();

  // EXPERIMENTAL: macecore-style rotate-then-fire
 if (pearlExpMode) {
    net.minecraft.util.math.Vec3d eyePos = p.getEyePos();

    if (!pearlExpDropDetected) {
        // ROTATE_NEXT_TICK: find catch position ticks 1-5, exactly macecore
        net.minecraft.util.math.Vec3d catchTarget = null;
        for (int t = 1; t <= 5; t++) {
            net.minecraft.util.math.Vec3d simPos = simulatePearlPos(pearlSpawnPos, pearlInitialVel, t);
            if (eyePos.distanceTo(simPos) <= 3.1) {
                catchTarget = simPos;
                break;
            }
        }

        if (catchTarget == null) {
            // pearl not reachable, abort
            pearlRedirecting = false;
            pearlExpMode = false;
            pearlSimTick = 0;
            pearlSpawnPos = null;
            pearlInitialVel = null;
        } else {
            // rotate toward catch position, exactly macecore rotatePlayerToward
            net.minecraft.util.math.Vec3d biasedTarget;
            if (!p.isSprinting()) {
                biasedTarget = catchTarget.add(0.0, 0.05, 0.0);
            } else {
                biasedTarget = catchTarget.add(0.0, 0.08, 0.0);
            }
            net.minecraft.util.math.Vec3d dir = biasedTarget.subtract(eyePos).normalize();
            float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
            float pitch = (float) Math.toDegrees(-Math.asin(dir.y));

            float correctedPitch;
            if (!p.isSprinting() && p.isGliding()) {
                if (!(p.getPitch() < -75.0F)) {
                    if (p.isSneaking()) {
                        correctedPitch = pitch + 4.0F;
                    } else if (p.getVelocity().y > 0.0F) {
                        correctedPitch = pitch + 4.0F;
                    } else {
                        correctedPitch = pitch + 4.0F;
                    }
                } else {
                    correctedPitch = pitch + 4.0F;
                }
            } else {
                correctedPitch = pitch + 4.0F;
            }

            p.setPitch(correctedPitch);
if (mc.getNetworkHandler() != null) {
    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
        p.getYaw(), correctedPitch, p.isOnGround(), false));
}
pearlExpDropDetected = true;
        }
    } else {
        // FIRE_NEXT_TICK: swap + fire
        pearlRedirecting = false;
        pearlExpMode = false;
        pearlExpDropDetected = false;
        pearlSimTick = 0;
        pearlSpawnPos = null;
        pearlInitialVel = null;
        int slot = findItem(p, "wind_charge");
        boolean offhand = slot == -1 && p.getOffHandStack().isOf(Items.WIND_CHARGE);
        if (slot != -1 || offhand) {
            if (!offhand) {
                inv.setSelectedSlot(slot);
                if (mc.getNetworkHandler() != null)
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            }
            windSlot = offhand ? -1 : slot;
            windReturnSlot = pearlReturnSlot;
            isPearlCatchFire = true;
            fireWindCharge(mc);
        } else {
            if (cfg.pearlCatchReturnAngle) {
                float returnPitch = -(float) cfg.pearlReturnAngle;
                p.setPitch(returnPitch);
                if (mc.getNetworkHandler() != null)
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        p.getYaw(), returnPitch, p.isOnGround(), false));
            }
        }
    }
} else {
        // normal catch: snap to -90, hold yaw, wait delay, then fire wind charge
        p.setPitch(-90f);
        p.setYaw(pearlSavedYaw);
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                pearlSavedYaw, -90f, p.isOnGround(), false));
        }
        if (pearlWindDelay > 0) {
            pearlWindDelay--;
        } else {
            pearlRedirecting = false;
            int slot = findItem(p, "wind_charge");
            boolean offhand = slot == -1 && p.getOffHandStack().isOf(Items.WIND_CHARGE);
            if (slot != -1 || offhand) {
                if (!offhand) {
                    inv.setSelectedSlot(slot);
                    if (mc.getNetworkHandler() != null)
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                }
                windSlot = offhand ? -1 : slot;
                windReturnSlot = pearlReturnSlot;
                isPearlCatchFire = true;
                fireWindCharge(mc);
            } else {
                if (cfg.pearlCatchReturnAngle) {
                    float returnPitch = -(float) cfg.pearlReturnAngle;
                    p.setPitch(returnPitch);
                    if (mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                            pearlSavedYaw, returnPitch, p.isOnGround(), false));
                    }
                }
            }
        }
    }
}

        if (windFireDelay > 0 && --windFireDelay == 0) fireWindCharge(client);
if (windReturnDelay > 0 && --windReturnDelay == 0) {
    if (windReturnSlot != -1) {
        inv.setSelectedSlot(windReturnSlot);
        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(windReturnSlot));
        windReturnSlot = -1;
    }
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
    private static void fireWindCharge(MinecraftClient client) {
    ClientPlayerEntity p = client.player;
    if (p == null) return;
    Hand fireHand = (windSlot == -1) ? Hand.OFF_HAND : Hand.MAIN_HAND;
    client.interactionManager.interactItem(p, fireHand);
    p.swingHand(fireHand);
    float savedYaw = p.getYaw();
    boolean pearlFire = isPearlCatchFire;
    isPearlCatchFire = false;
    windSlot = -1;
    cameraLocked = false;
    windReturnDelay = 1;
    if (pearlFire && ModConfig.get().pearlCatchReturnAngle) {
        float returnPitch = -(float) ModConfig.get().pearlReturnAngle;
        p.setYaw(savedYaw);
        p.setPitch(returnPitch);
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                savedYaw, returnPitch, p.isOnGround(), false));
        }
    }
}


private static void tickWindRightClick(ClientPlayerEntity p, MinecraftClient mc, InventoryAccessor inv, ModConfig cfg) {
    boolean usePressed = mc.options.useKey.isPressed();
    boolean justPressed = usePressed && !wasUsePressed;
    wasUsePressed = usePressed;
    if (justPressed && windActionCooldown <= 0) {
        ItemStack offHandStack = p.getOffHandStack();
       if (!offHandStack.isEmpty() && offHandStack.getItem().getMaxUseTime(offHandStack, p) > 0) {
            return;
        }
        ItemStack held = p.getMainHandStack();
        if (isSword(held) || isAxe(held) || held.isOf(Items.MACE)) {
            int wcSlot = findItem(p, "wind_charge");
            int fwSlot = findItem(p, "firework_rocket");
            boolean hasOffhandWC = p.getOffHandStack().isOf(Items.WIND_CHARGE);

            if (hasOffhandWC) {
                windReturnSlot = -1;
                windSlot = -1;
                windActionCooldown = 2;
                fireWindCharge(mc);
            } else if (wcSlot != -1 && fwSlot != -1 && cfg.rocketBoostEnabled) {
                int cur = inv.getSelectedSlot();
                int chosen = (Math.abs(wcSlot - cur) <= Math.abs(fwSlot - cur)) ? wcSlot : fwSlot;
                windReturnSlot = cur;
                windSlot = chosen;
                inv.setSelectedSlot(chosen);
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(chosen));
                windActionCooldown = 3;
                windFireDelay = 3 + (int)(Math.random() * 2);
            } else if (wcSlot != -1) {
                ItemStack wc = p.getInventory().getStack(wcSlot);
                if (!isValidWindItem(wc)) return;
                windReturnSlot = inv.getSelectedSlot();
                windSlot = wcSlot;
                inv.setSelectedSlot(wcSlot);
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(wcSlot));
                windActionCooldown = 3;
                windFireDelay = 1 + (int)(Math.random() * 2);
            } else if (fwSlot != -1 && cfg.rocketBoostEnabled) {
                ItemStack fw = p.getInventory().getStack(fwSlot);
                if (!isValidWindItem(fw)) return;
                windReturnSlot = inv.getSelectedSlot();
                windSlot = fwSlot;
                inv.setSelectedSlot(fwSlot);
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(fwSlot));
                windActionCooldown = 3;
                windFireDelay = 3 + (int)(Math.random() * 2);
            }
        }
    }
}

    private static void forceReset(ClientPlayerEntity player) {
        if (originalSlot != -1) ((InventoryAccessor) player.getInventory()).setSelectedSlot(originalSlot);
        isBreachSwapped = false;
        originalSlot = -1;
    }

    public static void fullReset(ClientPlayerEntity player) {
        forceReset(player);
        slamPending = false;
        slamTarget = null;
        slamMaceDelay = 0;
        pearlRedirecting = false;
// NEW
pearlExpMode = false;
pearlExpDropDetected = false;
pearlWindDelay = 0;
pearlSimTick = 0;
pearlSpawnPos = null;
pearlInitialVel = null;
pearlSavedYaw = 0f;
        pearlThrown = false;
        slamReturnPending = false;
        jumpDelay = 0;
        slamReturnSlotQueued = -1;
        slamReturnDelay = 0;
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
        isInSlamCombo = false;
        triggerBotHitsThisFall = 0;
        windReturnDelay = 0;
        triggerBotCooldown = 0;
triggerBotArmed = false;
        triggerBotFireDelay = 0;
        wasInAir = false;
        chestplateEquipped = false;
        swordSlotBeforeAxe = -1;
        axeSwingDelay = 0;
        axeSwingHasMace = false;
        AimAssist.reset();
    }

    public static void onSplashPotThrown(ClientPlayerEntity player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.airPotsEnabled) return;
        if (player.isOnGround()) return;
        airPotThrown = true;
        airPotWindDelay = 2;
    }

    private static void queueMaceSlam(Entity target, int maceSlot, int returnSlot, int delayTicks) {
    slamTarget = target;
    slamMaceSlot = maceSlot;
    slamReturnSlotQueued = returnSlot;
    slamPending = true;
    slamMaceDelay = delayTicks;
}

    private static void queueSlotReturn(int returnSlot, int delayTicks) {
        if (returnSlot == -1) return;
        slamFollowUpSlot = returnSlot;
        slamFollowUpTarget = null;
        slamFollowUpDelay = delayTicks;
        slamFollowUpPending = true;
    }

    private static void queueSlamFollowUp(Entity target, int returnSlot, int delayTicks) {
        slamFollowUpTarget = target;
        slamFollowUpSlot = returnSlot;
        slamFollowUpDelay = delayTicks;
        slamFollowUpPending = true;
    }
private static boolean isValidWindItem(ItemStack s) {
    if (!s.contains(net.minecraft.component.DataComponentTypes.CUSTOM_NAME)) return true;
    String name = s.get(net.minecraft.component.DataComponentTypes.CUSTOM_NAME).getString().toLowerCase();
    return name.equals("wind charge") || name.equals("firework rocket");
}

private static boolean isSword(ItemStack s) {
        return Registries.ITEM.getId(s.getItem()).toString().contains("sword");
    }

    private static boolean isAxe(ItemStack s) {
        return Registries.ITEM.getId(s.getItem()).getPath().endsWith("_axe");
    }

    private static boolean isSpear(ItemStack s) {
        return Registries.ITEM.getId(s.getItem()).getPath().endsWith("_spear");
    }

    private static int findSpear(ClientPlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!s.isEmpty() && Registries.ITEM.getId(s.getItem()).getPath().endsWith("_spear")) return i;
        }
        return -1;
    }

    private static int findItem(PlayerEntity p, String name) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!s.isEmpty() && Registries.ITEM.getId(s.getItem()).toString().contains(name)) return i;
        }
        return -1;
    }

    private static int findChestplate(ClientPlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!s.isEmpty() && Registries.ITEM.getId(s.getItem()).toString().contains("chestplate")) return i;
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
                .anyMatch(e -> e.getKey().isPresent() &&
                    e.getKey().get().getValue().getPath().contains(name.toLowerCase()));
    }

public static void onPearlThrown(ClientPlayerEntity player) {
    ModConfig cfg = ModConfig.get();
    if (!cfg.pearlCatchEnabled) return;
    InventoryAccessor inv = (InventoryAccessor) player.getInventory();
    pearlReturnSlot = inv.getSelectedSlot();
    pearlSavedYaw = player.getYaw();
    pearlRedirecting = true;
if (cfg.experimentalPearlCatch && player.getPitch() > -80.0F) {
        pearlExpMode = true;
        pearlExpDropDetected = false;
        pearlSimTick = 0;
        pearlSpawnPos = player.getEyePos();
        net.minecraft.util.math.Vec3d look = player.getRotationVec(1.0F);
        net.minecraft.util.math.Vec3d playerVel = player.getVelocity();
        pearlInitialVel = look.multiply(1.5).add(playerVel);
    } else {
        pearlExpMode = false;
        pearlWindDelay = cfg.pearlCatchDelayTicks;
    }
}


    private static net.minecraft.util.math.Vec3d simulatePearlPos(
            net.minecraft.util.math.Vec3d spawnPos,
            net.minecraft.util.math.Vec3d initialVel,
            int ticksAfterSpawn) {
        net.minecraft.util.math.Vec3d pos = spawnPos;
        net.minecraft.util.math.Vec3d vel = initialVel;
        for (int i = 0; i < ticksAfterSpawn; i++) {
            pos = pos.add(vel);
            vel = vel.add(0, -0.03, 0).multiply(0.99);
        }
        return pos;
    }

    public static void onPearlHitEntity(Entity target) {}
    public static void toggleAimLock(ClientPlayerEntity player, MinecraftClient client) {}
    public static boolean isAimLockActive() { return false; }
}