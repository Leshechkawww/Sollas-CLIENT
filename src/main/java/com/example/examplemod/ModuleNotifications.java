package com.example.examplemod;
import net.minecraft.client.gui.GuiGraphics;
import java.util.ArrayDeque;
public final class ModuleNotifications {
 private record Notice(String text,boolean enabled,long time){}
 private static final ArrayDeque<Notice> queue=new ArrayDeque<>();
 public static void push(String text,boolean enabled){if(queue.size()>=4)queue.removeFirst();queue.addLast(new Notice(text,enabled,System.nanoTime()));}
 public static void render(GuiGraphics g){long now=System.nanoTime();while(!queue.isEmpty()&&now-queue.peekFirst().time()>2_600_000_000L)queue.removeFirst();int y=20;
  for(var n:queue){float age=(now-n.time())/1e9f,alpha=Math.min(1,Math.min(age/.18f,(2.6f-age)/.3f));int w=150,x=g.guiWidth()-w-8;
   LiquidUi.flat(g,x,y,w,31,8,((int)(alpha*210)<<24)|0x18233A);LiquidUi.icon(g,14,x+8,y+7,15,n.enabled()?0xFF83E5BD:0xFFEA9BAC);
   LiquidUi.text(g,LiquidUi.fit(n.text(),115,10),x+29,y+6,10,0xFFF3F5FF,true);LiquidUi.text(g,n.enabled()?"Enabled":"Disabled",x+29,y+19,8,0xFFB7C6DE,false);y+=36;
  }
 }
}
