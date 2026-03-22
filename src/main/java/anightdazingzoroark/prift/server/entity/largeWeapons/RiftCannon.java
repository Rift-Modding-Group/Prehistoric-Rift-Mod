package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import anightdazingzoroark.riftlib.core.manager.AnimationData;

import javax.annotation.Nullable;

public class RiftCannon extends RiftLargeWeapon {
    public RiftCannon(World worldIn) {
        super(worldIn, RiftLargeWeaponType.CANNON, RiftItems.CANNON, RiftItems.CANNONBALL);
        this.setSize(1f, 1f);
    }

    @Override
    public void launchProjectile(EntityPlayer player, int charge) {
        RiftInventoryHandler.ItemSearchResult foundAmmoRes = this.weaponInventory.findItem(
                RiftInventoryHandler.ItemSearchDirection.LAST_TO_FIRST, this.ammoItem
        );
        boolean itemFound = foundAmmoRes.successful();

        if (!itemFound && !player.isCreative()) {
            player.sendStatusMessage(new TextComponentTranslation("reminder.cannon_no_ammo", this.getName()), false);
        }
        if (itemFound || player.isCreative()) {
            RiftCannonball cannonball = new RiftCannonball(this.world, this, player);
            cannonball.shoot(this, RiftUtil.clamp(this.rotationPitch, -180f, 0f), this.rotationYaw, 0.0F, 1.6F, 1.0F);
            this.world.spawnEntity(cannonball);
            if (itemFound) this.weaponInventory.getStackInSlot(foundAmmoRes.slot()).setCount(0);
            this.setLeftClickCooldown(this.maxCooldown());
        }
    }

    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-1.5) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-1.5) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY, zOffset);
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
