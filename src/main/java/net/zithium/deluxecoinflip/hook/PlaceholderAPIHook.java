/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.zithium.deluxecoinflip.DeluxeCoinflipPlugin;
import net.zithium.deluxecoinflip.storage.PlayerData;
import net.zithium.deluxecoinflip.storage.StorageManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final DeluxeCoinflipPlugin plugin;
    private final StorageManager storageManager;

    public PlaceholderAPIHook(DeluxeCoinflipPlugin plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    // We are aware that this is deprecated.
    @Override
    @SuppressWarnings("deprecation")
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "deluxecoinflip";
    }

    // We are aware that this is deprecated.
    @Override
    @SuppressWarnings("deprecation")
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        Optional<PlayerData> playerDataOptional = storageManager.getPlayer(player.getUniqueId());
        if (playerDataOptional.isEmpty()) {
            return "N/A";
        }

        PlayerData playerData = playerDataOptional.get();

        // Check default placeholders first
        String defaultValue = switch (identifier) {
            case "games_played" -> String.valueOf(playerData.getTotalGames());
            case "wins" -> String.valueOf(playerData.getWins());
            case "win_percentage" -> String.valueOf(playerData.getWinPercentage());
            case "losses" -> String.valueOf(playerData.getLosses());
            case "profit" -> String.valueOf(playerData.getProfit());
            case "profit_formatted" -> String.valueOf(playerData.getProfitFormatted());
            case "total_losses" -> String.valueOf(playerData.getTotalLosses());
            case "total_losses_formatted" -> String.valueOf(playerData.getTotalLossesFormatted());
            case "total_gambled" -> String.valueOf(playerData.getTotalGambled());
            case "total_gambled_formatted" -> String.valueOf(playerData.getTotalGambledFormatted());
            case "display_broadcast_messages" -> String.valueOf(playerData.isDisplayBroadcastMessages());
            case "total_games" -> String.valueOf(plugin.getGameManager().getCoinflipGames().size());
            default -> null;
        };

        // If default placeholder found, return it
        if (defaultValue != null) {
            return defaultValue;
        }

        // Try custom stat providers (converts identifier to uppercase for consistency)
        String customValue = plugin.getCustomStatManager().getPlaceholderValue(player, identifier.toUpperCase());
        return customValue != null ? customValue : null;
    }
}
