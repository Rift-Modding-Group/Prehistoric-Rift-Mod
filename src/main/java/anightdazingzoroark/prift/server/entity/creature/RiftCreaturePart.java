package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftMultipartInteract;
import net.ilexiconn.llibrary.server.entity.multipart.PartEntity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

public class RiftCreaturePart extends PartEntity {
    public RiftCreaturePart(EntityLiving parent, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
        super(parent, radius, angleYaw, offsetY, sizeX, sizeY, damageMultiplier);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (this.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftMultipartInteract((RiftCreature) this.parent, -1));
        return ((RiftCreature)this.parent).processInteract(player, hand);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (this.world.isRemote && source.getTrueSource() instanceof EntityPlayer) {
            RiftMessages.WRAPPER.sendToServer(new RiftMultipartInteract((RiftCreature) this.parent, damage * this.damageMultiplier));
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
        if (this.parent == null || shouldNotExist()) {
            this.world.removeEntityDangerously(this);
        }
    }

    public boolean shouldNotExist() {
        return !this.parent.isEntityAlive();
    }
}
