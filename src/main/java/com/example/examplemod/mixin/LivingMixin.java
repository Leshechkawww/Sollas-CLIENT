package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public abstract class LivingMixin {
    @Inject(method="getCurrentSwingDuration",at=@At("HEAD"),cancellable=true)
    private void sobaka$swing(CallbackInfoReturnable<Integer> ci){if((Object)this==Minecraft.getInstance().player&&ModuleSettings.on("sword") && !Minecraft.getInstance().player.isUsingItem() && (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof net.minecraft.world.item.SwordItem || Minecraft.getInstance().player.getMainHandItem().getItem() instanceof net.minecraft.world.item.AxeItem || Minecraft.getInstance().player.getMainHandItem().getItem() instanceof net.minecraft.world.item.MaceItem))ci.setReturnValue(ModuleSettings.mode("sword")==0?12:ModuleSettings.mode("sword")==4?3:6);}
}
