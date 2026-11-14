package com.example.examplemod;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

public final class ModulePostFx {
    private static ShaderInstance motion,hand,sky;
    private static RenderTarget scene,hands;
    private static boolean capturing;
    private static Matrix4f previous;
    private static Vec3 previousCamera;
    private static long lastFrame;
    public static void register(RegisterShadersEvent e){
        try{
            e.registerShader(new ShaderInstance(e.getResourceProvider(),ExampleModClient.id("motion"),DefaultVertexFormat.POSITION),s->motion=s);
            e.registerShader(new ShaderInstance(e.getResourceProvider(),ExampleModClient.id("hand"),DefaultVertexFormat.POSITION),s->hand=s);
            e.registerShader(new ShaderInstance(e.getResourceProvider(),ExampleModClient.id("sky"),DefaultVertexFormat.POSITION),s->sky=s);
        }catch(Exception error){ExampleMod.LOGGER.error("Optional module shader failed",error);}
        previous=null;
    }
    private static RenderTarget target(RenderTarget target){var main=Minecraft.getInstance().getMainRenderTarget();if(target==null||target.width!=main.width||target.height!=main.height){if(target!=null)target.destroyBuffers();target=new TextureTarget(main.width,main.height,true,Minecraft.ON_OSX);target.setFilterMode(GL11.GL_LINEAR);}return target;}
    public static void motion(RenderLevelStageEvent e){
        if(!ModuleSettings.on("motion")||motion==null){previous=null;return;}
        var mc=Minecraft.getInstance();var current=new Matrix4f(e.getProjectionMatrix()).mul(e.getModelViewMatrix());var cam=e.getCamera().getPosition();long now=System.nanoTime();
        if(previous!=null&&previousCamera.distanceToSqr(cam)<16&&now-lastFrame<250_000_000){
            var main=mc.getMainRenderTarget();scene=target(scene);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,main.frameBufferId);GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,scene.frameBufferId);
            GL30.glBlitFramebuffer(0,0,main.width,main.height,0,0,main.width,main.height,GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT,GL11.GL_NEAREST);
            main.bindWrite(true);
            RenderSystem.setShaderTexture(0,scene.getColorTextureId());RenderSystem.setShaderTexture(1,scene.getDepthTextureId());
            motion.safeGetUniform("InvVP").set(new Matrix4f(current).invert());motion.safeGetUniform("PrevVP").set(previous);
            Vec3 delta=cam.subtract(previousCamera);motion.safeGetUniform("CameraDelta").set((float)delta.x,(float)delta.y,(float)delta.z);
            motion.safeGetUniform("Resolution").set((float)main.width,(float)main.height);
            // Normalize camera velocity to a shutter interval, bounded during frame spikes.
            float shutter=(float)ModuleSettings.v("motion","strength")*Math.clamp(16_666_667f/(now-lastFrame),.25f,2f);
            motion.safeGetUniform("Strength").set(shutter);motion.safeGetUniform("Limit").set((float)ModuleSettings.v("motion","limit"));draw(motion,false);
        }
        previous=current;previousCamera=cam;lastFrame=now;
    }
    public static void beginHand(){
        var mc=Minecraft.getInstance();capturing=ModuleSettings.on("hand")&&hand!=null&&mc.player!=null&&mc.options.getCameraType().isFirstPerson();
        if(!capturing)return;hands=target(hands);hands.setClearColor(0,0,0,0);hands.clear(Minecraft.ON_OSX);hands.bindWrite(true);
    }
    public static void endHand(){
        if(!capturing)return;capturing=false;var main=Minecraft.getInstance().getMainRenderTarget();main.bindWrite(true);
        RenderSystem.setShaderTexture(0,hands.getColorTextureId());hand.safeGetUniform("Resolution").set((float)main.width,(float)main.height);
        hand.safeGetUniform("Mode").set((float)ModuleSettings.mode("hand"));hand.safeGetUniform("Opacity").set((float)ModuleSettings.v("hand","alpha"));
        hand.safeGetUniform("Radius").set((float)ModuleSettings.v("hand","width"));hand.safeGetUniform("Time").set((System.nanoTime()%1_000_000_000_000L)/1e9f);
        int c=ClientSettings.INSTANCE.color();hand.safeGetUniform("Color").set(((c>>16)&255)/255f,((c>>8)&255)/255f,(c&255)/255f);draw(hand,true);
    }
    public static void sky(RenderLevelStageEvent e){
        if(!ModuleSettings.on("world")||sky==null||ModuleSettings.mode("world")==1||ModuleRuntime.remove("sky"))return;
        sky.safeGetUniform("InvVP").set(new Matrix4f(e.getProjectionMatrix()).mul(e.getModelViewMatrix()).invert());
        sky.safeGetUniform("Time").set((System.nanoTime()%1_000_000_000_000L)/1e9f);sky.safeGetUniform("Mode").set((float)ModuleSettings.mode("world"));
        int c=java.awt.Color.HSBtoRGB((float)ModuleSettings.v("world","hue"),.65f,.8f);sky.safeGetUniform("Color").set(((c>>16)&255)/255f,((c>>8)&255)/255f,(c&255)/255f);draw(sky,true);
    }
    private static void draw(ShaderInstance shader,boolean blend){
        boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),wasBlend=GL11.glIsEnabled(GL11.GL_BLEND),write=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);var old=RenderSystem.getShader();
        RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();if(blend){RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();}else RenderSystem.disableBlend();
        RenderSystem.setShader(()->shader);var b=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION);
        b.addVertex(-1,-1,0);b.addVertex(1,-1,0);b.addVertex(1,1,0);b.addVertex(-1,1,0);BufferUploader.drawWithShader(b.buildOrThrow());
        RenderSystem.depthMask(write);if(depth)RenderSystem.enableDepthTest();if(cull)RenderSystem.enableCull();if(wasBlend)RenderSystem.enableBlend();else RenderSystem.disableBlend();RenderSystem.setShader(()->old);
    }
}
