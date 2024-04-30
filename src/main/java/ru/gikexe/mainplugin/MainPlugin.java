package ru.gikexe.mainplugin;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MainPlugin extends JavaPlugin {
	public Server server;
	public PluginManager pm;
	public Recipes recipes;

	public void onEnable() {
		server = getServer();
		pm = server.getPluginManager();

		pm.registerEvents(new MainListener(this), this);

		recipes = new Recipes(this);
	}

	public void onDisable() {

	}
}
