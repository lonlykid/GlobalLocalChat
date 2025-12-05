package org.chat.globalLocalChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class GlobalLocalChat extends JavaPlugin implements Listener {

    private int chatRadius;
    private TextColor globalPrefixColor;
    private TextColor localPrefixColor;
    private TextColor joinPrefixColor;
    private TextColor quitPrefixColor;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("GlobalLocalChat включен!");
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();

        config.addDefault("chat.local-radius", 150);
        config.addDefault("prefix-colors.global", "#00AAFF"); // Синий
        config.addDefault("prefix-colors.local", "#00FF00"); // Зеленый
        config.addDefault("prefix-colors.join", "#00FF00"); // Зеленый
        config.addDefault("prefix-colors.quit", "#FF5555"); // Красный
        config.options().copyDefaults(true);
        saveConfig();

        chatRadius = config.getInt("chat.local-radius");

        globalPrefixColor = TextColor.fromHexString(Objects.requireNonNull(config.getString("prefix-colors.global")));
        localPrefixColor = TextColor.fromHexString(Objects.requireNonNull(config.getString("prefix-colors.local")));
        joinPrefixColor = TextColor.fromHexString(Objects.requireNonNull(config.getString("prefix-colors.join")));
        quitPrefixColor = TextColor.fromHexString(Objects.requireNonNull(config.getString("prefix-colors.quit")));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player sender = event.getPlayer();
        String message = event.getMessage();
        String displayName = sender.getName();

        if (message.startsWith("!")) {
            // Глобальный чат
            sendGlobalMessage(sender, message.substring(1).trim(), displayName);
        } else {
            // Локальный чат
            sendLocalMessage(sender, message, displayName);
        }
    }

    private void sendGlobalMessage(Player sender, String message, String displayName) {
        Component prefix = Component.text("[г] ")
                .color(globalPrefixColor);
        Component fullMessage = prefix
                .append(Component.text(displayName + ": "))
                .append(Component.text(message));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(fullMessage);
        }
    }

    private void sendLocalMessage(Player sender, String message, String displayName) {
        Component prefix = Component.text("[л] ")
                .color(localPrefixColor);
        Component fullMessage = prefix
                .append(Component.text(displayName + ": "))
                .append(Component.text(message));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(sender.getWorld()) &&
                    player.getLocation().distance(sender.getLocation()) <= chatRadius) {
                player.sendMessage(fullMessage);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null); // Отключаем стандартное сообщение

        Component prefix = Component.text("[+] ")
                .color(joinPrefixColor);
        Component joinMessage = prefix
                .append(Component.text(event.getPlayer().getName()));

        Bukkit.getServer().sendMessage(joinMessage);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(null);

        Component prefix = Component.text("[-] ")
                .color(quitPrefixColor);
        Component quitMessage = prefix
                .append(Component.text(event.getPlayer().getName()));

        Bukkit.getServer().sendMessage(quitMessage);
    }

    @Override
    public void onDisable() {
        getLogger().info("GlobalLocalChat выключен!");
    }
}