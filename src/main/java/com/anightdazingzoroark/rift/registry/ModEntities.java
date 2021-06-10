package com.anightdazingzoroark.rift.registry;

import com.anightdazingzoroark.rift.InitializeServer;
import com.anightdazingzoroark.rift.entities.Creatures.TyrannosaurusEntity;
import com.anightdazingzoroark.rift.entities.Eggs.TyrannosaurusEgg;
import com.anightdazingzoroark.rift.entities.RiftEgg;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEntities {
    public static final EntityType<TyrannosaurusEntity> TYRANNOSAURUS = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(InitializeServer.MOD_ID, "tyrannosaurus"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TyrannosaurusEntity::new).dimensions(EntityDimensions.fixed(2.5f,3.5f)).build()
    );

    public static final EntityType<TyrannosaurusEgg> TYRANNOSAURUS_EGG = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(InitializeServer.MOD_ID, "tyrannosaurus_egg"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TyrannosaurusEgg::new).dimensions(EntityDimensions.fixed(1f,1f)).build()
    );

    public static void registerEntities() {
        FabricDefaultAttributeRegistry.register(ModEntities.TYRANNOSAURUS, TyrannosaurusEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.TYRANNOSAURUS_EGG, RiftEgg.createAttributes());
    }
}