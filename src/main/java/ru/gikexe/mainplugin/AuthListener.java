package ru.gikexe.mainplugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;

public class AuthListener implements Listener {
	MainPlugin plugin;

	Component prefix = Component.text(" -> ", GRAY);
	List<Component> msg = new ArrayList<>(List.of(
					// 0
					prefix.append(Component.text("Подключился", YELLOW)),
					// 1
					prefix.append(Component.text("Вошёл", GREEN)),
					// 2
					prefix.append(Component.text("Отключился", YELLOW)),
					// 3
					prefix.append(Component.text("Введи пароль для ", WHITE))
					.append(Component.text("регистрации", RED)),
					// 4
					prefix.append(Component.text("Ты ", WHITE))
					.append(Component.text("зарегистрирован", GREEN))
					.append(Component.text(", твой пароль: ", WHITE)),
					// 5
					prefix.append(Component.text("Введи пароль для ", WHITE))
					.append(Component.text("входа", RED)),
					// 6
					prefix.append(Component.text("Вход выполнен", GREEN)),
					// 7
					prefix.append(Component.text("Неверный пароль", RED)),
					// 8
					prefix.append(Component.text("Войди", RED))
					.append(Component.text(", чтобы использовать это", WHITE))

	));


	public AuthListener(MainPlugin plugin) {
		this.plugin = plugin;
	}


	public void checkData(String uuid) {
		if (plugin.players.containsKey(uuid)) return;
		plugin.players.put(uuid, new HashMap<>(Map.of("login", false)));
	}

	public void checkData(Player player) {
		checkData(player.getUniqueId().toString());
	}

	public Map<String, Object> getData(String uuid) {
		checkData(uuid);
		return (Map<String, Object>) plugin.players.get(uuid);
	}

	public Map<String, Object> getData(Player player) {
		return getData(player.getUniqueId().toString());
	}

	public void setPerm(Player player, String key, Boolean value) {
		player.addAttachment(plugin, key, value);
		player.recalculatePermissions();
	}

	@EventHandler
	public void on(PlayerLoginEvent event) {
		String name = event.getPlayer().getName();
		if (!name.matches("[a-zA-Zа-яА-Я0-9_]+")) {
			event.disallow(KICK_OTHER, Component.text("недопустимый ник", RED)); return;
		}
		if (Bukkit.getServer().getPlayer(name) != null) {
			event.disallow(KICK_OTHER, Component.text("игрок с ником \""+name+"\" уже есть на сервере", RED));
		}
	}

	@EventHandler
	public void on(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		plugin.server.broadcast(Component.text(player.getName(), WHITE).append(msg.get(0)), "gikexe.auth");
		player.sendMessage(msg.get(getData(player).get("pass") == null ? 3 : 5));
		event.joinMessage(null);
	}

	@EventHandler
	public void on(AsyncChatEvent event) {
		Player player = event.getPlayer();
		String text = ((TextComponent) event.message()).content();
		if ((boolean) getData(player).get("login")) return;
		if (getData(player).get("pass") == null) {
			getData(player).put("pass", text);
			getData(player).replace("login", true);
			player.sendMessage(msg.get(4).append(Component.text(text, GREEN)));
			plugin.server.broadcast(Component.text(player.getName(), WHITE).append(msg.get(1)), "gikexe.auth");
			setPerm(player, "gikexe.auth", true);

		} else if (getData(player).get("pass").equals(text)) {
			getData(player).replace("login", true);
			player.sendMessage(msg.get(6));
			plugin.server.broadcast(Component.text(player.getName(), WHITE).append(msg.get(1)), "gikexe.auth");
			setPerm(player, "gikexe.auth", true);

		} else {
			player.sendMessage(msg.get(7));
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void on(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		getData(player).replace("login", false);
		setPerm(player, "gikexe.auth", false);
		plugin.server.broadcast(Component.text(player.getName(), WHITE).append(msg.get(0)), "gikexe.auth");
		event.quitMessage(null);
	}

	//CANSEL NON LOGIN
	@EventHandler
	public void on(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if ((boolean) getData(player).get("login")) return;
		player.sendMessage(msg.get(8));
		event.setCancelled(true);
	}

}
