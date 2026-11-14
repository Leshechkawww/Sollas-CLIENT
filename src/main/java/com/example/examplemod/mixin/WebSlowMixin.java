package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WebBlock.class)
public abstract class WebSlowMixin {
 @Inject(method="entityInside",at=@At("HEAD"),cancellable=true)
 private void sollas$web(BlockState state,Level level,BlockPos pos,Entity entity,CallbackInfo ci){if(level.isClientSide&&entity==Minecraft.getInstance().player&&ModuleSettings.on("noslow"))ci.cancel();}
}
