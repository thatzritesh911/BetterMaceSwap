package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@Environment(EnvType.CLIENT)
public class AimAssist {
    private static Entity lockedAimTarget = null;
    private static int losGraceTicks = 0;
    private static long lastAimTimeMs = 0L;
    private static int aimWarmupTicks = 0;
    private static float lastYawGCD = 0.0F;
    private static float lastPitchGCD = 0.0F;

    public static void tick(MinecraftClient client, ModConfig cfg) {
        ClientPlayerEntity p = client.player;
        if (p == null || !cfg.aimAssistEnabled) {
            if (!cfg.aimAssistEnabled) reset();
            return;
        }

        double velocityY = p.getVelocity().y;
        boolean inAir = !p.isOnGround();
        boolean falling = inAir && velocityY < -0.1;

        if (cfg.aimMode != ModConfig.AimMode.ALWAYS && !falling) {
            reset();
            return;
        }

        double maxDistSq = (double) cfg.aimIntensity * cfg.aimIntensity;

        if (lockedAimTarget != null) {
            boolean valid = lockedAimTarget.isAlive()
                    && !lockedAimTarget.isSpectator()
                    && p.squaredDistanceTo(lockedAimTarget) <= maxDistSq;
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

        if (lockedAimTarget == null && client.world != null) {
            double bestDist = maxDistSq;
            for (Entity e : client.world.getEntities()) {
                if (e == p || !e.isAlive() || e.isSpectator()) continue;
                boolean isPlayer = e instanceof PlayerEntity;
                boolean isMob = e instanceof MobEntity || e instanceof AnimalEntity;
                if ((!isPlayer || cfg.aimTargetPlayers) && (!isMob || cfg.aimTargetMobs) && (isPlayer || isMob)) {
                    double dx = e.getX() - p.getX();
                    double dz = e.getZ() - p.getZ();
                    double yawRad = Math.toRadians(p.getYaw());
                    double dot = -Math.sin(yawRad) * dx + Math.cos(yawRad) * dz;
                    if (dot > 0 && hasLineOfSight(p, client, e)) {
                        double d = p.squaredDistanceTo(e);
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

    public static void applyRotation(ClientPlayerEntity p, ModConfig cfg) {
        if (lockedAimTarget == null || p == null) return;
        if (aimWarmupTicks > 0) { aimWarmupTicks--; return; }
        if (Math.random() < 0.35) return;

        long now = System.currentTimeMillis();
        float deltaSeconds = lastAimTimeMs == 0L ? 0.05F : (float)(now - lastAimTimeMs) / 1000.0F;
        deltaSeconds = Math.min(deltaSeconds, 0.15F);
        lastAimTimeMs = now;

        double targetY = switch (cfg.aimBone) {
            case CHEST -> lockedAimTarget.getY() + lockedAimTarget.getHeight() * 0.65;
            case LEGS  -> lockedAimTarget.getY() + lockedAimTarget.getHeight() * 0.2;
            default    -> lockedAimTarget.getEyeY();
        };

        double dx = lockedAimTarget.getX() - p.getX();
        double dy = targetY - p.getEyeY();
        double dz = lockedAimTarget.getZ() - p.getZ();
        double diffXZ = Math.sqrt(dx * dx + dz * dz);

        float targetYaw   = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, diffXZ)));
        float currentYaw   = p.getYaw();
        float currentPitch = p.getPitch();

        float yawDiff   = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = MathHelper.wrapDegrees(targetPitch - currentPitch);

        float yawSpeed   = cfg.aimSpeed + (float)(Math.random() * 8.0 - 4.0);
        float pitchSpeed = cfg.aimSpeed * 0.75F + (float)(Math.random() * 6.0 - 3.0);
        float yawMax   = yawSpeed * deltaSeconds;
        float pitchMax = pitchSpeed * deltaSeconds;

        float smoothing = 0.08F + (float)(Math.random() * 0.06);
        float rawYawStep   = MathHelper.clamp(yawDiff * smoothing, -yawMax, yawMax);
        float rawPitchStep = MathHelper.clamp(pitchDiff * smoothing, -pitchMax, pitchMax);

        if (Math.abs(yawDiff) > 15.0F) rawPitchStep *= 0.3F;

        float sens = cfg.aimSpeed / 100.0F;
        float gcdX = (float) Math.pow(sens * 0.6, 2.0) * 0.15F;
        float gcdY = (float) Math.pow(sens * 0.6, 2.0) * 0.15F;
        if (gcdX > 0.001F) rawYawStep   = Math.round(rawYawStep / gcdX) * gcdX;
        if (gcdY > 0.001F) rawPitchStep = Math.round(rawPitchStep / gcdY) * gcdY;

        float yawNoise   = (float)(Math.random() * 0.02 - 0.01);
        float pitchNoise = (float)(Math.random() * 0.015 - 0.0075);

        p.setYaw(currentYaw + rawYawStep + yawNoise);
        p.setPitch(MathHelper.clamp(currentPitch + rawPitchStep + pitchNoise, -90.0F, 90.0F));
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

    public static boolean hasLineOfSight(ClientPlayerEntity p, MinecraftClient mc, Entity target) {
        if (mc.world == null) return false;
        Vec3d eyes = p.getEyePos();
        Vec3d targetPos = target.getEyePos();
        return mc.world.raycast(new RaycastContext(
            eyes, targetPos,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, p
        )).getType() == HitResult.Type.MISS;
    }
}
