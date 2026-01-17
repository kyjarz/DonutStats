package ky.moneytagger.config;

import ky.moneytagger.MoneyTaggerClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class StatsConfigScreen extends Screen {
    private final Screen parent;
    private static final List<ChatFormatting> COLORS = Arrays.asList(
            ChatFormatting.GREEN, ChatFormatting.RED, ChatFormatting.GOLD, 
            ChatFormatting.YELLOW, ChatFormatting.AQUA, ChatFormatting.WHITE, 
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE, ChatFormatting.BLUE
    );

    public StatsConfigScreen(Screen parent) {
        super(Component.literal("DonutStats Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig.ConfigData config = ModConfig.get();
        int y = this.height / 4;
        int center = this.width / 2;

        this.addRenderableWidget(Button.builder(
                Component.literal("Left Stat: " + config.leftStat.name),
                button -> {
                    config.leftStat = nextEnum(config.leftStat);
                    button.setMessage(Component.literal("Left Stat: " + config.leftStat.name));
                })
                .bounds(center - 155, y, 150, 20)
                .build());


        this.addRenderableWidget(Button.builder(
                Component.literal("Left Color: " + formatColorName(config.leftColor)).withStyle(config.leftColor),
                button -> {
                    config.leftColor = nextColor(config.leftColor);
                    button.setMessage(Component.literal("Left Color: " + formatColorName(config.leftColor)).withStyle(config.leftColor));
                })
                .bounds(center + 5, y, 150, 20)
                .build());

        y += 24;

        this.addRenderableWidget(Button.builder(
                Component.literal("Right Stat: " + config.rightStat.name),
                button -> {
                    config.rightStat = nextEnum(config.rightStat);
                    button.setMessage(Component.literal("Right Stat: " + config.rightStat.name));
                })
                .bounds(center - 155, y, 150, 20)
                .build());


        this.addRenderableWidget(Button.builder(
                Component.literal("Right Color: " + formatColorName(config.rightColor)).withStyle(config.rightColor),
                button -> {
                    config.rightColor = nextColor(config.rightColor);
                    button.setMessage(Component.literal("Right Color: " + formatColorName(config.rightColor)).withStyle(config.rightColor));
                })
                .bounds(center + 5, y, 150, 20)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(center - 100, this.height - 40, 200, 20)
                .build());
    }

    private <T extends Enum<T>> T nextEnum(T current) {
        T[] values = current.getDeclaringClass().getEnumConstants();
        return values[(current.ordinal() + 1) % values.length];
    }

    private ChatFormatting nextColor(ChatFormatting current) {
        int index = COLORS.indexOf(current);
        if (index == -1) index = 0;
        return COLORS.get((index + 1) % COLORS.size());
    }

    private String formatColorName(ChatFormatting color) {
        String name = color.getName();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Override
    public void onClose() {
        ModConfig.save();
        MoneyTaggerClient.FETCHER.rebuildCache(); 
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}
