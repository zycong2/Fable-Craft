
# Fable Craft

**Fable Craft** (or *RPGCraft*) is a comprehensive RPG plugin for Minecraft. It focuses on deep customization so you can build a full RPG experience with just one plugin.

**Version:** 1.4 &nbsp;|&nbsp; **API:** 1.21.4+ &nbsp;|&nbsp; **License:** MIT  
**Authors:** zycong, Tonnam\_101

---

## Dependencies

| Plugin | Required | Purpose |
|--------|----------|---------|
| **Skript** | ✅ | Skript integration events and expressions |
| **Citizens** | ✅ | NPC system (quests, shops, bank) |
| **Vault** | ✅ | Economy bridge |
| **LuckPerms** | ✅ | Permissions |
| **PlaceholderAPI** | Optional | Chat and sidebar placeholders (`%rpgcraft_defense%`, `%rpgcraft_mana%`, etc.) |

---

## Features at a Glance

- 🗡️ **Custom Items** — Full GUI editor with stats, enchantments, recipes, rarities, and abilities
- 👾 **Custom Mobs** — GUI editor to create mobs with custom health, speed, abilities, and drops
- 📜 **Quest System** — Multi-step quests with auto-start triggers, tied to Citizens NPCs
- 📊 **Custom Stats** — Health, Mana, Defense, Attack Damage, Regeneration, and more
- 🎒 **Loot Tables** — Configurable drops for mobs, compatible with custom items
- ⚔️ **Skills** — Combat, Mining, Farming, Fishing, Foraging with XP progression and level-up rewards
- ✨ **Abilities** — Item-triggered effects (potions, lightning, particles, sounds)
- 🏪 **Shops** — NPC-based buy/sell shops configured via YAML
- 💰 **Money System** — In-game coins with `/money` command
- 🏦 **Banking System** — Deposit/withdraw via bank NPC; configurable death tax on carried coins
- 🏆 **Achievements** — Milestone tracking with coin rewards and notifications
- 👥 **Party System** — Create parties, share coins, transfer leadership
- 📦 **Personal Storage** — Private 54-slot chest per player, auto-saved
- 🌟 **Player Levels** — Overall level that aggregates vanilla XP and skill XP
- 🛡️ **Block Safety** — Auto-restore broken/placed blocks after a configurable delay
- 💬 **Chat Commands** — `/message`, `/broadcast`, `/discord`
- 📋 **Sidebar & Actionbar** — Dynamic scoreboard and actionbar displays
- 🔌 **Skript Support** — Custom events, expressions, and effects
- 🎨 **Color Support** — Hex colors, MiniMessage tags, and `&` color codes
- 🔮 **Custom Enchantments** — Unique enchantments beyond vanilla (Life Steal, Haste, Thorns of Fate, Soul Fire)
- 🔨 **Crafting Stations** — Advanced block-based crafting with unique recipes and requirements
- ⚙️ **Config Menu** — In-game GUI to change plugin settings without touching files

---

## Changelog

### v1.3 → v1.4

- Changed `getNextMessage` to `waitForNextMessage`
  - Old: `getNextMessage(Player).thenAccept(msg -> {})`
  - New: `waitForNextMessage(Player, (Player, Message) -> {})`
- Item editor lore improvements
  - Added 3 new commands: `#ADD`, `#SET`, `#REMOVE`
- Hovering over a player name now shows a prompt to message them
- Fixed `clickEvent` not working in the GUI class
- Fixed `/itemDB` not opening GUI or clickEvent not working
- Added `/message`, `/broadcast`, `/discord` commands
- Added SQLite database for persistent player data
- Numerous bug fixes

---

## Features Overview

---

### 🗡️ Custom Items

Create fully customized items with a GUI editor or via YAML configuration.

**Access:**

- **YAML:** On first start, the plugin creates an `itemDB` folder with a `Default.yml` file. You can create additional YAML files to organize items into categories.
- **GUI Editor:** Run `/rpgcraft itemDB` to open the item browser. Click any item to edit it.

**Item Options:**

| Option | Description |
|--------|-------------|
| `itemType` | Base material (defaults to `DIRT` for new items) |
| `ItemID` | Internal identifier (recommended: match the item name) |
| `name` | Display name (supports hex and `&` color codes) |
| `lore` | Lore lines (supports hex and `&` color codes) |
| `CustomModelData` | Custom model data for resource packs |
| `enchantments` | List of `[enchantment]:[level]` (e.g. `sharpness:5`) |
| `AttackDamage` | Bonus attack damage |
| `Health` | Bonus health |
| `Defense` | Bonus defense |
| `Mana` | Bonus mana |
| `Regeneration` | Health regeneration rate |
| `ManaRegeneration` | Mana regeneration rate |
| `MovementSpeed` | Movement speed bonus |
| `hide` | Hide attributes: `ENCHANTS`, `ATTRIBUTES`, `DYE`, `PLACED_ON`, `DESTROYS`, `ARMOR_TRIM` |
| `rarity` | Item rarity color (configurable in `config.yml`) |
| `recipe` | Custom crafting recipe (shaped or shapeless) |
| `Color` | Leather armor color (leather armor only) |
| `Tile` | Book title (books only) |
| `Author` | Book author (books only) |

**Recipes:**

- **Shaped:** Define a `shape` (3 strings of 3 characters) and `ingredients` (`[letter]:[material]`)
- **Shapeless:** Define `ingredients` as a list of `[material]:[amount]`

Both recipe types support an optional `permission` key to restrict crafting.

---

### 👾 Custom Mobs

Create custom mobs with a GUI editor. Configure health, speed, abilities, custom names, and more.

**Access:** Use the mob editor GUI via `/rpgcraft mobs`.

**Options:**

| Option | Description |
|--------|-------------|
| `type` | Base entity type (e.g. `SPIDER`, `ZOMBIE`) |
| `health` | Custom health value |
| `speed` | Movement speed (vanilla speeds are small, e.g. spider = 0.3; max 1024) |
| `customName` | Display name with placeholders |
| `abilities` | Triggered effects on attack |

Custom mobs are tracked in the `mobDB` folder.

---

### 📜 Quest System

Configure multi-step quests in the `quests` folder (YAML files). Each quest is tied to a Citizens NPC.

**Step Types:**

| Type | Description | Config |
|------|-------------|--------|
| `kill` | Kill entities | `entity`, `value` |
| `get` | Collect items | `item`, `value` |
| `talkToNPC` | Talk to an NPC | `NPCName`, optional `talk`, `removeItems`, `giveItems` |

**Auto-Start Triggers:**

Quests can start automatically without clicking an NPC:

```yaml
quests:
  John:
    quest:
      myQuest:
        name: "&6My Quest"
        autoStart:
          type: firstJoin
          once: true
          delay: 20
          permission: ""
        steps:
          amount: 1
          1:
            type: kill
            entity: ZOMBIE
            value: 10
        reward:
          - money:100
```

**Available Trigger Types:**

| Type | Description | Extra Options |
|------|-------------|---------------|
| `firstJoin` | Player joins for the first time | None |
| `rightClick` | Player right-clicks | `item`, `hand` (`main`/`off`) |
| `leftClick` | Player left-clicks | `item`, `hand` (`main`/`off`) |
| `blockPlace` | Player places a block | `block` (material) |
| `blockBreak` | Player breaks a block | `block` (material) |
| `command` | Player runs a command | `command` (without `/`) |
| `playerDeath` | Player dies | None |
| `worldChange` | Player enters a world | `world` (world name) |
| `entityInteract` | Player right-clicks an entity | `entity` (entity type) |

**Notes:**
- A quest will not auto-start if the player already has it active or completed it.
- The `once` flag (default `true`) ensures the trigger fires only once per player.
- If `min-level` is set, the player must meet the level requirement.
- Multiple quests can share the same trigger type.

---

### 📊 Custom Stats

Players have RPG stats that affect gameplay:

| Stat | Description |
|------|-------------|
| `Health` | Maximum HP (scaled to Minecraft's 20-heart system) |
| `Mana` | Mana pool for abilities |
| `Defense` | Damage reduction: `finalDamage = damage - (defense / (defense + 100)) * damage` |
| `AttackDamage` | Bonus damage on every attack |
| `Regeneration` | Health regeneration rate |
| `ManaRegeneration` | Mana regeneration rate |
| `MovementSpeed` | Movement speed modifier |

Stats are stored via PDC and persist across restarts. Equipped gear automatically adds its stat bonuses, which are refreshed in real time when gear changes.

---

### 🎒 Loot Tables

Configure what mobs drop in the `lootTables` folder. Loot tables support custom items from `itemDB` and can be used as quest rewards.

**Example:**
```yaml
spiderDrops:
  maxItems: 10
  items:
    - item: spiderEye
      chance: 0.5
      minAmount: 1
      maxAmount: 3
```

---

### ⚔️ Skills System

Players gain XP in skills through gameplay. Skills are configured in the `skills` folder.

**Built-in Skills:**

| Skill | How to Gain XP |
|-------|---------------|
| **Combat** | Kill mobs (vanilla and custom) |
| **Mining** | Break ore blocks |
| **Farming** | Harvest fully grown crops |
| **Fishing** | Catch fish |
| **Foraging** | Break trees, grass, etc. |

**Skill Configuration:**
```yaml
combat:
  name: "⚔️ Combat"
  max-level: 100
  progression:
    base-xp: 100
    xp-per-level: 25
    xp-multiplier: 1.0
  xp-gains:
    combat:
      vanilla:
        ZOMBIE: 10
        SKELETON: 15
      custom-mobs:
          spider: 20
  rewards:
    5:
      AttackDamage: 1
    10:
      Health: 5
  display:
    bossbar:
      enabled: true
      title: "&a%skill% &7Lv. %level% &8| &f%xp%/%required% XP"
      color: GREEN
      duration-ticks: 80
    levelup:
      enabled: true
      title: "&6LEVEL UP!"
      subtitle: "&e%skill% &7is now level &a%level%"
      sound:
        enabled: true
        type: ENTITY_PLAYER_LEVELUP
```

**Features:**
- XP progression with configurable base, per-level, and multiplier
- Level-up rewards (stat bonuses)
- Boss bar showing XP progress
- Title screen and sound effects on level-up
- Chat notification on level-up

---

### ✨ Abilities System

Custom items can have abilities that trigger on attack. Abilities are configured in the `abilities` folder.

**Ability Types:**

| Type | Description |
|------|-------------|
| `potion` | Applies a potion effect to the victim |
| `lighting` | Strikes lightning at the victim |
| `particle` | Spawns particles at a location |
| `sound` | Plays a sound at a location |

**Item Ability Configuration:**
```yaml
customSword:
  itemType: DIAMOND_SWORD
  abilities:
    poisonStrike:
      type: potion
      duration: 100
      level: 1
```

**Custom Ability Configuration:**
```yaml
fireball:
  id: fireball
  cooldown: 5
  trigger:
    type: attack
    item: DIAMOND_SWORD
  actions:
    particle:
      type: FLAME
      amount: 20
      location: victim
    sound:
      type: entity.fireball.shoot
      location: victim
```

---

### 🏪 Shop System

Create NPC-based shops configured in the `shops` folder. Players can buy and sell items.

**Shop Configuration:**
```yaml
weaponShop:
  npc-name: "Weapon Merchant"
  title: "&6Weapon Shop"
  items:
    ironSword:
      custom-item: ironSword
      amount: 1
      buy-enabled: true
      buy-price: 100
      sell-enabled: true
      sell-price: 50
    diamondSword:
      material: DIAMOND_SWORD
      amount: 1
      buy-enabled: true
      buy-price: 500
      sell-enabled: false
```

**Usage:** Set an NPC's type to `shop` and match the NPC name in the config.

---

### 💰 Money System

In-game coin system tracked via PDC.

**Commands:**
- `/money` — View your balance
- Money is earned through quest rewards, achievements, and selling items

---

### 🏦 Banking System

Players can deposit and withdraw coins via a bank NPC. Money in the bank is safe on death.

**Setup:** Create a Citizens NPC and set its NPC type to `bank`.

**Death Tax Configuration:**
```yaml
bank:
  deathTax:
    enabled: true
    percent: 10.0      # Percent of carried coins lost on death
    maxLoss: 0         # 0 = no cap; set a number for a maximum loss
```

**Features:**
- Deposit/withdraw all or fixed amounts (100, 1,000)
- Bank balance displayed in GUI
- Configurable death tax on carried coins only
- Bank money is never lost on death

---

### 🏆 Achievements System

Track player milestones and reward them with coins.

**Built-in Achievements:**

| Achievement | Requirement | Reward |
|-------------|-------------|--------|
| First Blood | Kill 1 mob | Coins |
| Monster Slayer | Kill 100 mobs | Coins |
| Miner | Mine 100 blocks | Coins |
| Master Miner | Mine 1,000 blocks | Coins |
| First Craft | Craft 1 item | Coins |
| Master Crafter | Craft 100 items | Coins |
| Fisherman | Catch 10 fish | Coins |
| Master Fisherman | Catch 100 fish | Coins |
| Getting Rich | Earn 1,000 coins | Coins |
| Wealthy | Earn 10,000 coins | Coins |

**Access:** Open the Nether Star menu and click the Achievements button.


### 👥 Party System

Form parties to quest and earn together.

**Features:**
- Create and manage parties (up to 5 members)
- Invite, accept, leave, and kick players
- Transfer leadership
- Share coins among party members
- Party GUI for management

**Access:** Open the Nether Star menu and click the Party button.

---

### 📦 Personal Storage

Each player has a private 54-slot chest that auto-saves.

**Features:**
- 54-slot personal inventory
- Auto-saves on close and on player quit
- Accessible via the Nether Star menu

---

### 🌟 Player Levels

An overall player level that aggregates XP from multiple sources.

**XP Sources:**
- Vanilla Minecraft XP (configurable multiplier)
- Skill XP gains (configurable fraction)
- Skill level-up bonuses

**Configuration:**
```yaml
playerLevel:
  enabled: true
  base-xp: 100
  xp-per-level: 50
  xp-multiplier: 1.2
  max-level: 100
  vanilla-xp-multiplier: 1.0
  skill-xp-multiplier: 0.5
  skill-levelup-bonus: 100
  bossbar:
    enabled: true
    title: "&6⭐ &fPlayer Level %level% &8| &f%xp%/%required% XP"
    color: GREEN
    style: PROGRESS
    duration-ticks: 80
  levelup-message: "&6⭐ &fYou reached Player Level &a%level%&f!"
```

---

### 🛡️ Block Safety

Automatically restore broken or placed blocks after a configurable delay to protect your world.

**Configuration:**
```yaml
breaking:
  enabled: true
  removeTime: 60    # seconds until block is restored
placing:
  enabled: true
  removeTime: 60    # seconds until placed block is removed
```

**Features:**
- Broken blocks are restored after the delay
- Placed blocks are removed after the delay
- Persistent storage survives server restarts
- Does not affect Creative mode players

---

### 💬 Chat Commands

| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/message <player> <msg>` | `/msg`, `/dm`, `/pm` | Send a private message | None |
| `/broadcast <msg>` | `/bc` | Send a centered broadcast to all players | OP |
| `/discord` | `/dc` | Show the server Discord link | None |

---

### 📋 Sidebar & Actionbar

- **Sidebar:** Dynamic scoreboard showing player stats, money, and level
- **Actionbar:** Real-time actionbar updates

---

### 🔌 Skript Support

Integrate Fable Craft with Skript scripts.

**Events:**

| Event | Description | Variables |
|-------|-------------|-----------|
| `on quest start` | Quest started | `event-player`, `event-quest` |
| `on quest complete` | Quest completed | `event-player`, `event-quest` |
| `on quest update` | Quest objective updated | `event-player`, `event-quest`, `event-location` |

**Expressions:**

| Expression | Returns |
|------------|---------|
| `custom item "name"` | `ItemStack` of the custom item |

**Effects:**

| Effect | Description |
|--------|-------------|
| `spawn custom mob "name" at "location"` | Spawns the custom mob |

---

### 🔮 Custom Enchantments

Define unique enchantments beyond vanilla Minecraft. Enchantments are stored in the `enchantments/` database folder (YAML) and apply automatically at enchanting tables.

**Access:** Open the Nether Star menu and click the Custom Enchantments button to browse all available enchantments.

**Enchantment Configuration:**
```yaml
lifeSteal:
  name: "&c&lLife Steal"
  description: "&7Heal yourself when you deal damage"
  maxLevel: 3
  icon: "REDSTONE"
  type: "normal"        # normal or treasure
  compatibleItems: ["DIAMOND_SWORD", "NETHERITE_SWORD", "IRON_SWORD"]
  conflicts: []
  levels:
    1:
      healPercent: 0.05
    2:
      healPercent: 0.10
    3:
      healPercent: 0.15
```

**Built-in Enchantments:**

| Enchantment | Type | Max Level | Compatible Items |
|-------------|------|-----------|------------------|
| Life Steal | Normal | 3 | Swords |
| Haste | Normal | 2 | Swords |
| Thorns of Fate | Normal | 3 | Chestplates |
| Soul Fire | Treasure | 2 | Diamond/Netherite Swords |

**Features:**
- Enchantments stored on items via Persistent Data Container (PDC)
- Automatic application at enchanting tables
- Lore display with Roman numerals (e.g. "§8✦ Life Steal II")
- Treasure enchantments cannot be obtained from enchanting tables
- Configurable compatible items and conflicts
- Browse all enchantments via the GUI browser

---

### 🔨 Crafting Stations

Special block-based crafting stations with unique recipes, ingredient requirements, and optional gates (combat level, coin cost).

**Access:** Right-click a configured station block (e.g. Anvil, Furnace) to open its recipe GUI. Open the Nether Star menu and click Crafting Stations to browse all station types.

**Station Configuration:**
```yaml
anvil:
  name: "Anvil"
  item: "ANVIL"
  title: "&7Anvil Crafting"
  rows: 3
  recipes:
    reinforced_sword:
      result: "DIAMOND_SWORD"
      resultAmount: 1
      resultName: "&7Reinforced Sword"
      resultLore: ["&7A stronger sword forged at the anvil"]
      pattern: [" D ", "DSD", " I "]
      ingredients: ["D:DIAMOND", "S:STICK", "I:IRON_INGOT"]
      requirements:
        combatLevel: 10
        coins: 500
    netherite_upgrade:
      result: "NETHERITE_INGOT"
      resultAmount: 1
      resultName: "&8Netherite Ingot"
      resultLore: ["&7Crafted from ancient debris"]
      ingredients: ["A:ANCIENT_DEBRIS", "G:GOLD_INGOT"]
      requirements:
        coins: 1000
```

**Built-in Stations:**

| Station | Block | Default Recipes |
|---------|-------|-----------------|
| Anvil | ANVIL | Reinforced Sword, Netherite Upgrade |
| Furnace | FURNACE | Smelted Chestplate |

**Features:**
- Shaped recipes with pattern-based crafting
- Shapeless recipes with ingredient lists
- Optional combat level requirements
- Optional coin costs deducted on craft
- Custom result names and lore
- Multiple stations can be configured independently

---

### ⚙️ Config Menu

Change plugin settings in-game through a GUI-based configuration editor. No need to touch YAML files or restart the server.

**Access:** Open the Nether Star menu and click the Config Menu button (requires `rpgcraft.config` permission).

**Categories:**

| Category | Settings |
|----------|----------|
| General | Plugin toggles, message settings |
| Stats | Health, Mana, Defense, Attack Damage defaults |
| Bank | Death tax enabled, percent, max loss |
| Skills | Skill progression settings |
| Display | Sidebar, actionbar, boss bar toggles |
| Items/Mobs | Unbreakable items toggle, mob settings |
| Player Level | Level progression, XP multipliers |

**Features:**
- Toggle boolean options with a single click
- Change numeric values with increment/decrement buttons
- Reload configurations without restarting
- Permission-gated access (`rpgcraft.config`)
- Changes take effect immediately

---

### 🎨 Color Support

Fable Craft supports multiple color formatting methods:

- **`&` codes:** Standard Minecraft color codes (`&6`, `&l`, etc.)
- **Hex colors:** `<color:#FF0000>Red text</color>`
- **MiniMessage tags:** `<gradient:#ff8480:#ff8670>Gradient text</gradient>`
- **Click events:** `<click:open_url:'url'>Click me</click>`

---

### 🎮 Main Menu (Nether Star)

Right-click a Nether Star to open the player menu:

| Button | Function |
|--------|----------|
| **Profile** | View stats and money |
| **Skills** | Open skill progression menu |
| **Achievements** | View achievement progress |
| **Party** | Manage your party |
| **Personal Storage** | Open private chest |
| **Custom Enchantments** | Browse enchantment definitions |
| **Crafting Stations** | Browse crafting stations |
| **Config Menu** | Change settings in-game (permission required) |

---

### 📁 Configuration Files

| File/Folder | Purpose |
|-------------|---------|
| `config.yml` | Main settings (stats, death tax, block safety, player levels, etc.) |
| `messages.yml` | All player-facing messages |
| `data.yml` | Runtime data (custom mobs, pending blocks) |
| `format.yml` | Number and text formatting |
| `itemDB/` | Custom item definitions |
| `mobDB/` | Custom mob definitions |
| `lootTables/` | Mob drop tables |
| `skills/` | Skill definitions and XP gains |
| `abilities/` | Custom ability definitions |
| `quests/` | Quest definitions |
| `shops/` | Shop definitions |
| `enchantments/` | Custom enchantment definitions |
| `craftingStations/` | Advanced crafting station definitions |

---

### 🔧 Commands

| Command | Aliases | Description |
|---------|---------|-------------|
| `/rpgcraft` | `/rc`, `/rCraft`, `/RPGC`, `/rpgc` | Main plugin command |

---

## Installation

1. Install required dependencies: **Skript**, **Citizens**, **Vault**, **LuckPerms**
2. Place `RPGCraft.jar` in your `plugins/` folder
3. Start the server — the plugin will generate all configuration files
4. Configure your custom items, mobs, quests, and shops
5. Create Citizens NPCs and assign them types (`quest`, `shop`, `bank`)

---

## Support

Join our Discord: <https://discord.gg/BUPSAhAy7W>
