package com.example.examplemod.mixin;
import com.example.examplemod.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandRenderer.class)
public abstract class HandMixin {
    @Inject(method="renderHandsWithItems",at=@At("HEAD")) private void sobaka$start(CallbackInfo ci){ModulePostFx.beginHand();}
    @Inject(method="renderHandsWithItems",at=@At("RETURN")) private void sobaka$end(CallbackInfo ci){ModulePostFx.endHand();}
    @Inject(method="renderArmWithItem",at=@At("HEAD"))
    private void sobaka$pose(AbstractClientPlayer p,float partial,float pitch,InteractionHand hand,float swing,ItemStack item,float equip,PoseStack pose,MultiBufferSource buffer,int light,CallbackInfo ci){
        pose.pushPose();boolean right=(hand==InteractionHand.MAIN_HAND?p.getMainArm():p.getMainArm().getOpposite())==HumanoidArm.RIGHT;
        if(ModuleSettings.on("view")){String side=right?"r":"l";pose.translate(ModuleSettings.v("view",side+"x"),ModuleSettings.v("view",side+"y"),ModuleSettings.v("view",side+"z"));}
        if(ModuleSettings.on("sword")&&!p.isUsingItem()&&weapon(item)){
            float t=p.swingingArm==hand?Math.clamp(p.getAttackAnim(partial),0f,1f):0f;
            // Smooth wind-up, fast strike and longer eased recovery. Both ends are exactly neutral.
            float wind=(float)Math.sin(Math.PI*Math.min(t/.38f,1f));
            float strike=(float)Math.sin(Math.PI*Math.pow(t,.72));
            float recovery=(float)Math.sin(Math.PI*t);
            float strength=(float)Math.min(1.5,ModuleSettings.v("sword","strength"));int side=right?1:-1;
            wind*=strength;strike*=strength;recovery*=strength;
            switch(ModuleSettings.mode("sword")){
                case 0->{pose.translate(side*(-.035*wind+.07*strike),-.07*strike,.06*wind-.10*strike);pose.mulPose(Axis.YP.rotationDegrees(side*(12*wind-25*strike)));pose.mulPose(Axis.XP.rotationDegrees(-42*strike));pose.mulPose(Axis.ZP.rotationDegrees(side*18*recovery));}
                case 1->{pose.translate(side*.09*strike,.035*wind-.06*strike,-.07*strike);pose.mulPose(Axis.YP.rotationDegrees(side*(-14*wind+32*strike)));pose.mulPose(Axis.ZP.rotationDegrees(side*(-8*wind+38*strike)));pose.mulPose(Axis.XP.rotationDegrees(-22*recovery));}
                case 2->{pose.translate(side*.025*strike,.035*wind,.19*strike);pose.mulPose(Axis.XP.rotationDegrees(18*wind-32*strike));pose.mulPose(Axis.YP.rotationDegrees(side*18*strike));}
                case 3->{pose.translate(-side*.04*strike,-.025*strike,-.24*strike);pose.mulPose(Axis.YP.rotationDegrees(-side*12*strike));pose.mulPose(Axis.XP.rotationDegrees(-26*strike));}
                case 4->{pose.translate(side*.04*strike,-.06*strike,-.15*strike);pose.mulPose(Axis.YP.rotationDegrees(-side*22*strike));pose.mulPose(Axis.XP.rotationDegrees(-48*strike));}
                case 5->{pose.translate(-side*.12*strike,-.025*strike,-.04*strike);pose.mulPose(Axis.YP.rotationDegrees(side*(18*wind-48*strike)));pose.mulPose(Axis.ZP.rotationDegrees(side*28*strike));}
                default->{pose.translate(0,-.12*strike,-.08*strike);pose.mulPose(Axis.XP.rotationDegrees(15*wind-65*strike));pose.mulPose(Axis.ZP.rotationDegrees(side*10*recovery));}
            }
        }
    }
    private static boolean weapon(ItemStack item){return item.getItem() instanceof net.minecraft.world.item.SwordItem || item.getItem() instanceof net.minecraft.world.item.AxeItem || item.getItem() instanceof net.minecraft.world.item.MaceItem;}
    @ModifyVariable(method="renderArmWithItem",at=@At("HEAD"),argsOnly=true,ordinal=2)
    private float sobaka$noVanillaSwing(float swing){
        var p=net.minecraft.client.Minecraft.getInstance().player;
        return ModuleSettings.on("sword")&&p!=null&&!p.isUsingItem()&&weapon(p.getItemInHand(p.swingingArm==null?InteractionHand.MAIN_HAND:p.swingingArm))?0:swing;
    }
    @Inject(method="renderArmWithItem",at=@At("RETURN"))
    private void sobaka$pop(AbstractClientPlayer p,float partial,float pitch,InteractionHand hand,float swing,ItemStack item,float equip,PoseStack pose,MultiBufferSource buffer,int light,CallbackInfo ci){pose.popPose();}
    @Inject(method="applyEatTransform",at=@At("HEAD"),cancellable=true)
    private void sobaka$eat(CallbackInfo ci){if(ModuleRuntime.remove("eat"))ci.cancel();}
}
