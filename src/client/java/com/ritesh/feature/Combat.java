package com.ritesh.feature;

import com.ritesh.config.ModConfig;
import com.ritesh.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

import static com.ritesh.feature.AutoFeaturesState.*;

public class Combat {

    public static void onAttack(Player player) {
        ModConfig cfg = ModConfig.get();
        Minecraft client = Minecraft.getInstance();
        if (player == null) return;

        if (cfg.lungeSwapEnabled && !lungeSwapPending && player instanceof LocalPlayer) {
            net.minecraft.world.phys.HitResult hit = client.hitResult;
            if (hit instanceof EntityHitResult) return;

            InventoryAccessor inv = (InventoryAccessor) player.getInventory();
            ItemStack held = player.getMainHandItem();
            if (!isSword(held) && !StunSlam.isAxe(held) && !held.is(Items.MACE) && !isSpear(held)) {
                int spearSlot = findSpear((LocalPlayer) player);
                if (spearSlot != -1 && client.getConnection() != null) {
                    int returnSlot = inv.getSelectedSlot();
                    inv.setSelectedSlot(spearSlot);
                    client.getConnection().send(new ServerboundSetCarriedItemPacket(spearSlot));
                    startLungeSwap(returnSlot);
                }
            }
        }
    }

    public static void onBlockHit(LocalPlayer p) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.lungeSwapEnabled || lungeSwapPending) return;
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();
        ItemStack held = p.getMainHandItem();
        if (!isSword(held) && !StunSlam.isAxe(held) && !held.is(Items.MACE) && !isSpear(held)) {
            int spearSlot = findSpear(p);
            Minecraft client = Minecraft.getInstance();
            if (spearSlot != -1 && client.getConnection() != null) {
                int returnSlot = inv.getSelectedSlot();
                inv.setSelectedSlot(spearSlot);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(spearSlot));
                startLungeSwap(returnSlot);
            }
        }
    }

    public static void startLungeSwap(int returnSlot) {
        lungeReturnSlot = returnSlot;
        lungeSwapPending = true;
        lungeSwapDelay = ModConfig.get().lungeSwapDelay;
    }

    public static void tickBreachSwap(LocalPlayer p) {
        if (isBreachSwapped && --swapBackTimer <= 0) forceReset(p);
    }

    public static void forceReset(LocalPlayer player) {
        if (originalSlot != -1) ((InventoryAccessor) player.getInventory()).setSelectedSlot(originalSlot);
        isBreachSwapped = false;
        originalSlot = -1;
    }

    public static void tickLungeSwap(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        InventoryAccessor inv = (InventoryAccessor) p.getInventory();
        if (lungeSwapPending) {
            if (lungeSwapDelay > 0) {
                lungeSwapDelay--;
            } else {
                lungeSwapPending = false;
                if (lungeReturnSlot != -1) {
                    inv.setSelectedSlot(lungeReturnSlot);
                    client.getConnection().send(new ServerboundSetCarriedItemPacket(lungeReturnSlot));
                    lungeReturnSlot = -1;
                }
            }
        }
    }

    public static void tickTriggerBot(Minecraft client) {
        LocalPlayer p = client.player;
        if (p == null) return;
        ModConfig cfg = ModConfig.get();
        if (!cfg.aimAssistEnabled) return;

        if (cfg.triggerBotEnabled && cfg.autoChestplateOnTrigger && !chestplateEquipped) {
            Entity aimTarget = AimAssist.getLockedTarget();
            if (aimTarget != null) {
                double dist = p.distanceTo(aimTarget);
                boolean fallingCheck = !cfg.triggerBotFallingOnly ||
                        (!p.onGround() && p.getDeltaMovement().y < -0.1);
                if (fallingCheck && dist <= 8.0) {
                    chestplateEquipped = true;
                }
            }
        }

        if (cfg.triggerBotEnabled
                && triggerBotHitsThisFall < 2
                && triggerBotCooldown == 0
                && !triggerBotArmed) {
            boolean fallingCheck = !cfg.triggerBotFallingOnly ||
                    (!p.onGround() && p.getDeltaMovement().y < -0.1);
            if (fallingCheck) {
                if (client.hitResult instanceof EntityHitResult ehr) {
                    Entity t = ehr.getEntity();
                    double dist = p.distanceTo(t);
                    boolean inRange = dist <= cfg.aimIntensity;
                    boolean valid = (t instanceof Player && cfg.combatTargetPlayers)
                            || ((t instanceof Mob || t instanceof Animal) && cfg.combatTargetMobs);
                    if (valid && inRange) {
                        triggerBotArmed = true;
                        triggerBotTarget = t;
                        triggerBotFireDelay = 1 + (int) (Math.random() * 3);
                    }
                }
            }
        }

        if (triggerBotArmed && --triggerBotFireDelay <= 0) {
            triggerBotArmed = false;
            if (triggerBotTarget != null && triggerBotTarget.isAlive()) {
                client.gameMode.attack(p, triggerBotTarget);
                p.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                triggerBotHitsThisFall++;
                triggerBotCooldown = 3 + (int) (Math.random() * 2);
                triggerBotTarget = null;
            }
        }
    }

    public static void applyBreachSwap(Player player, int startingSlot, ItemStack held, ModConfig cfg) {
        if (!cfg.autoBreachSwapEnabled || isBreachSwapped) return;
        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        boolean wantDensity;
        if (cfg.smartSwitchEnabled) {
            wantDensity = !player.onGround() && player.getDeltaMovement().y < -0.3;
        } else {
            wantDensity = (cfg.maceMode == ModConfig.MaceEnchantMode.DENSITY);
        }
        int maceSlot = StunSlam.findBestMace(player, wantDensity);
        if (maceSlot != -1 && maceSlot != startingSlot) {
            originalSlot = startingSlot;
            inv.setSelectedSlot(maceSlot);
            isBreachSwapped = true;
            swapBackTimer = cfg.breachSwapBackTicks + (Math.random() < 0.5 ? 1 : 0);
        }
    }

    public static void reset() {
        lungeSwapPending = false;
        lungeReturnSlot = -1;
        lungeSwapDelay = 0;
        triggerBotHitsThisFall = 0;
        triggerBotCooldown = 0;
        triggerBotArmed = false;
        triggerBotFireDelay = 0;
        wasInAir = false;
        chestplateEquipped = false;
        wasAttackPressed = false;
    }

    public static boolean isSword(ItemStack s) {
        return s.getItem().toString().contains("sword");
    }

    public static boolean isSpear(ItemStack s) {
        return s.getItem().toString().contains("spear");
    }

    public static int findSpear(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem().toString().contains("spear")) return i;
        }
        return -1;
    }

    public static int findItem(Player p, String name) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem().toString().contains(name)) return i;
        }
        return -1;
    }

    public static int findChestplate(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem().toString().contains("chestplate")) return i;
        }
        return -1;
    }
}
