package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.InteractionHand;

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
    private static int pearlFireDelay = 0;
    private static float pearlYaw = 0f;
    private static float pearlPitch = 0f;
    private static int pearlReturnSlot = -1;
    private static boolean cameraLocked = false;

    public static void onHit(LocalPlayer player, Entity target) {
        ModConfig cfg = ModConfig.get();
        if (player == null || target == null) return;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        int startingSlot = inv.getSelectedSlot();

        ItemStack held = player.getMainHandItem();
        if (!isSword(held) && !isAxe(held)) return;

        if (cfg.autoStunSlamEnabled && target instanceof Player tp) {
            if (tp.isBlocking()) {
                boolean wantDensity = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY);
                int maceSlot = findBestMace(player, wantDensity);

                if (isSword(held)) {
                    int axeSlot = findItem(player, "_axe");
                    if (axeSlot != -1 && maceSlot != -1) {
                        inv.setSelectedSlot(axeSlot);
                        Minecraft.getInstance().gameMode.attack(player, target);
                        player.swing(InteractionHand.MAIN_HAND);
                        queueMaceSlam(target, maceSlot, startingSlot);
                        return;
                    }
                } else if (isAxe(held) && maceSlot != -1) {
                    queueMaceSlam(target, maceSlot, startingSlot);
                    return;
                }
            }
        }

        if (cfg.autoBreachSwapEnabled && !isBreachSwapped) {
            if (target instanceof Player) {
                if (!cfg.attackPlayers) return;
            } else if (target instanceof Animal) {
                if (!cfg.attackAnimals) return;
            } else if (target instanceof Mob) {
                if (!cfg.attackMobs) return;
            } else {
                return;
            }

            boolean wantDensity = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY);
            int maceSlot = findBestMace(player, wantDensity);

            if (maceSlot != -1 && maceSlot != startingSlot) {
                originalSlot = startingSlot;
                inv.setSelectedSlot(maceSlot);
                isBreachSwapped = true;
                swapBackTimer = cfg.breachSwapBackTicks;
            }
        }
    }

    public static void onPearlThrown(LocalPlayer player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.pearlCatchEnabled) return;

        pearlThrown = true;
        pearlCatchTimer = cfg.pearlCatchDelayTicks;
        pearlYaw = player.getYRot();
        pearlPitch = player.getXRot();
        pearlReturnSlot = ((InventoryAccessor) player.getInventory()).getSelectedSlot();

        if (pearlPitch <= -60f) {
            player.setXRot(-90f);
            cameraLocked = true;
        }
    }

    private static void queueMaceSlam(Entity target, int maceSlot, int returnSlot) {
        slamTarget = target;
        slamMaceSlot = maceSlot;
        slamReturnSlot = returnSlot;
        slamPending = true;
    }

    public static void tick(Minecraft client) {
        if (client.player == null) return;

        if (cameraLocked) {
            client.player.setXRot(-90f);
        }

        if (slamPending && slamTarget != null) {
            slamPending = false;
            InventoryAccessor inv = (InventoryAccessor) client.player.getInventory();
            inv.setSelectedSlot(slamMaceSlot);
            client.gameMode.attack(client.player, slamTarget);
            client.player.swing(InteractionHand.MAIN_HAND);
            inv.setSelectedSlot(slamReturnSlot);

            slamTarget = null;
            slamMaceSlot = -1;
            slamReturnSlot = -1;
        }

        if (isBreachSwapped) {
            swapBackTimer--;
            if (swapBackTimer <= 0) forceReset(client.player);
        }

        if (pearlThrown) {
            pearlCatchTimer--;
            if (pearlCatchTimer <= 0) {
                pearlThrown = false;
                pearlFireDelay = 2;
            }
        }

        if (pearlFireDelay > 0) {
            pearlFireDelay--;
            if (pearlFireDelay == 0) {
                fireWindCharge(client);
            }
        }
    }

    private static void fireWindCharge(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        cameraLocked = false;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();

        if (player.getOffhandItem().is(Items.WIND_CHARGE)) {
            client.gameMode.useItem(player, InteractionHand.OFF_HAND);
            player.swing(InteractionHand.OFF_HAND);
        } else {
            int windSlot = findItem(player, "wind_charge");
            if (windSlot == -1) return;
            inv.setSelectedSlot(windSlot);
            client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
            player.swing(InteractionHand.MAIN_HAND);
        }

        int swordSlot = findItem(player, "sword");
        if (swordSlot != -1) {
            inv.setSelectedSlot(swordSlot);
        } else if (pearlReturnSlot != -1) {
            inv.setSelectedSlot(pearlReturnSlot);
        }
    }

    public static void forceReset(LocalPlayer player) {
        if (originalSlot != -1) {
            ((InventoryAccessor) player.getInventory()).setSelectedSlot(originalSlot);
        }
        isBreachSwapped = false;
        originalSlot = -1;
        swapBackTimer = 0;
    }

    private static boolean isSword(ItemStack s) { return s.getItem().toString().contains("sword"); }
    private static boolean isAxe(ItemStack s) { return s.getItem().toString().contains("_axe"); }

    private static int findItem(LocalPlayer p, String name) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).getItem().toString().contains(name)) return i;
        }
        return -1;
    }

    private static int findBestMace(LocalPlayer p, boolean wantDensity) {
        ResourceKey<Enchantment> goal = wantDensity ? Enchantments.DENSITY : Enchantments.BREACH;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(Items.MACE) && hasEnchant(s, goal)) return i;
        }
        return findItem(p, "mace");
    }

    private static boolean hasEnchant(ItemStack s, ResourceKey<Enchantment> key) {
        ItemEnchantments e = s.get(DataComponents.ENCHANTMENTS);
        if (e == null) return false;
        for (Holder<Enchantment> entry : e.keySet()) {
            if (entry.is(key)) return true;
        }
        return false;
    }
}