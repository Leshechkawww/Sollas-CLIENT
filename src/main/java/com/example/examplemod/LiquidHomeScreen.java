package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.neoforged.neoforge.client.gui.ModListScreen;

/** Branded entry point; gameplay and multiplayer restrictions remain vanilla's. */
public final class LiquidHomeScreen extends TitleScreen {
    private int x,y;private float scale;
    private final long opened=System.nanoTime();
    private static boolean introPlayed; private long introStart;
    @Override protected void init(){
        if(!introPlayed){introPlayed=true;introStart=System.nanoTime();}
        scale=Math.min(1f,Math.min((width-20)/470f,(height-20)/330f));
        x=(int)((width/scale-470)/2);y=(int)((height/scale-330)/2);
        addRenderableWidget(new LiquidButton(x+26,y+117,204,45,15,()->"Singleplayer","Your worlds",()->minecraft.setScreen(new SelectWorldScreen(this)),()->false,false));
        var online=new LiquidButton(x+240,y+117,204,45,16,()->"Multiplayer","Play together",()->minecraft.setScreen(minecraft.options.skipMultiplayerWarning?new JoinMultiplayerScreen(this):new SafetyScreen(this)),()->false,false);
        online.active=minecraft.allowsMultiplayer();addRenderableWidget(online);
        addRenderableWidget(new LiquidButton(x+26,y+173,418,45,14,()->"Open workspace","ClickGUI, liquid style and HUD",()->minecraft.setScreen(new ClickGuiScreen(this)),()->true,false));
        addRenderableWidget(new LiquidButton(x+26,y+231,132,33,17,()->"Options","",()->minecraft.setScreen(new OptionsScreen(this,minecraft.options)),()->false,false));
        addRenderableWidget(new LiquidButton(x+169,y+231,132,33,12,()->"Mods","",()->minecraft.setScreen(new ModListScreen(this)),()->false,false));
        addRenderableWidget(new LiquidButton(x+312,y+231,132,33,18,()->"Quit","",()->minecraft.stop(),()->false,false));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        renderPanorama(g,dt);
        if(introStart!=0){float t=(System.nanoTime()-introStart)/1_000_000_000f;if(t<3.1f){renderIntro(g,t);return;}introStart=0;}
        GlassRenderer.begin(g);g.fill(0,0,width,height,0x600B1425);
        g.pose().pushPose();g.pose().scale(scale,scale,1);
        float a=ClientSettings.INSTANCE.animations?Math.clamp((System.nanoTime()-opened)/350_000_000f,0,1):1;
        GlassRenderer.opacity=a;
        LiquidUi.surface(g,x,y,470,296,23,0xA9132035);
        
        LiquidUi.logo(g,x+29,y+26,36,ClientSettings.INSTANCE.color());
        LiquidUi.text(g,"Sollas CLIENT",x+83,y+34,23,LiquidUi.TEXT,true);
        
        LiquidUi.text(g,"A little clarity. A different perspective.",x+26,y+91,11,LiquidUi.MUTED,false);
        LiquidUi.text(g,"MINECRAFT 1.21.1  /  NEOFORGE",x+26,y+278,8,LiquidUi.MUTED,true);
        // Render only our widgets, not the vanilla logo and buttons.
        for(var child:children())if(child instanceof net.minecraft.client.gui.components.Renderable r)r.render(g,(int)(mx/scale),(int)(my/scale),dt);
        GlassRenderer.opacity=1;g.flush();g.pose().popPose();
    }
    private void renderIntro(GuiGraphics g,float t){
        float fade=t<.35f?t/.35f:t>2.6f?Math.max(0,(3.1f-t)/.5f):1;
        int cx=width/2,cy=height/2;g.fill(0,0,width,height,((int)(fade*225)<<24)|0x090415);
        float pulse=(float)(1+.08*Math.sin(t*5));int size=(int)(35*pulse);
        g.pose().pushPose();g.pose().translate(cx,cy,0);g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(t*18));
        g.setColor(1f,1f,1f,fade);LiquidUi.logo(g,-size*2,-size*2,size*4,0xFFFFFFFF);g.setColor(1f,1f,1f,1f);
        g.pose().popPose();
        String name="Sollas CLIENT";LiquidUi.text(g,name,(width-LiquidUi.width(name,12,true))/2,cy+size+22,12,((int)(fade*255)<<24)|0xE7D9FF,true);
        int bw=Math.min(160,width-40), bx=(width-bw)/2, by=cy+size+45;
        float progress=Math.clamp(t/2.6f,0,1); progress=1-(1-progress)*(1-progress);
        LiquidUi.flat(g,bx,by,bw,4,2,((int)(fade*100)<<24)|0x55476F);
        if(progress>.01f)LiquidUi.flat(g,bx,by,Math.max(2,(int)(bw*progress)),4,2,((int)(fade*255)<<24)|0xAF78FF);
        String phase="STARTING INTERFACE";LiquidUi.text(g,phase,(width-LiquidUi.width(phase,8,true))/2,by+10,8,((int)(fade*210)<<24)|0xBEABD6,true);
    }
    @Override public boolean mouseClicked(double x,double y,int b){return introStart==0 && super.mouseClicked(x/scale,y/scale,b);}
    @Override public boolean mouseReleased(double x,double y,int b){return super.mouseReleased(x/scale,y/scale,b);}
    @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){return super.mouseDragged(x/scale,y/scale,b,dx/scale,dy/scale);}
}
