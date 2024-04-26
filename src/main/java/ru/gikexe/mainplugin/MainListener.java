package ru.gikexe.mainplugin;

import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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
import static org.bukkit.Material.*;
import static org.bukkit.enchantments.Enchantment.SILK_TOUCH;

public class MainListener implements Listener {
	MainPlugin plugin;
	@Nullable Objective logic;
	@Nullable Objective decor;
	Integer cert_x = 50;
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

	public void getCertificate(Player player) {
		ItemStack certificate = new ItemStack(Material.PAPER);
		ItemMeta im = certificate.getItemMeta();
		im.displayName(Component.text("Сертификат", DARK_PURPLE).decoration(ITALIC, false));
		im.lore(List.of(Component.text(cert_x+" очков", GREEN).decoration(ITALIC, false),
						Component.text("принадлежит игроку "+player.getName(), GRAY).decoration(ITALIC, false)));
		certificate.setItemMeta(im);
		player.getInventory().addItem(certificate);
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
		if (score.getScore() >= cert_x) {
			score.setScore(score.getScore()- cert_x);
			getCertificate(player);
		}
	}

	// временный код
	@EventHandler
	public void on(PlayerDeathEvent event) {
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		player.sendMessage(Component.text(String.format("Вы умерли на: %s, %s, %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), RED));
		if (logic == null) return;
		logic.getScore(player).setScore(0);
	}

	@EventHandler
	public void on(PlayerDeepSleepEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		player.setHealth(Math.min(player.getHealth()+1.0, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
	}

	@EventHandler
	public void on(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		Block block = event.getBlock();
		Player player = event.getPlayer();
		World world = player.getWorld();
		ItemStack item = player.getInventory().getItemInMainHand();

		if (
			block.getType() == SPAWNER &&
			List.of(IRON_PICKAXE, DIAMOND_PICKAXE, NETHERITE_PICKAXE).contains(item.getType()) &&
			item.containsEnchantment(SILK_TOUCH)
		) {
			Location loc = block.getLocation().clone().add(0.5, 0.5, 0.5);
			world.dropItem(loc, new ItemStack(SPAWNER));

			EntityType type = ((CreatureSpawner) block.getState()).getSpawnedType();
			if (type != null) {
				Material material = Material.getMaterial(type.name()+"_SPAWN_EGG");
				if (material != null) world.dropItem(loc, new ItemStack(material));
			}
			getCertificate(player);
			event.setExpToDrop(0);
			player.getInventory().setItemInMainHand(null);
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 100.0f, 1.0f);
		}

	}
}
