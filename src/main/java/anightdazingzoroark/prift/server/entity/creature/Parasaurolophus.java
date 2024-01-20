package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.config.ParasaurolophusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Parasaurolophus extends RiftCreature {
    private static final DataParameter<Boolean> USING_HORN = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_USE_HORN = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);

    public Parasaurolophus(World worldIn) {
        super(worldIn, RiftCreatureType.PARASAUROLOPHUS);
        this.minCreatureHealth = ParasaurolophusConfig.getMinHealth();
        this.maxCreatureHealth = ParasaurolophusConfig.getMaxHealth();
        this.setSize(2f, 2f);
        this.favoriteFood = ParasaurolophusConfig.parasaurolophusFavoriteFood;
        this.tamingFood = ParasaurolophusConfig.parasaurolophusTamingFood;
        this.experienceValue = 20;
        this.speed = 0.25D;
        this.attackWidth = 3.5f;
        this.saddleItem = ParasaurolophusConfig.parasaurolophusSaddleItem;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(USING_HORN, false);
        this.dataManager.register(CAN_USE_HORN, true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ParasaurolophusConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, ParasaurolophusConfig.parasaurolophusTargets, false, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(1, new RiftParasaurControlledHornUse(this));
        this.tasks.addTask(2, new RiftParasaurolophusAlertHerd(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.52F, 0.24F));
        this.tasks.addTask(3, new RiftAttack.ParasaurolophusAttack(this, 1.0D, 0.52F, 0.24F));
        this.tasks.addTask(5, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(5, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(6, new RiftHerdMemberFollow(this, 8D, 4D, 1D));
        this.tasks.addTask(7, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
        this.tasks.addTask(9, new RiftLookAround(this));
    }

    public void useScareHorn() {
        AxisAlignedBB aabb = this.getEntityBoundingBox().grow(12D);
        List<String> blacklist = Arrays.asList(ParasaurolophusConfig.parasaurolophusScareBlacklist);
        List<EntityLiving> entityList = this.world.getEntitiesWithinAABB(EntityLiving.class, aabb, new Predicate<EntityLiving>() {
            @Override
            public boolean apply(@Nullable EntityLiving input) {
                if (input instanceof EntityTameable) {
                    EntityTameable tame = (EntityTameable) input;
                    return !tame.isTamed();
                }
                return true;
            }
        });
        entityList.remove(this);
        for (EntityLiving entityLiving : entityList) {
            if (ParasaurolophusConfig.parasaurolophusScareBlistAsWlist) {
                if (blacklist.contains(EntityList.getKey(entityLiving).toString())) {
                    entityLiving.getNavigator().clearPath();
                    entityLiving.setAttackTarget(null);
                    BlockPos pos = this.getValidSpot(entityLiving);
                    entityLiving.getMoveHelper().setMoveTo(pos.getX(), pos.getY(), pos.getZ(), 2.25D);
                }
            }
            else {
                if (!blacklist.contains(EntityList.getKey(entityLiving).toString())) {
                    entityLiving.getNavigator().clearPath();
                    entityLiving.setAttackTarget(null);
                    BlockPos pos = this.getValidSpot(entityLiving);
                    entityLiving.getMoveHelper().setMoveTo(pos.getX(), pos.getY(), pos.getZ(), 2.25D);
                }
            }
        }
    }

    private BlockPos getValidSpot(Entity entity) {
        Vec3d targetPos = this.getPositionVector();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double xOffset = 48 * Math.cos(angle);
            double zOffset = 48 * Math.sin(angle);

            BlockPos newPos = new BlockPos(targetPos.x + xOffset, targetPos.y, targetPos.z + zOffset);

            if (this.isPositionValid(newPos, entity)) return newPos;
        }
        return null;
    }

    private boolean isPositionValid(BlockPos pos, Entity entity) {
        IBlockState state = this.world.getBlockState(pos);
        if (state.getMaterial().isSolid()) {
            return false;
        }
        int height = Math.round(entity.height);
        int width = Math.round(entity.width);
        for (int x = (int)(width/2f - 1f); x <= (int)(width/2f + 1f); x++) {
            for (int z = (int)(width/2f - 1f); z <= (int)(width/2f + 1f); z++) {
                for (int y = -height; y <= height; y++) {
                    if (!this.world.getBlockState(pos.add(x, y, z)).getMaterial().isSolid()) return true;
                }
            }
        }
        return false;
    }

    @Override
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY - 0.35, this.posZ);
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (target == null) {
                    if (!this.isActing()) this.setAttacking(true);
                }
                else {
                    if (!this.isActing()) {
                        this.ssrTarget = target;
                        this.setAttacking(true);
                    }
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        else if (control == 1) {
            System.out.println("right click");
            if (this.getEnergy() > 0) {
                if (!this.isActing()) {
                    this.setUsingHorn(true);
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
    }

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    @Override
    public boolean isTameableByFeeding() {
        return true;
    }

    @Override
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 27;
    }

    public boolean isUsingHorn() {
        return this.dataManager.get(USING_HORN);
    }

    public void setUsingHorn(boolean value) {
        this.dataManager.set(USING_HORN, value);
        this.setActing(value);
    }

    public boolean canUseHorn() {
        return this.dataManager.get(CAN_USE_HORN);
    }

    public void setCanUseHorn(boolean value) {
        this.dataManager.set(CAN_USE_HORN, value);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::parasaurolophusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::parasaurolophusAttack));
        data.addAnimationController(new AnimationController(this, "hornUse", 0, this::parasaurolophusHorn));
    }

    private <E extends IAnimatable> PlayState parasaurolophusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.attack", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusHorn(AnimationEvent<E> event) {
        if (this.isUsingHorn()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.call", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }
}
