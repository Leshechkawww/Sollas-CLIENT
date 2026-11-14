package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.world.level.Level;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Level.class)
public abstract class WorldMixin {
    @Inject(method="getDayTime",at=@At("HEAD"),cancellable=true)
    private void sobaka$time(CallbackInfoReturnable<Long> ci){if((Object)this instanceof ClientLevel&&ModuleSettings.on("world"))ci.setReturnValue((long)ModuleSettings.v("world","time"));}
}
