# Custom Stats API - Example Implementation

This document shows how to use DeluxeCoinflip's Custom Stats API to add your own placeholders from external plugins.

## Example: DeluxeLifestealBridge

Here's how you would implement custom stat tracking for hearts in your DeluxeLifestealBridge plugin:

### 1. Implement CustomStatProvider

```java
package your.plugin.package;

import net.zithium.deluxecoinflip.api.CustomStatProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LifestealStatProvider implements CustomStatProvider {
    
    private final DeluxeLifestealBridge plugin;
    
    public LifestealStatProvider(DeluxeLifestealBridge plugin) {
        this.plugin = plugin;
    }
    
    @Override
    @Nullable
    public String getStatValue(@NotNull Player player, @NotNull String placeholder) {
        // Get player's lifesteal data from your plugin
        LifestealPlayerData data = plugin.getPlayerData(player);
        if (data == null) {
            return null;
        }
        
        // Handle your custom placeholders
        return switch (placeholder) {
            case "HEARTS_WIN" -> String.valueOf(data.getHeartsWon());
            case "HEARTS_LOST" -> String.valueOf(data.getHeartsLost());
            case "HEARTS_BET" -> String.valueOf(data.getHeartsBet());
            case "HEARTS_NET" -> String.valueOf(data.getHeartsWon() - data.getHeartsLost());
            default -> null; // Return null for placeholders you don't handle
        };
    }
    
    @Override
    @NotNull
    public String getProviderId() {
        return "deluxelifesteal";
    }
}
```

### 2. Register Your Provider

In your plugin's `onEnable()` method:

```java
@Override
public void onEnable() {
    // ... your other initialization code ...
    
    // Hook into DeluxeCoinflip API
    Plugin deluxeCoinflip = Bukkit.getPluginManager().getPlugin("DeluxeCoinflip");
    if (deluxeCoinflip != null && deluxeCoinflip instanceof DeluxeCoinflipAPI) {
        DeluxeCoinflipAPI api = (DeluxeCoinflipAPI) deluxeCoinflip;
        
        LifestealStatProvider statProvider = new LifestealStatProvider(this);
        boolean registered = api.registerCustomStatProvider(statProvider);
        
        if (registered) {
            getLogger().info("Successfully registered custom stats with DeluxeCoinflip!");
        } else {
            getLogger().warning("Failed to register custom stats (provider ID conflict)");
        }
    }
}
```

### 3. Unregister on Disable (Optional but Recommended)

```java
@Override
public void onDisable() {
    Plugin deluxeCoinflip = Bukkit.getPluginManager().getPlugin("DeluxeCoinflip");
    if (deluxeCoinflip != null && deluxeCoinflip instanceof DeluxeCoinflipAPI) {
        DeluxeCoinflipAPI api = (DeluxeCoinflipAPI) deluxeCoinflip;
        api.unregisterCustomStatProvider("deluxelifesteal");
    }
}
```

## Using Your Custom Placeholders

Once registered, your placeholders can be used in DeluxeCoinflip's configuration files:

### In config.yml:

```yaml
games-gui:
  stats:
    enabled: true
    slot: 4
    display_name: "&e&lYour Stats"
    lore:
      - "&7Wins: &a{WINS}"
      - "&7Losses: &c{LOSSES}"
      - "&7Profit: &e{PROFIT}"
      - ""
      - "&d&lLifesteal Stats:"
      - "&7Hearts Won: &c{HEARTS_WIN}"
      - "&7Hearts Lost: &7{HEARTS_LOST}"
      - "&7Hearts Bet: &e{HEARTS_BET}"
      - "&7Net Hearts: &a{HEARTS_NET}"
```

### With PlaceholderAPI:

Your custom stats will also work with PlaceholderAPI:

```
%deluxecoinflip_hearts_win%
%deluxecoinflip_hearts_lost%
%deluxecoinflip_hearts_bet%
%deluxecoinflip_hearts_net%
```

## Important Notes

1. **Placeholder Format**: Use uppercase with underscores (e.g., `HEARTS_WIN`)
2. **Provider ID**: Must be unique across all registered providers
3. **Return Values**: Return `null` for placeholders you don't handle
4. **Thread Safety**: Ensure your `getStatValue` method is thread-safe
5. **Performance**: Keep lookups fast; avoid database queries in this method

## Maven Dependency

Add DeluxeCoinflip as a dependency in your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>net.zithium</groupId>
        <artifactId>deluxecoinflip</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Or in your `build.gradle`:

```gradle
dependencies {
    compileOnly files('libs/DeluxeCoinflip.jar')
}
```

Add to your `plugin.yml`:

```yaml
depend: [DeluxeCoinflip]
# or
softdepend: [DeluxeCoinflip]
```
