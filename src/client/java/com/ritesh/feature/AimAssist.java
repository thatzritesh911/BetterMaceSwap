package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AimAssist {
    private static Entity lockedAimTarget = null;
    private static int losGraceTicks = 0;
    private static long lastAimTimeMs = 0L;
    private static int aimWarmupTicks = 0;
    private static float lastYawGCD = 0.0F;
    private static float lastPitchGCD = 0.0F;

    public static void tick(Minecraft client, ModConfig cfg) {
        LocalPlayer p = client.player;
        if (p == null || !cfg.aimAssistEnabled) {
            if (!cfg.aimAssistEnabled) reset();
            return;
        }

        double velocityY = p.getDeltaMovement().y;
        boolean inAir = !p.onGround();
        boolean falling = inAir && velocityY < -0.1;

        if (cfg.aimMode != ModConfig.AimMode.ALWAYS && !falling) {
            reset();
            return;
        }

        double maxDistSq = (double) cfg.aimIntensity * cfg.aimIntensity;

        if (lockedAimTarget != null) {
            boolean valid = lockedAimTarget.isAlive()
                    && !lockedAimTarget.isSpectator()
                    && p.distanceToSqr(lockedAimTarget) <= maxDistSq;
            if (valid && !hasLineOfSight(p, client, lockedAimTarget)) {
                if (losGraceTicks > 0) {
                    losGraceTicks--;
                } else {
                    lockedAimTarget = null;
                    lastAimTimeMs = 0L;
                }
            } else if (!valid) {
                lockedAimTarget = null;
                losGraceTicks = 0;
                lastAimTimeMs = 0L;
            } else {
                losGraceTicks = 3;
            }
        }

        if (lockedAimTarget == null && client.level != null) {
            double bestDist = maxDistSq;
            for (Entity e : client.level.entitiesForRendering()) {
                if (e == p || !e.isAlive() || e.isSpectator()) continue;
                boolean isPlayer = e instanceof Player;
                boolean isMob = e instanceof Mob || e instanceof Animal;
                if ((!isPlayer || cfg.aimTargetPlayers) && (!isMob || cfg.aimTargetMobs) && (isPlayer || isMob)) {
                    double dx = e.getX() - p.getX();
                    double dz = e.getZ() - p.getZ();
                    double yawRad = Math.toRadians(p.getYRot());
                    double dot = -Math.sin(yawRad) * dx + Math.cos(yawRad) * dz;
                    if (dot > 0 && hasLineOfSight(p, client, e)) {
                        double d = p.distanceToSqr(e);
                        if (d < bestDist) {
                            bestDist = d;
                            lockedAimTarget = e;
                        }
                    }
                }
            }
            losGraceTicks = 3;
            lastAimTimeMs = 0L;
            if (lockedAimTarget != null) {
                aimWarmupTicks = 1 + (int)(Math.random() * 2);
            }
        }
    }

    public static void applyRotation(LocalPlayer p, ModConfig cfg) {
        if (lockedAimTarget == null || p == null) return;
        if (aimWarmupTicks > 0) { aimWarmupTicks--; return; }
        if (Math.random() < 0.35) return;

        long now = System.currentTimeMillis();
        float deltaSeconds = lastAimTimeMs == 0L ? 0.05F : (float)(now - lastAimTimeMs) / 1000.0F;
        deltaSeconds = Math.min(deltaSeconds, 0.15F);
        lastAimTimeMs = now;

        double targetY = switch (cfg.aimBone) {
            case CHEST -> lockedAimTarget.getY() + lockedAimTarget.getBbHeight() * 0.65;
            case LEGS  -> lockedAimTarget.getY() + lockedAimTarget.getBbHeight() * 0.2;
            default    -> lockedAimTarget.getEyeY();
        };

        double dx = lockedAimTarget.getX() - p.getX();
        double dy = targetY - p.getEyeY();
        double dz = lockedAimTarget.getZ() - p.getZ();
        double diffXZ = Math.sqrt(dx * dx + dz * dz);

        float targetYaw   = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, diffXZ)));
        float currentYaw   = p.getYRot();
        float currentPitch = p.getXRot();

        float yawDiff   = Mth.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - currentPitch);

        float yawSpeed   = cfg.aimSpeed + (float)(Math.random() * 8.0 - 4.0);
        float pitchSpeed = cfg.aimSpeed * 0.75F + (float)(Math.random() * 6.0 - 3.0);
        float yawMax   = yawSpeed * deltaSeconds;
        float pitchMax = pitchSpeed * deltaSeconds;

        float smoothing = 0.08F + (float)(Math.random() * 0.06);
        float rawYawStep   = Mth.clamp(yawDiff * smoothing, -yawMax, yawMax);
        float rawPitchStep = Mth.clamp(pitchDiff * smoothing, -pitchMax, pitchMax);

        if (Math.abs(yawDiff) > 15.0F) rawPitchStep *= 0.3F;

        float sens = cfg.aimSpeed / 100.0F;
        float gcdX = (float) Math.pow(sens * 0.6, 2.0) * 0.15F;
        float gcdY = (float) Math.pow(sens * 0.6, 2.0) * 0.15F;
        if (gcdX > 0.001F) rawYawStep   = Math.round(rawYawStep / gcdX) * gcdX;
        if (gcdY > 0.001F) rawPitchStep = Math.round(rawPitchStep / gcdY) * gcdY;

        float yawNoise   = (float)(Math.random() * 0.02 - 0.01);
        float pitchNoise = (float)(Math.random() * 0.015 - 0.0075);

        p.setYRot(currentYaw + rawYawStep + yawNoise);
        p.setXRot(Mth.clamp(currentPitch + rawPitchStep + pitchNoise, -90.0F, 90.0F));
    }

    public static Entity getLockedTarget() { return lockedAimTarget; }
    public static boolean hasTarget() { return lockedAimTarget != null; }

    public static void reset() {
        lockedAimTarget = null;
        losGraceTicks = 0;
        lastAimTimeMs = 0L;
        aimWarmupTicks = 0;
        lastYawGCD = 0.0F;
        lastPitchGCD = 0.0F;
    }

    public static boolean hasLineOfSight(LocalPlayer p, Minecraft mc, Entity target) {
        if (mc.level == null) return false;
        Vec3 eyes = p.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        return mc.level.clip(new ClipContext(
            eyes, targetPos,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, p
        )).getType() == HitResult.Type.MISS;
    }
}
