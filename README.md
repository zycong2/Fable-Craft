
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
- Added 1 new commands
  - /message
- Added a database to store player's data

---

# Fable Craft

fablecraft (or *fable craft*) is a RPG styled plugin. focusing on customization
and the need to install only 1 single plugin

## What do we mean by RPG styled

I don't know myself I have to ask my friend to change this

## Features

- Custom Items system

I don't know I'm feeling kinna tired from writing this at the moment
Will fix ASAP

- 
