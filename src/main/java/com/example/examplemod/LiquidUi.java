package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/** Shared typography, icon atlas and surface tokens for every SOBAKA screen. */
public final class LiquidUi {
    public static final int TEXT=0xFFF2F5FF, MUTED=0xFFA9B7CE, SOFT=0xFF76859D;
    public static final int SURFACE=0xB0162032, CARD=0x871A273B;
    private static final Style REGULAR=Style.EMPTY.withFont(ExampleModClient.id("regular"));
    private static final Style SEMIBOLD=Style.EMPTY.withFont(ExampleModClient.id("semibold"));
    private static final Style ICONS=Style.EMPTY.withFont(ExampleModClient.id("icons"));
    public static Component text(String s,boolean bold){return Component.literal(s).setStyle(bold?SEMIBOLD:REGULAR);}
    public static float width(String s,float size,boolean bold){return Minecraft.getInstance().font.width(text(s,bold))*size/11f;}
    public static void text(GuiGraphics g,String s,float x,float y,float size,int color,boolean bold){
        g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(size/11f,size/11f,1);
        g.drawString(Minecraft.getInstance().font,text(s,bold),0,0,color,false);g.pose().popPose();
    }
    public static void icon(GuiGraphics g,int i,float x,float y,float size,int color){
        g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(size/14f,size/14f,1);
        g.drawString(Minecraft.getInstance().font,Component.literal(String.valueOf((char)(0xE001+i))).setStyle(ICONS),0,0,color,false);g.pose().popPose();
    }
    private static net.minecraft.client.renderer.texture.AbstractTexture logoTexture;
    public static void logo(GuiGraphics g,int x,int y,int s,int color){var id=ExampleModClient.id("textures/gui/star.png");var texture=net.minecraft.client.Minecraft.getInstance().getTextureManager().getTexture(id);if(texture!=logoTexture){texture.setFilter(true,false);logoTexture=texture;}com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.blit(id,x,y,s,s,0f,0f,256,256,256,256);g.flush();}
    public static void surface(GuiGraphics g,int x,int y,int w,int h,int radius,int color){GlassRenderer.panel(g,x,y,w,h,radius,color,true);}
    public static void flat(GuiGraphics g,int x,int y,int w,int h,int radius,int color){GlassRenderer.panel(g,x,y,w,h,radius,color,false);}
    public static int mix(int a,int b,float t){
        t=Math.clamp(t,0,1);int out=0;
        for(int shift=0;shift<=24;shift+=8)out|=(int)(((a>>>shift)&255)*(1-t)+((b>>>shift)&255)*t)<<shift;
        return out;
    }
    public static String fit(String s,float max,float size){
        if(width(s,size,false)<=max)return s;
        while(!s.isEmpty() && width(s+"…",size,false)>max)s=s.substring(0,s.offsetByCodePoints(s.length(),-1));
        return s+"…";
    }
    public static final class Motion {
        private float value; private long time=System.nanoTime();
        public Motion(float start){value=start;}
        public float to(float target){
            long now=System.nanoTime();float dt=Math.min(.05f,(now-time)/1_000_000_000f);time=now;
            value=ClientSettings.INSTANCE.animations?value+(target-value)*(1-(float)Math.exp(-dt*15)):target;
            return value;
        }
    }
}
