package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftLaunchLWeaponProjectile;
import anightdazingzoroark.prift.server.message.RiftManageUtilizingControl;
import anightdazingzoroark.prift.server.message.RiftMessages;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RiftMortar extends RiftLargeWeapon {
    public RiftMortar(World worldIn) {
        super(worldIn, RiftLargeWeaponType.MORTAR, RiftItems.MORTAR, RiftItems.MORTAR_SHELL);
        this.setSize(1f, 2f);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (this.isBeingRidden()) {
            if (this.getPassengers().get(0).equals(player)) {
                if (settings.keyBindAttack.isKeyDown() && !this.isUsingLeftClick()) {
                    boolean flag = false;
                    int indexToRemove = -1;
                    for (int x = this.weaponInventory.getSizeInventory() - 1; x >= 0; x--) {
                        if (!this.weaponInventory.getStackInSlot(x).isEmpty()) {
                            if (this.weaponInventory.getStackInSlot(x).getItem().equals(this.ammoItem)) {
                                flag = true;
                                indexToRemove = x;
                                break;
                            }
                        }
                    }
                    if (flag) RiftMessages.WRAPPER.sendToServer(new RiftLaunchLWeaponProjectile(this, indexToRemove));
                }
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, settings.keyBindAttack.isKeyDown()));
            }
        }
    }

    @Override
    public void launchProjectile(EntityPlayer player, int indexToRemove) {
        //get nearest entity first
        UUID userId = this.getPassengers().get(0).getUniqueID();
        AxisAlignedBB detectionBox = new AxisAlignedBB(this.posX - 16, this.posY, this.posZ - 16, this.posX + 16, this.posY + 16, this.posZ + 16);
        List<EntityLivingBase> entityList = this.world.getEntitiesWithinAABB(EntityLivingBase.class, detectionBox, new Predicate<EntityLivingBase>() {
            @Override
            public boolean apply(@Nullable EntityLivingBase input) {
                if (input instanceof EntityTameable) {
                    EntityTameable inpTameable = (EntityTameable)input;
                    if (inpTameable.isTamed()) {
                        return !userId.equals(inpTameable.getOwnerId());
                    }
                    else return true;
                }
                return true;
            }
        });
        entityList.remove(this);
        entityList.remove(this.getPassengers().get(0));

        //firing logic
        if (!entityList.isEmpty()) {
            RiftMortarShell mortarShell = new RiftMortarShell(this.world, this, player);
            mortarShell.shoot(this, entityList.get(0));
            this.world.spawnEntity(mortarShell);
            this.weaponInventory.getStackInSlot(indexToRemove).setCount(0);
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
