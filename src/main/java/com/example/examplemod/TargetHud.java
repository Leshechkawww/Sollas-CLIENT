package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import java.util.Locale;
import java.util.UUID;

/** A screen-space player card; no health or inventory is inferred from server data. */
public final class TargetHud {
    private static UUID current;
    private static float shownHealth;
    private static long lastFrame;
    public static void render(GuiGraphics g){
        var mc=Minecraft.getInstance();
        if(!ModuleSettings.on("targethud")||mc.screen!=null||!(ModuleRuntime.hoverTarget instanceof Player p)||p.isRemoved()||!p.isAlive()) {current=null;return;}
        long now=System.nanoTime();float dt=lastFrame==0?0:Math.min(.1f,(now-lastFrame)/1_000_000_000f);lastFrame=now;
        float max=Math.max(1,p.getMaxHealth());float health=Math.clamp(p.getHealth(),0,max);
        if(!p.getUUID().equals(current)){current=p.getUUID();shownHealth=health;}else shownHealth+=(health-shownHealth)*(1-(float)Math.exp(-dt*10));
        float scale=Math.min((float)ModuleSettings.v("targethud","scale"),Math.max(.45f,(g.guiWidth()-12)/190f));
        int hudColor=ModuleSettings.v("targethud","colorMode")<.5?ClientSettings.INSTANCE.hudColor():0xFF000000|java.awt.Color.HSBtoRGB((float)ModuleSettings.v("targethud","hue"),.65f,1f);
        int w=188,h=68;int x=(int)((g.guiWidth()/scale-w)/2+ModuleSettings.v("targethud","offsetX"));x=Math.clamp(x,0,Math.max(0,(int)(g.guiWidth()/scale)-w));int y=(int)((g.guiHeight()/2f+24+ModuleSettings.v("targethud","height")*20)/scale);
        y=Math.min(y,(int)(g.guiHeight()/scale)-h-4);y=Math.max(4,y);
        GlassRenderer.begin(g);g.pose().pushPose();g.pose().scale(scale,scale,1);
        LiquidUi.surface(g,x,y,w,h,12,0xD2182338);
        LiquidUi.flat(g,x+8,y+8,36,36,8,0xAA34425E);
        if(p instanceof AbstractClientPlayer client){PlayerFaceRenderer.draw(g,client.getSkin(),x+10,y+10,32);}
        else LiquidUi.icon(g,2,x+16,y+16,20,hudColor);
        String name=LiquidUi.fit(p.getGameProfile().getName(),125,11);
        LiquidUi.text(g,name,x+50,y+9,11,LiquidUi.TEXT,true);
        String hp=String.format(Locale.ROOT,"%.1f / %.1f HP",health,max);
        LiquidUi.text(g,hp,x+50,y+25,9,hudColor,true);
        LiquidUi.flat(g,x+50,y+39,128,5,2,0xFF3B475F);
        int amount=Math.round(128*Math.clamp(shownHealth/max,0,1));if(amount>0)LiquidUi.flat(g,x+50,y+39,amount,5,2,hudColor);
        EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
        for(int i=0;i<slots.length;i++){ItemStack stack=p.getItemBySlot(slots[i]);int ix=x+50+i*21;
            if(!stack.isEmpty()){g.renderItem(stack,ix,y+48);g.renderItemDecorations(mc.font,stack,ix,y+48);}else LiquidUi.flat(g,ix+3,y+51,10,10,3,0x554B5972);
        }
        LiquidUi.text(g,"ARMOR "+p.getArmorValue(),x+136,y+51,8,LiquidUi.MUTED,true);
        g.flush();g.pose().popPose();
    }
}
