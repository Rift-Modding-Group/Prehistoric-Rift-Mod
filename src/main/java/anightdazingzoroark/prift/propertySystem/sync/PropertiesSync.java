package anightdazingzoroark.prift.propertySystem.sync;

import anightdazingzoroark.prift.propertySystem.Property;
import anightdazingzoroark.prift.propertySystem.networking.PropertiesNetworking;
import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import net.minecraft.entity.Entity;

public class PropertiesSync {
    //server -> client full sync of a set
    public static void syncSet(Entity entity, String setKey) {
        if (entity.world.isRemote) return;

        AbstractEntityProperties set = Property.getProperty(setKey, entity);
        if (set == null) return;

        PropertiesNetworking.sendFull(entity, setKey, set.writeAllToNBT());
    }

    //server -> client delta sync of ONE property inside a set
    public static void syncProp(Entity entity, String setKey, String propKey) {
        if (entity.world.isRemote) return;

        AbstractEntityProperties set = Property.getProperty(setKey, entity);
        if (set == null) return;

        PropertiesNetworking.sendDelta(entity, setKey, propKey, set.writeOneToNBT(propKey));
    }
}
