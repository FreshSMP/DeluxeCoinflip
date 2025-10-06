/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.api;

import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import net.zithium.deluxecoinflip.storage.PlayerData;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface DeluxeCoinflipAPI {

    /**
     * Register a custom economy provider with a required plugin.
     * We will check if the plugin is enabled.
     *
     * @param provider       The economy provider
     * @param requiredPlugin The plugin required
     */
    void registerEconomyProvider(EconomyProvider provider, String requiredPlugin);

    /**
     * Register a custom stat provider to add custom placeholders.
     * This allows external plugins to add their own stats that can be displayed
     * in DeluxeCoinflip menus and messages using {PLACEHOLDER_NAME} format.
     * 
     * Example: DeluxeLifestealBridge can register a provider to track
     * {HEARTS_WIN}, {HEARTS_LOST}, and {HEARTS_BET}
     *
     * @param provider The custom stat provider to register
     * @return true if registered successfully, false if a provider with the same ID already exists
     */
    boolean registerCustomStatProvider(CustomStatProvider provider);

    /**
     * Unregister a custom stat provider.
     *
     * @param providerId The ID of the provider to unregister
     * @return true if a provider was removed, false otherwise
     */
    boolean unregisterCustomStatProvider(String providerId);

    /**
     * Fetch player data
     *
     * @param player The player to search
     * @return Optional of player data, represents if they are loaded in cache
     */
    Optional<PlayerData> getPlayerData(Player player);
}
