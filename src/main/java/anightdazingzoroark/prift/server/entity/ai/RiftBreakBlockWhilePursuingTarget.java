package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.client.RiftSoundLooper;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftCreatureMove;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class RiftBreakBlockWhilePursuingTarget extends EntityAIBase {
    private final RiftCreature creature;
    private EntityLivingBase target;
    private BlockPos blockPosToDig;

    private int maxMoveAnimTime; //entire time spent for animation
    private int moveAnimInitDelayTime; //time until end of delay point
    private int moveAnimChargeUpTime; //time until end of charge up point
    private int moveAnimChargeToUseTime; //time until end of charge up to use point
    private int moveAnimUseTime; //time until end of use anim
    private int animTime = 0;
    private boolean finishedAnimMarker;
    private boolean finishedMoveMarker;

    private RiftCreatureMove currentInvokedMove;

    private int scanTick = 0;
    private int moveChoiceCooldown = 0;

    private RiftSoundLooper useSoundLooper;

    public RiftBreakBlockWhilePursuingTarget(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!RiftConfigHandler.getConfig(this.creature.creatureType).general.breakBlocksInPursuit) return false;

        //some moves make it so the creature can't dig up blocks in front
        if (this.creature.currentCreatureMove() == CreatureMove.BURROW) return false;

        this.target = this.creature.getAttackTarget();
        if (this.target == null
                || !this.target.isEntityAlive()
                || !this.creature.getNavigator().noPath()
                || this.target.canEntityBeSeen(this.creature)
                || (this.creature.canEnterTurretMode() && this.creature.isTurretMode())
        ) return false;

        this.blockPosToDig = (this.blockPosToDig != null && this.creature.getDistanceSq(this.blockPosToDig) <= Math.pow(this.creature.attackWidth() + this.creature.width, 2) && this.creature.checkIfCanBreakBlock(this.blockPosToDig))
                ? this.blockPosToDig
                : this.getNextBlock(this.target, (this.creature.attackWidth() + this.creature.width) / 2);

        return (!this.creature.isTamed() || !this.creature.isBeingRidden())
                && this.blockPosToDig != null
                && this.hasAvailableMeleeMoves();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.isBeingRidden()
                && this.target != null
                && this.target.isEntityAlive()
                && !this.finishedMoveMarker;
    }

    @Override
    public void startExecuting() {
        this.finishedAnimMarker = true;
        this.finishedMoveMarker = false;

        this.creature.setCurrentCreatureMove(null);
        this.currentInvokedMove = null;

        this.maxMoveAnimTime = 0;
        this.moveAnimInitDelayTime = 0;
        this.moveAnimChargeUpTime = 0;
        this.moveAnimChargeToUseTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;

        this.useSoundLooper = null;
    }

    @Override
    public void resetTask() {
        this.finishedAnimMarker = true;
        this.finishedMoveMarker = false;
        this.creature.setCanMove(true);
        this.creature.setUsingUnchargedAnim(false);
        this.currentInvokedMove = null;

        this.creature.setPlayingInfiniteMoveAnim(false);
        this.creature.setCurrentCreatureMove(null);

        this.maxMoveAnimTime = 0;
        this.moveAnimInitDelayTime = 0;
        this.moveAnimChargeUpTime = 0;
        this.moveAnimChargeToUseTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;

        this.creature.setUnchargedMultistepMoveStep(0);

        this.blockPosToDig = null;
    }

    @Override
    public void updateTask() {
        this.creature.getLookHelper().setLookPosition(this.target.posX, this.target.posY + (double)this.target.getEyeHeight(), this.target.posZ, (float)this.creature.getHorizontalFaceSpeed(), (float)this.creature.getVerticalFaceSpeed());
        //this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
        //this.creature.getMoveHelper().setMoveTo(this.target.posX, this.target.posY, this.target.posZ, 1.0);

        if (this.finishedAnimMarker) this.selectMeleeMoveForBlockBreak();
        else {
            if (this.animTime == 0) {
                this.creature.setUsingUnchargedAnim(true);
                if (this.creature.currentCreatureMove().useTimeIsInfinite) {
                    this.creature.setUnchargedMultistepMoveStep(1);
                }
                if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound() != null
                        && !this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).chargeUpSoundCanLoop())
                    this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound(),
                            1f,
                            1f);
            }
            if (this.animTime == this.moveAnimChargeToUseTime) {
                this.currentInvokedMove.breakBlocksInFront(this.creature);

                if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound() != null)
                    this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound(),
                            1f,
                            1f);
                if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseParticles() != null)
                    this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseParticles().createParticle();
            }
            if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                if (this.useSoundLooper != null) this.useSoundLooper.playSound();
                if (this.creature.currentCreatureMove().useTimeIsInfinite) {
                    this.creature.setUnchargedMultistepMoveStep(2);
                }
            }
            if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                    || this.currentInvokedMove.forceStopFlag) {
                if (this.creature.currentCreatureMove().useTimeIsInfinite)
                    this.creature.setUnchargedMultistepMoveStep(3);

                if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound() != null)
                    this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound(),
                            1f,
                            1f);
            }
            if (this.animTime >= this.maxMoveAnimTime) {
                this.creature.setUsingUnchargedAnim(false);
                this.currentInvokedMove.onStopExecuting(this.creature);
                //the cloak move only has a cooldown when removing the cloaking
                if (this.creature.currentCreatureMove() == CreatureMove.CLOAK && !this.creature.isCloaked()) {
                    this.creature.setMoveCooldown(this.creature.currentCreatureMove().maxCooldown);
                }
                //all other moves should have their cooldown applied as usual
                else if (this.creature.currentCreatureMove() != CreatureMove.CLOAK) this.creature.setMoveCooldown(this.creature.currentCreatureMove().maxCooldown);
                this.creature.setEnergy(this.creature.getEnergy() - this.creature.currentCreatureMove().energyUse[0]);
                this.moveChoiceCooldown = 20;
                this.animTime = 0;
                this.finishedAnimMarker = true;
                this.finishedMoveMarker = true;
            }

            //updating move anim tick
            if (!this.finishedAnimMarker) {
                this.animTime++;
                if (this.creature.currentCreatureMove().useTimeIsInfinite && this.animTime > this.moveAnimChargeToUseTime && !this.currentInvokedMove.forceStopFlag) {
                    this.moveAnimUseTime++;
                    this.maxMoveAnimTime++;
                }
            }
        }
    }

    private void selectMeleeMoveForBlockBreak() {
        //block with moveChoiceCooldown first
        if (this.moveChoiceCooldown-- > 0) return;

        CreatureMove moveToSelect = null;

        //select from available moves first
        for (CreatureMove creatureMove : this.creature.getLearnedMoves().getList()) {
            if (creatureMove == null) continue;
            if (creatureMove.moveAnimType.moveType != CreatureMove.MoveType.MELEE
                    || creatureMove.chargeType.requiresCharge()
                    || creatureMove.creatureMove == null
                    || this.creature.animatorsForMoveType().get(creatureMove.moveAnimType) == null) continue;
            if (this.creature.getMoveCooldown(creatureMove) <= 0) {
                moveToSelect = creatureMove;
                break;
            }
        }
        if (moveToSelect == null) return;

        //now set animation related stuff
        this.creature.setCurrentCreatureMove(moveToSelect);
        this.currentInvokedMove = moveToSelect.invokeMove();

        this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getStartMoveDelayPoint();
        this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpPoint();
        this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUsePoint();
        this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getUseDurationPoint();
        this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUsePoint();

        if (this.creature.animatorsForMoveType().get(moveToSelect.moveAnimType).getUseDurationSound() != null)
            this.useSoundLooper = new RiftSoundLooper(this.creature,
                    this.creature.animatorsForMoveType().get(moveToSelect.moveAnimType).getUseDurationSound(),
                    5,
                    1f,
                    1f);

        this.finishedAnimMarker = false;
    }

    //a melee move will be used to cause block breaking, hence this
    //preferred melee move will be the one that doesn't require charging up
    private boolean hasAvailableMeleeMoves() {
        for (CreatureMove creatureMove : this.creature.getLearnedMoves().getList()) {
            if (creatureMove == null) continue;
            if (creatureMove.moveAnimType.moveType != CreatureMove.MoveType.MELEE
                    || creatureMove.chargeType.requiresCharge()
                    || creatureMove.creatureMove == null
                    || this.creature.animatorsForMoveType().get(creatureMove.moveAnimType) == null) continue;
            if (this.creature.getMoveCooldown(creatureMove) <= 0) return true;
        }
        return false;
    }

    private BlockPos getNextBlock(EntityLivingBase target, double dist) {
        int digWidth = MathHelper.ceil(this.creature.width);
        int digHeight = MathHelper.ceil(this.creature.height);

        int passMax = digWidth * digWidth * digHeight;

        int y = this.scanTick % digHeight;
        int x = (this.scanTick % (digWidth * digHeight))/digHeight;
        int z = this.scanTick/(digWidth * digHeight);

        double rayX = x + this.creature.posX - (digWidth / 2D);
        double rayY = y + this.creature.posY + 0.5D;
        double rayZ = z + this.creature.posZ - (digWidth / 2D);
        Vec3d rayOrigin = new Vec3d(rayX, rayY, rayZ);
        Vec3d rayOffset = target.getPositionVector();
        rayOffset = rayOrigin.add(rayOffset.subtract(rayOrigin).normalize().scale(dist));

        BlockPos p1 = this.creature.getPosition();
        BlockPos p2 = target.getPosition();

        if (p1.getDistance(p2.getX(), p1.getY(), p2.getZ()) < this.creature.attackWidth() + this.creature.width) {
            if (p2.getY() - p1.getY() > (this.creature.attackWidth() + this.creature.width) / 2) {
                rayOffset = rayOrigin.add(0D, dist, 0D);
            }
            else if (p2.getY() - p1.getY() < (this.creature.attackWidth() + this.creature.width) / 2) {
                rayOffset = rayOrigin.add(0D, -dist, 0D);
            }
        }

        RayTraceResult ray = this.creature.world.rayTraceBlocks(rayOrigin, rayOffset, false, true, false);
        this.scanTick = (this.scanTick + 1) % passMax;

        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = ray.getBlockPos();
            IBlockState state = this.creature.world.getBlockState(pos);

            if (this.creature.checkIfCanBreakBlock(state)
                    && !state.getBlock().isPassable(this.creature.world, pos)
            ) return pos;
        }

        return null;
    }
}
