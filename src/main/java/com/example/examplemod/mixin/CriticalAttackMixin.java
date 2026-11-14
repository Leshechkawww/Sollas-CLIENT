package com.example.examplemod.mixin;
import com.example.examplemod.Criticals;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MultiPlayerGameMode.class)
public abstract class CriticalAttackMixin {
 @Inject(method="attack",at=@At("HEAD"),cancellable=true)
 private void sollas$critical(Player player,Entity target,CallbackInfo ci){if(Criticals.defer(player,target))ci.cancel();}
}
