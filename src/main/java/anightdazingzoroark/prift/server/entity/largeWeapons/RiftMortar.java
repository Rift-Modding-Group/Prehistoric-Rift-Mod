package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftIncrementControlUse;
import anightdazingzoroark.prift.server.message.RiftLaunchLWeaponProjectile;
import anightdazingzoroark.prift.server.message.RiftManageUtilizingControl;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import anightdazingzoroark.riftlib.core.manager.AnimationData;

import javax.annotation.Nullable;

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
        if (this.world.isRemote) return;

        int launchDist = (int) RiftUtil.slopeResult(charge, true, 0, 100, 6, 16);
        RiftInventoryHandler.ItemSearchResult foundAmmoRes = this.weaponInventory.findItem(
                RiftInventoryHandler.ItemSearchDirection.LAST_TO_FIRST, this.ammoItem
        );
        boolean itemFound = foundAmmoRes.successful();

        if (!itemFound && !player.isCreative()) {
            player.sendStatusMessage(new TextComponentTranslation("reminder.mortar_no_ammo", this.getName()), false);
        }
        if (itemFound || player.isCreative()) {
            System.out.println("hello!");
            RiftMortarShell mortarShell = new RiftMortarShell(this.world, this, player);
            mortarShell.shoot(this, launchDist);
            this.world.spawnEntity(mortarShell);
            if (itemFound) this.weaponInventory.getStackInSlot(foundAmmoRes.slot()).setCount(0);
            this.setLeftClickCooldown(Math.max(charge * 2, 60));
        }
        this.setLeftClickUse(0);
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
