# BetterMaceSwap

A client-side Fabric mod for Minecraft that automates mace PvP combat sequences — no manual hotkey juggling required. Focus on the fight, not the inventory.

> **7,000+ downloads on CurseForge** · [Download here](https://www.curseforge.com/minecraft/mc-mods/bettermaceswap)

---

## Features

- **Auto attribute swap** — swaps to mace after sword/axe hit, swaps back after configurable delay
- **Smart mace mode** — auto picks Density when falling, Breach when grounded
- **Auto stun slam** — axe-stuns shields, mace slam follow-up, sword chain finish
- **Pearl catch** — fires Wind Charge after ender pearl throw, camera locks up, switches back to sword
- **Lunge swap** — swaps to lunge spear on left click, swaps back automatically
- **Auto chestplate** — equips chestplate when close to target during fall
- **Aim assist** — smooth target tracking while falling or always, configurable speed
- **Trigger bot** — auto attacks when locked target is on crosshair
- All features toggleable via keybinds or Mod Menu config

## Installation

1. Install [Fabric Loader 0.18.4+](https://fabricmc.net/use/installer/)
2. Install Fabric API, Cloth Config, and Mod Menu
3. Download the latest jar from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/bettermaceswap)
4. Drop into `.minecraft/mods` and launch

## Keybinds

| Action | Default |
|---|---|
| Toggle Auto Attribute Swap | B |
| Toggle Stun Slam | V |
| Cycle Mace Mode (Density / Breach) | N |
| Toggle Pearl Catch | M |
| Toggle Aim Assist | G |
| Toggle Trigger Bot | H |
| Toggle Lunge Swap | J |

## Requirements

Minecraft 1.21.10 / 1.21.11 / 26.1+ · Fabric Loader 0.18.4+ · Fabric API · Cloth Config · Mod Menu · Java 21+

## Building

```bash
git clone https://github.com/thatzritesh911/BetterMaceSwap
cd BetterMaceSwap
gradlew build
```

## License

MIT — *Also check out [BetterPvPSprint](https://www.curseforge.com/minecraft/mc-mods/betterpvpsprint) — full sprint control for PvP.*
