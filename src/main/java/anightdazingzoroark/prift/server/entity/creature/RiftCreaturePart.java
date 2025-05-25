package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftMultipartInteract;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class RiftCreaturePart extends MultiPartEntityPart {
    private final RiftCreature partParent;
    public final float radius;
    private final float angleYaw;
    private final float offsetY;
    private final float damageMultiplier;
    private boolean isDisabled;
    private boolean immuneToMelee;
    private boolean immuneToProjectiles;
    private final float initWidth;
    private final float initHeight;

    public RiftCreaturePart(RiftCreature parent, float radius, float angleYaw, float offsetY, float width, float height, float damageMultiplier) {
        this(parent, "", radius, angleYaw, offsetY, width, height, damageMultiplier);
    }

    public RiftCreaturePart(RiftCreature parent, String name, float radius, float angleYaw, float offsetY, float width, float height, float damageMultiplier) {
        super(parent, name, width, height);
        this.initWidth = width;
        this.initHeight = height;
        this.partParent = parent;
        this.radius = radius;
        this.angleYaw = (angleYaw + 90.0F) * 0.017453292F;
        this.offsetY = offsetY;
        this.damageMultiplier = damageMultiplier;
    }

    public RiftCreaturePart setInvulnerable() {
        this.setEntityInvulnerable(true);
        return this;
    }

    //for sources this part cannot be hurt by
    public RiftCreaturePart setImmuneToMelee() {
        this.immuneToMelee = true;
        return this;
    }

    public RiftCreaturePart setImmuneToProjectile() {
        this.immuneToProjectiles = true;
        return this;
    }

    //test for sources
    public boolean testForMeleeImmunity() {
        return this.immuneToMelee;
    }

    public boolean testForMeleeImmunity(DamageSource source) {
        return !this.immuneToMelee || (source.isExplosion() || !(source instanceof EntityDamageSource));
    }

    public boolean testForProjectileImmunity(DamageSource source) {
        return !this.immuneToProjectiles || !source.isProjectile();
    }

    public void setDisabled(boolean value) {
        this.isDisabled = value;
    }

    public boolean isDisabled() {
        return this.isDisabled;
    }

    @Override
    public void onUpdate() {
        //define and set offsets
        double xOffset = this.radius * (this.width / (this.initWidth * this.partParent.ageScaleParams()[1])) * Math.cos(this.partParent.renderYawOffset * (Math.PI / 180.0F) + this.angleYaw);
        double yOffset = this.offsetY * (this.height / (this.initHeight * this.partParent.ageScaleParams()[1]));
        double zOffset = this.radius * (this.width / (this.initWidth * this.partParent.ageScaleParams()[1])) * Math.sin(this.partParent.renderYawOffset * (Math.PI / 180.0F) + this.angleYaw);
        this.setPositionAndUpdate(this.partParent.posX + xOffset, this.partParent.posY + yOffset, this.partParent.posZ + zOffset);
        if (!this.partParent.isEntityAlive()) this.world.removeEntityDangerously(this);
        super.onUpdate();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!this.partParent.isBeingRidden()) {
            if (this.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftMultipartInteract((RiftCreature) this.parent));
            return this.partParent.processInteract(player, hand) || this.partParent.processInitialInteract(player, hand);
        }
        return false;
    }

    public void resize(float scale) {
        this.setSize(this.initWidth * scale, this.initHeight * scale);
    }

    @Override
    public boolean canBeCollidedWith() {
        boolean aliveTest = this.partParent.isEntityAlive();
        return !this.isDisabled && aliveTest;
    }

    public void collideWithNearbyEntities() {
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        entities.stream().filter(entity -> entity != this.partParent && !(entity instanceof MultiPartEntityPart) && entity.canBePushed()).forEach(entity -> entity.applyEntityCollision(this.partParent));
    }

    public float getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public RiftCreature getParent() {
        return this.partParent;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return super.attackEntityFrom(source, amount) && !this.isDisabled && this.testForMeleeImmunity(source) && this.testForProjectileImmunity(source);
    }

    public boolean isUnderwater() {
        BlockPos thisPos = new BlockPos(this.posX, this.posY, this.posZ);
        BlockPos abovePos = thisPos.up();
        return this.world.getBlockState(thisPos).getMaterial().isLiquid() && this.world.getBlockState(abovePos).getMaterial().isLiquid();
    }

    public boolean isEntityInvulnerable(DamageSource source) {
        return this.invulnerable && source != DamageSource.OUT_OF_WORLD;
    }
}
