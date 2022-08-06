package com.anightdazingzoroark.rift.server.events;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.server.entities.RiftEntityRegistry;
import com.anightdazingzoroark.rift.server.entities.creatures.TyrannosaurusEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RiftInitialize.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RiftEventBusEvents {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(RiftEntityRegistry.TYRANNOSAURUS.get(), TyrannosaurusEntity.createAttributes().build());
    }
}
