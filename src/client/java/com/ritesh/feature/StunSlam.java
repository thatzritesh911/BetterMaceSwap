package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import static com.ritesh.feature.AutoFeaturesState.*;

public class StunSlam {

    public static void handleSlamWithMace(Player player, Entity target, int axeSlot, int maceSlot, int returnAfterSlam, ModConfig cfg) {
        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        Minecraft mc = Minecraft.getInstance();
        ItemStack held = player.getMainHandItem();
        isInSlamCombo = true;
        try {
            if (!isAxe(held)) {
                inv.setSelectedSlot(axeSlot);
                mc.gameMode.attack(player, target);
                player.swing(InteractionHand.MAIN_HAND);
            }
        } finally {
            isInSlamCombo = false;
        }
        if (cfg.safeStunSlam) {
            slamReturnSlotQueued = returnAfterSlam;
            queueMaceSlam(target, maceSlot, cfg.stunSlamReturnSlot ? returnAfterSlam : maceSlot, 1);
        } else {
            isInSlamCombo = true;
            inv.setSelectedSlot(maceSlot);
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(maceSlot));
            mc.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
            isInSlamCombo = false;
            if (cfg.stunSlamReturnSlot) queueSlotReturn(returnAfterSlam, SLAM_RETURN_SLOT_DELAY);
        }
    }

    public static void handleSlamNoMace(Player player, Entity target, int axeSlot, int maceSlot, int returnAfterSlam) {
        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        Minecraft mc = Minecraft.getInstance();
        inv.setSelectedSlot(axeSlot);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(axeSlot));
        axeHitDelay = NO_MACE_AXE_HIT_DELAY;
        slamTarget = target;
        slamReturnSlotQueued = returnAfterSlam;
        if (maceSlot != -1) {
            axeHitHasMace = true;
            slamMaceSlot = maceSlot;
        } else {
            axeHitHasMace = false;
        }
    }

    public static void handleSlamDirectAxe(Entity target, int maceSlot, int returnAfterSlam, ModConfig cfg) {
        if (maceSlot != -1) {
            queueMaceSlam(target, maceSlot, cfg.stunSlamReturnSlot ? returnAfterSlam : slamMaceSlot, cfg.safeStunSlam ? 1 : 0);
        } else {
            queueSlotReturn(cfg.stunSlamReturnSlot ? returnAfterSlam : -1, SLAM_RETURN_SLOT_DELAY);
        }
    }

    public static void queueMaceSlam(Entity target, int maceSlot, int returnSlot, int delayTicks) {
        slamTarget = target;
        slamMaceSlot = maceSlot;
        slamReturnSlotQueued = returnSlot;
        slamPending = true;
        slamMaceDelay = delayTicks;
    }

    public static void queueSlotReturn(int returnSlot, int delayTicks) {
        if (returnSlot == -1) return;
        slamFollowUpSlot = returnSlot;
        slamFollowUpTarget = null;
        slamFollowUpDelay = delayTicks;
        slamFollowUpPending = true;
    }

    public static void tick(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();

        if (slamPending && slamTarget != null) {
            if (slamMaceDelay > 0) {
                slamMaceDelay--;
            } else {
                isAttacking = false;
                slamPending = false;
                inv.setSelectedSlot(slamMaceSlot);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(slamMaceSlot));
                if (slamTarget != null) {
                    client.gameMode.attack(p, slamTarget);
                    p.swing(InteractionHand.MAIN_HAND);
                }
                slamReturnDelay = SLAM_RETURN_SLOT_DELAY;
                slamReturnPending = true;
                slamTarget = null;
            }
        }

        if (slamReturnPending && --slamReturnDelay <= 0) {
            slamReturnPending = false;
            if (slamReturnSlotQueued != -1) {
                inv.setSelectedSlot(slamReturnSlotQueued);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(slamReturnSlotQueued));
            }
            slamReturnSlotQueued = -1;
        }

        if (axeHitDelay > 0 && --axeHitDelay == 0) {
            if (slamTarget != null && slamTarget.isAlive()) {
                if (axeHitHasMace) {
                    int captured = slamReturnSlotQueued;
                    queueMaceSlam(slamTarget, slamMaceSlot, cfg.stunSlamReturnSlot ? captured : slamMaceSlot, cfg.safeStunSlam ? 1 : 0);
                } else {
                    if (cfg.stunSlamReturnSlot) queueSlotReturn(slamReturnSlotQueued, 2);
                }
            }
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
            } else {
                inv.setSelectedSlot(slamFollowUpSlot);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(slamFollowUpSlot));
            }
        }
    }

    public static void reset() {
        slamPending = false;
        slamTarget = null;
        slamMaceDelay = 0;
        slamReturnPending = false;
        slamReturnSlotQueued = -1;
        slamReturnDelay = 0;
        axeHitDelay = 0;
        axeHitHasMace = false;
        slamFollowUpPending = false;
        slamFollowUpTarget = null;
        slamFollowUpDelay = 0;
        isAttacking = false;
        isInSlamCombo = false;
        swordSlotBeforeAxe = -1;
    }

    public static boolean isAxe(ItemStack s) {
        return s.getItem().toString().contains("_axe");
    }

    public static int findBestMace(Player p, boolean wantDensity) {
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
        for (Holder<Enchantment> entry : enc.keySet()) {
            if (entry.is(key)) return true;
        }
        return false;
    }
}
