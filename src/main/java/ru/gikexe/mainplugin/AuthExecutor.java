package ru.gikexe.mainplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class AuthExecutor implements CommandExecutor, TabCompleter {
	MainPlugin plugin;

	public AuthExecutor(MainPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String name, String[] args) {
		if (!sender.isOp()) return true;
		if (args.length == 4 && args[0].equals("set") && args[1].equals("pass")) {
			Player player = Bukkit.getServer().getPlayer(args[2]);
			if (player == null) return true;
			((Map<String, Object>) plugin.players.get(player.getUniqueId().toString())).replace("pass", args[3]);
			sender.sendMessage("Пароль изменён");
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String name, String[] args) {
		if (args.length == 0) {
			return List.of("set");
		} else if (args.length == 1) {
			if (args[0].equals("set")) {
				return List.of("pass", "login");
			} else {
				return List.of();
			}
		} else if (args.length == 2) {
			return null;
		}
		return List.of();
	}
}
