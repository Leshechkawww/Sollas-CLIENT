package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MultiPlayerGameMode.class)
public abstract class FastBreakMixin {
 @Shadow private int destroyDelay;
 @Inject(method="continueDestroyBlock",at=@At("HEAD"))
 private void sollas$delay(CallbackInfoReturnable<Boolean> ci){if(ModuleSettings.on("fastbreak"))destroyDelay=0;}
}
