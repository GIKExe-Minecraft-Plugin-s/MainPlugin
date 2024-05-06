package ru.gikexe.mainplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class CoinExecutor implements CommandExecutor, TabExecutor {
	MainPlugin plugin = MainPlugin.me;
	Component prefix = MainPlugin.me.prefix;
	Server server = MainPlugin.me.server;

	Component msgOtherPlayerNotFound = prefix
		.append(Component.text("Игрок не найден", RED));
	Component msgAdminOnly = prefix
		.append(Component.text("Команда только для администраторов", RED));
	Component msgNotFloat = prefix
		.append(Component.text("Аргумент не является числом с плавающей точкой", RED));

	private static double getScore(Player player) {
		return MainPlugin.me.listener.getScore(MainPlugin.me.listener.coin, player);
	}

	private static void setScore(Player player, double value) {
		MainPlugin.me.listener.setScore(MainPlugin.me.listener.coin, player, value);
	}

	private static void addScore(Player player, double value) {
		MainPlugin.me.listener.addScore(MainPlugin.me.listener.coin, player, value);
	}

	public boolean onCommand(
		@NotNull CommandSender sender,
		@NotNull Command command,
		@NotNull String alias,
		String[] args
	) {
		if (!(sender instanceof Player player)) return false;

		if (args.length >= 1) {
			if (args[0].equals("pay") && args.length >= 3) {
				Player otherPlayer = server.getPlayer(args[1]);
				if (otherPlayer == null) {
					player.sendMessage(msgOtherPlayerNotFound); return true;
				} else {
					if (otherPlayer.getName().equals(player.getName())) {
						player.sendMessage(prefix
							.append(Component.text("нахуя себе отправлять? бан хочешь?", RED))
						); return true;
					}
					double otherValue;
					try {
						if (args[2].equals("all")) otherValue = getScore(player);
						else otherValue = Double.parseDouble(args[2]);
					} catch (NumberFormatException e) {
						player.sendMessage(msgNotFloat); return true;
					}
					if (otherValue < 0.001) {
						player.sendMessage(prefix
							.append(Component.text("Нельзя отправить меньше 0.001$", RED))
						); return true;
					}
					if (otherValue > getScore(player)) {
						player.sendMessage(prefix
							.append(Component.text("У тебя не хватает $", RED))
						); return true;
					}
					addScore(player, -otherValue);
					addScore(otherPlayer, otherValue);
					player.sendMessage(prefix
						.append(Component.text("Отправлено ", WHITE))
						.append(Component.text(otherValue+"$", GREEN))
						.append(Component.text(" игроку ", WHITE))
						.append(Component.text(otherPlayer.getName(), YELLOW))
					);
					otherPlayer.sendMessage(prefix
						.append(Component.text("Игрок ", WHITE))
						.append(Component.text(player.getName(), YELLOW))
						.append(Component.text(" отправил тебе ", WHITE))
						.append(Component.text(otherValue+"$", GREEN))
					);
				}
			} else if (List.of("add", "set").contains(args[0]) && args.length >= 3) {
				if (!sender.isOp()) {player.sendMessage(msgAdminOnly); return true;}
				Player otherPlayer = server.getPlayer(args[1]);
				if (otherPlayer == null) {
					player.sendMessage(msgOtherPlayerNotFound); return true;
				} else {
					double value;
					try {
						value = Double.parseDouble(args[2]);
					} catch (NumberFormatException e) {
						player.sendMessage(msgNotFloat); return true;
					}
					if (args[0].equals("add")) {
						if (value > -0.001 && value < 0.001) {
							player.sendMessage(prefix
								.append(Component.text("Нельзя добавить меньше ±0.001$", RED))
							); return true;
						}
						addScore(player, value);
					} else setScore(otherPlayer, value);
				}
			} else if (args[0].equals("get") && args.length >= 2) {
				if (!sender.isOp()) {player.sendMessage(msgAdminOnly); return true;}
				Player otherPlayer = server.getPlayer(args[1]);
				if (otherPlayer == null) {
					player.sendMessage(msgOtherPlayerNotFound); return true;
				} else {
					double value = getScore(otherPlayer);
					player.sendMessage(prefix
						.append(Component.text("У игрока ", WHITE))
						.append(Component.text(otherPlayer.getName(), YELLOW))
						.appendSpace()
						.append(Component.text(value+"$", GREEN))
					);
				}
			}
		} else {
			double value = getScore(player);
			player.sendMessage(prefix
				.append(Component.text("У тебя ", WHITE))
				.append(Component.text(value+"$", GREEN))
			);
		}
		return true;
	}


	public @Nullable List<String> onTabComplete(
		@NotNull CommandSender sender,
		@NotNull Command command,
		@NotNull String alias,
		String[] args
	) {
		List<String> res = new java.util.ArrayList<>();
		if (args.length <= 1) {
			res.add("pay");
			if (sender.isOp()) res.addAll(List.of("set", "get", "add"));
			return res;
		}
		else if (args.length == 2 && List.of("set", "get", "add").contains(args[0])) {
			if (sender.isOp()) return null;
		}
		else if (args.length == 2 && args[0].equals("pay")) {
			return null;
		}
		else if (args.length == 3 && args[0].equals("pay")) {
			return List.of("all");
		}
		return List.of();
	}
}
