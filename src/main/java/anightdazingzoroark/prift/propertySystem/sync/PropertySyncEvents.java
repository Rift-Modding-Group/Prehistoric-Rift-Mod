package anightdazingzoroark.prift.propertySystem.sync;

import anightdazingzoroark.prift.propertySystem.Property;
import anightdazingzoroark.prift.propertySystem.networking.PropertiesNetworking;
import anightdazingzoroark.prift.propertySystem.networking.SPacketPropsFull;
import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.prift.propertySystem.registry.PropertyRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PropertySyncEvents {
    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        EntityPlayerMP watcher = (EntityPlayerMP) event.getEntityPlayer();

        for (String name : PropertyRegistry.getAllPropertyNames()) {
            this.syncSetTo(target, watcher, name);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayerMP player)) return;

        //ensure player receives their own full state
        for (String name : PropertyRegistry.getAllPropertyNames()) {
            if (!PropertyRegistry.entityCanHaveProperty(name, player)) continue;
            this.syncSetTo(player, player, name);
        }
    }

    private void syncSetTo(Entity target, EntityPlayerMP watcher, String setKey) {
        AbstractEntityProperties set = Property.getProperty(setKey, target);
        if (set == null) return;

        PropertiesNetworking.channel.sendTo(
                new SPacketPropsFull(target.getEntityId(), setKey, set.writeAllToNBT()),
                watcher
        );
    }
}
