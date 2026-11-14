package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.function.Supplier;

public final class LiquidButton extends AbstractWidget {
    private final Supplier<String> label;
    private final String detail;
    private final int icon;
    private final Runnable action;
    private final Supplier<Boolean> selected;
    private final boolean toggle;
    private final LiquidUi.Motion hover=new LiquidUi.Motion(0),selection=new LiquidUi.Motion(0);
    public LiquidButton(int x,int y,int w,int h,int icon,Supplier<String> label,String detail,Runnable action,Supplier<Boolean> selected,boolean toggle){
        super(x,y,w,h,Component.literal(label.get()));this.icon=icon;this.label=label;this.detail=detail;this.action=action;this.selected=selected;this.toggle=toggle;
    }
    @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){
        float hot=hover.to(isHoveredOrFocused()?1:0),on=selection.to(selected.get()?1:0);
        int accent=ClientSettings.INSTANCE.color();
        int bg=LiquidUi.mix(0x721B293D,0xAF293F5B,hot);
        bg=LiquidUi.mix(bg,0xC1394266,on*.7f);
        LiquidUi.surface(g,getX(),getY(),width,height,10,bg);
        LiquidUi.icon(g,icon,getX()+11,getY()+(height-16)/2+4,16,LiquidUi.mix(LiquidUi.MUTED,accent,Math.max(hot,on)));
        float tx=getX()+35;
        float titleY = detail.isEmpty() ? getY()+(height-9)/2f+2 : getY()+(height-23)/2f+2;
        LiquidUi.text(g,label.get(),tx,titleY,11,LiquidUi.TEXT,true);
        if(!detail.isEmpty())LiquidUi.text(g,LiquidUi.fit(detail,width-80,9),tx,titleY+14,9,LiquidUi.MUTED,false);
        if(toggle){
            int sx=getX()+width-42,sy=getY()+height/2-7;
            LiquidUi.flat(g,sx,sy,28,14,7,LiquidUi.mix(0xFF43516A,accent,on));
            LiquidUi.flat(g,sx+2+(int)(on*14),sy+2,10,10,5,0xFFF1F5FF);
        }else if(width>150)LiquidUi.icon(g,10,getX()+width-24,getY()+(height-12)/2+4,12,LiquidUi.MUTED);
        setMessage(Component.literal(label.get()+(toggle?(selected.get()?", enabled":", disabled"):"")));
    }
    @Override public void onClick(double x,double y){action.run();}
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(isFocused()&&(key==GLFW.GLFW_KEY_ENTER||key==GLFW.GLFW_KEY_SPACE||key==GLFW.GLFW_KEY_KP_ENTER)){action.run();return true;}
        return super.keyPressed(key,scan,mods);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput output){defaultButtonNarrationText(output);}
}
