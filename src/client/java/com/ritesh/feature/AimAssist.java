package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AimAssist {

    private static Entity lockedAimTarget = null;
    private static int losGraceTicks = 0;
    private static long lastAimTimeMs = 0;
    private static int aimWarmupTicks = 0;
    private static float lastYawGCD = 0f;
    private static float lastPitchGCD = 0f;

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
                    && !lockedAimTarget.isRemoved()
                    && p.distanceToSqr(lockedAimTarget) <= maxDistSq;

            if (valid && !hasLineOfSight(p, client, lockedAimTarget)) {
                if (losGraceTicks > 0) {
                    losGraceTicks--;
                } else {
                    lockedAimTarget = null;
                    lastAimTimeMs = 0;
                }
            } else if (!valid) {
                lockedAimTarget = null;
                losGraceTicks = 0;
                lastAimTimeMs = 0;
            } else {
                losGraceTicks = 3;
            }
        }

        if (lockedAimTarget == null && client.level != null) {
            double bestDist = maxDistSq;
            for (Entity e : client.level.entitiesForRendering()) {
                if (e == p || !e.isAlive() || e.isRemoved()) continue;
                boolean isPlayer = e instanceof Player;
                boolean isMob = e instanceof Mob || e instanceof Animal;
                if (isPlayer && !cfg.aimTargetPlayers) continue;
                if (isMob && !cfg.aimTargetMobs) continue;
                if (!isPlayer && !isMob) continue;

                double dx = e.getX() - p.getX();
                double dz = e.getZ() - p.getZ();
                double yawRad = Math.toRadians(p.getYRot());
                double dot = (-Math.sin(yawRad)) * dx + Math.cos(yawRad) * dz;
                if (dot <= 0) continue;

                if (!hasLineOfSight(p, client, e)) continue;

                double d = p.distanceToSqr(e);
                if (d < bestDist) {
                    bestDist = d;
                    lockedAimTarget = e;
                }
            }
            losGraceTicks = 3;
            lastAimTimeMs = 0;
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
        float deltaSeconds = (lastAimTimeMs == 0) ? (1f / 20f) : (now - lastAimTimeMs) / 1000f;
        deltaSeconds = Math.min(deltaSeconds, 3f / 20f);
        lastAimTimeMs = now;

        double targetY;
        switch (cfg.aimBone) {
            case CHEST -> targetY = lockedAimTarget.getY() + (lockedAimTarget.getBbHeight() * 0.65);
            case LEGS  -> targetY = lockedAimTarget.getY() + (lockedAimTarget.getBbHeight() * 0.2);
            default    -> targetY = lockedAimTarget.getEyeY();
        }

        double dx = lockedAimTarget.getX() - p.getX();
        double dy = targetY - p.getEyeY();
        double dz = lockedAimTarget.getZ() - p.getZ();
        double diffXZ = Math.sqrt(dx * dx + dz * dz);

        float targetYaw   = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, diffXZ));

        float currentYaw   = p.getYRot();
        float currentPitch = p.getXRot();

        float yawDiff   = Mth.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - currentPitch);

        float yawSpeed   = cfg.aimSpeed + (float)(Math.random() * 8 - 4);
        float pitchSpeed = cfg.aimSpeed * 0.75f + (float)(Math.random() * 6 - 3);

        float yawMax   = yawSpeed * deltaSeconds;
        float pitchMax = pitchSpeed * deltaSeconds;

        float smoothing = 0.08f + (float)(Math.random() * 0.06f);

        float rawYawStep   = Mth.clamp(yawDiff * smoothing, -yawMax, yawMax);
        float rawPitchStep = Mth.clamp(pitchDiff * smoothing, -pitchMax, pitchMax);

        if (Math.abs(yawDiff) > 15f) {
            rawPitchStep *= 0.3f;
        }

        float sens = cfg.aimSpeed / 100f;
        float gcdX = (float) Math.pow(sens * 0.6, 2) * 0.15f;
        float gcdY = (float) Math.pow(sens * 0.6, 2) * 0.15f;

        if (gcdX > 0.001f) rawYawStep   = Math.round(rawYawStep   / gcdX) * gcdX;
        if (gcdY > 0.001f) rawPitchStep = Math.round(rawPitchStep / gcdY) * gcdY;

        float yawNoise   = (float)(Math.random() * 0.02 - 0.01);
        float pitchNoise = (float)(Math.random() * 0.015 - 0.0075);

        p.setYRot(currentYaw + rawYawStep + yawNoise);
        p.setXRot(Mth.clamp(currentPitch + rawPitchStep + pitchNoise, -90f, 90f));
    }

    public static Entity getLockedTarget() {
        return lockedAimTarget;
    }

    public static boolean hasTarget() {
        return lockedAimTarget != null;
    }

    public static void reset() {
        lockedAimTarget = null;
        losGraceTicks = 0;
        lastAimTimeMs = 0;
        aimWarmupTicks = 0;
        lastYawGCD = 0f;
        lastPitchGCD = 0f;
    }

    public static boolean hasLineOfSight(LocalPlayer p, Minecraft mc, Entity target) {
        if (mc.level == null) return false;
        Vec3 eyes = p.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        return mc.level.clip(new net.minecraft.world.level.ClipContext(
                eyes, targetPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, p
        )).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }
}
