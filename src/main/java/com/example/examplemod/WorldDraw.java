package com.example.examplemod;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;
import java.util.*;

/** Batched quads, lines and soft sprites; all world geometry is depth tested. */
public final class WorldDraw {
    private record Q(Vec3 a,Vec3 b,Vec3 c,Vec3 d,int bottom,int top){}
    private record L(Vec3 a,Vec3 b,int c){}
    private record S(Vec3 p,float size,int color,boolean ground){}
    private static final List<L> overlayLines=new ArrayList<>();
    private static final List<Q> quads=new ArrayList<>();private static final List<L> lines=new ArrayList<>();private static final List<S> sprites=new ArrayList<>();
    private static Vec3 camera;
    public static void begin(Vec3 cam){camera=cam;quads.clear();lines.clear();overlayLines.clear();sprites.clear();}
    public static int alpha(int c,float a){return (Math.clamp((int)(a*255),0,255)<<24)|(c&0xFFFFFF);}
    public static void line(Vec3 a,Vec3 b,int c){lines.add(new L(a,b,c));}
    public static void overlayBox(AABB b,int c){Vec3[] v={new Vec3(b.minX,b.minY,b.minZ),new Vec3(b.maxX,b.minY,b.minZ),new Vec3(b.maxX,b.minY,b.maxZ),new Vec3(b.minX,b.minY,b.maxZ),new Vec3(b.minX,b.maxY,b.minZ),new Vec3(b.maxX,b.maxY,b.minZ),new Vec3(b.maxX,b.maxY,b.maxZ),new Vec3(b.minX,b.maxY,b.maxZ)};for(int i=0;i<4;i++){overlayLines.add(new L(v[i],v[(i+1)%4],c));overlayLines.add(new L(v[i+4],v[(i+1)%4+4],c));overlayLines.add(new L(v[i],v[i+4],c));}}
    public static void quad(Vec3 a,Vec3 b,Vec3 c,Vec3 d,int low,int high){quads.add(new Q(a,b,c,d,low,high));}
    public static void glow(Vec3 p,float size,int c){if(sprites.size()<1500)sprites.add(new S(p,size,c,false));}
    public static void pool(Vec3 p,float size,int c){if(sprites.size()<1500)sprites.add(new S(p,size,c,true));}
    public static void ring(Vec3 p,double radius,double height,int c){Vec3 last=null;for(int i=0;i<=64;i++){double a=i*Math.PI/32;Vec3 next=p.add(Math.cos(a)*radius,height,Math.sin(a)*radius);if(last!=null)line(last,next,c);last=next;}}
    public static void box(AABB b,int c,float fill,boolean gradient){
        Vec3[] v={new Vec3(b.minX,b.minY,b.minZ),new Vec3(b.maxX,b.minY,b.minZ),new Vec3(b.maxX,b.minY,b.maxZ),new Vec3(b.minX,b.minY,b.maxZ),new Vec3(b.minX,b.maxY,b.minZ),new Vec3(b.maxX,b.maxY,b.minZ),new Vec3(b.maxX,b.maxY,b.maxZ),new Vec3(b.minX,b.maxY,b.maxZ)};
        for(int i=0;i<4;i++){line(v[i],v[(i+1)%4],c);line(v[i+4],v[(i+1)%4+4],gradient?alpha(c,.3f):c);line(v[i],v[i+4],c);if(fill>0)quad(v[i],v[(i+1)%4],v[(i+1)%4+4],v[i+4],alpha(c,fill),alpha(c,gradient?fill*.07f:fill));}
    }
    private static void vertex(BufferBuilder b,Vec3 p,int c){b.addVertex((float)(p.x-camera.x),(float)(p.y-camera.y),(float)(p.z-camera.z)).setColor(c);}
    public static void flush(){
        RenderSystem.enableDepthTest();RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();
        if(!quads.isEmpty()){
            RenderSystem.setShader(GameRenderer::getPositionColorShader);var b=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);
            for(Q q:quads){vertex(b,q.a,q.bottom);vertex(b,q.b,q.bottom);vertex(b,q.c,q.top);vertex(b,q.d,q.top);}BufferUploader.drawWithShader(b.buildOrThrow());
        }
        if(!lines.isEmpty()){
            RenderSystem.setShader(GameRenderer::getPositionColorShader);var b=Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES,DefaultVertexFormat.POSITION_COLOR);
            for(L l:lines){vertex(b,l.a,l.c);vertex(b,l.b,l.c);}BufferUploader.drawWithShader(b.buildOrThrow());
        }
        if(!overlayLines.isEmpty()){
            RenderSystem.disableDepthTest();RenderSystem.setShader(GameRenderer::getPositionColorShader);
            var b=Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES,DefaultVertexFormat.POSITION_COLOR);
            for(L l:overlayLines){vertex(b,l.a,l.c);vertex(b,l.b,l.c);}BufferUploader.drawWithShader(b.buildOrThrow());RenderSystem.enableDepthTest();
        }
        if(!sprites.isEmpty()){
            var mc=Minecraft.getInstance();RenderSystem.setShader(GameRenderer::getPositionTexColorShader);RenderSystem.setShaderTexture(0,ExampleModClient.id("textures/effects/soft.png"));
            var rotation=mc.gameRenderer.getMainCamera().rotation();Vector3f right=new Vector3f(1,0,0).rotate(rotation),up=new Vector3f(0,1,0).rotate(rotation);
            var b=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX_COLOR);
            for(S s:sprites){Vec3 r=s.ground?new Vec3(s.size,0,0):new Vec3(right).scale(s.size);Vec3 u=s.ground?new Vec3(0,0,s.size):new Vec3(up).scale(s.size);spriteVertex(b,s.p.subtract(r).subtract(u),0,1,s.color);spriteVertex(b,s.p.add(r).subtract(u),1,1,s.color);spriteVertex(b,s.p.add(r).add(u),1,0,s.color);spriteVertex(b,s.p.subtract(r).add(u),0,0,s.color);}
            BufferUploader.drawWithShader(b.buildOrThrow());
        }
        RenderSystem.depthMask(true);RenderSystem.enableCull();RenderSystem.disableBlend();
    }
    private static void spriteVertex(BufferBuilder b,Vec3 p,float u,float v,int c){b.addVertex((float)(p.x-camera.x),(float)(p.y-camera.y),(float)(p.z-camera.z)).setUv(u,v).setColor(c);}
}
