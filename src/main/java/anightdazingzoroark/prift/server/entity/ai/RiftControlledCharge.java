package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RiftControlledCharge extends EntityAIBase {
    protected RiftCreature attacker;
    protected final double chargeBoost;
    protected final int initAnimLength;
    protected final int chargeTime;
    protected int animTick;
    private boolean endFlag;
    private int cooldownTime;

    public RiftControlledCharge(RiftCreature attacker, float chargeBoost, float initAnimLength, float chargeTime) {
        this.attacker = attacker;
        this.chargeBoost = chargeBoost;
        this.initAnimLength = (int)(initAnimLength * 20);
        this.chargeTime = (int)(chargeTime * 20);
    }

    @Override
    public boolean shouldExecute() {
        if (this.attacker.isBeingRidden()) {
            return this.attacker.getRightClickCooldown() == 0 && this.attacker.getRightClickUse() > 0;
        }
        return false;
    }

    public void startExecuting() {
        this.attacker.removeSpeed();
        this.attacker.setCanBeSteered(false);
        this.animTick = 0;
        this.attacker.setLowerHead(true);
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
        this.attacker.setLowerHead(false);
        this.attacker.setStartCharging(false);
        this.attacker.setIsCharging(false);
        this.attacker.setEndCharging(false);
    }

    public void updateTask() {
        if (this.animTick >= this.initAnimLength && this.attacker.isLoweringHead()) {
            this.attacker.setLowerHead(false);
            this.attacker.setStartCharging(true);
        }
        else if (this.attacker.isLoweringHead()) this.animTick++;

        if (!this.attacker.isUsingRightClick() && this.attacker.isStartCharging()) {
            this.attacker.setStartCharging(false);
            this.attacker.setIsCharging(true);
            this.animTick = 0;
        }

        if (this.attacker.isCharging()) {
            this.attacker.forcedChargePower--;
            this.attacker.motionX = this.attacker.getLookVec().x * this.chargeBoost;
            this.attacker.motionZ = this.attacker.getLookVec().z * this.chargeBoost;

            //stop if it hits a mob
            AxisAlignedBB chargerHitbox = this.attacker.getEntityBoundingBox().grow(1D, 2D, 1D);
            List<EntityLivingBase> chargedIntoEntities = this.attacker.world.getEntitiesWithinAABB(EntityLivingBase.class, chargerHitbox, null);
            chargedIntoEntities.remove(this.attacker);
            if (this.attacker.isTamed()) {
                UUID ownerUUID = this.attacker.getOwnerId();
                chargedIntoEntities.removeIf(entityLivingBase -> {
                    if (entityLivingBase instanceof EntityPlayer) {
                        if (entityLivingBase.getUniqueID().equals(ownerUUID)) return true;
                    }
                    if (entityLivingBase instanceof EntityTameable) {
                        if (((EntityTameable)entityLivingBase).isTamed()) {
                            return ((EntityTameable) entityLivingBase).getOwnerId().equals(ownerUUID);
                        }
                    }
                    return false;
                });
            }
            else {
                chargedIntoEntities.removeIf(entityLivingBase -> {
                    if (entityLivingBase.getClass().isInstance(this.attacker)) {
                        return ((RiftCreature) entityLivingBase).isTamed();
                    }
                    return false;
                });
            }

            //stop if it hits a block
            boolean breakBlocksFlag = false;
            for (int x = MathHelper.floor(chargerHitbox.minX); x < MathHelper.ceil(chargerHitbox.maxX); x++) {
                for (int z = MathHelper.floor(chargerHitbox.minZ); z < MathHelper.ceil(chargerHitbox.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, this.attacker.posY, z);
                    IBlockState state = this.attacker.world.getBlockState(pos);

                    if (!state.getBlock().isAir(state, this.attacker.world, pos)) {
                        if (!state.getBlock().isAir(state, this.attacker.world, new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()))) {
                            breakBlocksFlag = true;
                            break;
                        }
                    }
                }
            }

            if (breakBlocksFlag || !chargedIntoEntities.isEmpty() || this.attacker.forcedChargePower == 0) {
                if (!chargedIntoEntities.isEmpty()) for (EntityLivingBase entity : chargedIntoEntities) this.attacker.attackEntityAsMob(entity);

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

                this.attacker.setIsCharging(false);
                this.attacker.setEndCharging(true);
            }
        }

        if (this.animTick >= this.initAnimLength && this.attacker.isEndCharging()) {
            this.attacker.setRightClickUse(0);
            this.attacker.setEndCharging(false);
            this.attacker.setRightClickCooldown(this.attacker.chargeCooldown * 2);
            this.attacker.forcedChargePower = 0;
            this.attacker.setCanCharge(false);
            this.attacker.setEnergy(this.attacker.getEnergy() - (int)(0.06d * (double)Math.min(this.attacker.chargeCooldown/2, 100) + 6d));
            this.endFlag = true;
        }
        else if (this.attacker.isEndCharging()) this.animTick++;
    }
}
