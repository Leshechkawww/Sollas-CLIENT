package com.example.examplemod.mixin;
import com.example.examplemod.ModuleRuntime;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Redirect(method="turnPlayer",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void sobaka$turn(LocalPlayer player,double x,double y){ModuleRuntime.turn(player,x,y);}
}
