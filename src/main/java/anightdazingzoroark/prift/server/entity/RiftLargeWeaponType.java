package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCannon;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftMortar;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.items.RiftLargeWeaponItem;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.function.Function;

public enum RiftLargeWeaponType {
    NONE(null, null),
    CANNON(RiftCannon.class, RiftCannon::new),
    MORTAR(RiftMortar.class, RiftMortar::new),
    CATAPULT(RiftCatapult.class, RiftCatapult::new);

    private final Class<? extends RiftLargeWeapon> weaponClass;
    private final Function<World, RiftLargeWeapon> weaponConstructor;

    RiftLargeWeaponType(Class<? extends RiftLargeWeapon> weaponClass, Function<World, RiftLargeWeapon> weaponConstructor) {
        this.weaponClass = weaponClass;
        this.weaponConstructor = weaponConstructor;
    }

    public Class<? extends RiftLargeWeapon> getWeaponClass() {
        return this.weaponClass;
    }

    public Function<World, RiftLargeWeapon> getConstructor() {
        return this.weaponConstructor;
    }
}
