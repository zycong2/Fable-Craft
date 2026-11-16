
# Fable Craft

**Fable Craft** (or *RPGCraft*) is a comprehensive RPG plugin for Minecraft. It focuses on deep customization so you can build a full RPG experience with just one plugin.

**Version:** 1.4 &nbsp;|&nbsp; **API:** Paper 26.2 (Java 25) &nbsp;|&nbsp; **License:** MIT  
**Authors:** zycong, Tonnam\_101

---

## Dependencies

| Plugin | Required | Purpose |
|--------|----------|---------|
| **Skript** | Optional | Skript integration events, expressions, and effects |
| **Citizens** | ✅ | NPC system (quests, shops, bank) |
| **Vault** | ✅ | Economy bridge |
| **LuckPerms** | ✅ | Permissions |
| **PlaceholderAPI** | Optional | Chat and sidebar placeholders (`%rpgcraft_defense%`, `%rpgcraft_mana%`, etc.) |

---

## Features at a Glance

- 🗡️ **Custom Items** — Full GUI editor with stats, enchantments, recipes, rarities, and abilities
- 👾 **Custom Mobs** — GUI editor to create mobs with custom health, speed, boss bars, spawn points, and skills
- 🛡️ **Mob Skills** — Assignable abilities for mobs and NPCs (fire attacks, regeneration, buffs)
- 📜 **Quest System** — Multi-step quests with auto-start triggers, tied to Citizens NPCs
- 📊 **Custom Stats** — Health, Mana, Defense, Attack Damage, Regeneration, and more
- 🎒 **Loot Tables** — Configurable drops for mobs, compatible with custom items
- ⚔️ **Skills** — Combat, Mining, Farming, Fishing, Foraging with XP progression and level-up rewards
- ✨ **Abilities** — Item-triggered effects (potions, lightning, particles, sounds)
- 🏪 **Shops** — NPC-based buy/sell shops configured via YAML
- 💰 **Money System** — In-game coins with `/money` command
- 🏦 **Banking System** — Deposit/withdraw via bank NPC; configurable death tax on carried coins
- 🏆 **Achievements** — Vanilla-integrated advancement system with configurable trees, coin rewards, and toast notifications
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
- ✨ **Magic System** — Configurable spells castable from items with mana costs, cooldowns, and effects (damage, particles, fire, glow, attributes, summoning, potions, knockback)
- ⚙️ **Config Menu** — In-game GUI to change plugin settings via `/rpgcraft editor config` (includes advancement settings)
- 🔧 **Auto Config Defaults** — Missing config options are automatically filled on startup without overwriting user values

---

## Changelog

### v1.3 → v1.4

- **Upgraded to Paper 26.2** — Requires Java 25; all APIs updated to match
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
- **Vanilla Advancement Integration** — Achievements now appear in the vanilla Minecraft advancement menu (L-key) with toast notifications
- **Configurable Advancement Trees** — Each tree in `advancements.yml` creates a separate tab in the advancement menu, fully configurable via YAML or in-game GUI
- **GUI Item Paper 26.2 Features** — Added support for Use Cooldown, Swing Animation, Piercing Weapon, and other new data components
- **Config Menu — Advancement Settings** — In-game GUI toggles for Remove Vanilla Advancements and Achievements GUI Background
- **Auto Config Defaults** — Missing config options are automatically filled on startup without overwriting user-set values via `copyMissingDefaults()`
- **Advancements Config Separated** — Moved advancement trees from `config.yml` to a dedicated `advancements.yml` for cleaner organization
- **Player Menu Update** — Removed achievements button (replaced by vanilla menu); moved Quests button to its place
- **Achievement Trigger Events** — Each advancement can now define configurable trigger events (kill, mine, craft, fish, levelUp, death, join, money) with custom counts; managed via in-game editor or `advancements.yml`
- **Config System Fix** — Fixed bug where default config files for DB folders (itemDB, mobDB, etc.) were never written to disk
- Numerous bug fixes

---

## Features Overview

---

### 🗡️ Custom Items

Create fully customized items with a GUI editor or via YAML configuration.

**Access:**

- **YAML:** On first start, the plugin creates an `itemDB` folder with a `Default.yml` file. You can create additional YAML files to organize items into categories.
- **GUI Editor:** Run `/rpgcraft editor itemdb` to open the item browser. Click any item to edit it.

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

Create custom mobs with a GUI editor or via YAML configuration. Each mob is defined by a unique ID in the `mobDB/` folder.

**Access:**

- **YAML:** On first start, the plugin creates a `mobDB` folder with a `Default.yml` file containing a fully-documented example. You can create additional YAML files to organize mobs.
- **GUI Editor:** Run `/mobs editor` to open the mob browser. Click any mob to edit it.

**Options:**

| Option | Description |
|--------|-------------|
| `type` | Base entity type — any spawnable `LivingEntity` (e.g. `ZOMBIE`, `SPIDER`, `CREEPER`) |
| `health` | Max HP (1–1024); defaults to vanilla |
| `damage` | Attack damage (0–2048) |
| `speed` | Movement speed (0–1024; vanilla zombie ≈ 0.23) |
| `glowing` | Entity glows visibly (`true`/`false`) |
| `invulnerable` | Immune to all damage (`true`/`false`) |
| `customName.name` | Display name above the entity (supports MiniMessage, hex colors, `&` codes, and placeholders) |
| `customName.visible` | Show the custom name (`true`/`false`; default `false`) |
| `bossBar.color` | Boss bar color: `RED`, `GOLD`, `YELLOW`, `GREEN`, `BLUE`, `PURPLE`, `WHITE` |
| `bossBar.barStyle` | Boss bar style: `SOLID`, `SEGMENTED_6`, `SEGMENTED_10`, `SEGMENTED_12`, `SEGMENTED_20` |
| `lootTable` | Reference a loot table from the `lootTables/` folder |
| `skills` | List of mob skill IDs from the `mobSkills/` folder (e.g. `fireAttack`, `berserker`) |
| `randomSpawns.frequency` | Chance per natural spawn attempt to replace a vanilla mob (0.0–1.0) |
| `randomSpawns.options.spawnOn` | Only spawn on these block types (e.g. `SOUL_SAND`, `SOIL`) — optional |
| `randomSpawns.options.biomes` | Only spawn in these biomes (e.g. `minecraft:swamp`) — optional |
| `spawnPoints.respawnSeconds` | Delay after death before the spawn point respawns (default 60) |
| `spawnPoints.maxAlive` | Max mobs from this point alive at once (default 1) |
| `spawnPoints.activationRange` | Player must be within this range (blocks) for spawning (default 48) |
| `spawnPoints.locations` | List of fixed spawn locations in format `"world, x, y, z"` or `"world, x, y, z, yaw"` |

**Commands:**

| Command | Description |
|---------|-------------|
| `/mobs spawn <mobId>` | Spawn the mob at your location |
| `/mobs editor` | Open the GUI editor |
| `/mobs addspawn <mobId>` | Add a spawn point at your location |
| `/mobs removespawn <mobId> <number>` | Remove a spawn point |
| `/mobs listspawns <mobId>` | List all spawn points |
| `/mobs killall` | Kill all custom mobs |
| `/mobs reload` | Reload spawns and spawn points |

**Example YAML:**

```yaml
zombie_king:
  type: ZOMBIE
  health: 200
  damage: 15
  speed: 0.25
  glowing: true
  invulnerable: false
  customName:
    name: "<gradient:#ff0000:#ff8800>Zombie King</gradient>"
    visible: true
  bossBar:
    color: RED
    barStyle: SOLID
  lootTable: zombieKingDrops
  skills:
    - fireAttack
    - berserker
    - glowingAura
  randomSpawns:
    frequency: 0.05
    options:
      spawnOn:
        - SOUL_SAND
      biomes:
        - minecraft:swamp
  spawnPoints:
    respawnSeconds: 60
    maxAlive: 1
    activationRange: 48
    locations:
      - "world, 100.5, 64, -200.5"
      - "world, 150.5, 70, -180.5, 90"
```

---

### ⚔️ Mob Skills

Mob Skills let you give mobs and Citizens NPCs special abilities. Skills are defined in the `mobSkills/` folder and assigned via YAML or commands.

**Access:** Skills are defined in `mobSkills/Default.yml`. Create additional YAML files to organize skills.

**Trigger Types:**

| Trigger | Description |
|---------|-------------|
| `onSpawn` | Applied once when the entity spawns |
| `onAttack` | Applied each time the entity attacks (respects cooldown) |
| `periodic` | Runs on a timer interval (respects cooldown) |

**Available Effects:**

| Effect | Description |
|--------|-------------|
| `attackDamageBonus` | Adds to the entity's attack damage attribute |
| `movementSpeedBonus` | Adds to the entity's movement speed attribute |
| `healthBonus` | Adds to the entity's max health attribute |
| `setOnFire` | Sets the entity on fire (`true`/`false`) |
| `fireTicks` | How long the fire lasts in ticks (20 ticks = 1 second) |
| `healAmount` | Heals the entity by this amount (in hearts) |
| `knockback` | Knockback force applied on attack |
| `glowing` | Makes the entity glow visibly (`true`/`false`) |

**Assigning Skills:**

- **Mobs (YAML):** Add a `skills:` list under the mob entry in `mobDB/`.
- **Citizens NPCs:** Use `/npc skill add <skillName>` while looking at the NPC.

**Commands:**

| Command | Description |
|---------|-------------|
| `/npc skill add <skillName>` | Add a skill to the nearest NPC |
| `/npc skill remove <skillName>` | Remove a skill from the nearest NPC |
| `/npc skill remove` | Remove all skills from the nearest NPC |

**Example Skill Definition:**

```yaml
fireAttack:
  name: "Fire Attack"
  description: "Deals extra fire damage and sets targets on fire"
  effects:
    attackDamageBonus: 5.0
    setOnFire: true
    fireTicks: 100
  cooldown: 40
  trigger: "onAttack"

healOverTime:
  name: "Regeneration"
  description: "Heals 1 heart every 10 seconds"
  effects:
    healAmount: 2.0
  cooldown: 100
  trigger: "periodic"
  interval: 200

berserker:
  name: "Berserker"
  description: "High damage and speed"
  effects:
    attackDamageBonus: 10.0
    movementSpeedBonus: 0.2
  cooldown: 0
  trigger: "onSpawn"
```

**Built-in Skills:**

| Skill ID | Trigger | Effects |
|----------|---------|---------|
| `fireAttack` | onAttack | +5 damage, sets target on fire |
| `speedBoost` | onSpawn | +0.3 movement speed |
| `healOverTime` | periodic | Heals 2 HP every 10 seconds |
| `tank` | onSpawn | +100 max health |
| `berserker` | onSpawn | +10 damage, +0.2 speed |
| `glowingAura` | onSpawn | Entity glows |

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

Achievements are fully integrated into the **vanilla Minecraft advancement menu** (press `L` to open). Players see toast notifications, progress tracking, and organized advancement trees — all through the native Minecraft UI.

**How It Works:**

Achievements are defined in a separate `advancements.yml` file. Each tree creates a **separate tab** in the vanilla advancement menu. The root advancement defines the tab, and children form the tree structure within that tab.

**Configuration Options (in `advancements.yml`):**

| Option | Description |
|--------|-------------|
| `removeVanilla` | When `true`, revokes all vanilla Minecraft advancements on join (default: `false`) |
| `guiBackground` | Background material for the custom achievements GUI (default: `GRAY_STAINED_GLASS_PANE`) |

**Advancement Tree Structure:**

Each tree has a `root` advancement (creates the tab) and optional `children` that reference their parent by ID:

```yaml
# advancements.yml
removeVanilla: false
guiBackground: GRAY_STAINED_GLASS_PANE
trees:
    # Each key is a separate tab in the advancement menu
    combat:
      # Root advancement — creates the tab
      root:
        id: first_kill              # Unique identifier
        name: "&c&lFirst Blood"     # Display name (supports color codes)
        description: "Kill your first mob."
        icon: REDSTONE              # Icon item
        background: REDSTONE_BLOCK  # Tab background texture (root only)
        rewardCoins: 10             # Coin reward on unlock
        frame: task                 # Frame type: task, goal, challenge
      # Child advancements — form the tree within the tab
      children:
        kill_10:
          id: kill_10
          name: "&c&lWarrior"
          description: "Kill 10 mobs."
          icon: IRON_SWORD
          parent: first_kill        # References parent by ID
          rewardCoins: 25
          frame: task
        kill_100:
          id: kill_100
          name: "&c&lSlayer"
          description: "Kill 100 mobs."
          icon: DIAMOND_SWORD
          parent: kill_10           # Chains from kill_10
          rewardCoins: 100
          frame: challenge
    mining:
      root:
        id: mine_10
        name: "&7&lMiner"
        description: "Mine 10 blocks."
        icon: IRON_PICKAXE
        background: IRON_BLOCK
        rewardCoins: 15
        frame: task
      children:
        mine_100:
          id: mine_100
          name: "&7&lQuarry Master"
          description: "Mine 100 blocks."
          icon: DIAMOND_PICKAXE
          parent: mine_10
          rewardCoins: 50
          frame: challenge
    crafting:
      root:
        id: craft_10
        name: "&e&lCraftsman"
        description: "Craft 10 items."
        icon: CRAFTING_TABLE
        background: CRAFTING_TABLE
        rewardCoins: 20
        frame: goal
    fishing:
      root:
        id: fish_1
        name: "&b&lFisherman"
        description: "Catch your first fish."
        icon: FISHING_ROD
        background: DEEPSLATE_COPPER_BLOCK
        rewardCoins: 10
        frame: task
      children:
        fish_50:
          id: fish_50
          name: "&b&lMaster Angler"
          description: "Catch 50 fish."
          icon: COD
          parent: fish_1
          rewardCoins: 75
          frame: challenge
    wealth:
      root:
        id: rich
        name: "&6&lRich"
        description: "Have 1000 coins at once."
        icon: GOLD_BLOCK
        background: GOLD_BLOCK
        rewardCoins: 50
        frame: goal
    completionist:
      root:
        id: all_achievements
        name: "&5&lCompletionist"
        description: "Get all other achievements."
        icon: ENDER_EYE
        background: ENDER_CHEST
        rewardCoins: 500
        frame: challenge
```

**Adding a New Tab:**

To add a new advancement tab, simply add a new key under `trees` in `advancements.yml` with a `root` section. Children are optional:

```yaml
# advancements.yml
trees:
  myNewTree:
    root:
      id: my_first
      name: "&a&lMy Achievement"
      description: "Do something awesome."
      icon: EMERALD
      background: EMERALD_BLOCK
      rewardCoins: 50
      frame: goal
```

**Frame Types:**

| Frame | Appearance |
|-------|-----------|
| `task` | Standard white frame |
| `goal` | Green-tinted frame |
| `challenge` | Orange-tinted frame with special icon |

**Trigger Events:**

Each achievement can define one or more **trigger events** that determine when it unlocks. A trigger specifies an action type and how many times that action must occur:

```yaml
triggers:
  kill:
    type: kill
    count: 10        # Unlocks after killing 10 mobs
```

| Trigger Type | Description |
|-------------|-------------|
| `kill` | Player kills a mob |
| `mine` | Player mines a block |
| `craft` | Player crafts an item |
| `fish` | Player catches fish |
| `levelUp` | Player levels up |
| `death` | Player dies |
| `join` | Player joins the server |
| `money` | Player has X coins (checked periodically) |

An achievement unlocks when **any** of its triggers are met. You can configure triggers via the in-game editor (`/rpgcraft editor advancements` → edit an achievement → Trigger Events) or directly in `advancements.yml`.

**Example with Triggers:**

```yaml
trees:
  combat:
    root:
      id: first_kill
      name: "&c&lFirst Blood"
      description: "Kill your first mob."
      icon: REDSTONE
      background: REDSTONE_BLOCK
      rewardCoins: 10
      frame: task
      triggers:
        kill:
          type: kill
          count: 1
    children:
      kill_10:
        id: kill_10
        name: "&c&lWarrior"
        description: "Kill 10 mobs."
        icon: IRON_SWORD
        parent: first_kill
        rewardCoins: 25
        frame: task
        triggers:
          kill:
            type: kill
            count: 10
```

**Built-in Achievements:**

The default config includes 6 tabs (Combat, Mining, Crafting, Fishing, Wealth, Completionist) with 10 total achievements:

| Achievement | Tree | Requirement | Reward |
|-------------|------|-------------|--------|
| First Blood | Combat | Kill your first mob | 10 coins |
| Warrior | Combat | Kill 10 mobs | 25 coins |
| Slayer | Combat | Kill 100 mobs | 100 coins |
| Miner | Mining | Mine 10 blocks | 15 coins |
| Quarry Master | Mining | Mine 100 blocks | 50 coins |
| Craftsman | Crafting | Craft 10 items | 20 coins |
| Fisherman | Fishing | Catch your first fish | 10 coins |
| Master Angler | Fishing | Catch 50 fish | 75 coins |
| Rich | Wealth | Have 1,000 coins at once | 50 coins |
| Completionist | Completionist | Get all other achievements | 500 coins |

**Access:**

- **Vanilla Menu:** Press `L` to open the advancement menu and browse all custom achievement tabs
- **Custom GUI:** Run `/rpgcraft editor advancements` to view achievements in a custom GUI (requires `rpgcraft.advancements` permission)
- **Config GUI:** Configure advancement settings via `/rpgcraft editor config` → Items & Mobs

**Features:**

- Toast notifications when unlocking achievements
- Coin rewards automatically awarded on unlock
- Configurable trigger events (kill, mine, craft, fish, levelUp, death, join, money) with custom counts
- Skript event `on achievement unlock` fires for each achievement
- Completionist auto-unlocks when all other achievements are earned
- Vanilla advancements can be revoked on join via config option
- Tab backgrounds customizable per tree
- Manage triggers via in-game editor (`/rpgcraft editor advancements`)

---

### 📜 Quests System

Quests are assigned by NPCs and tracked automatically. Use the quests GUI to view your progress.

**Access:** Run `/rpgcraft quests` to open the quests GUI (requires `rpgcraft.quests` permission).
Run `/rpgcraft editor quests` to open the quest editor (requires `rpgcraft.quests` + `rpgcraft.questEdit` permission).

**GUI Features (Player View):**
- The main quests GUI shows only **active quests** with step progress and completion requirements
- The inventory size **scales** with the number of active quests
- Click an active quest to disband it
- A **Completed Quests** button at the bottom left opens a separate view of completed quests
- The completed quests view has a **Back** button to return to active quests
- Color-coded icons: emerald (active), lime dye (completed)

**Quest Editor Features (Admin View):**
- Browse all quests grouped by NPC
- Create new quests under any NPC
- Edit quest properties: name, order, min-level, steps amount, rewards, and default location
- Edit individual steps: type (kill/get/talkToNPC), value, entity, item, NPC name
- Edit step actions: dialogue lines, remove items, give items
- Edit step-specific locations
- Delete quests with confirmation
- Move quests between NPCs
- Reload quests from disk

**Commands:**

| Command | Description |
|---------|-------------|
| `/rpgcraft quests` | Open the quests GUI |
| `/rpgcraft quests gui` | Open the quests GUI |
| `/rpgcraft quests startNew <id>` | Start a quest by ID |
| `/rpgcraft quests disband <id>` | Disband an active quest |
| `/rpgcraft quests resetAllPlayersQuests` | Reset all players' quests (requires `rpgcraft.resetQuests`) |
| `/rpgcraft editor quests` | Open the quest editor (requires `rpgcraft.questEdit`) |


### 👥 Party System

Form parties to quest and earn together.

**Features:**
- Create and manage parties (up to 5 members)
- Invite, accept, decline, leave, and kick players
- Transfer leadership on leader leave
- Disband entire party
- Share coins among party members
- Party GUI for management with invite and kick buttons

**Access:** Open the Nether Star menu and click the Party button, or use `/rpgcraft party` (requires `rpgcraft.party` permission).

**Commands:**

| Command | Description |
|---------|-------------|
| `/rpgcraft party` | Open the Party GUI |
| `/rpgcraft party create [name]` | Create a new party (optional custom name) |
| `/rpgcraft party invite <player>` | Invite a player to your party |
| `/rpgcraft party accept` | Accept a pending party invite |
| `/rpgcraft party decline` | Decline a pending party invite |
| `/rpgcraft party leave` | Leave your current party |
| `/rpgcraft party kick <player>` | Kick a member (leader only) |
| `/rpgcraft party list` | List all party members with status |
| `/rpgcraft party disband` | Disband the entire party (leader only) |

**GUI Features:**
- **Invite Player** button — prompts for a player name to invite (appears when party is not full)
- **Decline Invite** button — decline a pending invite (appears when not in a party)
- **Member click-to-kick** — party leader can click on a member's head to kick them
- **Leave Party** button — leave the current party

---

### 📦 Personal Storage

Each player has a private 54-slot chest that auto-saves.

**Features:**
- 54-slot personal inventory (all slots available for items)
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
| `/broadcast <msg>` | `/bc` | Broadcast to all players | `rpgcraft.broadcast` or OP |
| `/discord` | `/dc` | Show the server Discord link | None |

---

### 📋 Sidebar & Actionbar

- **Sidebar:** Dynamic scoreboard showing player stats, money, and level
- **Actionbar:** Real-time actionbar updates

---

### 🔌 Skript Support

Integrate Fable Craft with Skript scripts. All Skript elements are automatically registered when Skript is detected on the server.

**Events:**

| Event | Description | Variables |
|-------|-------------|-----------|
| `on quest start` | Quest started | `event-player`, `event-quest` |
| `on quest complete` | Quest completed | `event-player`, `event-quest` |
| `on quest update` | Quest objective updated | `event-player`, `event-quest`, `event-location` |
| `on achievement unlock` | Achievement unlocked | `event-player`, `event-achievement` |
| `on player level up` | Player level increased | `event-player`, `event-newlevel` |
| `on party join` | Player joined a party | `event-player`, `event-party` |
| `on party leave` | Player left a party | `event-player`, `event-party` |
| `on money change` | Player coins changed | `event-player`, `event-oldamount`, `event-newamount` |
| `on skill XP gain` | Player gained skill XP | `event-player`, `event-skill`, `event-xp` |

**Expressions:**

| Expression | Returns |
|------------|---------|
| `custom item "name"` | `ItemStack` of the custom item |
| `player's coins` | Player's coin balance |
| `player's level` | Player's overall level |
| `player's stat "Health"` | Player's stat value (Health, Mana, Defense, etc.) |
| `player's party` | Player's party name (empty if none) |
| `player's skill XP "mining"` | Player's XP in a skill |

**Effects:**

| Effect | Description |
|--------|-------------|
| `spawn custom mob "name" at location` | Spawns the custom mob |
| `add 100 to player's coins` | Adds coins to the player |
| `remove 50 from player's coins` | Removes coins from the player |
| `set stat "Health" of player to 20` | Sets a player's stat value |
| `add 10 to player's skill XP "mining"` | Awards skill XP |
| `give custom item "sword" to player` | Gives a custom item from the ItemDB |

---

### 🔮 Custom Enchantments

Define unique enchantments beyond vanilla Minecraft. Enchantments are stored in the `enchantments/` database folder (YAML) and apply automatically at enchanting tables.

**Access:** Run `/rpgcraft editor enchantments` to open the unified enchantment browser (requires `rpgcraft.enchantments` permission).
With `rpgcraft.enchantEdit` permission, the browser includes create, edit, and delete functionality.

**Enchantment Editor Features:**
- Browse all enchantments with their properties
- Create new enchantments with a custom ID
- Edit display name, description, max level, icon, and type (normal/treasure)
- Manage compatible items and conflicts via `#ADD`, `#REMOVE`, `#SET`, `#CLEAR`
- Set per-level properties (e.g., `1 damage 5`)
- Delete enchantments with confirmation
- Reload enchantments from disk

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

**Access:** Right-click a configured station block (e.g. Anvil, Furnace) to open its recipe GUI. Run `/rpgcraft editor crafting` to browse all station types (requires `rpgcraft.crafting` permission).

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

**Access:** Run `/rpgcraft editor config` (requires `rpgcraft.config` permission).

**Categories:**

| Category | Settings |
|----------|----------|
| General | Plugin toggles, message settings |
| Stats | Health, Mana, Defense, Attack Damage defaults |
| Bank | Death tax enabled, percent, max loss |
| Skills | Skill progression settings |
| Display | Sidebar, actionbar, boss bar toggles |
| Items/Mobs | Unbreakable items toggle, mob settings, Remove Vanilla Advancements, Achievements GUI Background |
| Player Level | Level progression, XP multipliers |

**Features:**
- Toggle boolean options with a single click
- Change numeric values with increment/decrement buttons
- Reload configurations without restarting
- Permission-gated access (`rpgcraft.config`)
- Changes take effect immediately

**Items & Mobs Settings:**

| Setting | Type | Description |
|---------|------|-------------|
| Remove Vanilla Advancements | Toggle | When enabled, revokes all vanilla Minecraft advancements when players join |
| Achievements GUI Background | Material | Click to enter a material name (e.g., `GRAY_STAINED_GLASS_PANE`) for the achievements GUI background |

---

### 🔧 Auto Config Defaults

The plugin automatically manages config file updates without overwriting your custom values.

**How It Works:**

When the plugin starts, it checks all config files against the built-in defaults. If any new config options have been added in a recent update, they are automatically filled in with their default values. Your existing custom values are **never overwritten**.

**Key Behaviors:**

- **New options are added automatically** — When a new version adds config options, they appear in your `config.yml` on the next restart
- **User values are preserved** — If you've changed a value, your change is kept even if the default changes
- **Nested sections work recursively** — Deeply nested options (e.g., `stats.Health.default`) are handled the same way
- **No manual migration needed** — You never need to manually merge config files or delete and regenerate them

**Example:**

If your `config.yml` has `advancements.removeVanilla: true` and a new update adds `advancements.guiBackground`, the next restart will add `advancements.guiBackground: GRAY_STAINED_GLASS_PANE` while keeping your `removeVanilla: true` value intact.

---

### ✨ Magic System

The magic system lets you create configurable spells that can be cast from custom items. Each spell can combine multiple effects, costs mana to cast, and has a cooldown between uses.

**How It Works:**

1. Define spells in the `spells/` folder (YAML files)
2. Assign a spell to a custom item in `itemDB/` via the `spell` key
3. Right-click with the item to cast the spell (targets entities in your line of sight)

**Assigning a Spell to an Item:**

In your `itemDB/` configuration, add a `spell` key to any custom item:

```yaml
wizardStaff:
  itemType: STICK
  name: "&dWizard's Staff"
  spell: fireball    # references a spell ID from the spells/ folder
```

**Spell Configuration:**

Each spell is defined by a unique ID and supports these top-level options:

| Option | Description |
|--------|-------------|
| `manaCost` | Mana consumed per cast (0 = free) |
| `cooldown` | Cooldown in ticks between casts (20 ticks = 1 second) |
| `castSound` | Sound played when casting (e.g. `ENTITY_FIREBALL_SHOOT`) |
| `effects` | List of effect definitions (see below) |

**Available Effect Types:**

| Effect Type | Description |
|-------------|-------------|
| `damage` | Deals damage to targets (supports damage types and area radius) |
| `particle` | Spawns particles with optional color and ring patterns |
| `fire` | Sets targets on fire with optional damage over time |
| `glow` | Makes targets glow visibly for a duration |
| `attribute` | Modifies entity attributes (attack damage, speed, health, etc.) |
| `summon` | Spawns mobs (vanilla or custom) at a location |
| `potion` | Applies potion effects to targets |
| `sound` | Plays a sound at a location |
| `knockback` | Pushes targets away with configurable force |

**Effect Configuration Details:**

**Damage:**
```yaml
myDamage:
  type: damage
  amount: 8.0              # Damage amount
  damageType: MAGIC        # Damage cause: MAGIC, FIRE, LIGHTNING_BOLT, etc.
  radius: 2.0              # Area of effect radius (optional)
```

**Particle:**
```yaml
myParticles:
  type: particle
  particle: FLAME          # Particle type
  amount: 30               # Number of particles
  offsetX: 0               # X offset from target
  offsetY: 1.0             # Y offset from target
  offsetZ: 0               # Z offset from target
  speed: 0.3               # Particle speed
  color: "255,100,0"       # RGB color (optional, comma-separated)
  ring: 1.5                # Ring radius for circular pattern (optional)
```

**Fire:**
```yaml
myFire:
  type: fire
  duration: 100            # Fire duration in ticks
  damageOverTime: 2.0      # Extra damage per tick interval (optional)
```

**Glow:**
```yaml
myGlow:
  type: glow
  duration: 200            # Glow duration in ticks
```

**Attribute:**
```yaml
myAttribute:
  type: attribute
  attribute: GENERIC_ATTACK_DAMAGE   # Attribute to modify
  amount: 5.0                        # Amount to add
  duration: 6000                     # Duration in ticks
```

Available attributes: `GENERIC_MAX_HEALTH`, `GENERIC_ATTACK_DAMAGE`, `GENERIC_MOVEMENT_SPEED`, `GENERIC_ARMOR`, `GENERIC_ARMOR_TOUGHNESS`, `GENERIC_KNOCKBACK_RESISTANCE`, `GENERIC_LUCK`, etc.

**Summon:**
```yaml
mySummon:
  type: summon
  mobType: ZOMBIE          # Base entity type
  amount: 1                # Number to spawn
  customMobId: myMob       # Custom mob ID from mobDB (optional)
  owner: caster            # Set to "caster" to mark as owned
```

**Potion:**
```yaml
myPotion:
  type: potion
  effectType: SLOW         # Potion effect type
  duration: 100            # Duration in ticks
  amplifier: 1             # Effect level (0 = level I)
  ambient: true            # No particle overlay
  particles: true          # Show effect particles
```

**Sound:**
```yaml
mySound:
  type: sound
  sound: BLOCK_FIRE_AMBIENT  # Sound to play
  volume: 1.0                # Sound volume
  pitch: 0.8                 # Sound pitch
```

**Knockback:**
```yaml
myKnockback:
  type: knockback
  strength: 2.0              # Horizontal knockback force
  height: 1.0               # Vertical knockback force
```

**Built-in Spells:**

| Spell ID | Mana | Cooldown | Effects |
|----------|------|----------|---------|
| `fireball` | 15 | 3s | Magic damage (AoE), fire with DoT, flame particles |
| `iceShard` | 10 | 2s | Magic damage, Slow II, blue particles |
| `lightningBolt` | 25 | 6s | High lightning damage (AoE), fire, electric spark particles |
| `heal` | 20 | 10s | Regeneration potion, heart particles |
| `summonZombie` | 30 | 15s | Summons a zombie, soul particles |
| `strengthBuff` | 12 | 9s | +5 attack damage, Strength potion, happy particles |
| `glowArrow` | 8 | 4s | Glow effect, glow squid ink particles |
| `knockbackBlast` | 10 | 3s | Knockback + magic damage, cloud particles |

**Example Spell Definition:**

```yaml
fireball:
  manaCost: 15.0
  cooldown: 60
  castSound: ENTITY_FIREBALL_SHOOT
  effects:
    fireDamage:
      type: damage
      amount: 8.0
      damageType: MAGIC
      radius: 2.0
    fireEffect:
      type: fire
      duration: 100
      damageOverTime: 2.0
    fireParticles:
      type: particle
      particle: FLAME
      amount: 30
      offsetY: 1.0
      speed: 0.3
      color: "255,100,0"
      ring: 1.5
    fireSound:
      type: sound
      sound: BLOCK_FIRE_AMBIENT
      volume: 1.0
      pitch: 0.8
```

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
| **Party** | Manage your party |
| **Personal Storage** | Open private chest |
| **Quests** | View and manage your quests |

---

### 📁 Configuration Files

| File/Folder | Purpose |
|-------------|---------|
| `config.yml` | Main settings (stats, death tax, block safety, player levels, etc.) |
| `advancements.yml` | Achievement/advancement trees and settings (separate from main config) |
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
| `mobSkills/` | Mob skill definitions (assignable to mobs and NPCs) |
| `spells/` | Spell definitions for the magic system |

**Note:** All config files use the **auto-config-defaults** system — new options are automatically added on startup without overwriting your custom values. See the [Auto Config Defaults](#-auto-config-defaults) section for details.

---

### 🎛️ Unified Editor Command

The `/rpgcraft editor` command provides access to all in-game editors through a single command.

**Usage:** `/rpgcraft editor <name>`

| Editor Name | Description | Additional Permission |
|-------------|-------------|----------------------|
| `itemdb` | Browse and edit custom items | `rpgcraft.itemDB` |
| `config` | In-game config menu | `rpgcraft.config` |
| `crafting` | Browse crafting stations | `rpgcraft.crafting` |
| `enchantments` | Browse, create, edit, and delete enchantments | `rpgcraft.enchantments` (+ `rpgcraft.enchantEdit` for editing) |
| `advancements` | View achievements | `rpgcraft.advancements` |
| `mobs` | Browse and edit custom mobs | `rpgcraft.mobs` |
| `quests` | View, create, edit, and delete quests | `rpgcraft.quests` (+ `rpgcraft.questEdit` for editing) |
| `yamledit` | Toggle YAML click-to-edit mode | `rpgcraft.yamlEdit` |

All editors require `rpgcraft.editor` plus the specific permission listed above. Run `/rpgcraft editor` without arguments to see the help list. Tab completion is supported for editor names.

---

### 🔧 Commands

| Command | Aliases | Permission | Description |
|---------|---------|------------|-------------|
| `/rpgcraft` | `/rc`, `/rCraft`, `/RPGC`, `/rpgc` | `rpgcraft.command` | Main plugin command |
| `/rpgcraft reload` | — | `rpgcraft.reload` | Reload all configuration files |
| `/rpgcraft quests` | — | `rpgcraft.quests` | Open quests GUI (view active/completed quests) |
| `/rpgcraft money` | — | `rpgcraft.money` | Manage player coins |
| `/rpgcraft mobs` | — | `rpgcraft.mobs` | Spawn and manage custom mobs |
| `/rpgcraft build` | — | `rpgcraft.build` | Build helper tools |
| `/rpgcraft resetPDC` | — | `rpgcraft.resetPDC` | Clear player persistent data |
| `/rpgcraft resetStats` | — | `rpgcraft.resetStats` | Reset player stats |
| `/rpgcraft npc` | — | `rpgcraft.npc` | Manage NPCs (set type, skills) |
| `/rpgcraft createnpc` | — | `rpgcraft.npc` | Create a new Citizens NPC |
| `/rpgcraft quests gui` | — | `rpgcraft.quests` | Open quests GUI |
| `/rpgcraft quests startNew <id>` | — | `rpgcraft.quests` | Start a quest |
| `/rpgcraft quests disband <id>` | — | `rpgcraft.quests` | Disband an active quest |
| `/rpgcraft lootTable` | — | `rpgcraft.lootTable` | Manage chest loot tables |
| `/rpgcraft party` | — | `rpgcraft.party` | Open the Party GUI |
| `/rpgcraft party invite <player>` | — | `rpgcraft.party` | Invite a player to your party |
| `/rpgcraft party accept` | — | `rpgcraft.party` | Accept a pending party invite |
| `/rpgcraft editor` | — | `rpgcraft.editor` | Show list of available editors |
| `/rpgcraft editor <name>` | — | `rpgcraft.editor` + specific | Open a specific editor (see above) |
| `/broadcast <msg>` | `/bc` | `rpgcraft.broadcast` | Broadcast a message to all players |
| `/givemenu` | `/givem` | `rpgcraft.givemenu` | Give the RPG Menu Nether Star |
| `/message <player> <msg>` | `/msg`, `/dm`, `/pm` | None | Send a private message |
| `/discord` | `/dc` | None | Show Discord invite link |

---

### 🔑 Permissions

All permissions use the `rpgcraft.*` namespace. Grant `rpgcraft.*` to give all permissions at once.

| Permission | Default | Description |
|------------|---------|-------------|
| `rpgcraft.*` | — | Grants all RPGCraft permissions |
| `rpgcraft.command` | true | Use `/rpgcraft` subcommands |
| `rpgcraft.reload` | op | Reload all configuration files |
| `rpgcraft.config` | op | Open the in-game config menu |
| `rpgcraft.enchantments` | op | Access the enchantment browser |
| `rpgcraft.enchantEdit` | op | Create, edit, and delete enchantments in the browser |
| `rpgcraft.advancements` | op | View achievements/advancements GUI |
| `rpgcraft.crafting` | op | Browse crafting stations |
| `rpgcraft.itemDB` | op | Browse and edit custom items |
| `rpgcraft.money` | op | Manage player coins (add, subtract, set, get) |
| `rpgcraft.mobs` | op | Spawn and manage custom mobs |
| `rpgcraft.build` | op | Use build helper tools; prevents inventory replacement on join |
| `rpgcraft.resetPDC` | op | Clear player persistent data |
| `rpgcraft.resetStats` | op | Reset player stats |
| `rpgcraft.npc` | op | Create and manage NPCs |
| `rpgcraft.quests` | op | Open quests GUI, start and disband quests |
| `rpgcraft.questEdit` | op | Create, edit, and delete quests in the quest editor |
| `rpgcraft.resetQuests` | op | Reset all players' quests |
| `rpgcraft.lootTable` | op | Manage chest loot tables |
| `rpgcraft.edit` | op | Edit NPCs while sneaking |
| `rpgcraft.party` | true | Use party commands (create, invite, accept, leave, kick, disband) |
| `rpgcraft.givemenu` | op | Give the RPG Menu Nether Star item |
| `rpgcraft.broadcast` | op | Send broadcast messages to all players |
| `rpgcraft.setStat` | op | Set player stats via `/setStat` |
| `rpgcraft.debug` | op | Debug commands (view stats memory) |
| `rpgcraft.yamlEdit` | op | Toggle YAML editing mode |
| `rpgcraft.editor` | op | Access the unified editor command |
| `rpgcraft.chat.color` | op | Use color codes in chat messages |
