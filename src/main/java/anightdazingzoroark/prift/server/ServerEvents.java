package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.server.config.RiftGeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
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
}
