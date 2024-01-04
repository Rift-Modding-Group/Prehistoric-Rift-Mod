package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftChangeWeaponInvFromMenu;
import anightdazingzoroark.prift.server.message.RiftLaunchLWeaponProjectile;
import anightdazingzoroark.prift.server.message.RiftManageUtilizingControl;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;

public class RiftCannon extends RiftLargeWeapon {
    public RiftCannon(World worldIn) {
        super(worldIn, RiftLargeWeaponType.CANNON, RiftItems.CANNON, RiftItems.CANNONBALL);
        this.setSize(1f, 1f);
    }

    @Override
    public void launchProjectile(EntityPlayer player, int charge) {
        boolean flag1 = false;
        boolean flag2 = player.isCreative();
        int indexToRemove = -1;
        for (int x = this.weaponInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.weaponInventory.getStackInSlot(x).isEmpty()) {
                if (this.weaponInventory.getStackInSlot(x).getItem().equals(this.ammoItem)) {
                    flag1 = true;
                    indexToRemove = x;
                    break;
                }
            }
        }
        if (flag1 || flag2) {
            RiftCannonball cannonball = new RiftCannonball(this.world, this, player);
            cannonball.shoot(this, RiftUtil.clamp(this.rotationPitch, -180f, 0f), this.rotationYaw, 0.0F, 1.6F, 1.0F);
            this.world.spawnEntity(cannonball);
            this.weaponInventory.getStackInSlot(indexToRemove).setCount(0);
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
