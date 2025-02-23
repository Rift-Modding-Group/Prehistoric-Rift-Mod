package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
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
    private boolean canFireWeaponWithAnim;
    private int animTime;
    private final int catapultLaunchTime = 2;
    private final int catapultAnimTime = 10;

    public RiftCreatureUseLargeWeaponMounted(RiftCreature creature) {
        this.creature = creature;
    }

    @Override
    public boolean shouldExecute() {
        return this.creature.creatureType.canHoldLargeWeapon
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
        this.canFireWeaponWithAnim = false;
        this.animTime = 0;
        this.creature.setLargeWeaponUse(0);
        this.creature.resetSpeed();
    }

    @Override
    public void updateTask() {
        switch (this.creature.getLargeWeapon()) {
            case CANNON:
                if (this.creature.getUsingLargeWeapon()) {
                    if (this.creature.canFireLargeWeapon()) this.manageCannonFiring();
                    else {
                        EntityPlayer rider = (EntityPlayer) this.creature.getControllingPassenger();
                        if (rider != null) rider.sendStatusMessage(new TextComponentTranslation("reminder.creature_catapult_no_ammo"), false);
                        this.creature.setLargeWeaponUse(0);
                        this.finishFlag = true;
                    }
                }
                break;
            case MORTAR:
                if (this.creature.canFireLargeWeapon() && this.creature.getUsingLargeWeapon() && this.creature.getLargeWeaponUse() > 0) {
                    this.creature.removeSpeed();
                }
                if (this.creature.canFireLargeWeapon() && !this.creature.getUsingLargeWeapon() && this.creature.getLargeWeaponUse() > 0) {
                    this.manageMortarFiring();
                }
                else if (!this.creature.canFireLargeWeapon() && this.creature.getUsingLargeWeapon()) {
                    EntityPlayer rider = (EntityPlayer) this.creature.getControllingPassenger();
                    if (rider != null) rider.sendStatusMessage(new TextComponentTranslation("reminder.creature_mortar_no_ammo"), false);
                    this.creature.setLargeWeaponUse(0);
                    this.finishFlag = true;
                }
                break;
            case CATAPULT:
                if (this.canFireWeaponWithAnim) this.manageCatapultFiring();
                else {
                    if (this.creature.canFireLargeWeapon() && this.creature.getUsingLargeWeapon() && this.creature.getLargeWeaponUse() > 0) {
                        this.creature.removeSpeed();
                    }
                    if (this.creature.canFireLargeWeapon() && !this.creature.getUsingLargeWeapon() && this.creature.getLargeWeaponUse() > 0) {
                        this.canFireWeaponWithAnim = true;
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
        RiftCannonball cannonball = new RiftCannonball(this.creature.world, this.creature, rider);
        cannonball.shoot(this.creature, RiftUtil.clamp(this.creature.rotationPitch, -180f, 0f), this.creature.rotationYaw, 0.0F, 2.4F, 1.0F);
        this.creature.world.spawnEntity(cannonball);
        this.creature.creatureInventory.removeItemStackStartingFromLast(new ItemStack(RiftItems.CANNONBALL), 1);
        this.creature.setLargeWeaponCooldown(this.creature.getLargeWeapon().maxCooldown);
        this.creature.setLargeWeaponUse(0);
        this.finishFlag = true;
    }

    private void manageMortarFiring() {
        EntityPlayer rider = (EntityPlayer)this.creature.getControllingPassenger();
        int launchDist = (int)RiftUtil.slopeResult(this.creature.getLargeWeaponUse(), true, 0, this.creature.getLargeWeapon().maxUse, 6, 16);
        RiftMortarShell mortarShell = new RiftMortarShell(this.creature.world, this.creature, rider);
        mortarShell.shoot(this.creature, launchDist);
        this.creature.world.spawnEntity(mortarShell);
        this.creature.creatureInventory.removeItemStackStartingFromLast(new ItemStack(RiftItems.MORTAR_SHELL), 1);
        this.creature.setLargeWeaponCooldown(Math.min(this.creature.getLargeWeaponUse() * 2, this.creature.getLargeWeapon().maxCooldown));
        this.creature.setLargeWeaponUse(0);
        this.finishFlag = true;
    }

    private void manageCatapultFiring() {
        EntityPlayer rider = (EntityPlayer) this.creature.getControllingPassenger();
        if (this.animTime == 0) this.creature.setFiringCatapult(true);
        if (this.animTime == this.catapultLaunchTime) {
            RiftCatapultBoulder boulder = new RiftCatapultBoulder(this.creature.world, this.creature, rider);
            float velocity = RiftUtil.slopeResult(this.creature.getLargeWeaponUse(), true, 0, this.creature.getLargeWeapon().maxUse, 1.5f, 3f);
            float power = RiftUtil.slopeResult(this.creature.getLargeWeaponUse(), true, 0, this.creature.getLargeWeapon().maxUse, 3f, 6f);
            boulder.setPower(power);
            boulder.shoot(this.creature, RiftUtil.clamp(this.creature.rotationPitch, -180f, 0f), this.creature.rotationYaw, 0.0F, velocity, 1.0F);
            this.creature.world.spawnEntity(boulder);
            this.creature.creatureInventory.removeItemStackStartingFromLast(new ItemStack(RiftItems.CATAPULT_BOULDER), 1);
        }
        if (this.animTime >= this.catapultAnimTime) {
            this.creature.setLargeWeaponCooldown(Math.min(this.creature.getLargeWeaponUse() * 2, this.creature.getLargeWeapon().maxCooldown));
            this.creature.setLargeWeaponUse(0);
            this.creature.setFiringCatapult(false);
            this.finishFlag = true;
        }
        this.animTime++;
    }
}
