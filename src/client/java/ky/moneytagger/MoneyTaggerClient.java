package ky.moneytagger;

import ky.moneytagger.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;

public class MoneyTaggerClient implements ClientModInitializer {
    public static final DonutMoneyFetcher FETCHER = new DonutMoneyFetcher();

    @Override
    public void onInitializeClient() {
        ModConfig.load();
    }
}