package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private String dragging; private int dragW,dragH;
    private double offsetX,offsetY;
    public HudEditorScreen(Screen parent){super(Component.literal("Sollas HUD editor"));this.parent=parent;}
    @Override protected void init(){
        addRenderableWidget(new LiquidButton(width/2-55,height-43,110,30,19,()->"Done","",this::onClose,()->false,false));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        if(minecraft.level==null)renderPanorama(g,dt);
        GlassRenderer.begin(g);
        g.fill(0,0,width,height,0x660A1224);
        for(int x=0;x<width;x+=24)for(int y=0;y<height;y+=24)g.fill(x,y,x+1,y+1,0x28BDCAE5);
        for(var b:ClientHud.tileBounds(width,height)) LiquidUi.flat(g,b.x()-2,b.y()-2,b.width()+4,b.height()+4,12,b.id().equals(dragging)?0x668C9FFF:0x337F93C1);
        ClientHud.render(g,true);
        String hint="Drag each card · Layout saves automatically";
        LiquidUi.text(g,hint,(width-LiquidUi.width(hint,10,false))/2,height-62,10,LiquidUi.TEXT,false);
        super.render(g,mx,my,dt);
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float dt){}
    @Override public boolean mouseClicked(double x,double y,int button){
        if(button==0){var tiles=ClientHud.tileBounds(width,height);for(int i=tiles.size()-1;i>=0;i--){var b=tiles.get(i);
            if(x>=b.x()&&x<=b.x()+b.width()&&y>=b.y()&&y<=b.y()+b.height()){
                dragging=b.id();dragW=b.width();dragH=b.height();offsetX=x-b.x();offsetY=y-b.y();return true;
            }
        }}return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){
        if(dragging!=null){int nx=(int)(x-offsetX),ny=(int)(y-offsetY);
            if(Math.abs(nx-4)<7)nx=4;if(Math.abs(ny-4)<7)ny=4;
            if(Math.abs(nx-(width-dragW-4))<7)nx=width-dragW-4;if(Math.abs(ny-(height-dragH-4))<7)ny=height-dragH-4;
            ClientHud.place(dragging,nx,ny,width,height,dragW,dragH);return true;}
        return super.mouseDragged(x,y,button,dx,dy);
    }
    @Override public boolean mouseReleased(double x,double y,int button){dragging=null;ClientSettings.INSTANCE.save();return super.mouseReleased(x,y,button);}
    @Override public void onClose(){ClientSettings.INSTANCE.save();minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
