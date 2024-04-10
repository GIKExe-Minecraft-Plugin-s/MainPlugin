package ru.gikexe.mainplugin;

import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public final class MainPlugin extends JavaPlugin {
	public Server server;
	public PluginManager pm;
	public Config players;

	public void onEnable() {
		server = getServer();
		pm = server.getPluginManager();

		pm.registerEvents(new AuthListener(this), this);

		@Nullable PluginCommand com = getCommand("auth");
		if (com != null) {
			AuthExecutor exec = new AuthExecutor(this);
			com.setExecutor(exec);
			com.setTabCompleter(exec);
		}

		players = new Config(this, "players.yml");
		for (Player player : server.getOnlinePlayers()) {
			((Map<String, Object>) players.get(player.getUniqueId().toString())).replace("login", true);
		}
	}

	public void onDisable() {
		if (players != null) {
			for (Object data : players.values()) ((Map<String, Object>) data).replace("login", false);
			players.save();
		}
	}
}
