package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftMultipartInteract;
import net.ilexiconn.llibrary.server.entity.multipart.PartEntity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class RiftCreaturePart extends PartEntity {
    public RiftCreaturePart(EntityLiving parent, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
        super(parent, radius, angleYaw, offsetY, sizeX, sizeY, damageMultiplier);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!this.parent.isBeingRidden()) {
            if (this.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftMultipartInteract((RiftCreature) this.parent, -1));
            return ((RiftCreature)this.parent).processInteract(player, hand) || this.parent.processInitialInteract(player, hand);
        }
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (this.world.isRemote) {
            if (source.getTrueSource() != null) {
                if (source.getTrueSource().equals(this.parent)) return false;
                else if (source.getTrueSource() instanceof EntityPlayer) {
                    RiftMessages.WRAPPER.sendToServer(new RiftMultipartInteract((RiftCreature) this.parent, damage * this.damageMultiplier));
                }
            }
        }
        return this.parent.attackEntityFrom(source, damage * this.damageMultiplier);
    }

    public RiftCreature getParent() {
        return (RiftCreature) this.parent;
    }

    public void resize(float width, float height) {
        this.setSize(width, height);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.parent == null || this.shouldNotExist()) {
            this.world.removeEntityDangerously(this);
        }
    }

    public boolean shouldNotExist() {
        return !this.parent.isEntityAlive();
    }

    public boolean isUnderwater() {
        BlockPos thisPos = new BlockPos(this.posX, this.posY, this.posZ);
        BlockPos abovePos = thisPos.add(0, 1, 0);
        return this.world.getBlockState(thisPos).getMaterial().isLiquid() && this.world.getBlockState(abovePos).getMaterial().isLiquid();
    }
}
