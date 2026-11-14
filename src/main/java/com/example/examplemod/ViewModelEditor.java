package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class ViewModelEditor {
    private static boolean right=true,dragging;
    private static boolean valid(ScreenEvent e){return ModuleSettings.on("view")&&e.getScreen() instanceof ChatScreen;}
    public static void init(ScreenEvent.Init.Post e){dragging=false;if(!valid(e))return;
        int x=e.getScreen().width/2-115;
        e.addListener(new LiquidButton(x,12,110,27,8,()->"Left hand","",()->right=false,()->!right,false));
        e.addListener(new LiquidButton(x+120,12,110,27,8,()->"Right hand","",()->right=true,()->right,false));
    }
    public static void draw(ScreenEvent.Render.Post e){if(!valid(e))return;var g=e.getGuiGraphics();LiquidUi.text(g,"Drag the open area to move "+(right?"RIGHT":"LEFT")+" hand · Wheel = depth",12,48,10,LiquidUi.TEXT,false);}
    public static void click(ScreenEvent.MouseButtonPressed.Pre e){if(valid(e)&&e.getButton()==0&&e.getMouseY()>65&&e.getMouseY()<e.getScreen().height-55){dragging=true;e.setCanceled(true);}}
    public static void drag(ScreenEvent.MouseDragged.Pre e){if(valid(e)&&dragging){String side=right?"r":"l";ModuleSettings.set("view",side+"x",ModuleSettings.v("view",side+"x")+e.getDragX()*.006);ModuleSettings.set("view",side+"y",ModuleSettings.v("view",side+"y")-e.getDragY()*.006);e.setCanceled(true);}}
    public static void scroll(ScreenEvent.MouseScrolled.Pre e){if(valid(e)&&e.getMouseY()<e.getScreen().height-55){String key=(right?"r":"l")+"z";ModuleSettings.set("view",key,ModuleSettings.v("view",key)+e.getScrollDeltaY()*.05);ModuleSettings.save();e.setCanceled(true);}}
    public static void release(ScreenEvent.MouseButtonReleased.Pre e){if(dragging){dragging=false;ModuleSettings.save();if(valid(e))e.setCanceled(true);}}
}
