package ru.gikexe.mainplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static org.bukkit.Material.*;

public class Recipes {
	MainPlugin plugin;
	Server server;

	public Recipes(MainPlugin plugin) {
		this.plugin = plugin;
		this.server = plugin.server;
		ItemStack item;
		ItemMeta im;
		NamespacedKey key;
		ShapedRecipe recipe;

		key = new NamespacedKey(plugin, "craft_enchanted_golden_apple");
		item = new ItemStack(ENCHANTED_GOLDEN_APPLE);
		im = item.getItemMeta();
		im.lore(List.of(Component.text("На вид золотое, но на вкус алмазное...", GRAY).decoration(ITALIC, false)));
		item.setItemMeta(im);
		recipe = new ShapedRecipe(key, item);
		recipe.shape("bbb", "bab", "bbb");
		recipe.setIngredient('b', DIAMOND_BLOCK);
		recipe.setIngredient('a', GOLDEN_APPLE);
		server.addRecipe(recipe);
	}
}
