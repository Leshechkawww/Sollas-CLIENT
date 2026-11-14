package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(LocalPlayer.class)
public abstract class NoSlowMixin {
 @Redirect(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",ordinal=0))
 private boolean sollas$itemSlow(LocalPlayer player){return !ModuleSettings.on("noslow")&&player.isUsingItem();}
}
