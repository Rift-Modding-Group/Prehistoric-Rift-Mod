package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import anightdazingzoroark.prift.server.entity.interfaces.IChargingMob;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RiftControlledCharge extends EntityAIBase {
    protected final RiftCreature attacker;
    protected IChargingMob charger;
    protected final int initAnimLength;
    protected final int chargeTime;
    protected int animTick;
    private boolean endFlag;

    public RiftControlledCharge(RiftCreature attacker, float initAnimLength, float chargeTime) {
        this.attacker = attacker;
        this.initAnimLength = (int)(initAnimLength * 20);
        this.chargeTime = (int)(chargeTime * 20);
    }

    @Override
    public boolean shouldExecute() {
        if (this.attacker instanceof IChargingMob) {
            this.charger = (IChargingMob) this.attacker;
            if (this.attacker.isBeingRidden()) {
                return this.attacker.getRightClickCooldown() == 0 && this.attacker.getRightClickUse() > 0 && !this.attacker.isInWater();
            }
        }
        return false;
    }

    public void startExecuting() {
        this.attacker.removeSpeed();
        this.attacker.setCanBeSteered(false);
        this.animTick = 0;
        this.charger.setLowerHead(true);
        this.endFlag = false;
    }

    public boolean shouldContinueExecuting() {
        return !this.endFlag && this.attacker.isBeingRidden();
    }

    public void resetTask() {
        this.attacker.forcedChargePower = 0;
        this.attacker.chargeCooldown = 0;
        this.attacker.setRightClickUse(0);
        this.attacker.setCanBeSteered(true);
        this.attacker.resetSpeed();
        this.attacker.setActing(false);
        this.charger.setLowerHead(false);
        this.charger.setStartCharging(false);
        this.charger.setIsCharging(false);
        this.charger.setEndCharging(false);
    }

    public void updateTask() {
        if (this.animTick >= this.initAnimLength && this.charger.isLoweringHead()) {
            this.charger.setLowerHead(false);
            this.charger.setStartCharging(true);
        }
        else if (this.charger.isLoweringHead()) this.animTick++;

        if (!this.attacker.isUsingRightClick() && this.charger.isStartCharging()) {
            this.charger.setStartCharging(false);
            this.charger.setIsCharging(true);
            this.animTick = 0;
        }

        if (this.charger.isCharging()) {
            this.attacker.forcedChargePower--;

            //stop if it hits a mob
            AxisAlignedBB chargerHitbox = this.attacker.getEntityBoundingBox().grow(1D, 2D, 1D);
            List<Entity> chargedIntoEntities = this.attacker.world.getEntitiesWithinAABB(Entity.class, chargerHitbox, new Predicate<Entity>() {
                @Override
                public boolean apply(@Nullable Entity entity) {
                    if (entity instanceof EntityPlayer) {
                        if (!entity.getUniqueID().equals(attacker.getOwnerId())) {
                            return true;
                        }
                    }
                    else if (entity instanceof EntityTameable) {
                        if ((((EntityTameable) entity).isTamed())) {
                            if (attacker.getOwner() != null) {
                                if (!((EntityTameable) entity).getOwner().equals(attacker.getOwner())) {
                                    return true;
                                }
                            }
                        }
                        else return true;
                    }
                    else if (entity instanceof RiftCreaturePart) {
                        RiftCreature parent = ((RiftCreaturePart)entity).getParent();
                        return parent != attacker;
                    }
                    else return entity instanceof EntityLivingBase;
                    return false;
                }
            });
            chargedIntoEntities.remove(this.attacker);

            //stop if it hits a block
            boolean breakBlocksFlag = false;
            breakBlocksLoop: for (int x = MathHelper.floor(chargerHitbox.minX); x < MathHelper.ceil(chargerHitbox.maxX); x++) {
                for (int z = MathHelper.floor(chargerHitbox.minZ); z < MathHelper.ceil(chargerHitbox.maxZ); z++) {
                    IBlockState state = this.attacker.world.getBlockState(new BlockPos(x, this.attacker.posY, z));
                    IBlockState stateUp = this.attacker.world.getBlockState(new BlockPos(x, this.attacker.posY + 1, z));

                    if (state.getMaterial() != Material.AIR && stateUp.getMaterial() != Material.AIR) {
                        breakBlocksFlag = true;
                        break breakBlocksLoop;
                    }
                }
            }

            if (breakBlocksFlag || !chargedIntoEntities.isEmpty() || this.attacker.forcedChargePower == 0) {
                if (!chargedIntoEntities.isEmpty()) for (Entity entity : chargedIntoEntities) {
                    if (entity instanceof RiftCreaturePart) {
                        RiftCreature parent = ((RiftCreaturePart)entity).getParent();
                        this.attacker.attackEntityAsMob(parent);
                    }
                    this.attacker.attackEntityAsMob(entity);
                }

                boolean canBreak = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.attacker.world, this.attacker);
                if (breakBlocksFlag && canBreak) {
                    List<BlockPos> toBreak = new ArrayList<>();
                    for (int x = MathHelper.floor(chargerHitbox.minX); x < MathHelper.ceil(chargerHitbox.maxX); x++) {
                        for (int y = MathHelper.floor(chargerHitbox.minY); y < MathHelper.ceil(chargerHitbox.maxY); y++) {
                            for (int z = MathHelper.floor(chargerHitbox.minZ); z < MathHelper.ceil(chargerHitbox.maxZ); z++) {
                                BlockPos blockpos = new BlockPos(x, y, z);
                                IBlockState iblockstate = this.attacker.world.getBlockState(blockpos);
                                Block block = iblockstate.getBlock();

                                if (iblockstate.getMaterial() != Material.AIR && y >= this.attacker.posY) {
                                    if (this.attacker.checkBasedOnStrength(block, iblockstate)) toBreak.add(blockpos);
                                }
                            }
                        }
                    }
                    for (BlockPos blockPos : toBreak) this.attacker.world.destroyBlock(blockPos, false);
                }

                this.charger.setIsCharging(false);
                this.charger.setEndCharging(true);
            }
        }

        if (this.animTick >= this.initAnimLength && this.charger.isEndCharging()) {
            this.attacker.setRightClickUse(0);
            this.charger.setEndCharging(false);
            this.attacker.setRightClickCooldown(this.attacker.chargeCooldown * 2);
            this.attacker.forcedChargePower = 0;
            this.charger.setCanCharge(false);
            this.attacker.setEnergy(this.attacker.getEnergy() - (int)(0.06d * (double)Math.min(this.attacker.chargeCooldown/2, 100) + 6d));
            this.endFlag = true;
        }
        else if (this.charger.isEndCharging()) this.animTick++;
    }
}
