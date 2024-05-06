package ru.gikexe.mainplugin;

import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static org.bukkit.Material.*;
import static org.bukkit.enchantments.Enchantment.SILK_TOUCH;
import static org.bukkit.event.Event.Result.DENY;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
import static org.bukkit.event.inventory.InventoryAction.*;

public class MainListener implements Listener {
	MainPlugin plugin;
	Logger logger;

	Scoreboard scoreboard;
	@Nullable Objective logic;
	@Nullable Objective coin;

	Integer cert_x = 50;

	static List<Material> HARVESTED_CROPS = List.of(
		WHEAT,
		CARROTS,
		POTATOES,
		BEETROOTS,
		MELON,
		PUMPKIN
	);

	static List<Material> ALL_SIGN = List.of(
		OAK_WALL_SIGN,
		SPRUCE_WALL_SIGN,
		BIRCH_WALL_SIGN,
		JUNGLE_WALL_SIGN,
		ACACIA_WALL_SIGN,
		DARK_OAK_WALL_SIGN,
		MANGROVE_WALL_SIGN,
		CHERRY_WALL_SIGN,
		BAMBOO_WALL_SIGN,
		CRIMSON_WALL_SIGN,
		WARPED_WALL_SIGN
	);

	public MainListener(MainPlugin plugin) {
		this.plugin = plugin;
		logger = plugin.getLogger();

		scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		logic = scoreboard.getObjective("farmer_event_logic");
		if (logic == null) logger.warning("на сервере нет задачи farmer_event_logic!");

		coin = scoreboard.getObjective("coin");
		if (coin == null) logger.warning("на сервере нет задачи coin!");
	}

	public double getScore(@Nullable Objective objective, Player player) {
		if (objective == null) return 0;
		return ((double) objective.getScore(player).getScore()) / 1000;
	}

	public void setScore(@Nullable Objective objective, Player player, double value) {
		if (objective == null) return;
		objective.getScore(player).setScore((int) (value * 1000));
	}

	public void addScore(@Nullable Objective objective, Player player, double value) {
		if (objective == null) return;
		setScore(objective, player, getScore(objective, player) + value);
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

	private void interactOfSing(PlayerInteractEvent event) {

	}

	private void interactOfCrops(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		World world = player.getWorld();
		ItemStack item = event.getItem();

		Collection<ItemStack> drop = block.getDrops(item, player);
		Location loc = block.getLocation().clone().add(0.5, 0.0, 0.5);
		if (block.getBlockData() instanceof Ageable data) {
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
		event.setUseInteractedBlock(DENY);
		event.setUseItemInHand(DENY);

		double value = getScore(logic, player);
		if (value < 0) return;
		setScore(logic, player, value+1);
		if (value >= cert_x) {
			setScore(logic, player, value - cert_x);
			getCertificate(player);
		}
	}

	@EventHandler
	public void on(PlayerInteractEvent event) {
		if (event.getAction() != RIGHT_CLICK_BLOCK) return;
		if (plugin.airOrNull(event.getClickedBlock())) return;

		Block block = event.getClickedBlock();
		if (ALL_SIGN.contains(block.getType())) interactOfSing(event);
		else if (HARVESTED_CROPS.contains(block.getType())) interactOfCrops(event);
	}

	@EventHandler
	public void on(PlayerDeathEvent event) {
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		player.sendMessage(Component.text(String.format("Вы умерли на: %s, %s, %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), RED));
		setScore(logic, player, 0);
	}

	@EventHandler
	public void on(PlayerDeepSleepEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		player.setHealth(Math.min(player.getHealth()+1.0, maxHealth == null ? 20 : maxHealth.getValue()));
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

	@EventHandler
	public void on(PrepareGrindstoneEvent event) {
		GrindstoneInventory inv = event.getInventory();
		updateBook(inv);
		event.setResult(inv.getResult());
	}

	public List<Enchantment> getEnchants(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		HashMap<Enchantment, Integer> enchantments;
		if (meta == null) return List.of();
		if (item.getType() == ENCHANTED_BOOK)
			enchantments = new LinkedHashMap<>(((EnchantmentStorageMeta) meta).getStoredEnchants());
		else enchantments = new LinkedHashMap<>(meta.getEnchants());
		return enchantments.keySet().stream().toList();
	}

	public @Nullable Enchantment getLastEnchant(ItemStack item) {
		List<Enchantment> enchantments = getEnchants(item);
		return enchantments.isEmpty() ? null : enchantments.get(enchantments.size() - 1);
	}

	public Integer getEnchantLevel(ItemStack item, Enchantment enchantment) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return null;
		if (item.getType() == ENCHANTED_BOOK)
			return ((EnchantmentStorageMeta) meta).getStoredEnchantLevel(enchantment);
		return meta.getEnchantLevel(enchantment);
	}

	public void delLastEnchant(ItemStack item) {
		Enchantment enchantment = getLastEnchant(item);
		if (enchantment == null) return;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		if (item.getType() == ENCHANTED_BOOK)
			((EnchantmentStorageMeta) meta).removeStoredEnchant(enchantment);
		else meta.removeEnchant(enchantment);
		item.setItemMeta(meta);
	}

	public void updateBook(GrindstoneInventory inv) {
		inv.setResult(null);
		ItemStack upItem = inv.getUpperItem();
		ItemStack lowItem = inv.getLowerItem();
		if (upItem == null || lowItem == null) return;
		if (lowItem.getType() != BOOK) return;
		Enchantment enchantment = getLastEnchant(upItem);
		if (enchantment == null) return;

		ItemStack resItem = new ItemStack(ENCHANTED_BOOK);
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) resItem.getItemMeta();
		meta.addStoredEnchant(enchantment, getEnchantLevel(upItem, enchantment), true);
		resItem.setItemMeta(meta);
		inv.setResult(resItem);
	}

	@EventHandler
	public void on(InventoryClickEvent event) {
		if (!(event.getClickedInventory() instanceof GrindstoneInventory inv)) return;

		HumanEntity human = event.getWhoClicked();
		ItemStack cursorItem = event.getCursor();
		ItemStack slotItem = event.getCurrentItem();
		ItemStack upItem = inv.getUpperItem();
		ItemStack lowItem = inv.getLowerItem();

		switch (event.getSlotType()) {
			case CRAFTING -> {
				switch (event.getAction()) {
					case SWAP_WITH_CURSOR, NOTHING -> {
						event.setCurrentItem(cursorItem);
						human.setItemOnCursor(slotItem);
						event.setResult(DENY);
					}
					case PLACE_ALL -> {
						event.setCurrentItem(cursorItem);
						human.setItemOnCursor(null);
						event.setResult(DENY);
					}
					case PLACE_ONE -> {
						ItemStack item1312 = cursorItem.clone();
						item1312.setAmount(1);
						event.setCurrentItem(item1312);
						cursorItem.setAmount(cursorItem.getAmount() - 1);
						human.setItemOnCursor(cursorItem);
						event.setResult(DENY);
					}
				}
			} case RESULT -> {
				if (event.getAction() != PICKUP_ALL) return;
				if (slotItem == null) return;
				if (slotItem.getType() != ENCHANTED_BOOK) return;
				if (!plugin.airOrNull(cursorItem)) return;
				if (upItem == null || lowItem == null) return;

				delLastEnchant(inv.getUpperItem());
				if (upItem.getType() == ENCHANTED_BOOK && getEnchants(upItem).isEmpty())
					inv.setUpperItem(null);
				human.setItemOnCursor(slotItem);
				inv.setResult(null);
				lowItem.setAmount(lowItem.getAmount() - 1);
				if (lowItem.getAmount() <= 0)
					inv.setLowerItem(null);
				event.setResult(DENY);
				updateBook(inv);
			}
		}
	}
}
