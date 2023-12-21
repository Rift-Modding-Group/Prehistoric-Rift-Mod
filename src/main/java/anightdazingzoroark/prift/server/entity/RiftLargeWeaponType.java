package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;

public enum RiftLargeWeaponType {
    CANNON(RiftLargeWeapon.class),
    MORTAR(RiftLargeWeapon.class),
    CATAPULT(RiftLargeWeapon.class);

    private final Class<? extends RiftLargeWeapon> weapon;

    RiftLargeWeaponType(Class<? extends RiftLargeWeapon> weapon) {
        this.weapon = weapon;
    }

    public Class<? extends RiftLargeWeapon> getWeapon() {
        return this.weapon;
    }
}
