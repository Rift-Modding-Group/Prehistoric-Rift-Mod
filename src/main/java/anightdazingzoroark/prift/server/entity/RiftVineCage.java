package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creatureMoves.builders.VineCageBuilder;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityNonLiving;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RiftVineCage extends Entity implements IAnimatable<AnimationDataEntityNonLiving>, IEntityAdditionalSpawnData {
    public static final int LIFETIME = 100;
    public static final int GROWTH_TICKS = 8;
    public static final double WALL_THICKNESS_RATIO = 1D / 12D;

    @Nullable
    private RiftCreature creator;
    private float damage;
    @NotNull
    private Element element = Element.GRASS;
    @NotNull
    private final AnimationDataEntityNonLiving animationData = new AnimationDataEntityNonLiving(this);

    public RiftVineCage(@NotNull World world) {
        super(world);
        this.setSize(3f, 3f);
        this.setNoGravity(true);
    }

    public RiftVineCage(
            @NotNull RiftCreature creator, @NotNull CreatureMoveBuilder move,
            @NotNull Vec3d floor, @Nullable EntityLivingBase target
    ) {
        this(creator.world);
        this.creator = creator;
        this.damage = (float) CreatureMoveHelper.calculateDamage(creator, null, move);
        this.element = move.getElement();
        if (target != null) this.setSize(Math.max(3f, (target.width + 0.5f) * 1.2f), Math.max(3f, target.height + 0.5f));
        this.setPosition(floor.x, floor.y, floor.z);
    }

    public static void spawn(@NotNull ICreature creature) {
        if (!(creature.getAnimationData().getHolder() instanceof RiftCreature creator) || creator.world.isRemote) return;
        CreatureMoveBuilder move = creator.getCreatureMoves().getMoveBuilderCurrentMove();
        if (move == null) return;

        RayTraceResult hit = creature.getCrosshairTargets();
        EntityLivingBase target = null;
        if (creature.getControllingPassenger() == null) target = creature.getAttackTarget();
        else if (hit != null && hit.entityHit instanceof EntityLivingBase living) target = living;
        Vec3d floor = VineCageBuilder.getStrikeFloor(creature, target);
        if (floor == null || !VineCageBuilder.isWithinRange(creature, floor)) return;

        if (creator.world.spawnEntity(new RiftVineCage(creator, move, floor, target))) {
            creator.world.playSound(null, floor.x, floor.y, floor.z,
                    RiftSounds.getSound("root_stab.use"), SoundCategory.NEUTRAL, 1f, 0.8f);
        }
    }

    public boolean isActive() {
        return !this.isDead && this.ticksExisted >= GROWTH_TICKS && this.ticksExisted < LIFETIME - GROWTH_TICKS;
    }

    @NotNull
    public static List<AxisAlignedBB> getWallBounds(@NotNull AxisAlignedBB bounds) {
        double thickness = (bounds.maxX - bounds.minX) * WALL_THICKNESS_RATIO;
        return List.of(
                new AxisAlignedBB(bounds.minX, bounds.minY, bounds.minZ, bounds.minX + thickness, bounds.maxY, bounds.maxZ),
                new AxisAlignedBB(bounds.maxX - thickness, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ),
                new AxisAlignedBB(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.minZ + thickness),
                new AxisAlignedBB(bounds.minX, bounds.minY, bounds.maxZ - thickness, bounds.maxX, bounds.maxY, bounds.maxZ)
        );
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            double growth = 1D;
            if (this.ticksExisted < GROWTH_TICKS) {
                growth = MathUtil.slopeResult(this.ticksExisted, true, 0D, GROWTH_TICKS, 0D, 1D);
            }
            else if (this.ticksExisted > LIFETIME - GROWTH_TICKS) {
                growth = MathUtil.slopeResult(this.ticksExisted, true, LIFETIME - GROWTH_TICKS, LIFETIME, 1D, 0D);
            }
            this.animationData.setVariable("cage_growth", growth);
            return;
        }
        if (this.creator == null || this.ticksExisted >= LIFETIME) {
            this.setDead();
            return;
        }
        if (!this.isActive()) return;

        double thickness = this.width * WALL_THICKNESS_RATIO;
        AxisAlignedBB interior = this.getEntityBoundingBox().grow(-thickness, 0D, -thickness);
        boolean damagePulse = (this.ticksExisted - GROWTH_TICKS) % 20 == 0;
        for (EntityLivingBase living : this.world.getEntitiesWithinAABB(EntityLivingBase.class, interior,
                living -> living != null && living.isEntityAlive() && EntitySelectors.NOT_SPECTATING.apply(living))) {
            if (this.creator.isRelatedToEntity(living)) continue;
            living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10, 1, false, false));
            if (damagePulse) {
                living.attackEntityFrom(RiftDamage.getElementDamageSource("mob", this, this.creator, this.element), this.damage);
            }
        }
    }

    @Override
    public void writeSpawnData(@NotNull ByteBuf buffer) {
        buffer.writeFloat(this.width);
        buffer.writeFloat(this.height);
        buffer.writeInt(this.ticksExisted);
    }

    @Override
    public void readSpawnData(@NotNull ByteBuf buffer) {
        this.setSize(buffer.readFloat(), buffer.readFloat());
        this.ticksExisted = buffer.readInt();
    }

    @Override
    protected void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        this.setDead();
    }

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound compound) {}

    @Override
    public boolean writeToNBTOptional(@NotNull NBTTagCompound compound) {
        return false;
    }

    @Override
    public boolean writeToNBTAtomically(@NotNull NBTTagCompound compound) {
        return false;
    }

    @Override
    @NotNull
    public AnimationDataEntityNonLiving getAnimationData() {
        return this.animationData;
    }

    @Override
    public void initializeAnimationData(@NotNull AnimationDataEntityNonLiving animationData) {
        animationData.addAnimationController(new AnimationController<RiftVineCage, AnimationDataEntityNonLiving>(this, "cage", "default",
                new AnimationControllerState<AnimationDataEntityNonLiving>("default")
                        .addAnimation("animation.vine_cage.grow")));
    }
}
