# GUILib Documentation

## Table of Contents
1. [Getting Started](#getting-started)
2. [Creating GUIs](#creating-guis)
3. [Working with Items](#working-with-items)
4. [Custom Skulls](#custom-skulls)
5. [Animations](#animations)
6. [Event Handling](#event-handling)
7. [Examples](#examples)
8. [Best Practices](#best-practices)
9. [Troubleshooting](#troubleshooting)

## Getting Started

### Installation
Add the following to your `build.gradle`:
```gradle
repositories {
    maven {
        name = 'papermc-repo'
        url = 'https://repo.papermc.io/repository/maven-public/'
    }
}

dependencies {
    implementation 'com.example:guilib:1.0-SNAPSHOT'
}
```

### Plugin Setup
In your main plugin class:
```java
import com.example.guilib.api.GUI;
import com.example.guilib.api.GUIItem;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

public class YourPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Your plugin initialization code
    }
}
```

### Basic Usage
```java
// Create a simple GUI with 3 rows (27 slots)
GUI gui = new GUI(Component.text("My GUI"), 3);

// Create a clickable button
GUIItem button = GUIItem.builder()
    .material(Material.DIAMOND)
    .name(Component.text("Click me!"))
    .lore(List.of(
        Component.text("This is a button"),
        Component.text("Click to perform an action")
    ))
    .clickHandler(context -> {
        Player player = context.player();
        player.sendMessage(Component.text("Button clicked!"));
    })
    .build();

// Place button in the center slot (slot 13)
gui.setItem(13, button);

// Open GUI for player
gui.open(player);
```

## Creating GUIs

### GUI Constructor Options
```java
// Basic GUI with title and rows (1-6)
GUI gui = new GUI(Component.text("Title"), 3);

// Slot calculation:
// - 1 row = 9 slots
// - 2 rows = 18 slots
// - 3 rows = 27 slots
// - 4 rows = 36 slots
// - 5 rows = 45 slots
// - 6 rows = 54 slots
```

### GUI Methods
#### Setting Items
```java
// Set item at specific slot
gui.setItem(slot, item);

// Get item at slot (returns Optional<GUIItem>)
Optional<GUIItem> item = gui.getItem(slot);

// Example usage with Optional
gui.getItem(slot).ifPresent(item -> {
    // Do something with the item
});
```

#### Opening/Closing
```java
// Open GUI for player
gui.open(player);

// GUI automatically closes when:
// - Player closes inventory
// - Player disconnects
// - Another inventory is opened
```

#### Animation Control
```java
// Start animation
gui.startAnimation(animation);

// Stop current animation
gui.stopAnimation();

// Animation automatically stops when:
// - GUI is closed
// - Another animation starts
// - Plugin is disabled
```

## Working with Items

### Item Builder
The `GUIItem.builder()` provides a fluent API for creating items:

```java
GUIItem item = GUIItem.builder()
    .material(Material.DIAMOND_SWORD)     // Required: Base material
    .name(Component.text("Special Sword")) // Optional: Display name
    .lore(List.of(                        // Optional: Item lore
        Component.text("Line 1"),
        Component.text("Line 2")
    ))
    .customModelData(1001)                // Optional: Custom model data
    .amount(1)                            // Optional: Stack size (default: 1)
    .maxStackSize(16)                     // Optional: Max stack size (default: material's default)
    .durability(100)                      // Optional: Item durability
    .clickHandler(context -> {            // Optional: Click handler
        // Handle click
    })
    .build();
```

### Item Properties in Detail

#### Material
```java
// Basic materials
.material(Material.STONE)
.material(Material.DIAMOND)
.material(Material.EMERALD)

// Tools and weapons
.material(Material.DIAMOND_SWORD)
.material(Material.NETHERITE_PICKAXE)

// Special items
.material(Material.ENCHANTED_BOOK)
.material(Material.POTION)
```

#### Display Name
```java
// Simple text
.name(Component.text("Item Name"))

// Colored text
.name(Component.text("Special Item")
    .color(TextColor.color(255, 0, 0)))

// Styled text
.name(Component.text("Rare Item")
    .decoration(TextDecoration.BOLD, true)
    .decoration(TextDecoration.ITALIC, false))
```

#### Lore
```java
// Single line
.lore(List.of(Component.text("Description")))

// Multiple lines
.lore(List.of(
    Component.text("Line 1"),
    Component.text("Line 2").color(TextColor.color(0, 255, 0)),
    Component.text("Line 3").decoration(TextDecoration.ITALIC, true)
))
```

#### Custom Model Data
```java
// For resource pack custom models
.customModelData(1001)

// Remove custom model data
.customModelData(0)
```

#### Amount and Stack Size
```java
// Set item amount
.amount(16)

// Set custom max stack size
.maxStackSize(64)

// Note: amount cannot exceed maxStackSize
```

#### Durability
```java
// Set specific durability
.durability(100)

// Full durability
.durability(0)

// Note: Only works on items with durability (tools, weapons, armor)
```

## Custom Skulls

### Creating Player Skulls
The `GUISkull` class extends `GUIItem` to provide easy creation of player head items:

```java
// Create a skull using player name
GUISkull playerSkull = GUISkull.skullBuilder()
    .name(Component.text("Player's Head"))
    .playerName("Notch")
    .lore(List.of(Component.text("Click to view profile")))
    .clickHandler(context -> {
        Player player = context.player();
        player.sendMessage("Viewing profile...");
    })
    .build();

// Place the skull in GUI
gui.setItem(13, playerSkull);
```

### Custom Texture Skulls
You can create skulls with custom textures using texture URLs:

```java
GUISkull customSkull = GUISkull.skullBuilder()
    .name(Component.text("Custom Skull"))
    .texture("http://textures.minecraft.net/texture/YOUR_TEXTURE_HERE")
    .amount(1)
    .customModelData(1001)  // Optional
    .build();
```

### Skull Properties

#### Player Name
```java
// Set player name (uses player's skin)
.playerName("Notch")
```

#### Texture URL
```java
// Set custom texture URL
.texture("http://textures.minecraft.net/texture/...")
```

#### Additional Properties
GUISkull supports all standard GUIItem properties:
```java
GUISkull skull = GUISkull.skullBuilder()
    .name(Component.text("Special Skull"))
    .lore(List.of(
        Component.text("Line 1"),
        Component.text("Line 2")
    ))
    .amount(1)
    .customModelData(1001)
    .clickHandler(context -> {
        // Handle click
    })
    .texture("http://textures.minecraft.net/texture/...")
    .build();
```

### Error Handling
The GUISkull class includes built-in error handling:
- Falls back to default player head if texture loading fails
- Logs errors for debugging
- Never throws exceptions to the GUI system

## Animations

### Creating Animations
```java
// Create frame maps
Map<Integer, GUIItem> frame1 = new HashMap<>();
frame1.put(0, GUIItem.builder()
    .material(Material.DIAMOND)
    .name(Component.text("Frame 1"))
    .build());
frame1.put(1, GUIItem.builder()
    .material(Material.EMERALD)
    .name(Component.text("Also Frame 1"))
    .build());

Map<Integer, GUIItem> frame2 = new HashMap<>();
frame2.put(0, GUIItem.builder()
    .material(Material.GOLD_INGOT)
    .name(Component.text("Frame 2"))
    .build());
frame2.put(1, GUIItem.builder()
    .material(Material.IRON_INGOT)
    .name(Component.text("Also Frame 2"))
    .build());

// Create animation
GUIAnimation animation = GUIAnimation.builder()
    .frames(List.of(
        new GUIAnimation.Frame(frame1),
        new GUIAnimation.Frame(frame2)
    ))
    .interval(20L) // 20 ticks = 1 second
    .build();

// Start animation
gui.startAnimation(animation);
```

### Animation Tips
- Use consistent frame intervals (20 ticks recommended minimum)
- Keep animations simple to avoid performance issues
- Keep frame count low
- Test animations with multiple players viewing the GUI
- Remember to stop animations when no longer needed

## Event Handling

### Click Handler
```java
GUIItem button = GUIItem.builder()
    .material(Material.EMERALD)
    .name(Component.text("Interactive Button"))
    .clickHandler(context -> {
        Player player = context.player();
        ClickType clickType = context.clickType();
        GUI gui = context.gui();

        // Handle different click types
        switch (clickType) {
            case LEFT -> {
                player.sendMessage("Left click!");
                // Example: Open another menu
                GUI newMenu = new GUI(Component.text("New Menu"), 3);
                newMenu.open(player);
            }
            case RIGHT -> {
                player.sendMessage("Right click!");
                // Example: Execute command
                player.performCommand("spawn");
            }
            case SHIFT_LEFT -> {
                player.sendMessage("Shift + Left click!");
                // Example: Close menu
                player.closeInventory();
            }
            case SHIFT_RIGHT -> {
                player.sendMessage("Shift + Right click!");
                // Example: Play sound
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }
    })
    .build();
```

### Click Context Properties
```java
context.player()    // The player who clicked
context.clickType() // Type of click (LEFT, RIGHT, SHIFT_LEFT, etc.)
context.gui()       // The GUI instance that was clicked

// Example usage
clickHandler(context -> {
    Player player = context.player();
    if (!player.hasPermission("your.permission")) {
        player.sendMessage("No permission!");
        return;
    }

    // Handle the click
    switch (context.clickType()) {
        case LEFT -> handleLeftClick(player);
        case RIGHT -> handleRightClick(player);
    }
});
```

## Examples

### Shop Menu
```java
public class ShopGUI {
    public static GUI create() {
        GUI shop = new GUI(Component.text("Shop"), 6);

        // Create categories
        GUIItem weapons = createCategory("Weapons", Material.DIAMOND_SWORD);
        GUIItem armor = createCategory("Armor", Material.DIAMOND_CHESTPLATE);
        GUIItem tools = createCategory("Tools", Material.DIAMOND_PICKAXE);
        GUIItem food = createCategory("Food", Material.GOLDEN_APPLE);

        // Place categories
        shop.setItem(10, weapons);
        shop.setItem(12, armor);
        shop.setItem(14, tools);
        shop.setItem(16, food);

        return shop;
    }

    private static GUIItem createCategory(String name, Material icon) {
        return GUIItem.builder()
            .material(icon)
            .name(Component.text(name))
            .lore(List.of(
                Component.text("Click to view items"),
                Component.text("Right-click for quick buy")
            ))
            .clickHandler(context -> {
                if (context.clickType() == ClickType.LEFT) {
                    openCategoryMenu(context.player(), name);
                } else if (context.clickType() == ClickType.RIGHT) {
                    openQuickBuy(context.player(), name);
                }
            })
            .build();
    }
}
```

### Animated Loading Screen
```java
public class LoadingGUI {
    public static GUI create() {
        GUI loading = new GUI(Component.text("Loading..."), 3);

        // Create loading animation frames
        List<GUIAnimation.Frame> frames = new ArrayList<>();
        Material[] materials = {
            Material.WHITE_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE
        };

        // Generate frames
        for (Material material : materials) {
            Map<Integer, GUIItem> frame = new HashMap<>();
            for (int i = 0; i < 27; i++) {
                frame.put(i, GUIItem.builder()
                    .material(material)
                    .name(Component.text("Loading..."))
                    .build());
            }
            frames.add(new GUIAnimation.Frame(frame));
        }

        // Create and start animation
        GUIAnimation animation = GUIAnimation.builder()
            .frames(frames)
            .interval(10L)
            .build();

        loading.startAnimation(animation);
        return loading;
    }
}
```

### Paginated Item Browser
```java
public class BrowserGUI {
    public static GUI createPage(List<ItemStack> items, int page, int totalPages) {
        GUI browser = new GUI(Component.text("Items - Page " + (page + 1) + "/" + totalPages), 6);

        // Add items for current page
        int itemsPerPage = 45; // 5 rows for items
        int startIndex = page * itemsPerPage;
        for (int i = 0; i < itemsPerPage && (startIndex + i) < items.size(); i++) {
            ItemStack item = items.get(startIndex + i);
            browser.setItem(i, GUIItem.builder()
                .material(item.getType())
                .name(Component.text(item.getType().name()))
                .amount(item.getAmount())
                .build());
        }

        // Navigation buttons
        if (page > 0) {
            browser.setItem(45, createNavigationButton("Previous", page - 1, items));
        }
        if (page < totalPages - 1) {
            browser.setItem(53, createNavigationButton("Next", page + 1, items));
        }

        return browser;
    }

    private static GUIItem createNavigationButton(String text, int targetPage, List<ItemStack> items) {
        return GUIItem.builder()
            .material(Material.ARROW)
            .name(Component.text(text))
            .clickHandler(context -> {
                GUI newPage = createPage(items, targetPage, (items.size() + 44) / 45);
                newPage.open(context.player());
            })
            .build();
    }
}
```

## Best Practices

### Performance Optimization
1. **Minimize Animation Frames**
  - Keep frame count low
  - Use reasonable intervals (20+ ticks)
  - Stop animations when GUI is closed

2. **Efficient Item Creation**
  - Cache commonly used items
  - Avoid creating new items unnecessarily
  - Use builder pattern efficiently

3. **Memory Management**
  - Clean up resources when GUI closes
  - Don't store unnecessary references
  - Use weak references for long-term storage

### Code Organization
1. **GUI Class Structure**
```java
public class CustomGUI {
    private final GUI gui;
    private final Map<Integer, GUIItem> items;

    public CustomGUI() {
        this.gui = new GUI(Component.text("Custom GUI"), 3);
        this.items = new HashMap<>();
        this.initialize();
    }

    private void initialize() {
        // Setup GUI items
    }

    public void open(Player player) {
        this.gui.open(player);
    }
}
```

2. **Item Management**
```java
public class ItemRegistry {
    private static final Map<String, GUIItem> ITEMS = new HashMap<>();

    static {
        // Register common items
        ITEMS.put("close", createCloseButton());
        ITEMS.put("back", createBackButton());
        ITEMS.put("next", createNextButton());
    }

    private static GUIItem createCloseButton() {
        return GUIItem.builder()
            .material(Material.BARRIER)
            .name(Component.text("Close"))
            .clickHandler(context -> context.player().closeInventory())
            .build();
    }

    // ... other methods
}
```

### Error Handling
```java
public class SafeGUI {
    public static void openSafely(Player player, GUI gui) {
        try {
            gui.open(player);
        } catch (Exception e) {
            player.sendMessage(Component.text("Error opening GUI: " + e.getMessage()));
            // Log error
        }
    }

    public static void setItemSafely(GUI gui, int slot, GUIItem item) {
        try {
            if (slot >= 0 && slot < gui.getInventory().getSize()) {
                gui.setItem(slot, item);
            }
        } catch (Exception e) {
            // Log error
        }
    }
}
```

## Troubleshooting

### Common Issues and Solutions

1. **Items Not Appearing**
  - Check slot numbers (0-53 for 6 rows)
  - Verify material is valid
  - Ensure GUI is properly initialized

2. **Click Handlers Not Working**
  - Verify click handler is set
  - Check for exceptions in handler
  - Ensure GUI is registered properly

3. **Animations Not Playing**
  - Check interval timing
  - Verify frame content
  - Ensure animation is started

4. **Memory Leaks**
  - Stop animations when unused
  - Clear references when GUI closes
  - Use proper cleanup methods

5. **Skull Issues**
  - Verify player names are correct
  - Check texture URLs are valid
  - Ensure proper error handling

### Debug Tips
```java
public class GUIDebug {
    public static void logGUIState(GUI gui) {
        System.out.println("GUI Title: " + gui.getTitle());
        System.out.println("GUI Size: " + gui.getInventory().getSize());
        System.out.println("Items:");
        for (int i = 0; i < gui.getInventory().getSize(); i++) {
            gui.getItem(i).ifPresent(item -> 
                System.out.println("Slot " + i + ": " + item.getMaterial()));
        }
    }
}
```
