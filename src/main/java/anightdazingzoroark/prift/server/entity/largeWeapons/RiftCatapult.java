package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import anightdazingzoroark.prift.server.entity.projectile.RiftCatapultBoulder;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftIncrementControlUse;
import anightdazingzoroark.prift.server.message.RiftLaunchLWeaponProjectile;
import anightdazingzoroark.prift.server.message.RiftManageUtilizingControl;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;

import javax.annotation.Nullable;

public class RiftCatapult extends RiftLargeWeapon {
    private static final DataParameter<Boolean> LAUNCHING = EntityDataManager.createKey(RiftCatapult.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(RiftCatapult.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LOADED = EntityDataManager.createKey(RiftCatapult.class, DataSerializers.BOOLEAN);
    private int launchTick;

    public RiftCatapult(World worldIn) {
        super(worldIn, RiftLargeWeaponType.CATAPULT, RiftItems.CATAPULT, RiftItems.CATAPULT_BOULDER);
        this.setSize(1f, 1f);
        this.launchTick = 0;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LAUNCHING, false);
        this.dataManager.register(CHARGING, false);
        this.dataManager.register(LOADED, false);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.catapultLogic();
        this.catapultIsLoaded();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (this.isBeingRidden()) {
            if (this.getPassengers().getFirst().equals(player)) {
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

    private void catapultLogic() {
        if (!this.world.isRemote) return;
        if (!this.isCharging() && this.isUsingLeftClick()) this.setCharging(true);
        else if (this.isCharging() && !this.isUsingLeftClick()) this.setCharging(false);

        if (this.isLaunching()) {
            this.launchTick++;
            if (this.launchTick > 7) {
                this.setLaunching(false);
                this.launchTick = 0;
            }
        }
    }

    private void catapultIsLoaded() {
        boolean controllerInCreative = this.isBeingRidden() && (this.getPassengers().getFirst() instanceof EntityPlayer && ((EntityPlayer) this.getPassengers().getFirst()).isCreative());
        RiftInventoryHandler.ItemSearchResult foundAmmoRes = this.weaponInventory.findItem(
                RiftInventoryHandler.ItemSearchDirection.LAST_TO_FIRST, this.ammoItem
        );
        boolean itemFound = foundAmmoRes.successful();
        this.setLoaded(itemFound || controllerInCreative);
    }

    @Override
    public void launchProjectile(EntityPlayer player, int charge) {
        if (this.world.isRemote) return;
        RiftInventoryHandler.ItemSearchResult foundAmmoRes = this.weaponInventory.findItem(
                RiftInventoryHandler.ItemSearchDirection.LAST_TO_FIRST, this.ammoItem
        );
        boolean itemFound = foundAmmoRes.successful();

        if (!itemFound && !player.isCreative()) {
            player.sendStatusMessage(new TextComponentTranslation("reminder.catapult_no_ammo", this.getName()), false);
        }
        if (itemFound || player.isCreative()) {
            this.setLaunching(true);
            RiftCatapultBoulder boulder = new RiftCatapultBoulder(this.world, this, player);
            float velocity = RiftUtil.slopeResult(charge, true, 0, 100, 1.5f, 3f);
            float power = RiftUtil.slopeResult(charge, true, 0, 100, 3f, 6f);
            boulder.setPower(power);
            boulder.shoot(this, RiftUtil.clamp(this.rotationPitch, -180f, 0f), this.rotationYaw, 0.0F, velocity, 1.0F);
            this.world.spawnEntity(boulder);
            if (itemFound) this.weaponInventory.getStackInSlot(foundAmmoRes.slot()).setCount(0);
            this.setLeftClickCooldown(charge * 2);
        }
        this.setLeftClickUse(0);
    }

    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-1.5) * Math.cos((this.rotationYaw + 120) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-1.5) * Math.sin((this.rotationYaw + 120) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY, zOffset);
    }

    public boolean isLaunching() {
        return this.dataManager.get(LAUNCHING);
    }

    public void setLaunching(boolean value) {
        this.dataManager.set(LAUNCHING, value);
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING);
    }

    public void setCharging(boolean value) {
        this.dataManager.set(CHARGING, value);
    }

    public boolean isLoaded() {
        return this.dataManager.get(LOADED);
    }

    public void setLoaded(boolean value) {
        this.dataManager.set(LOADED, value);
    }

    @Override
    public int maxCooldown() {
        return 100;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "charge", 0, this::catapultCharge));
        data.addAnimationController(new AnimationController(this, "launch", 0, this::catapultLaunch));
    }

    private <E extends IAnimatable> PlayState catapultCharge(AnimationEvent<E> event) {
        if (this.isCharging()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.catapult.charging", LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState catapultLaunch(AnimationEvent<E> event) {
        if (this.isLaunching()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.catapult.launching", LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
