package ky.moneytagger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ky.moneytagger.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DonutMoneyFetcher {
    private static final String API_KEY = "ec4fb09993064f9bba720bdafdf0cacc";

    public record PlayerStats(long money, long kills, long deaths, long shards, long playtime) {}

    private final Map<String, PlayerStats> statsCache = new ConcurrentHashMap<>();
    private final Map<String, Component> componentCache = new ConcurrentHashMap<>();

    private final Set<String> pendingRequests = ConcurrentHashMap.newKeySet();
    private final Set<String> failedRequests = ConcurrentHashMap.newKeySet();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Gson gson = new Gson();

    public void fetchStats(String playerName) {
        if (playerName == null || playerName.isEmpty()) return;
        if (statsCache.containsKey(playerName) || failedRequests.contains(playerName) || pendingRequests.contains(playerName)) return;

        pendingRequests.add(playerName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.donutsmp.net/v1/stats/" + playerName))
                .header("accept", "application/json")
                .header("Authorization", API_KEY)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    pendingRequests.remove(playerName);

                    if (response.statusCode() == 200) {
                        try {
                            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                            JsonObject result = json.has("result") ? json.getAsJsonObject("result") : json;

                            long money = getLong(result, "money");
                            long kills = getLong(result, "kills");
                            long deaths = getLong(result, "deaths");
                            long shards = getLong(result, "shards"); 

                            long playtime = getLong(result, "playtime");

                            PlayerStats stats = new PlayerStats(money, kills, deaths, shards, playtime);
                            statsCache.put(playerName, stats);
                            updateComponentCache(playerName, stats);
                        } catch (Exception e) {
                            failedRequests.add(playerName);
                        }
                    } else {
                        failedRequests.add(playerName);
                    }
                })
                .exceptionally(e -> {
                    pendingRequests.remove(playerName);
                    failedRequests.add(playerName);
                    return null;
                });
    }

    private long getLong(JsonObject json, String member) {
        if (!json.has(member) || json.get(member).isJsonNull()) return 0;
        try {
             return (long) Double.parseDouble(json.get(member).getAsString());
        } catch (Exception e) {
            return 0;
        }
    }

    public void updateComponentCache(String playerName, PlayerStats stats) {
        ModConfig.ConfigData config = ModConfig.get();
        Component statsText = Component.empty();

        statsText.getSiblings().add(
            formatStat(config.leftStat, stats).withStyle(config.leftColor)
        );

        statsText.getSiblings().add(Component.literal(" "));

        statsText.getSiblings().add(
            formatStat(config.rightStat, stats).withStyle(config.rightColor)
        );

        componentCache.put(playerName, statsText);
    }
    
    public void rebuildCache() {
        statsCache.forEach(this::updateComponentCache);
    }

    private net.minecraft.network.chat.MutableComponent formatStat(ModConfig.StatType type, PlayerStats stats) {
        return switch (type) {
            case MONEY -> Component.literal(formatCompact(stats.money, "$", true));
            case KILLS -> Component.literal(formatCompact(stats.kills, "☠", false));
            case DEATHS -> Component.literal(formatCompact(stats.deaths, "☠", false));
            case SHARDS -> Component.literal(formatCompact(stats.shards, "⭐", true));
            case PLAYTIME -> Component.literal(formatPlaytime(stats.playtime));
        };
    }
    
    private String formatCompact(long value, String icon, boolean useMoneySign) {
        String formatted;
        if (value >= 1_000_000_000L) formatted = compact(value / 1_000_000_000.0, "B");
        else if (value >= 1_000_000L) formatted = compact(value / 1_000_000.0, "M");
        else if (value >= 1_000L) formatted = compact(value / 1_000.0, "k");
        else formatted = String.valueOf(value);
        
        return icon + formatted;
    }

    private static String compact(double value, String unit) {
        String s = String.format("%.1f", value);
        if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
        return s + unit;
    }
    
    private String formatPlaytime(long seconds) {
        long d = seconds / 86400;
        long h = (seconds % 86400) / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        
        String timeStr;
        if (d > 0) {
            timeStr = d + "d " + h + "h";
        } else if (h > 0) {
            timeStr = h + "h";
        } else {
            timeStr = m + "m " + s + "s";
        }
        return "🕒 " + timeStr;
    }

    public Component getStatsText(String playerName) {
        return componentCache.get(playerName);
    }

    public void clear() {
        statsCache.clear();
        componentCache.clear();
        pendingRequests.clear();
        failedRequests.clear();
    }
}
