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
    NONE(null, null, null),
    CANNON(RiftCannon.class, RiftCannon::new, RiftItems.CANNON),
    MORTAR(RiftMortar.class, RiftMortar::new, RiftItems.MORTAR),
    CATAPULT(RiftCatapult.class, RiftCatapult::new, RiftItems.CATAPULT);

    private final Class<? extends RiftLargeWeapon> weaponClass;
    private final Function<World, RiftLargeWeapon> weaponConstructor;
    private final RiftLargeWeaponItem item;

    RiftLargeWeaponType(Class<? extends RiftLargeWeapon> weaponClass, Function<World, RiftLargeWeapon> weaponConstructor, Item item) {
        this.weaponClass = weaponClass;
        this.weaponConstructor = weaponConstructor;
        this.item = (RiftLargeWeaponItem) item;
    }

    public Class<? extends RiftLargeWeapon> getWeaponClass() {
        return this.weaponClass;
    }

    public Function<World, RiftLargeWeapon> getConstructor() {
        return this.weaponConstructor;
    }

    public RiftLargeWeaponItem getItem() {
        return this.item;
    }
}
