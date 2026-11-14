package com.example.examplemod.mixin;
import com.example.examplemod.FakePlayerManager;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MultiPlayerGameMode.class)
public abstract class FakeAttackMixin {
    @Inject(method="attack",at=@At("HEAD"),cancellable=true)
    private void sollas$testAttack(Player player,Entity target,CallbackInfo ci){if(FakePlayerManager.isFake(target)){FakePlayerManager.hit();player.resetAttackStrengthTicker();ci.cancel();}}
}
