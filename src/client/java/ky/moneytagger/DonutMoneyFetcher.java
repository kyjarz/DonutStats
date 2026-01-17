package ky.moneytagger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
    //Api Key Goes Hear
    private static final String API_KEY = "";

    private final Map<String, Long> moneyCache = new ConcurrentHashMap<>();
    private final Map<String, Long> killsCache = new ConcurrentHashMap<>();
    private final Map<String, Component> componentCache = new ConcurrentHashMap<>();

    private final Set<String> pendingRequests = ConcurrentHashMap.newKeySet();
    private final Set<String> failedRequests = ConcurrentHashMap.newKeySet();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Gson gson = new Gson();

    public void fetchStats(String playerName) {
        if (playerName == null || playerName.isEmpty()) return;
        if (moneyCache.containsKey(playerName) || failedRequests.contains(playerName) || pendingRequests.contains(playerName)) return;

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
                            JsonObject result = json.getAsJsonObject("result");

                            long money = (long) Double.parseDouble(result.get("money").getAsString());
                            long kills = Long.parseLong(result.get("kills").getAsString());

                            moneyCache.put(playerName, money);
                            killsCache.put(playerName, kills);
                            updateComponentCache(playerName, money, kills);
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

    private void updateComponentCache(String playerName, long money, long kills) {
        Component statsText = Component.empty();

        statsText.getSiblings().add(
            Component.literal(formatMoney(money)).withStyle(ChatFormatting.GREEN)
        );

        statsText.getSiblings().add(Component.literal(" "));

        statsText.getSiblings().add(
            Component.literal("☠" + formatMoney(kills).replace("$", "")).withStyle(ChatFormatting.RED)
        );

        componentCache.put(playerName, statsText);
    }

    public Component getStatsText(String playerName) {
        return componentCache.get(playerName);
    }

    public Long getMoney(String playerName) {
        return moneyCache.get(playerName);
    }

    public Long getKills(String playerName) {
        return killsCache.get(playerName);
    }

    public void clear() {
        moneyCache.clear();
        killsCache.clear();
        componentCache.clear();
        pendingRequests.clear();
        failedRequests.clear();
    }

    public static String formatMoney(long money) {
        if (money >= 1_000_000_000L) return compact(money / 1_000_000_000.0, "B");
        if (money >= 1_000_000L)     return compact(money / 1_000_000.0, "M");
        if (money >= 1_000L)         return compact(money / 1_000.0, "k");
        return "$" + money;
    }

    private static String compact(double value, String unit) {
        String s = String.format("%.1f", value);
        if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
        return "$" + s + unit;
    }
}
