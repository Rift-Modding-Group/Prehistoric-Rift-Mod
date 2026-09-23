package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.server.config.RiftGeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class ServerEvents {
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        //make people join le discord
        if (!RiftGeneralConfig.other.showJoinDiscordMessage) return;
        TextComponentString message = new TextComponentString("Click here to join the Discord server for this mod to hang out and receive updates! We beg you!");
        message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/JnjQtkVt8R"));
        message.getStyle().setUnderlined(true);
        event.player.sendMessage(message);
    }

    @SubscribeEvent
    public void livingDropsEvent(LivingDropsEvent event) {
        //to reduce potential lag, mobs killed by wild creatures will not drop items
        if (event.getSource().getTrueSource() instanceof RiftCreature && RiftGeneralConfig.creatures.creatureKillNoLoot) {
            Entity attacked = event.getEntity();
            boolean tameableFlag = attacked instanceof EntityTameable tameable && tameable.isTamed();
            boolean playerFlag = attacked instanceof EntityPlayer;
            event.setCanceled(!tameableFlag && !playerFlag);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void gainCreatureXPFromKill(LivingDeathEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        if (!(event.getSource().getTrueSource() instanceof RiftCreature creature) || !creature.isTamed()) return;

        EntityLivingBase defeatedEntity = event.getEntityLiving();
        EntityPlayer owner = creature.getOwner() instanceof EntityPlayer player ? player : null;
        creature.addXP(defeatedEntity.getExperiencePoints(owner));
    }

    @SubscribeEvent
    public void redirectCreatureKillXP(LivingExperienceDropEvent event) {
        if (event.getEntityLiving().getLastDamageSource() != null
                && event.getEntityLiving().getLastDamageSource().getTrueSource() instanceof RiftCreature creature
                && creature.isTamed()
        ) {
            event.setDroppedExperience(0);
        }
    }
}
