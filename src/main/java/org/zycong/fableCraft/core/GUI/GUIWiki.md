# GUILib Documentation

## Table of Contents
1. [Getting Started](#getting-started)
2. [Creating GUIs](#creating-guis)
3. [Working with Items](#working-with-items)
4. [Animations](#animations)
5. [Event Handling](#event-handling)
6. [Examples](#examples)

## Getting Started

### Installation
idk

### Basic Usage
```java
// Create a simple GUI
GUI gui = new GUI(Component.text("My GUI"), 3); // 3 rows

// Add items
GUIItem button = GUIItem.builder()
    .material(Material.DIAMOND)
    .name(Component.text("Click me!"))
    .build();

gui.setItem(13, button); // Center slot

// Open for player
gui.open(player);
```

## Creating GUIs

### GUI Constructor
```java
// Create GUI with title and rows (1-6)
GUI gui = new GUI(Component.text("Title"), 3);
```

### GUI Methods
- `setItem(int slot, GUIItem item)` - Set item at slot
- `getItem(int slot)` - Get item at slot (returns Optional<GUIItem>)
- `open(Player player)` - Open GUI for player
- `startAnimation(GUIAnimation animation)` - Start an animation
- `stopAnimation()` - Stop current animation
- `getInventory()` - Get Bukkit inventory

## Working with Items

### Creating Items
```java
GUIItem item = GUIItem.builder()
    .material(Material.DIAMOND_SWORD)
    .name(Component.text("Special Sword"))
    .lore(List.of(
        Component.text("Line 1"),
        Component.text("Line 2")
    ))
    .customModelData(1001)
    .amount(1)
    .maxStackSize(16)
    .durability(100)
    .clickHandler(context -> {
        // Handle click
    })
    .build();
```

### Item Properties
- `material` - Item material (required)
- `name` - Display name (Component)
- `lore` - Item lore (List<Component>)
- `customModelData` - Custom model data for resource packs
- `amount` - Stack size
- `maxStackSize` - Maximum stack size
- `durability` - Item durability
- `clickHandler` - Click event handler

## Animations

### Creating Animations
```java
// Create frames
Map<Integer, GUIItem> frame1 = new HashMap<>();
frame1.put(0, item1);
frame1.put(1, item2);

Map<Integer, GUIItem> frame2 = new HashMap<>();
frame2.put(0, item3);
frame2.put(1, item4);

// Create animation
GUIAnimation animation = GUIAnimation.builder()
    .frames(List.of(
        new GUIAnimation.Frame(frame1),
        new GUIAnimation.Frame(frame2)
    ))
    .interval(20L) // Ticks between frames (20 ticks = 1 second)
    .build();

// Start animation
gui.startAnimation(animation);
```

### Animation Control
- `start(GUI gui)` - Start animation
- `stop()` - Stop animation
- Animations automatically stop when GUI is closed

## Event Handling

### Click Events
```java
GUIItem button = GUIItem.builder()
    .material(Material.EMERALD)
    .clickHandler(context -> {
        Player player = context.player();
        ClickType clickType = context.clickType();
        GUI gui = context.gui();

        // Handle different click types
        switch (clickType) {
            case LEFT -> player.sendMessage("Left click!");
            case RIGHT -> player.sendMessage("Right click!");
            case SHIFT_LEFT -> player.sendMessage("Shift + Left click!");
        }
    })
    .build();
```

### Click Context
The `ClickContext` record provides:
- `player()` - Clicking player
- `clickType()` - Type of click (LEFT, RIGHT, etc.)
- `gui()` - GUI instance

## Examples

### Simple Menu
```java
GUI menu = new GUI(Component.text("Menu"), 3);

// Create buttons
GUIItem homeButton = GUIItem.builder()
    .material(Material.COMPASS)
    .name(Component.text("Home"))
    .clickHandler(context -> {
        context.player().performCommand("home");
    })
    .build();

GUIItem shopButton = GUIItem.builder()
    .material(Material.EMERALD)
    .name(Component.text("Shop"))
    .clickHandler(context -> {
        context.player().performCommand("shop");
    })
    .build();

// Set buttons
menu.setItem(11, homeButton);
menu.setItem(15, shopButton);
```

### Animated Menu
```java
// Create frames
Map<Integer, GUIItem> frame1 = new HashMap<>();
frame1.put(4, GUIItem.builder()
    .material(Material.DIAMOND)
    .name(Component.text("Frame 1"))
    .build());

Map<Integer, GUIItem> frame2 = new HashMap<>();
frame2.put(4, GUIItem.builder()
    .material(Material.EMERALD)
    .name(Component.text("Frame 2"))
    .build());

// Create animation
GUIAnimation animation = GUIAnimation.builder()
    .frames(List.of(
        new GUIAnimation.Frame(frame1),
        new GUIAnimation.Frame(frame2)
    ))
    .interval(20L)
    .build();

// Create GUI and start animation
GUI gui = new GUI(Component.text("Animated Menu"), 1);
gui.startAnimation(animation);
```

### Paginated Menu
```java
// Create multiple GUIs for pages
GUI page1 = new GUI(Component.text("Page 1/2"), 3);
GUI page2 = new GUI(Component.text("Page 2/2"), 3);

// Navigation buttons
GUIItem nextPage = GUIItem.builder()
    .material(Material.ARROW)
    .name(Component.text("Next Page"))
    .clickHandler(context -> {
        page2.open(context.player());
    })
    .build();

GUIItem prevPage = GUIItem.builder()
    .material(Material.ARROW)
    .name(Component.text("Previous Page"))
    .clickHandler(context -> {
        page1.open(context.player());
    })
    .build();

// Add navigation
page1.setItem(26, nextPage);
page2.setItem(18, prevPage);
```