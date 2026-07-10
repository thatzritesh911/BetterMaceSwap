package com.ritesh.feature;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class AutoFeaturesState {
    // Breach swap
    public static int originalSlot = -1;
    public static int swapBackTimer = 0;
    public static boolean isBreachSwapped = false;

    // Stun slam
    public static Entity slamTarget = null;
    public static int slamMaceSlot = -1;
    public static boolean slamPending = false;
    public static int slamMaceDelay = 0;
    public static boolean isAttacking = false;
    public static boolean isInSlamCombo = false;
    public static boolean slamReturnPending = false;
    public static int slamReturnSlotQueued = -1;
    public static int slamReturnDelay = 0;
    public static int axeHitDelay = 0;
    public static boolean axeHitHasMace = false;
    public static int maceHitDelay = 0;
    public static int swordSlotBeforeAxe = -1;
    public static boolean slamFollowUpPending = false;
    public static Entity slamFollowUpTarget = null;
    public static int slamFollowUpSlot = -1;
    public static int slamFollowUpDelay = 0;

    public static final int MACE_HIT_DELAY = 1;
    public static final int NO_MACE_AXE_HIT_DELAY = 1;
    public static final int SLAM_RETURN_SLOT_DELAY = 5;

    // Pearl
    public static boolean pearlThrown = false;
    public static float pearlSavedYaw = 0f;
    public static int pearlWindDelay = 0;
    public static boolean pearlRedirecting = false;
    public static boolean pearlExpMode = false;
    public static boolean pearlExpDropDetected = false;
    public static Vec3 pearlSpawnPos = null;
    public static Vec3 pearlInitialVel = null;
    public static int pearlSimTick = 0;
    public static int pearlCatchTimer = 0;
    public static int pearlReturnSlot = -1;
    public static boolean isPearlCatchFire = false;
    public static boolean cameraLocked = false;

    // Wind / Misc
    public static int windFireDelay = 0;
    public static int windSlot = -1;
    public static int windReturnSlot = -1;
    public static int windActionCooldown = 0;
    public static int windReturnDelay = 0;
    public static boolean wasUsePressed = false;
    public static boolean wasAttackPressed = false;
    public static int jumpDelay = 0;
    public static boolean airPotThrown = false;
    public static int airPotWindDelay = 0;

    // Lunge swap
    public static boolean lungeSwapPending = false;
    public static int lungeReturnSlot = -1;
    public static int lungeSwapDelay = 0;

    // Trigger bot / combat
    public static Entity triggerBotTarget = null;
    public static int triggerBotHitsThisFall = 0;
    public static int triggerBotCooldown = 0;
    public static boolean triggerBotArmed = false;
    public static int triggerBotFireDelay = 0;
    public static boolean wasInAir = false;
    public static boolean chestplateEquipped = false;

    // Misc
    public static int lastSelectedSlot = -1;
}
