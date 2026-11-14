package com.example.examplemod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Camera-relative, depth-tested block selection geometry. */
public final class WorldOverlay {
    public static void render(RenderLevelStageEvent e) {
        var mc=Minecraft.getInstance();
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES || !ClientSettings.INSTANCE.outline || mc.level==null) return;
        if(!(mc.hitResult instanceof BlockHitResult hit) || hit.getType()!=HitResult.Type.BLOCK) return;
        var pos=hit.getBlockPos(); var state=mc.level.getBlockState(pos);
        var camera=e.getCamera().getPosition();
        PoseStack pose=e.getPoseStack();
        pose.pushPose();
        pose.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);
        var buffers=mc.renderBuffers().bufferSource();
        var lines=buffers.getBuffer(RenderType.lines());
        int c=ClientSettings.INSTANCE.color();
        state.getShape(mc.level,pos).forAllBoxes((x1,y1,z1,x2,y2,z2) ->
            LevelRenderer.renderLineBox(pose,lines,x1-.002,y1-.002,z1-.002,x2+.002,y2+.002,z2+.002,((c>>16)&255)/255f,((c>>8)&255)/255f,(c&255)/255f,1));
        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }
}
