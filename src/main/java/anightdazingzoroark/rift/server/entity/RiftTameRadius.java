package anightdazingzoroark.rift.server.entity;

import java.util.Arrays;
import java.util.List;

public class RiftTameRadius {
    public static List<RiftTameRadialChoice> getMain() {
        return Arrays.asList(RiftTameRadialChoice.INVENTORY, RiftTameRadialChoice.STATE, RiftTameRadialChoice.RIDE, RiftTameRadialChoice.BEHAVIOR);
    }

    public static List<RiftTameRadialChoice> getMainUnrideable() {
        return Arrays.asList(RiftTameRadialChoice.INVENTORY, RiftTameRadialChoice.STATE, RiftTameRadialChoice.BEHAVIOR);
    }

    public static List<RiftTameRadialChoice> getState() {
        return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.STAND, RiftTameRadialChoice.SIT, RiftTameRadialChoice.WANDER);
    }

    public static List<RiftTameRadialChoice> getBehavior() {
        return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.ASSIST, RiftTameRadialChoice.NEUTRAL, RiftTameRadialChoice.AGGRESSIVE, RiftTameRadialChoice.PASSIVE);
    }

    public static List<RiftTameRadialChoice> getBehaviorCanTurret() {
        return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.ASSIST, RiftTameRadialChoice.NEUTRAL, RiftTameRadialChoice.AGGRESSIVE, RiftTameRadialChoice.PASSIVE, RiftTameRadialChoice.TURRET);
    }

    public static List<RiftTameRadialChoice> getBehaviorTurretOnly() {
        return Arrays.asList(RiftTameRadialChoice.BACK,RiftTameRadialChoice.PASSIVE, RiftTameRadialChoice.TURRET);
    }
}
