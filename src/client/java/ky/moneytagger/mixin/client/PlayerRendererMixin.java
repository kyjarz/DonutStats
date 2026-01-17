package ky.moneytagger.mixin.client;

import ky.moneytagger.duck.DuckPlayerState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void capturePlayerName(AbstractClientPlayer entity, PlayerRenderState state, float partialTick, CallbackInfo ci) {
        ((DuckPlayerState) state).moneytagger$setPlayerName(entity.getGameProfile().getName());
    }
}
