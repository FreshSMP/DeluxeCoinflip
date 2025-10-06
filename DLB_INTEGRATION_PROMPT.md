# DeluxeLifestealBridge Integration Prompt

Use this prompt to integrate DeluxeLifestealBridge with DeluxeCoinflip's Custom Stats API.

---

## Prompt for AI Assistant:

```
I need to integrate my DeluxeLifestealBridge plugin with DeluxeCoinflip's Custom Stats API 
to track heart-based coinflip statistics.

Requirements:
1. Implement the CustomStatProvider interface from DeluxeCoinflip
2. Track the following statistics for each player:
   - HEARTS_WIN: Total hearts won from coinflips
   - HEARTS_LOST: Total hearts lost from coinflips
   - HEARTS_BET: Total hearts bet in coinflips
   - HEARTS_NET: Net hearts (won - lost)

3. The stats should be:
   - Stored persistently (database or file)
   - Retrieved efficiently without blocking
   - Updated whenever a heart-based coinflip completes

4. Register the provider with DeluxeCoinflip on plugin enable
5. Unregister on plugin disable
6. Add proper error handling and logging

Implementation Details:
- Create a LifestealStatProvider class that implements CustomStatProvider
- Store data in a PlayerData or similar class
- Use DeluxeCoinflip's API to register: api.registerCustomStatProvider(provider)
- Provider ID should be: "deluxelifesteal"
- Handle cases where DeluxeCoinflip is not installed gracefully

Additional Features:
- Add commands to view/reset heart stats if needed
- Consider adding these placeholders to your own PlaceholderAPI expansion
- Ensure thread-safety for data access

Please implement this integration following best practices for Spigot plugin development.
```

---

## Technical Specifications

### API Reference

**Interface to Implement:**
```java
net.zithium.deluxecoinflip.api.CustomStatProvider
```

**Required Methods:**
- `String getStatValue(Player player, String placeholder)` - Returns stat value or null
- `String getProviderId()` - Returns "deluxelifesteal"

**API Access:**
```java
DeluxeCoinflipAPI api = (DeluxeCoinflipAPI) Bukkit.getPluginManager().getPlugin("DeluxeCoinflip");
boolean success = api.registerCustomStatProvider(yourProvider);
```

### Placeholder Names

Use these exact placeholder names (case-sensitive):
- `HEARTS_WIN`
- `HEARTS_LOST`
- `HEARTS_BET`
- `HEARTS_NET`

These will be usable in DeluxeCoinflip config as:
```yaml
lore:
  - "&cHearts Won: {HEARTS_WIN}"
  - "&7Hearts Lost: {HEARTS_LOST}"
  - "&eHearts Bet: {HEARTS_BET}"
  - "&aNet Hearts: {HEARTS_NET}"
```

And via PlaceholderAPI as:
```
%deluxecoinflip_hearts_win%
%deluxecoinflip_hearts_lost%
%deluxecoinflip_hearts_bet%
%deluxecoinflip_hearts_net%
```

### Data Storage Schema

Suggested database table structure:
```sql
CREATE TABLE IF NOT EXISTS lifesteal_coinflip_stats (
    uuid VARCHAR(36) PRIMARY KEY,
    hearts_won INT DEFAULT 0,
    hearts_lost INT DEFAULT 0,
    hearts_bet INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Or YAML format:
```yaml
players:
  uuid-here:
    hearts_won: 0
    hearts_lost: 0
    hearts_bet: 0
```

### Integration Points

**When to Update Stats:**
1. Listen for DeluxeCoinflip game completion events (if available)
2. Or hook into your heart transaction system
3. Check if the transaction was from a coinflip
4. Update the appropriate stats

**Example Flow:**
```
Player bets 5 hearts -> HEARTS_BET += 5
Player wins 10 hearts -> HEARTS_WIN += 10
Player loses -> HEARTS_LOST += 5
```

### Dependencies

Add to your `plugin.yml`:
```yaml
name: DeluxeLifestealBridge
depend: [DeluxeCoinflip]  # Or use softdepend if optional
```

Add to your `pom.xml` or `build.gradle`:
```xml
<!-- Maven -->
<dependency>
    <groupId>net.zithium</groupId>
    <artifactId>deluxecoinflip</artifactId>
    <version>LATEST</version>
    <scope>provided</scope>
</dependency>
```

```gradle
// Gradle
dependencies {
    compileOnly files('libs/DeluxeCoinflip.jar')
}
```

### Error Handling

Handle these cases:
1. DeluxeCoinflip not installed or disabled
2. Player data not found
3. Database connection issues
4. Concurrent access to player data

### Testing Checklist

- [ ] Provider registers successfully on enable
- [ ] Placeholders return correct values in DeluxeCoinflip GUI
- [ ] Stats persist across server restarts
- [ ] Stats update correctly when hearts are won/lost
- [ ] PlaceholderAPI integration works
- [ ] No errors when DeluxeCoinflip is not installed
- [ ] Provider unregisters cleanly on disable
- [ ] Thread-safe access to player data

---

## Quick Start Code Template

```java
// In your main plugin class onEnable():
if (Bukkit.getPluginManager().isPluginEnabled("DeluxeCoinflip")) {
    Plugin dcPlugin = Bukkit.getPluginManager().getPlugin("DeluxeCoinflip");
    if (dcPlugin instanceof DeluxeCoinflipAPI) {
        DeluxeCoinflipAPI api = (DeluxeCoinflipAPI) dcPlugin;
        LifestealStatProvider provider = new LifestealStatProvider(this);
        
        if (api.registerCustomStatProvider(provider)) {
            getLogger().info("Successfully integrated with DeluxeCoinflip!");
        } else {
            getLogger().warning("Failed to register stats provider with DeluxeCoinflip");
        }
    }
}
```
