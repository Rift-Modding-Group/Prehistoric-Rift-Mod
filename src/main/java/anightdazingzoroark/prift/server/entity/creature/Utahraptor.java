package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.UtahraptorConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureinterface.ILeapingMob;
import anightdazingzoroark.prift.server.entity.creatureinterface.IPackHunter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import java.util.ArrayList;
import java.util.List;

public class Utahraptor extends RiftCreature implements ILeapingMob, IPackHunter {
    private static final DataParameter<Boolean> PACK_BUFFING = EntityDataManager.createKey(Utahraptor.class, DataSerializers.BOOLEAN);
    private int packBuffCooldown;

    public Utahraptor(World worldIn) {
        super(worldIn, RiftCreatureType.UTAHRAPTOR);
        this.minCreatureHealth = UtahraptorConfig.getMinHealth();
        this.maxCreatureHealth = UtahraptorConfig.getMaxHealth();
        this.setSize(1.25f, 1.5f);
        this.experienceValue = 3;
        this.favoriteFood = UtahraptorConfig.utahraptorFavoriteFood;
        this.tamingFood = UtahraptorConfig.utahraptorTamingFood;
        this.speed = 0.35D;
        this.isRideable = true;
        this.attackWidth = 2f;
        this.leapWidth = 16f;
        this.packBuffCooldown = 0;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PACK_BUFFING, Boolean.valueOf(false));
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(UtahraptorConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, UtahraptorConfig.utahraptorTargets, true, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, UtahraptorConfig.utahraptorFavoriteFood, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftPackBuff(this, 1.68f, 90f));
        this.tasks.addTask(3, new RiftControlledAttack(this, 0.28F, 0.28F));
        this.tasks.addTask(4, new RiftLeapAttack(this, 1.5f, 160));
        this.tasks.addTask(5, new RiftAttack(this, 1.0D, 0.28F, 0.28F));
        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(7, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(8, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(9, new RiftHerdMemberFollow(this, 10D, 2D, 1D));
        this.tasks.addTask(10, new RiftWander(this, 1.0D));
        this.tasks.addTask(11, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanLeap();
        this.manageCanPackBuff();
        if (!this.world.isRemote) {
            this.setClimbing(this.collidedHorizontally);
        }
    }

    private void manageCanLeap() {
        if (this.leapCooldown > 0) this.leapCooldown--;
    }

    private void manageCanPackBuff() {
        if (this.packBuffCooldown > 0) this.packBuffCooldown--;
    }

    public void fall(float distance, float damageMultiplier) {}

    protected PathNavigate createNavigator(World worldIn)
    {
        return new PathNavigateClimber(this, worldIn);
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
        return 18;
    }

    public void setPackBuffing(boolean value) {
        this.dataManager.set(PACK_BUFFING, Boolean.valueOf(value));
        this.setActing(value);
    }

    public boolean isPackBuffing() {
        return this.dataManager.get(PACK_BUFFING);
    }

    public void setPackBuffCooldown(int value) {
        this.packBuffCooldown = value;
    }

    public int getPackBuffCooldown() {
        return this.packBuffCooldown;
    }

    public List<PotionEffect> packBuffEffect() {
        List<PotionEffect> packBuffEffects = new ArrayList<>();
        packBuffEffects.add(new PotionEffect(MobEffects.SPEED, 90 * 20, 2));
        packBuffEffects.add(new PotionEffect(MobEffects.STRENGTH, 90 * 20, 2));
        return packBuffEffects;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (1.25) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (1.25) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(this.posX, this.posY - 0.75, this.posZ);
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
        return true;
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1f);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::utahraptorMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::utahraptorAttack));
        data.addAnimationController(new AnimationController(this, "pack_buff", 0, this::utahraptorPackBuff));
    }

    private <E extends IAnimatable> PlayState utahraptorMovement(AnimationEvent<E> event) {
        if (event.isMoving() && this.onGround) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.walk", true));
            return PlayState.CONTINUE;
        }
        else if (!this.onGround && !this.isSitting()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.pounce", true));
            return PlayState.CONTINUE;
        }
        else if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.sitting", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState utahraptorAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.attack", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState utahraptorPackBuff(AnimationEvent<E> event) {
        if (this.isPackBuffing()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.pack_buffing", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }
}
