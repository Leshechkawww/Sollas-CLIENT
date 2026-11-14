package com.example.examplemod.mixin;
import com.example.examplemod.ModuleRuntime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(targets="net.minecraft.client.gui.Gui$HeartType")
public abstract class HeartsMixin {
    @Redirect(method="forPlayer",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private static boolean sobaka$wither(Player p,Holder<MobEffect> effect){return !(effect==MobEffects.WITHER&&ModuleRuntime.remove("wither"))&&p.hasEffect(effect);}
}
