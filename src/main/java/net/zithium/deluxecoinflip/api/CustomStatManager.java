/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager for custom stat providers.
 * Handles registration and placeholder replacement for external plugin stats.
 */
public class CustomStatManager {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Z_]+)}");
    
    private final List<CustomStatProvider> providers = new ArrayList<>();
    private final Logger logger;

    public CustomStatManager(Logger logger) {
        this.logger = logger;
    }

    /**
     * Register a custom stat provider.
     * 
     * @param provider The provider to register
     * @return true if registered successfully, false if a provider with the same ID already exists
     */
    public boolean registerProvider(@NotNull CustomStatProvider provider) {
        // Check for duplicate provider IDs
        for (CustomStatProvider existingProvider : providers) {
            if (existingProvider.getProviderId().equalsIgnoreCase(provider.getProviderId())) {
                logger.log(Level.WARNING, "Custom stat provider with ID '" + provider.getProviderId() 
                        + "' is already registered. Skipping duplicate registration.");
                return false;
            }
        }
        
        providers.add(provider);
        logger.log(Level.INFO, "Registered custom stat provider: " + provider.getProviderId());
        return true;
    }

    /**
     * Unregister a custom stat provider.
     * 
     * @param providerId The ID of the provider to unregister
     * @return true if a provider was removed, false otherwise
     */
    public boolean unregisterProvider(@NotNull String providerId) {
        boolean removed = providers.removeIf(p -> p.getProviderId().equalsIgnoreCase(providerId));
        if (removed) {
            logger.log(Level.INFO, "Unregistered custom stat provider: " + providerId);
        }
        return removed;
    }

    /**
     * Get all registered providers.
     * 
     * @return An unmodifiable view of registered providers
     */
    @NotNull
    public List<CustomStatProvider> getProviders() {
        return new ArrayList<>(providers);
    }

    /**
     * Replace all custom placeholders in a string with their values.
     * Iterates through all registered providers to find matching placeholders.
     * 
     * @param player The player for whom to resolve placeholders
     * @param text   The text containing placeholders
     * @return The text with all custom placeholders replaced
     */
    @NotNull
    public String replacePlaceholders(@NotNull Player player, @NotNull String text) {
        if (providers.isEmpty()) {
            return text;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(1); // Get the text between {}
            String replacement = getPlaceholderValue(player, placeholder);
            
            // If we found a replacement, use it; otherwise keep the original placeholder
            if (replacement != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Get the value for a specific placeholder from registered providers.
     * 
     * @param player      The player to get the stat for
     * @param placeholder The placeholder identifier (without braces)
     * @return The value from the first provider that handles it, or null if none do
     */
    @Nullable
    public String getPlaceholderValue(@NotNull Player player, @NotNull String placeholder) {
        for (CustomStatProvider provider : providers) {
            try {
                String value = provider.getStatValue(player, placeholder);
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error getting stat value from provider '" 
                        + provider.getProviderId() + "' for placeholder '" + placeholder + "'", e);
            }
        }
        return null;
    }
}
