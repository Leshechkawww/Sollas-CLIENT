package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;

public final class ModuleHud {
    public static void render(GuiGraphics g,float partial){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui)return;
        if(ModuleSettings.on("hp")){
            float health=mc.player.getHealth();float threshold=(float)ModuleSettings.v("hp","threshold");
            float a=Math.clamp((threshold-health)/threshold,0,1)*(float)ModuleSettings.v("hp","strength");
            if(a>0){int depth=Math.max(25,g.guiHeight()/4);for(int i=0;i<depth;i+=2){int color=((int)(a*155*Math.pow(1-i/(float)depth,2))<<24)|0xBA0C24;g.fill(i,i,g.guiWidth()-i,i+2,color);g.fill(i,g.guiHeight()-i-2,g.guiWidth()-i,g.guiHeight()-i,color);g.fill(i,i+2,i+2,g.guiHeight()-i-2,color);g.fill(g.guiWidth()-i-2,i+2,g.guiWidth()-i,g.guiHeight()-i-2,color);}}
        }
        if(ModuleSettings.on("cross")&&mc.options.getCameraType().isFirstPerson()&&mc.screen==null){
            int x=g.guiWidth()/2,y=g.guiHeight()/2;
            float hit=Math.max(0,1-(System.nanoTime()-ModuleRuntime.attackedAt)/350_000_000f);
            if(!ModuleSettings.b("cross","animate"))hit=0;
            int size=(int)ModuleSettings.v("cross","size"),gap=(int)(ModuleSettings.v("cross","gap")+hit*5),thick=(int)ModuleSettings.v("cross","width");
            int c=ModuleSettings.b("cross","theme")?ClientSettings.INSTANCE.color():0xFF000000|java.awt.Color.HSBtoRGB((float)ModuleSettings.v("cross","hue"),.7f,1);
            switch(ModuleSettings.mode("cross")){
                case 0->{g.fill(x-gap-size,y-thick,x-gap,y+thick,c);g.fill(x+gap,y-thick,x+gap+size,y+thick,c);g.fill(x-thick,y-gap-size,x+thick,y-gap,c);g.fill(x-thick,y+gap,x+thick,y+gap+size,c);}
                case 1->{ring(g,x,y,size+gap+hit*3,thick,c);g.fill(x-1,y-1,x+1,y+1,c);}
                default->{ring(g,x,y,Math.max(1,size*.35f),Math.max(1,(int)(size*.35f)),c);if(hit>0)ring(g,x,y,size+hit*8,1,((int)(hit*255)<<24)|(c&0xFFFFFF));}
            }
        }
        if(ModuleSettings.on("predict")&&Prediction.lastImpact!=null&&mc.screen==null){String text=Prediction.description;LiquidUi.text(g,text,(g.guiWidth()-LiquidUi.width(text,9,false))/2,g.guiHeight()/2+26,9,ClientSettings.INSTANCE.color(),false);}
        if(ModuleSettings.on("kill")&&ModuleSettings.b("kill","screen"))WorldModules.screenBlood(g);
        TargetHud.render(g);
        ModuleNotifications.render(g);
    }
    private static void ring(GuiGraphics g,int x,int y,float r,int thickness,int color){for(int i=0;i<64;i++){double a=i*Math.PI/32;int px=x+(int)Math.round(Math.cos(a)*r),py=y+(int)Math.round(Math.sin(a)*r);g.fill(px-thickness/2,py-thickness/2,px+(thickness+1)/2,py+(thickness+1)/2,color);}}
}
