package anightdazingzoroark.prift.propertySystem;

import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.prift.propertySystem.registry.PropertiesBootstrap;
import anightdazingzoroark.prift.propertySystem.registry.PropertiesRoot;
import anightdazingzoroark.prift.propertySystem.registry.PropertyRegistry;
import anightdazingzoroark.prift.server.properties.RiftPropertyRegistry;
import net.minecraft.entity.Entity;

public class Property {
    public static <T extends AbstractEntityProperties<?>> T getProperty(String name, Entity entity) {
        PropertiesRoot root = entity.getCapability(PropertiesBootstrap.CAP, null);
        if (root == null) return null;
        return root.getOrCreate(name, entity);
    }
}
