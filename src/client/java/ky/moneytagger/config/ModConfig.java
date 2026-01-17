package ky.moneytagger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("donutstats.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum StatType {
        MONEY("Money", "$", true),
        KILLS("Kills", "☠", false),
        DEATHS("Deaths", "☠", false),
        SHARDS("Shards", "⭐", false),
        PLAYTIME("Playtime", "🕒", false);

        public final String name;
        public final String icon;
        public final boolean iconPrefix; 

        StatType(String name, String icon, boolean iconPrefix) {
            this.name = name;
            this.icon = icon;
            this.iconPrefix = iconPrefix;
        }
    }

    public static class ConfigData {
        public StatType leftStat = StatType.MONEY;
        public ChatFormatting leftColor = ChatFormatting.GREEN;
        
        public StatType rightStat = StatType.KILLS;
        public ChatFormatting rightColor = ChatFormatting.RED;
    }

    private static ConfigData instance = new ConfigData();

    public static ConfigData get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            instance = GSON.fromJson(json, ConfigData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            String json = GSON.toJson(instance);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
