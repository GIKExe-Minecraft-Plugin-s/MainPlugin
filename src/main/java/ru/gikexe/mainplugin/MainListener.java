package ru.gikexe.mainplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public class MainListener implements Listener {
	MainPlugin plugin;
	@Nullable Objective logic;
	@Nullable Objective decor;
	Integer cerf_x = 50;
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

    // временный код
		logic = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("farmer_event_logic");
		if (logic == null) plugin.getLogger().warning("на сервере нет задачи farmer_event_logic!");

		decor = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("farmer_event");
		if (decor == null) plugin.getLogger().warning("на сервере нет задачи farmer_event!");
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

		// временный код
		if (logic == null) return;
		Score score = logic.getScore(player);
		if (score.getScore() < 0) return;
		score.setScore(score.getScore()+1);
		if (decor != null) {
			decor.getScore(player).setScore(decor.getScore(player).getScore()+1);
		}
		if (score.getScore() >= cerf_x) {
			score.setScore(score.getScore()-cerf_x);
			ItemStack cerf = new ItemStack(Material.PAPER);
			ItemMeta im = cerf.getItemMeta();
			im.displayName(Component.text("Сертификат", DARK_PURPLE).decoration(ITALIC, false));
			im.lore(List.of(Component.text(cerf_x+" очков", GREEN).decoration(ITALIC, false),
							Component.text("принадлежит игроку "+player.getName(), GRAY).decoration(ITALIC, false)));
			cerf.setItemMeta(im);
			player.getInventory().addItem(cerf);
		}
	}
	
	// временный код
	@EventHandler
	public void on(PlayerDeathEvent event) {
		Player player = event.getPlayer();
		if (logic == null) return;
		logic.getScore(player).setScore(0);
	}
}
