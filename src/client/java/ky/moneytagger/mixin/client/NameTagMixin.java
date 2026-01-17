package ky.moneytagger.mixin.client;

import ky.moneytagger.MoneyTaggerClient;
import ky.moneytagger.duck.DuckPlayerState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class NameTagMixin {

    @Shadow public abstract Font getFont();

    @Inject(method = "renderNameTag", at = @At("HEAD"))
    private void moveNameTagUp(EntityRenderState state, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (state instanceof PlayerRenderState) {
             poseStack.translate(0.0f, 0.25f, 0.0f);
        }
    }

    @Inject(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void renderStats(EntityRenderState state, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (state instanceof PlayerRenderState) {
            String playerName = ((DuckPlayerState) state).moneytagger$getPlayerName();
            if (playerName == null || playerName.isEmpty()) return;

            MoneyTaggerClient.FETCHER.fetchStats(playerName);
            Component statsText = MoneyTaggerClient.FETCHER.getStatsText(playerName);

            if (statsText != null) {
                Font font = this.getFont();
                Matrix4f matrix4f = poseStack.last().pose();
                float xOffset = (float)(-font.width(statsText) / 2);
                float yOffset = 10.0f;
                
                font.drawInBatch(statsText, xOffset, yOffset, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, i);
            }
        }
    }
}
