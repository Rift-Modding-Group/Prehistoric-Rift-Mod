package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftIncrementControlUse;
import anightdazingzoroark.prift.server.message.RiftLaunchLWeaponProjectile;
import anightdazingzoroark.prift.server.message.RiftManageUtilizingControl;
import anightdazingzoroark.prift.server.message.RiftMessages;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
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
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, settings.keyBindAttack.isKeyDown() && this.getLeftClickCooldown() == 0));
                if (settings.keyBindAttack.isKeyDown() && this.getLeftClickCooldown() == 0) {
                    RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this));
                }
                else if (!settings.keyBindAttack.isKeyDown() && this.getLeftClickCooldown() == 0 && this.getLeftClickUse() > 0) {
                    RiftMessages.WRAPPER.sendToServer(new RiftLaunchLWeaponProjectile(this, this.getLeftClickUse()));
                }
            }
        }
    }

    @Override
    public void launchProjectile(EntityPlayer player, int charge) {
        if (!this.world.isRemote) {
            int launchDist = RiftUtil.clamp((int)(0.1D * charge) + 6, 6, 16);
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
                RiftMortarShell mortarShell = new RiftMortarShell(this.world, this, player);
                mortarShell.shoot(this, launchDist);
                this.world.spawnEntity(mortarShell);
                this.weaponInventory.getStackInSlot(indexToRemove).setCount(0);
                this.setLeftClickCooldown(Math.max(charge * 2, 60));
            }
            this.setLeftClickUse(0);
        }
    }

    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-1.5) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-1.5) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY, zOffset);
    }

    @Override
    public int maxCooldown() {
        return 100;
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
