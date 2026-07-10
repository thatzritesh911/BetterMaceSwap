package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import static com.ritesh.feature.AutoFeaturesState.*;

public class Pearl {

    public static void onPearlThrown(LocalPlayer player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.pearlCatchEnabled) return;

        float pitchBeforeSnap = player.getXRot();

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        pearlReturnSlot = inv.getSelectedSlot();
        pearlSavedYaw = player.getYRot();
        pearlRedirecting = true;

        if (pitchBeforeSnap <= -cfg.pearlCatchAngle) {
            int wcSlot = Combat.findItem(player, "wind_charge");
            boolean wcOffhand = wcSlot == -1 && player.getOffhandItem().is(Items.WIND_CHARGE);
            if (wcSlot == -1 && !wcOffhand) {
                pearlRedirecting = false;
                return;
            }
            pearlExpMode = false;
            pearlWindDelay = (cfg.pearlCatchAngle <= 61) ? Math.max(1, cfg.pearlCatchDelayTicks - 1) : cfg.pearlCatchDelayTicks;
            player.setXRot(-90.0F);
        } else if (cfg.experimentalPearlCatch) {
            pearlExpMode = true;
            pearlExpDropDetected = false;
            pearlSimTick = 0;
            pearlSpawnPos = player.getEyePosition();
            Vec3 look = player.getViewVector(1.0F);
            Vec3 playerVel = player.getDeltaMovement();
            pearlInitialVel = look.multiply(1.5, 1.5, 1.5).add(playerVel);
        } else {
            pearlRedirecting = false;
        }
    }

    public static void tick(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        if (!pearlRedirecting) return;

        if (pearlExpMode) {
            Vec3 eyePos = p.getEyePosition();

            if (!pearlExpDropDetected) {
                int wcSlot = Combat.findItem(p, "wind_charge");
                boolean wcOffhand = wcSlot == -1 && p.getOffhandItem().is(Items.WIND_CHARGE);

                if (wcSlot == -1 && !wcOffhand) {
                    pearlRedirecting = false;
                    pearlExpMode = false;
                    pearlSimTick = 0;
                    pearlSpawnPos = null;
                    pearlInitialVel = null;
                    return;
                }

                Vec3 catchTarget = null;
                for (int t = 1; t <= 5; t++) {
                    Vec3 simPos = simulatePearlPos(pearlSpawnPos, pearlInitialVel, t);
                    if (eyePos.distanceTo(simPos) <= 3.1) {
                        catchTarget = simPos;
                        break;
                    }
                }

                if (catchTarget == null) {
                    pearlRedirecting = false;
                    pearlExpMode = false;
                    pearlSimTick = 0;
                    pearlSpawnPos = null;
                    pearlInitialVel = null;
                } else {
                    Vec3 biasedTarget = p.isSprinting()
                            ? catchTarget.add(0.0, 0.08, 0.0)
                            : catchTarget.add(0.0, 0.05, 0.0);
                    Vec3 dir = biasedTarget.subtract(eyePos).normalize();
                    float pitch = (float) Math.toDegrees(-Math.asin(dir.y));
                    float correctedPitch = pitch + 4.0F;

                    p.setXRot(correctedPitch);
                    if (client.getConnection() != null) {
                        client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                                p.getYRot(), correctedPitch, p.onGround(), false));
                    }
                    pearlExpDropDetected = true;
                }
            } else {
                pearlRedirecting = false;
                pearlExpMode = false;
                pearlExpDropDetected = false;
                pearlSimTick = 0;
                pearlSpawnPos = null;
                pearlInitialVel = null;
                int slot = Combat.findItem(p, "wind_charge");
                boolean offhand = slot == -1 && p.getOffhandItem().is(Items.WIND_CHARGE);
                if (slot != -1 || offhand) {
                    if (!offhand) {
                        inv.setSelectedSlot(slot);
                        if (client.getConnection() != null)
                            client.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
                    }
                    windSlot = offhand ? -1 : slot;
                    windReturnSlot = resolveReturnSlot(p, cfg);
                    isPearlCatchFire = true;
                    Misc.fireWindCharge(client);
                } else {
                    if (cfg.pearlCatchReturnAngle) {
                        float returnPitch = -(float) cfg.pearlReturnAngle;
                        p.setXRot(returnPitch);
                        if (client.getConnection() != null)
                            client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                                    p.getYRot(), returnPitch, p.onGround(), false));
                    }
                }
            }
        } else {
            p.setXRot(-90f);
            p.setYRot(pearlSavedYaw);
            if (client.getConnection() != null) {
                client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                        pearlSavedYaw, -90f, p.onGround(), false));
            }
            if (pearlWindDelay > 0) {
                pearlWindDelay--;
            } else {
                pearlRedirecting = false;
                int slot = Combat.findItem(p, "wind_charge");
                boolean offhand = slot == -1 && p.getOffhandItem().is(Items.WIND_CHARGE);
                if (slot != -1 || offhand) {
                    if (!offhand) {
                        inv.setSelectedSlot(slot);
                        if (client.getConnection() != null)
                            client.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
                    }
                    windSlot = offhand ? -1 : slot;
                    windReturnSlot = resolveReturnSlot(p, cfg);
                    isPearlCatchFire = true;
                    Misc.fireWindCharge(client);
                } else {
                    if (cfg.pearlCatchReturnAngle) {
                        float returnPitch = -(float) cfg.pearlReturnAngle;
                        p.setXRot(returnPitch);
                        if (client.getConnection() != null) {
                            client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                                    pearlSavedYaw, returnPitch, p.onGround(), false));
                        }
                    }
                }
            }
        }
    }

    private static int resolveReturnSlot(LocalPlayer p, ModConfig cfg) {
        return switch (cfg.pearlReturnMode) {
            case PREVIOUS -> (!p.getInventory().getItem(pearlReturnSlot).isEmpty()) ? pearlReturnSlot : -1;
            case SWORD -> Combat.findItem(p, "sword");
            case AXE -> Combat.findItem(p, "axe");
            case ELYTRA -> Combat.findItem(p, "elytra");
        };
    }

    private static Vec3 simulatePearlPos(Vec3 spawnPos, Vec3 initialVel, int ticksAfterSpawn) {
        Vec3 pos = spawnPos;
        Vec3 vel = initialVel;
        for (int i = 0; i < ticksAfterSpawn; i++) {
            pos = pos.add(vel);
            vel = vel.add(0, -0.03, 0).scale(0.99);
        }
        return pos;
    }

    public static void reset() {
        pearlRedirecting = false;
        pearlExpMode = false;
        pearlExpDropDetected = false;
        pearlWindDelay = 0;
        pearlSimTick = 0;
        pearlSpawnPos = null;
        pearlInitialVel = null;
        pearlSavedYaw = 0f;
        pearlThrown = false;
        cameraLocked = false;
        isPearlCatchFire = false;
    }
}
