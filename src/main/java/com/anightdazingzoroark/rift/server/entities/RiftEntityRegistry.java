package com.anightdazingzoroark.rift.server.entities;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.server.entities.creatures.TyrannosaurusEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RiftEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,
        RiftInitialize.MODID);

    public static final RegistryObject<EntityType<TyrannosaurusEntity>> TYRANNOSAURUS = ENTITY_TYPES
            .register("tyrannosaurus",
                    () -> EntityType.Builder.<TyrannosaurusEntity>of(TyrannosaurusEntity::new, MobCategory.CREATURE)
                            .sized(4F, 6F)
                            .build(new ResourceLocation(RiftInitialize.MODID, "tyrannosaurus").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
