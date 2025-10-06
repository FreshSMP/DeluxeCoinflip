/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for providing custom statistics that can be used in placeholders.
 * External plugins can implement this interface to add their own stat tracking
 * to DeluxeCoinflip's placeholder system.
 * 
 * Example: DeluxeLifestealBridge could track HEARTS_WIN, HEARTS_LOST, HEARTS_BET
 */
public interface CustomStatProvider {

    /**
     * Get the value for a custom placeholder for the given player.
     * 
     * @param player      The player to get the stat for
     * @param placeholder The placeholder identifier (without braces), e.g., "HEARTS_WIN"
     * @return The value to replace the placeholder with, or null if this provider doesn't handle it
     */
    @Nullable
    String getStatValue(@NotNull Player player, @NotNull String placeholder);

    /**
     * Get the unique identifier for this stat provider.
     * This helps prevent conflicts between multiple providers.
     * 
     * @return A unique identifier for this provider (e.g., "deluxelifesteal")
     */
    @NotNull
    String getProviderId();
}
