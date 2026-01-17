package ky.moneytagger.mixin.client;

import ky.moneytagger.config.StatsConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addStatsButton(CallbackInfo ci) {
        int l = this.height / 4 + 48;
        int buttonY = l + 24;
        int buttonX = this.width / 2 + 104;

        this.addRenderableWidget(Button.builder(Component.literal("DS"), button -> {
            this.minecraft.setScreen(new StatsConfigScreen(this));
        })
        .bounds(buttonX, buttonY, 20, 20)
        .build());
    }
}
