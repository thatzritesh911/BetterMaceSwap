# BetterMaceSwap
A client-side Fabric mod for Minecraft 1.21.11 / 26.1+ that automates mace PvP combat sequences — no manual hotkey juggling required.

## Features
- Auto Attribute Swap — swaps to Mace after hitting with Sword or Axe, swaps back after configurable delay
- Smart Switch — auto picks Density when falling, Breach when grounded
- Auto Stun Slam — detects shield blocking, axe stuns then follows up with Mace slam and sword chain. Includes Safe Mode and Return Slot
- Lunge Swap — swaps to Lunge Spear on left click when crosshair is clear, swaps back automatically
- Pearl Catch — fires Wind Charge after throwing Ender Pearl while looking up
- Experimental Pearl Catch — tick-by-tick physics simulation tracks the pearl mid-flight and fires at the right moment
- Pearl Return Angle — snaps pitch to a configurable angle after pearl catch fires
- Right-Click Wind Charge — right clicking with Sword, Axe or Mace fires Wind Charge from inventory
- Aim Assist — smoothly tracks targets while falling or always. Adjustable speed, range, and target bone
- Trigger Bot — auto attacks when locked target appears on crosshair
- Auto Chestplate — equips chestplate from hotbar when closing in on a target during a fall
- Auto Air Pots — automatically splashes potions while airborne
- Rocket Boost — auto fires firework rocket while flying with elytra
- `/bms` Command System — toggle and configure everything from in-game chat
- All features toggleable via keybinds or Mod Menu config screen
- Target filters: Players, Mobs, Animals

## `/bms` Commands
```
/bms status                            shows all features with clickable ON/OFF toggles
/bms menu                              opens config screen
/bms combat attributeswap <on/off>
/bms combat lungeswap <on/off>
/bms combat smartswitch <on/off>
/bms combat airpots <on/off>
/bms stunslam stun_slam <on/off>
/bms stunslam safe_mode <on/off>
/bms stunslam return_slot <on/off>
/bms pearl pearl_catch <on/off>
/bms pearl return_angle <on/off>
/bms aim aim_assist <on/off>
/bms aim triggerbot <on/off>
/bms aim auto_chestplate <on/off>
/bms misc wind <on/off>
/bms misc rocketboost <on/off>
/bms misc experimental_pearl <on/off>
```

## Requirements
- Fabric Loader 0.18.4+
- Fabric API
- Cloth Config
- Mod Menu
- Java 21+
- Minecraft 1.21.11 / 26.1+

## Building
```
gradlew build
```
## Keybinds
Find these in Minecraft Controls under **BetterMaceSwap**:
- **Toggle Auto Attribute Swap** — default: B
- **Toggle Stun Slam** — default: V
- **Cycle Mace Mode (Density / Breach)** — default: N
- **Toggle Pearl Catch** — default: M
- **Toggle Aim Assist** — default: G
- **Toggle Trigger Bot** — default: H
- **Toggle Lunge Swap** — default: J

## License
MIT
