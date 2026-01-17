package ky.moneytagger.mixin.client;

import ky.moneytagger.duck.DuckPlayerState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements DuckPlayerState {
    @Unique
    private String moneytagger$playerName;

    @Override
    public void moneytagger$setPlayerName(String name) {
        this.moneytagger$playerName = name;
    }

    @Override
    public String moneytagger$getPlayerName() {
        return this.moneytagger$playerName;
    }
}
