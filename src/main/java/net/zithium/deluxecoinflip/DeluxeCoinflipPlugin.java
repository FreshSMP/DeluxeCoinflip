/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip;

import co.aikar.commands.PaperCommandManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.nahu.scheduler.wrapper.FoliaWrappedJavaPlugin;
import net.zithium.deluxecoinflip.api.CustomStatManager;
import net.zithium.deluxecoinflip.api.DeluxeCoinflipAPI;
import net.zithium.deluxecoinflip.cache.ActiveGamesCache;
import net.zithium.deluxecoinflip.command.CoinflipCommand;
import net.zithium.deluxecoinflip.config.ConfigHandler;
import net.zithium.deluxecoinflip.config.ConfigType;
import net.zithium.deluxecoinflip.config.Messages;
import net.zithium.deluxecoinflip.economy.EconomyManager;
import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import net.zithium.deluxecoinflip.game.CoinflipGame;
import net.zithium.deluxecoinflip.game.GameManager;
import net.zithium.deluxecoinflip.hook.DiscordHook;
import net.zithium.deluxecoinflip.hook.PlaceholderAPIHook;
import net.zithium.deluxecoinflip.listener.PlayerChatListener;
import net.zithium.deluxecoinflip.listener.game.ActiveGameQuitListener;
import net.zithium.deluxecoinflip.listener.game.GameQuitListener;
import net.zithium.deluxecoinflip.menu.DupeProtection;
import net.zithium.deluxecoinflip.menu.InventoryManager;
import net.zithium.deluxecoinflip.storage.PlayerData;
import net.zithium.deluxecoinflip.storage.StorageManager;
import net.zithium.deluxecoinflip.storage.handler.GameShutdownProvider;
import net.zithium.deluxecoinflip.storage.handler.impl.DefaultGameShutdownProvider;
import net.zithium.deluxecoinflip.utility.ItemStackBuilder;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DeluxeCoinflipPlugin extends FoliaWrappedJavaPlugin implements DeluxeCoinflipAPI {

    private static final int BSTATS_PLUGIN_ID = 20887;

    private static DeluxeCoinflipPlugin instance;

    public static DeluxeCoinflipPlugin getInstance() {
        return instance;
    }

    private Map<ConfigType, ConfigHandler> configMap;
    private StorageManager storageManager;
    private GameManager gameManager;
    private ActiveGamesCache activeGamesCache;
    private InventoryManager inventoryManager;
    private EconomyManager economyManager;
    private DiscordHook discordHook;
    private CustomStatManager customStatManager;

    private Cache<UUID, CoinflipGame> listenerCache;

    private GameShutdownProvider shutdownProvider;

    @Override
    public void onEnable() {
        instance = this;
        final long startNanos = System.nanoTime();

        // We are fully aware that both of these are deprecated; however, they are necessary.
        @SuppressWarnings("deprecation")
        final String pluginVersion = getDescription().getVersion();
        // get(0) is required since getFirst is not supported for getAuthors.
        @SuppressWarnings("deprecation")
        final String pluginAuthor = getDescription().getAuthors().isEmpty() ? "Unknown" : getDescription().getAuthors().get(0);

        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, " __ __    DeluxeCoinflip v" + pluginVersion);
        getLogger().log(Level.INFO, "/  |_     Author: " + pluginAuthor);
        getLogger().log(Level.INFO, "\\_ |      (c) Zithium Studios 2021 - 2025. All rights reserved.");
        getLogger().log(Level.INFO, "");

        enableMetrics();

        listenerCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(500).build();

        // Register configurations
        configMap = new EnumMap<>(ConfigType.class);
        registerConfig(ConfigType.CONFIG);
        registerConfig(ConfigType.MESSAGES);
        Messages.setConfiguration(configMap.get(ConfigType.MESSAGES).getConfig());

        // Initialize economy manager early
        economyManager = new EconomyManager(this);
        economyManager.onEnable();

        // Load storage
        storageManager = new StorageManager(this);
        try {
            storageManager.onEnable();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "There was an issue attempting to load the storage handler.", ex);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        discordHook = new DiscordHook(this);
        
        customStatManager = new CustomStatManager(getLogger());

        gameManager = new GameManager(this);

        activeGamesCache = new ActiveGamesCache();

        inventoryManager = new InventoryManager();
        inventoryManager.load(this);
        new DupeProtection(this);
        ItemStackBuilder.setPlugin(this);

        shutdownProvider = new DefaultGameShutdownProvider(this);

        final List<String> aliases = getConfigHandler(ConfigType.CONFIG).getConfig().getStringList("settings.command_aliases");

        final PaperCommandManager paperCommandManager = new PaperCommandManager(this);
        paperCommandManager.getCommandCompletions().registerAsyncCompletion(
                "providers",
                completionContext -> economyManager.getEconomyProviders()
                        .values()
                        .stream()
                        .map(EconomyProvider::getDisplayName)
                        .toList()
        );
        paperCommandManager.getCommandReplacements().addReplacement("main", "coinflip|" + String.join("|", aliases));
        paperCommandManager.registerCommand(
                new CoinflipCommand(this).setExceptionHandler((command, registeredCommand, sender, args, throwable) -> {
                    Messages.NO_PERMISSION.send(sender.getIssuer());
                    return true;
                })
        );

        new PlayerChatListener(this);
        new ActiveGameQuitListener(this);
        new GameQuitListener(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().log(Level.INFO, "Hooked into PlaceholderAPI successfully");
        }

        final long loadMilliseconds = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, "Successfully loaded in " + loadMilliseconds + "ms");
        getLogger().log(Level.INFO, "");
    }

    private void enableMetrics() {
        if (getConfig().getBoolean("metrics", true)) {
            getLogger().log(Level.INFO, "Loading bStats metrics...");
            new Metrics(this, BSTATS_PLUGIN_ID);
        } else {
            getLogger().log(Level.INFO, "Metrics are disabled.");
        }
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            shutdownProvider.shutdownAll();
            storageManager.onDisable(true);
        }
    }

    public void reload() {
        configMap.values().forEach(ConfigHandler::reload);
        Messages.setConfiguration(configMap.get(ConfigType.MESSAGES).getConfig());

        inventoryManager.load(this);
        economyManager.onEnable();
    }

    private void registerConfig(ConfigType type) {
        final String fileName = type.name().toLowerCase(Locale.ROOT);
        ConfigHandler handler = new ConfigHandler(this, fileName);
        handler.saveDefaultConfig();
        configMap.put(type, handler);
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public ConfigHandler getConfigHandler(ConfigType type) {
        return configMap.get(type);
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ActiveGamesCache getActiveGamesCache() {
        return activeGamesCache;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public DiscordHook getDiscordHook() {
        return discordHook;
    }

    public CustomStatManager getCustomStatManager() {
        return customStatManager;
    }

    public Cache<UUID, CoinflipGame> getListenerCache() {
        return listenerCache;
    }

    public NamespacedKey getKey(String key) {
        return new NamespacedKey(this, key);
    }

    // API methods
    @Override
    public void registerEconomyProvider(EconomyProvider provider, String requiredPlugin) {
        economyManager.registerEconomyProvider(provider, requiredPlugin);
    }

    @Override
    public boolean registerCustomStatProvider(net.zithium.deluxecoinflip.api.CustomStatProvider provider) {
        return customStatManager.registerProvider(provider);
    }

    @Override
    public boolean unregisterCustomStatProvider(String providerId) {
        return customStatManager.unregisterProvider(providerId);
    }

    @Override
    public Optional<PlayerData> getPlayerData(Player player) {
        return storageManager.getPlayer(player.getUniqueId());
    }
}
