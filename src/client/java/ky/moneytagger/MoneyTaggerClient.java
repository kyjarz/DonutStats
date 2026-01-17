package ky.moneytagger;

import net.fabricmc.api.ClientModInitializer;

public class MoneyTaggerClient implements ClientModInitializer {
	public static final DonutMoneyFetcher FETCHER = new DonutMoneyFetcher();

	@Override
	public void onInitializeClient() {
	}
}