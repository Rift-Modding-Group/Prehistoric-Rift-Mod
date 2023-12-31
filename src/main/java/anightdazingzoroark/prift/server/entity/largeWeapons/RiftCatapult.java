package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.prift.server.entity.projectile.RiftCatapultBoulder;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftIncrementControlUse;
import anightdazingzoroark.prift.server.message.RiftLaunchLWeaponProjectile;
import anightdazingzoroark.prift.server.message.RiftManageUtilizingControl;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;

public class RiftCatapult extends RiftLargeWeapon {
    private static final DataParameter<Integer> LEFT_CLICK_USE = EntityDataManager.createKey(RiftCatapult.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEFT_CLICK_COOLDOWN = EntityDataManager.createKey(RiftCatapult.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> LAUNCHING = EntityDataManager.createKey(RiftCatapult.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(RiftCatapult.class, DataSerializers.BOOLEAN);

    public RiftCatapult(World worldIn) {
        super(worldIn, RiftLargeWeaponType.CATAPULT, RiftItems.CATAPULT, RiftItems.CATAPULT_BOULDER);
        this.setSize(1f, 1f);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEFT_CLICK_USE, 0);
        this.dataManager.register(LEFT_CLICK_COOLDOWN, 0);
        this.dataManager.register(LAUNCHING, false);
        this.dataManager.register(CHARGING, false);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        System.out.println(this.isUsingLeftClick());
        System.out.println(this.getLeftClickUse());
        System.out.println(this.getLeftClickCooldown());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (this.isBeingRidden()) {
            if (this.getPassengers().get(0).equals(player)) {
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, settings.keyBindAttack.isKeyDown()));
                if (settings.keyBindAttack.isKeyDown() && this.getLeftClickCooldown() == 0) {
                    RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this));
                }
                else if (!settings.keyBindAttack.isKeyDown() && this.getLeftClickCooldown() == 0 && this.getLeftClickUse() > 0) {
                    RiftMessages.WRAPPER.sendToServer(new RiftLaunchLWeaponProjectile(this));
                }
            }
        }
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
            RiftCatapultBoulder boulder = new RiftCatapultBoulder(this.world, this, player);
            float velocity = RiftUtil.clamp((float) charge * 0.015f + 1.5f, 1.5f, 3f);
            float power = RiftUtil.clamp( 0.03f * charge + 3f, 3f, 6f);
            boulder.setPower(power);
            boulder.shoot(this, RiftUtil.clamp(this.rotationPitch, -180f, 0f), this.rotationYaw, 0.0F, velocity, 1.0F);
            this.world.spawnEntity(boulder);
            this.weaponInventory.getStackInSlot(indexToRemove).setCount(0);
            this.setLeftClickCooldown(charge * 2);
        }
        this.setLeftClickUse(0);
    }

    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-1.5) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-1.5) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY, zOffset);
    }

    public int getLeftClickUse() {
        return this.dataManager.get(LEFT_CLICK_USE).intValue();
    }

    public void setLeftClickUse(int value) {
        this.dataManager.set(LEFT_CLICK_USE, value);
    }

    public int getLeftClickCooldown() {
        return Math.max(0, this.dataManager.get(LEFT_CLICK_COOLDOWN));
    }

    public void setLeftClickCooldown(int value) {
        this.dataManager.set(LEFT_CLICK_COOLDOWN, Math.max(0, value));
    }

    public boolean isLaunching() {
        return this.dataManager.get(LAUNCHING);
    }

    public void setLaunching(boolean value) {
        this.dataManager.set(LAUNCHING, value);
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
