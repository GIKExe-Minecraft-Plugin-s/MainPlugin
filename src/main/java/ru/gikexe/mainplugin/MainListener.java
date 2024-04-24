package ru.gikexe.mainplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

public class MainListener implements Listener {
	MainPlugin plugin;
	List<Material> harvested_crops = List.of(
					Material.WHEAT,
					Material.CARROTS,
					Material.POTATOES,
					Material.BEETROOTS,
					Material.MELON,
					Material.PUMPKIN
	);

	public MainListener(MainPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void on(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Player player = event.getPlayer();
		World world = player.getWorld();
		ItemStack item = event.getItem();
		Block block = event.getClickedBlock();
		if (block == null) return;
		if (!harvested_crops.contains(block.getType())) return;

		Collection<ItemStack> drop = block.getDrops(item, player);
		Location loc = block.getLocation().clone().add(0.5, 0.0, 0.5);
		if (block.getBlockData() instanceof Ageable) {
			Ageable data = (Ageable) block.getBlockData();
			if (data.getAge() < data.getMaximumAge()) return;
			data.setAge(0);
			block.setBlockData(data);
			world.playSound(loc, Sound.BLOCK_CROP_BREAK, 100.0f, 1.0f);
		} else {
			if (item != null && item.getType().isBlock()) return;
			block.setType(Material.AIR);
			world.playSound(loc, Sound.BLOCK_WOOD_BREAK, 100.0f, 1.0f);
		}
		for (ItemStack toSpawn : drop) world.dropItem(loc, toSpawn).setVelocity(new Vector(0, 0, 0));
		event.setUseInteractedBlock(Event.Result.DENY);
		event.setUseItemInHand(Event.Result.DENY);
	}

//	@EventHandler
//	public void on(BlockBreakEvent event) {
//		if (event.isCancelled()) return;
//		Player player = event.getPlayer();
//		Block block = event.getBlock();
//		ItemStack item = player.getInventory().getItemInMainHand();
//
//		if (
//			!block.getDrops(item, player).isEmpty() &&
//			item.getType() != Material.TRIDENT &&
//			item.containsEnchantment(Enchantment.LOYALTY) &&
//			player.isSneaking()
//		) {
//			Material _type = block.getType();
//			Integer _counter = 0;
//		}
//	}
}
