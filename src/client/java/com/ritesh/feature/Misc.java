package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.ritesh.feature.AutoFeaturesState.*;

public class Misc {

    public static void tick(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        if (cameraLocked) p.setXRot(-90.0F);
        if (windActionCooldown > 0) windActionCooldown--;
        if (jumpDelay > 0 && --jumpDelay == 0) p.jumpFromGround();

        if (cfg.autoJumpWindCharge && p.onGround() && windActionCooldown <= 0 && p.getXRot() > 70.0F) {
            int wcSlot = Combat.findItem(p, "wind_charge");
            if (wcSlot != -1 && client.options.keyUse.isDown()) {
                ItemStack wc = p.getInventory().getItem(wcSlot);
                if (!isValidWindItem(wc)) return;
                ItemStack held = p.getMainHandItem();
                if (Combat.isSword(held) || StunSlam.isAxe(held) || held.is(Items.MACE)) {
                    int delay = 3 + (int) (Math.random() * 2);
                    windReturnSlot = inv.getSelectedSlot();
                    windSlot = wcSlot;
                    inv.setSelectedSlot(wcSlot);
                    client.getConnection().send(new ServerboundSetCarriedItemPacket(wcSlot));
                    windActionCooldown = 3;
                    windFireDelay = delay;
                    jumpDelay = delay;
                }
            }
        }

        if (cfg.windOnRightClickEnabled) tickWindRightClick(p, client, inv, cfg);

        if (windFireDelay > 0 && --windFireDelay == 0) fireWindCharge(client);

        if (windReturnDelay > 0 && --windReturnDelay == 0) {
            if (windReturnSlot != -1) {
                inv.setSelectedSlot(windReturnSlot);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(windReturnSlot));
                windReturnSlot = -1;
            }
        }

        if (airPotThrown && --airPotWindDelay <= 0) {
            airPotThrown = false;
            fireAirPotWindCharge(client);
        }
    }

    public static void onSplashPotThrown(LocalPlayer player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.airPotsEnabled) return;
        if (player.onGround()) return;
        airPotThrown = true;
        airPotWindDelay = 2;
    }

    private static void tickWindRightClick(LocalPlayer p, Minecraft mc, InventoryAccessor inv, ModConfig cfg) {
        boolean usePressed = mc.options.keyUse.isDown();
        boolean justPressed = usePressed && !wasUsePressed;
        wasUsePressed = usePressed;
        if (justPressed && windActionCooldown <= 0) {
            ItemStack offHandStack = p.getOffhandItem();
            if (!offHandStack.isEmpty() && offHandStack.getItem().getUseDuration(offHandStack, p) > 0) return;
            ItemStack held = p.getMainHandItem();
            if (Combat.isSword(held) || StunSlam.isAxe(held) || held.is(Items.MACE)) {
                int wcSlot = Combat.findItem(p, "wind_charge");
                int fwSlot = Combat.findItem(p, "firework_rocket");
                boolean hasOffhandWC = p.getOffhandItem().is(Items.WIND_CHARGE);

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
                    mc.getConnection().send(new ServerboundSetCarriedItemPacket(chosen));
                    windActionCooldown = 3;
                    windFireDelay = 3 + (int) (Math.random() * 2);
                } else if (wcSlot != -1) {
                    ItemStack wc = p.getInventory().getItem(wcSlot);
                    if (!isValidWindItem(wc)) return;
                    windReturnSlot = inv.getSelectedSlot();
                    windSlot = wcSlot;
                    inv.setSelectedSlot(wcSlot);
                    mc.getConnection().send(new ServerboundSetCarriedItemPacket(wcSlot));
                    windActionCooldown = 3;
                    windFireDelay = 1 + (int) (Math.random() * 2);
                } else if (fwSlot != -1 && cfg.rocketBoostEnabled) {
                    ItemStack fw = p.getInventory().getItem(fwSlot);
                    if (!isValidWindItem(fw)) return;
                    windReturnSlot = inv.getSelectedSlot();
                    windSlot = fwSlot;
                    inv.setSelectedSlot(fwSlot);
                    mc.getConnection().send(new ServerboundSetCarriedItemPacket(fwSlot));
                    windActionCooldown = 3;
                    windFireDelay = 3 + (int) (Math.random() * 2);
                }
            }
        }
    }

    public static void fireWindCharge(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        InteractionHand fireHand = (windSlot == -1) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        client.gameMode.useItem(p, fireHand);
        p.swing(fireHand);
        float savedYaw = p.getYRot();
        boolean pearlFire = isPearlCatchFire;
        isPearlCatchFire = false;
        windSlot = -1;
        cameraLocked = false;
        windReturnDelay = 1;
        if (pearlFire && ModConfig.get().pearlCatchReturnAngle) {
            float returnPitch = -(float) ModConfig.get().pearlReturnAngle;
            p.setYRot(savedYaw);
            p.setXRot(returnPitch);
            if (client.getConnection() != null) {
                client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                        savedYaw, returnPitch, p.onGround(), false));
            }
        }
    }

    private static void fireAirPotWindCharge(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        if (p.getOffhandItem().is(Items.WIND_CHARGE)) {
            client.gameMode.useItem(p, InteractionHand.OFF_HAND);
            p.swing(InteractionHand.OFF_HAND);
            return;
        }
        int slot = Combat.findItem(p, "wind_charge");
        if (slot == -1) return;
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();
        int returnSlot = inv.getSelectedSlot();
        inv.setSelectedSlot(slot);
        client.gameMode.useItem(p, InteractionHand.MAIN_HAND);
        p.swing(InteractionHand.MAIN_HAND);
        client.execute(() -> inv.setSelectedSlot(returnSlot));
    }

    public static boolean isValidWindItem(ItemStack s) {
        if (!s.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) return true;
        String name = s.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME).getString().toLowerCase();
        return name.equals("wind charge") || name.equals("firework rocket");
    }

    public static void reset() {
        windFireDelay = 0;
        windSlot = -1;
        windReturnSlot = -1;
        windActionCooldown = 0;
        windReturnDelay = 0;
        jumpDelay = 0;
        airPotThrown = false;
        airPotWindDelay = 0;
        wasUsePressed = false;
        cameraLocked = false;
    }
}
