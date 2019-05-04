package example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ClickableChatFunctions implements Listener {
	
	private Map<UUID, ClickActionInternal> que = new HashMap<>();

	private ClickableChatFunctions() {}

	private static ClickableChatFunctions instance;

	public static ClickableChatFunctions getInstance() {
		return instance == null ? (instance = new ClickableChatFunctions()) : instance;
	}
	
	public void onEnable(Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		plugin.getLogger().info("Eric's ClickableChat Utility enabled!");
	}

	
	public interface PlayerClickAction {
		/**
         * Executes a fuction when the player clicks it
         * @param player The player who clicked
         */
		void run(Player player);
	}


	private class ClickActionInternal {

		private UUID playerId;
		private PlayerClickAction action;
		private boolean expire;

		private ClickActionInternal(UUID playerId, PlayerClickAction action, boolean expire) {
			this.playerId = playerId;
			this.action = action;
			this.expire = expire;
		}

		private UUID getPlayerId() {
			return playerId;
		}


		private PlayerClickAction getAction() {
			return action;
		}


		private boolean shouldExpire() {
			return expire;
		}
	}
	
	/**
     * Sends a clickable action message to a player
     * @param player The player to send the message to
     * @param msg The message to send to the player
     * @param expire Whether the action should expire after being used once
     * @param action The action to execute when the player clicks the message
     */
	public void sendMessage(@Nonnull Player player, @Nonnull String msg, @Nonnull boolean expire, @Nonnull PlayerClickAction action) {
		sendMessage(player, new TextComponent(msg), expire, action);
	}

	/**
     * Sends a clickable action message to a player
     * @param player The player to send the message to
     * @param component The text component to send to the player
     * @param expire Whether the action should expire after being used once
     * @param action The action to execute when the player clicks the message
     */
	public void sendMessage(@Nonnull Player player, @Nonnull TextComponent component, @Nonnull boolean expire, @Nonnull PlayerClickAction action) {
		sendMessage(player, new TextComponent[] {component}, expire, action);
	}

	/**
     * Sends clickable messages to a player
     * @param player The player to send the message to
     * @param components The text components to send to the player
     * @param expire Whether the action should expire after being used once
     * @param action The action to execute when the player clicks the message
     */
	public void sendMessage(@Nonnull Player player, @Nonnull TextComponent[] components, @Nonnull boolean expire, @Nonnull PlayerClickAction action) {

		UUID id = UUID.randomUUID();
		while (que.keySet().contains(id)) {
			id = UUID.randomUUID(); //this should never happen but like just in case
		}

		que.put(id, new ClickActionInternal(player.getUniqueId(), action, expire));

		for (BaseComponent component : components) {
			component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + id.toString()));
		}

		player.spigot().sendMessage(components);
	}

	/**
     * Remove all the clickable messages from the que that are associated with a player
     * @param player The player who's actions should be removed
     */
	public void removeFromQue(Player player) {
		for (Map.Entry<UUID, ClickActionInternal> entry : que.entrySet()) {
			if (entry.getValue().getPlayerId().equals(player.getUniqueId())) {
				que.remove(entry.getKey());
			}
		}
	}


	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		removeFromQue(event.getPlayer());
	}
	
	@EventHandler
	public void commandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		UUID id;

		try {
			id = UUID.fromString(event.getMessage().substring(1)); //all commands start with a /
		}
		catch (IllegalArgumentException ignored) {
			return;
		}
		
		ClickActionInternal data = que.get(id);

		if (data == null) {
			return;
		}

		event.setCancelled(true);

		Player player = event.getPlayer();

		if (player.getUniqueId().equals(data.playerId)) {
			data.getAction().run(player);
			if (data.shouldExpire()) {
				que.remove(id);
			}
		}
	}
}
