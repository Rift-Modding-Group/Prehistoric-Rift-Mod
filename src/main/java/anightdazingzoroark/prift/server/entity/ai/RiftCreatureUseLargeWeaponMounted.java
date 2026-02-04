package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.CreatureInventoryHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.prift.server.entity.projectile.RiftCatapultBoulder;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class RiftCreatureUseLargeWeaponMounted extends EntityAIBase {
    private final RiftCreature creature;
    private boolean finishFlag;
    private boolean canFireWeapon;
    private int pauseTime;
    private final int catapultLaunchTime = 2;
    private final int maxLaunchPauseTime = 10;

    public RiftCreatureUseLargeWeaponMounted(RiftCreature creature) {
        this.creature = creature;
    }

    @Override
    public boolean shouldExecute() {
        return this.creature.creatureType.canUseGearType(RiftCreatureType.InventoryGearType.LARGE_WEAPON)
                && this.creature.isBeingRidden()
                && (this.creature.getControllingPassenger() instanceof EntityPlayer && ((EntityPlayer)this.creature.getControllingPassenger()).getHeldItemMainhand().getItem() == RiftItems.COMMAND_CONSOLE)
                && this.creature.getLargeWeaponCooldown() == 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.finishFlag;
    }

    @Override
    public void resetTask() {
        this.finishFlag = false;
        this.canFireWeapon = false;
        this.pauseTime = 0;
        this.creature.setLargeWeaponUse(0);
        this.creature.setCanMove(true);
    }

    @Override
    public void updateTask() {
        switch (this.creature.getLargeWeapon()) {
            case CANNON:
                if (this.canFireWeapon) this.manageCannonFiring();
                else if (this.creature.getUsingLargeWeapon()) {
                    if (this.creature.canFireLargeWeapon()) {
                        this.creature.setCanMove(false);
                        this.canFireWeapon = true;
                    }
                    else {
                        EntityPlayer rider = (EntityPlayer) this.creature.getControllingPassenger();
                        if (rider != null) rider.sendStatusMessage(new TextComponentTranslation("reminder.creature_catapult_no_ammo"), false);
                        this.creature.setLargeWeaponUse(0);
                        this.finishFlag = true;
                    }
                }
                break;
            case MORTAR:
                if (this.canFireWeapon) this.manageMortarFiring();
                else {
                    if (this.creature.canFireLargeWeapon() && !this.creature.getUsingLargeWeapon() && this.creature.getLargeWeaponUse() > 0) {
                        this.creature.setCanMove(false);
                        this.canFireWeapon = true;
                    }
                    else if (!this.creature.canFireLargeWeapon() && this.creature.getUsingLargeWeapon()) {
                        EntityPlayer rider = (EntityPlayer) this.creature.getControllingPassenger();
                        if (rider != null) rider.sendStatusMessage(new TextComponentTranslation("reminder.creature_mortar_no_ammo"), false);
                        this.creature.setLargeWeaponUse(0);
                        this.finishFlag = true;
                    }
                }
                break;
            case CATAPULT:
                if (this.canFireWeapon) this.manageCatapultFiring();
                else {
                    if (this.creature.canFireLargeWeapon() && !this.creature.getUsingLargeWeapon() && this.creature.getLargeWeaponUse() > 0) {
                        this.creature.setCanMove(false);
                        this.canFireWeapon = true;
                    }
                    else if (!this.creature.canFireLargeWeapon() && this.creature.getUsingLargeWeapon()) {
                        EntityPlayer rider = (EntityPlayer) this.creature.getControllingPassenger();
                        if (rider != null) rider.sendStatusMessage(new TextComponentTranslation("reminder.creature_catapult_no_ammo"), false);
                        this.creature.setLargeWeaponUse(0);
                        this.finishFlag = true;
                    }
                }
                break;
        }
    }

    private void manageCannonFiring() {
        EntityPlayer rider = (EntityPlayer) this.creature.getControllingPassenger();
        if (this.pauseTime == 0) {
            RiftCannonball cannonball = new RiftCannonball(this.creature.world, this.creature, rider);
            cannonball.shoot(this.creature, RiftUtil.clamp(this.creature.rotationPitch, -180f, 0f), this.creature.rotationYaw, 0.0F, 2.4F, 1.0F);
            this.creature.world.spawnEntity(cannonball);
            this.creature.creatureInventory.removeItem(
                    CreatureInventoryHandler.ItemSearchDirection.LAST_TO_FIRST,
                    new ItemStack(RiftItems.CANNONBALL, 1)
            );
            this.creature.setLargeWeaponCooldown(this.creature.getLargeWeapon().maxCooldown);
            this.creature.setLargeWeaponUse(0);
        }
        if (this.pauseTime >= this.maxLaunchPauseTime) this.finishFlag = true;
        this.pauseTime++;
    }

    private void manageMortarFiring() {
        EntityPlayer rider = (EntityPlayer)this.creature.getControllingPassenger();
        if (this.pauseTime == 0) {
            int launchDist = (int)RiftUtil.slopeResult(this.creature.getLargeWeaponUse(), true, 0, this.creature.getLargeWeapon().maxUse, 6, 16);
            RiftMortarShell mortarShell = new RiftMortarShell(this.creature.world, this.creature, rider);
            mortarShell.shoot(this.creature, launchDist);
            this.creature.world.spawnEntity(mortarShell);
            this.creature.creatureInventory.removeItem(
                    CreatureInventoryHandler.ItemSearchDirection.LAST_TO_FIRST,
                    new ItemStack(RiftItems.MORTAR_SHELL, 1)
            );
            this.creature.setLargeWeaponCooldown(Math.min(this.creature.getLargeWeaponUse() * 2, this.creature.getLargeWeapon().maxCooldown));
            this.creature.setLargeWeaponUse(0);
        }
        if (this.pauseTime >= this.maxLaunchPauseTime) this.finishFlag = true;
        this.pauseTime++;
    }

    private void manageCatapultFiring() {
        EntityPlayer rider = (EntityPlayer) this.creature.getControllingPassenger();
        if (this.pauseTime == 0) this.creature.setFiringCatapult(true);
        if (this.pauseTime == this.catapultLaunchTime) {
            RiftCatapultBoulder boulder = new RiftCatapultBoulder(this.creature.world, this.creature, rider);
            float velocity = RiftUtil.slopeResult(this.creature.getLargeWeaponUse(), true, 0, this.creature.getLargeWeapon().maxUse, 1.5f, 3f);
            float power = RiftUtil.slopeResult(this.creature.getLargeWeaponUse(), true, 0, this.creature.getLargeWeapon().maxUse, 3f, 6f);
            boulder.setPower(power);
            boulder.shoot(this.creature, RiftUtil.clamp(this.creature.rotationPitch, -180f, 0f), this.creature.rotationYaw, 0.0F, velocity, 1.0F);
            this.creature.world.spawnEntity(boulder);
            this.creature.creatureInventory.removeItem(
                    CreatureInventoryHandler.ItemSearchDirection.LAST_TO_FIRST,
                    new ItemStack(RiftItems.CATAPULT_BOULDER, 1)
            );
        }
        if (this.pauseTime >= this.maxLaunchPauseTime) {
            this.creature.setLargeWeaponCooldown(Math.min(this.creature.getLargeWeaponUse() * 2, this.creature.getLargeWeapon().maxCooldown));
            this.creature.setLargeWeaponUse(0);
            this.creature.setFiringCatapult(false);
            this.finishFlag = true;
        }
        this.pauseTime++;
    }
}
