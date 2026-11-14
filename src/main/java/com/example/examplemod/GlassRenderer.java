package com.example.examplemod;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;

/** Two half-resolution passes shared by every panel in a GUI/HUD pass. */
public final class GlassRenderer {
    public static ShaderInstance blurShader, panelShader;
    private static RenderTarget ping, pong;
    private static boolean captured;
    public static int captures, panelCount;
    public static float opacity=1;

    public static void begin(GuiGraphics g) {
        captured = false;
        captures = panelCount = 0;
        if (ClientSettings.INSTANCE.glass == 0 || blurShader == null || panelShader == null) return;
        g.flush();
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        int w = Math.max(1, main.width / 2), h = Math.max(1, main.height / 2);
        if (ping == null || ping.width != w || ping.height != h) {
            if (ping != null) { ping.destroyBuffers(); pong.destroyBuffers(); }
            ping = new TextureTarget(w, h, false, Minecraft.ON_OSX);
            pong = new TextureTarget(w, h, false, Minecraft.ON_OSX);
            ping.setFilterMode(GL11.GL_LINEAR);
            pong.setFilterMode(GL11.GL_LINEAR);
        }
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        ShaderInstance previous = RenderSystem.getShader();
        try {
            RenderSystem.disableDepthTest(); RenderSystem.disableBlend(); RenderSystem.disableCull();
            if (scissor) RenderSystem.disableScissor();
            float radius = ClientSettings.INSTANCE.blur;
            pass(main.getColorTextureId(), ping, radius / w, 0);
            pass(ping.getColorTextureId(), pong, 0, radius / h);
            captured = true;
            captures = 1;
        } finally {
            main.bindWrite(true);
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
            if (scissor) GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderSystem.setShader(() -> previous);
        }
    }
    private static void pass(int texture, RenderTarget target, float dx, float dy) {
        target.bindWrite(true);
        RenderSystem.setShader(() -> blurShader);
        RenderSystem.setShaderTexture(0, texture);
        blurShader.safeGetUniform("Direction").set(dx, dy);
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        b.addVertex(-1,-1,0); b.addVertex(1,-1,0); b.addVertex(1,1,0); b.addVertex(-1,1,0);
        BufferUploader.drawWithShader(b.buildOrThrow());
    }
    public static void panel(GuiGraphics g, int x, int y, int w, int h, float radius, int tint, boolean glass) {
        panelCount++;
        if (panelShader == null) { g.fill(x,y,x+w,y+h,tint); return; }
        g.flush();
        Minecraft mc = Minecraft.getInstance();
        float sx = (float) mc.getMainRenderTarget().width / mc.getWindow().getGuiScaledWidth();
        float sy = (float) mc.getMainRenderTarget().height / mc.getWindow().getGuiScaledHeight();
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        RenderSystem.disableDepthTest(); RenderSystem.disableCull();
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> panelShader);
        RenderSystem.setShaderTexture(0, captured ? pong.getColorTextureId() : 0);
        var matrix=g.pose().last().pose();
        float tx=x*matrix.m00()+matrix.m30(), ty=y*matrix.m11()+matrix.m31();
        float tw=w*matrix.m00(), th=h*matrix.m11();
        panelShader.safeGetUniform("Rect").set(tx*sx, mc.getMainRenderTarget().height-(ty+th)*sy, tw*sx, th*sy);
        panelShader.safeGetUniform("Resolution").set((float)mc.getMainRenderTarget().width,(float)mc.getMainRenderTarget().height);
        panelShader.safeGetUniform("Radius").set(radius*sx*matrix.m00());
        panelShader.safeGetUniform("Tint").set(((tint>>16)&255)/255f,((tint>>8)&255)/255f,(tint&255)/255f,((tint>>>24)&255)/255f);
        panelShader.safeGetUniform("Opacity").set(opacity);
        panelShader.safeGetUniform("Mode").set(glass && captured ? (float)ClientSettings.INSTANCE.glass : 0f);
        panelShader.safeGetUniform("Refraction").set(ClientSettings.INSTANCE.refraction*sx);
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        var m = g.pose().last().pose();
        b.addVertex(m,x,y,0); b.addVertex(m,x,y+h,0); b.addVertex(m,x+w,y+h,0); b.addVertex(m,x+w,y,0);
        BufferUploader.drawWithShader(b.buildOrThrow());
        if (depth) RenderSystem.enableDepthTest();
        if (!blend) RenderSystem.disableBlend();
        if (cull) RenderSystem.enableCull();
    }
}
