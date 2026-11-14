package com.example.examplemod;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** A local-only entity: never spawned on the server and attacks are intercepted before networking. */
public final class FakePlayerManager {
    private static TestPlayer fake;private static ClientLevel owner;private static int deathTicks;
    public static void tick(){
        var mc=Minecraft.getInstance();
        if(owner!=null&&(owner!=mc.level||!ModuleSettings.on("fake")||mc.player==null))remove();
        if(!ModuleSettings.on("fake")||mc.level==null||mc.player==null)return;
        if(fake==null){
            ClientLevel level=mc.level;int id=-100000;while(level.getEntity(id)!=null&&id>Integer.MIN_VALUE+1)id--;
            var npc=new TestPlayer(level,new GameProfile(UUID.randomUUID(),"Sollas_Test"));npc.setId(id);
            Vec3 dir=mc.player.getLookAngle();Vec3 horizontal=new Vec3(dir.x,0,dir.z).normalize();if(horizontal.lengthSqr()<.001)horizontal=new Vec3(0,0,1);
            Vec3 pos=mc.player.position().add(horizontal.scale(ModuleSettings.v("fake","distance")));
            npc.setPos(pos.x,pos.y,pos.z);npc.setYRot(mc.player.getYRot()+180);npc.setYHeadRot(npc.getYRot());npc.yBodyRot=npc.getYRot();npc.setHealth((float)ModuleSettings.v("fake","health"));
                        int equipment=(int)ModuleSettings.v("fake","equipment");
            var slots=new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
            net.minecraft.world.item.Item[][] armor={ {},{}, {net.minecraft.world.item.Items.IRON_HELMET,net.minecraft.world.item.Items.IRON_CHESTPLATE,net.minecraft.world.item.Items.IRON_LEGGINGS,net.minecraft.world.item.Items.IRON_BOOTS},{net.minecraft.world.item.Items.DIAMOND_HELMET,net.minecraft.world.item.Items.DIAMOND_CHESTPLATE,net.minecraft.world.item.Items.DIAMOND_LEGGINGS,net.minecraft.world.item.Items.DIAMOND_BOOTS},{net.minecraft.world.item.Items.NETHERITE_HELMET,net.minecraft.world.item.Items.NETHERITE_CHESTPLATE,net.minecraft.world.item.Items.NETHERITE_LEGGINGS,net.minecraft.world.item.Items.NETHERITE_BOOTS} };
            for(int i=0;i<slots.length;i++)if(equipment==0)npc.setItemSlot(slots[i],mc.player.getItemBySlot(slots[i]).copy());else if(equipment>=2)npc.setItemSlot(slots[i],new net.minecraft.world.item.ItemStack(armor[equipment][i]));
            level.addEntity(npc);if(level.getEntity(id)==npc){owner=level;fake=npc;deathTicks=0;}
        }
        if(fake!=null&&fake.getHealth()<=0){fake.deathTime=1;if(++deathTicks>=35){fake.setHealth((float)ModuleSettings.v("fake","health"));fake.deathTime=0;deathTicks=0;WorldModules.forgetTestEntity(fake.getId());}}
    }
    private static void remove(){if(owner!=null&&fake!=null&&owner.getEntity(fake.getId())==fake)owner.removeEntity(fake.getId(),Entity.RemovalReason.DISCARDED);fake=null;owner=null;deathTicks=0;}
    public static void recreate(){remove();tick();}
    public static boolean isFake(Entity e){return fake!=null&&e==fake;}
    public static void hit(){
        if(fake==null||fake.getHealth()<=0)return;
        fake.setHealth(Math.max(0,fake.getHealth()-(float)ModuleSettings.v("fake","damage")));fake.hurtTime=10;fake.hurtDuration=10;
        ModuleRuntime.target=fake;ModuleRuntime.attackedAt=System.nanoTime();ModuleRuntime.attackDelay=(int)ModuleSettings.v("sprint","delay");
    }
    private static final class TestPlayer extends RemotePlayer {
        TestPlayer(ClientLevel level,GameProfile profile){super(level,profile);}
        @Override public PlayerSkin getSkin(){var p=Minecraft.getInstance().player;return p!=null?p.getSkin():super.getSkin();}
    }
}
