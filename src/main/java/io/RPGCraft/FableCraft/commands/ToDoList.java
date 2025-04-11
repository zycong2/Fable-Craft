package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.core.GUI.GUI;
import io.RPGCraft.FableCraft.core.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.GUI.GUISkull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;

public class ToDoList implements CommandExecutor {
  List<String> todoList = List.of(
    "Fix the death error",
    "Fix itemDB"
  );

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
    if(commandSender instanceof ConsoleCommandSender){return false;}
    Player player = (Player) commandSender;

    GUI todoList = new GUI(Colorize("&aTo Do List"), 4);
    Integer i = 0;
    for(String todo : this.todoList) {
      GUISkull todoItem = GUISkull.skullBuilder()
        .name(Colorize( "&f" + i++ + ". &7" + todo))
        .playerName("Tonnam_101")
        .lore(List.of(
          Colorize("&fPlease do these tasks")
        ))
        .clickHandler(clickContext -> {
          player.sendMessage(Colorize("&fYou wanna do this task?"));
        })
        .build();
      todoList.setItem(i , todoItem);
      i++;
    }

    todoList.open(player);


    return false;
  }
}
