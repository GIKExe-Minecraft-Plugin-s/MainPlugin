package ru.gikexe.mainplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static org.bukkit.Material.AIR;

public final class MainPlugin extends JavaPlugin {
	static MainPlugin me;
	Server server;
	Logger logger;
	PluginManager pm;
	Component prefix = Component.text(" -> ", GRAY);

	Recipes recipes;
	PluginCommand command;
	MainListener listener;

	public void onEnable() {
		me = this;
		server = getServer();
		logger = getLogger();
		pm = server.getPluginManager();

		listener = new MainListener(this);
		pm.registerEvents(listener, this);

		regExecutor(new CoinExecutor(), "coin");

		recipes = new Recipes(this);
	}

	public void onDisable() {}

	private void regExecutor(Object executor, String name) {
		PluginCommand command = getCommand(name);
		if (command == null) logger.warning("На сервере нет команды: "+name);
		else {
			command.setExecutor((CommandExecutor) executor);
			command.setTabCompleter((TabCompleter) executor);
		}
	}

	public boolean airOrNull(ItemStack item) {
		if (item == null) return true;
		return item.getType() == AIR;
	}

	public boolean airOrNull(Block block) {
		if (block == null) return true;
		return block.getType() == AIR;
	}
}
