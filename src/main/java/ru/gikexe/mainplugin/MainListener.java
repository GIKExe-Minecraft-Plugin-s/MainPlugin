package ru.gikexe.mainplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MainListener implements Listener {
	MainPlugin plugin;

	public MainListener(MainPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void on(BlockBreakEvent event) {

	}
}
