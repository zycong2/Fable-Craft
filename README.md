
# Changelog V. 1.3 -> 1.4

- Changes the getNextMessage method to waitForNextMessage
  - old: `getNextMessage(Player).thenAccept(msg -> {})`
  - new: `waitForNextMessage(Player, (Player, Message) -> {})`
- Changes some functionalities of some editor
  - item lore
    - old: type what line to change then set it
    - new: added 3 new commands
      - #ADD add a new line of lore
      - #SET set the specify line to a new one
      - #REMOVE remove the specify line
- Changes when hovering over a player name will now give a prompt to message them (default format)
- Fix clickEvent would not work using the GUI class
- Fix /itemDB not opening GUI or clickEvent does not work
- A whole lot of bug fixes
- Added 3 new commands
  - /message
  - /broadcast
  - /discord
- Added a database to store player's data

---

# Fable Craft

fablecraft (or *fable craft*) is an RPG styled plugin. Focusing on customization
and the need to install only one single plugin


## Features

- Custom items system (with GUI editor)
- Custom mob system (with GUI editor)
- Custom quest system (requires citizens)
- Custom Stats
- Custom loot tables


## Features Overview
### Items:
- Access:
  * YML: \
      On initial start of the plugin a new subfolder is created named 'itemDB'. This folder contains by default a file named Default.yml in this file you can see all the available options you can change. In the itemDB folder you can also create new files to order all your custom items better.
  * Editor: \
      When you have loaded the plugin you can open the item editor using the `/rpgcraft itemDB` command. This opens a GUI containing all the current custom items defaulting to the 3 default items noted before. You can click on any of the items to open the editor of the item.
- Options:
  - itemType: \
    This option sets the type of the item when a new item is created in the editor this is defaulted to a dirt block
  - ItemID: \
    This option sets as what the item is saved in game. Recommended to set this to the item name.
  - name: \
    This option sets the custom name of the item that is shown in game. This also supports hex color codes and Minecraft color codes.
  - lore: \
    This option sets the lore of the also supports hex and Minecraft color codes. In the YAML of this item this is saved as a list.
  - CustomModelData: \
    This sets the custom model data of the item you will only need to set this if you use a recourse pack.
  - enchantments: \
    This option sets the enchantments of the item. This is saved in a list, with the format of [enchantment name]:[enchantment level] with both required if the level isn't given it WON'T load so if the enchantment doesn't have levels you need to set this to 1.
  - AttackDamage: \
    This options sets the number of how much these items adds to the player's attack damage.
  - MinLevel: \
    Work in progress.
  - hide: \
    This options sets what to hide of the items attribute. You can add the following tags: ENCHANTS, ATTRIBUTES, DYE, PLACED_ON, DESTROYS and ARMOR_TRIM.
  - group: \
    Work in progress.
  - rarity: \
    This sets the rarity of the items. You can change what rarities the plugin creates in the config.yml
  - recipe: \
    This creates custom recipes for the item both types also contains a permission options where you can set the permission required to craft the recipe. \
    The two types of recipe's:
    - shaped: \
      Shaped recipes require a few options:
      - shape: \
        The shape is saved in a list of always 3 items. The items in this list must be a string of three characters so the list represents a crafting grid. In that grid a space represents air and a letter represents an item that is set in the following option. 
      - ingredients: \
        This sets what the shape of the recipe should represent in a list from with the following format: [Letter that represent the item in the shape]:[the item type]
    - unshaped: \
      Unshaped recipes are easier to set up, it only requires the ingredients option where you type a list of all the items to make the recipe with a : then the amount of that item you want added.
  - Color: \
    This option is a leather armor exclusive and sets the color of the leather armor in a rpg format.
  - Tile: \
    This option is a book exclusive option that sets the title of the book.
  - Author:
    This option is also a book exclusive option that sets the author or the book.
  
### Mobs:

