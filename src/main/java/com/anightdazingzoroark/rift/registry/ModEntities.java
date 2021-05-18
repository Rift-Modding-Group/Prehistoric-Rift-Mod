package com.anightdazingzoroark.rift.registry;

import com.anightdazingzoroark.rift.InitializeServer;
import com.anightdazingzoroark.rift.entities.TyrannosaurusEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import static com.anightdazingzoroark.rift.InitializeServer.MOD_ID;

public class ModEntities {
    public static final EntityType<TyrannosaurusEntity> TYRANNOSAURUS = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MOD_ID, "tyrannosaurus"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TyrannosaurusEntity::new).dimensions(EntityDimensions.fixed(1f,1f)).build()
    );

    public static void registerEntities() {
        System.out.println("say hi");
        FabricDefaultAttributeRegistry.register(TYRANNOSAURUS, TyrannosaurusEntity.createMobAttributes());
    }
}
