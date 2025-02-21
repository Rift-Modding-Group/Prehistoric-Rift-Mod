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
    NONE(null, null, 0, 0),
    CANNON(RiftCannon.class, RiftCannon::new, 0, 30),
    MORTAR(RiftMortar.class, RiftMortar::new, 30, 60),
    CATAPULT(RiftCatapult.class, RiftCatapult::new, 30, 60);

    private final Class<? extends RiftLargeWeapon> weaponClass;
    private final Function<World, RiftLargeWeapon> weaponConstructor;
    public final int maxUse;
    public final int maxCooldown;

    RiftLargeWeaponType(Class<? extends RiftLargeWeapon> weaponClass, Function<World, RiftLargeWeapon> weaponConstructor, int maxUse, int maxCooldown) {
        this.weaponClass = weaponClass;
        this.weaponConstructor = weaponConstructor;
        this.maxUse = maxUse;
        this.maxCooldown = maxCooldown;
    }

    public Class<? extends RiftLargeWeapon> getWeaponClass() {
        return this.weaponClass;
    }

    public Function<World, RiftLargeWeapon> getConstructor() {
        return this.weaponConstructor;
    }
}
