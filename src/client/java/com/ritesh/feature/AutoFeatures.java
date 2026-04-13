package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

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

    public static void onHit(ClientPlayerEntity player, Entity target) {
        ModConfig cfg = ModConfig.get();
        if (player == null || target == null) return;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        int startingSlot = inv.getSelectedSlot();

        ItemStack held = player.getMainHandStack();
        if (!isSword(held) && !isAxe(held)) return;

        if (cfg.autoStunSlamEnabled && target instanceof PlayerEntity tp) {
            if (tp.isBlocking()) {
                boolean wantDensity = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY);
                int maceSlot = findBestMace(player, wantDensity);

                if (isSword(held)) {
                    int axeSlot = findItem(player, "_axe");
                    if (axeSlot != -1 && maceSlot != -1) {
                        inv.setSelectedSlot(axeSlot);
                        MinecraftClient.getInstance().interactionManager.attackEntity(player, target);
                        player.swingHand(Hand.MAIN_HAND);
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
            if (target instanceof PlayerEntity && !cfg.attackPlayers) return;

            boolean isAnimal = target instanceof PassiveEntity || target instanceof AmbientEntity;
            if (isAnimal && !cfg.attackAnimals) return;
            if (target instanceof MobEntity && !isAnimal && !cfg.attackMobs) return;

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

    public static void onPearlThrown(ClientPlayerEntity player) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.pearlCatchEnabled) return;

        pearlThrown = true;
        pearlCatchTimer = cfg.pearlCatchDelayTicks;
        pearlYaw = player.getYaw();
        pearlPitch = player.getPitch();
        pearlReturnSlot = ((InventoryAccessor) player.getInventory()).getSelectedSlot();

        if (pearlPitch <= -60f) {
            player.setPitch(-90f);
            cameraLocked = true;
        }
    }

    private static void queueMaceSlam(Entity target, int maceSlot, int returnSlot) {
        slamTarget = target;
        slamMaceSlot = maceSlot;
        slamReturnSlot = returnSlot;
        slamPending = true;
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null) return;

        if (cameraLocked) {
            client.player.setPitch(-90f);
        }

        if (slamPending && slamTarget != null) {
            slamPending = false;
            InventoryAccessor inv = (InventoryAccessor) client.player.getInventory();
            inv.setSelectedSlot(slamMaceSlot);
            client.interactionManager.attackEntity(client.player, slamTarget);
            client.player.swingHand(Hand.MAIN_HAND);
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

    private static void fireWindCharge(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        cameraLocked = false;

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();

        if (player.getOffHandStack().isOf(Items.WIND_CHARGE)) {
            client.interactionManager.interactItem(player, Hand.OFF_HAND);
            player.swingHand(Hand.OFF_HAND);
        } else {
            int windSlot = findItem(player, "wind_charge");
            if (windSlot == -1) return;
            inv.setSelectedSlot(windSlot);
            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
            player.swingHand(Hand.MAIN_HAND);
        }

        int swordSlot = findItem(player, "sword");
        if (swordSlot != -1) {
            inv.setSelectedSlot(swordSlot);
        } else if (pearlReturnSlot != -1) {
            inv.setSelectedSlot(pearlReturnSlot);
        }
    }

    public static void forceReset(ClientPlayerEntity player) {
        if (originalSlot != -1) {
            ((InventoryAccessor) player.getInventory()).setSelectedSlot(originalSlot);
        }
        isBreachSwapped = false;
        originalSlot = -1;
        swapBackTimer = 0;
    }

    private static boolean isSword(ItemStack s) { return s.getItem().toString().contains("sword"); }
    private static boolean isAxe(ItemStack s) { return s.getItem().toString().contains("_axe"); }
    private static int findItem(ClientPlayerEntity p, String name) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).getItem().toString().contains(name)) return i;
        }
        return -1;
    }

    private static int findBestMace(ClientPlayerEntity p, boolean wantDensity) {
        RegistryKey<Enchantment> goal = wantDensity ? Enchantments.DENSITY : Enchantments.BREACH;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(Items.MACE) && hasEnchant(s, goal)) return i;
        }
        return findItem(p, "mace");
    }

    private static boolean hasEnchant(ItemStack s, RegistryKey<Enchantment> key) {
        ItemEnchantmentsComponent e = s.get(DataComponentTypes.ENCHANTMENTS);
        if (e == null) return false;
        for (RegistryEntry<Enchantment> entry : e.getEnchantments()) {
            if (entry.matchesKey(key)) return true;
        }
        return false;
    }
}