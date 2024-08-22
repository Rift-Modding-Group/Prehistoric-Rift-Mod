package anightdazingzoroark.prift.server.entity.interfaces;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public interface IHerder {
    default boolean canDoHerding() {
        return true;
    }
    public RiftCreature getHerder();
    public RiftCreature getHerdLeader();
    public void setHerdLeader(RiftCreature creature);
    public int getHerdSize();
    public void setHerdSize(int value);
    default boolean hasHerdLeader() {
        return this.getHerdLeader() != null && this.getHerdLeader().isEntityAlive();
    }

    default void addToHerdLeader(RiftCreature creature) {
        this.setHerdLeader(creature);
        ((IHerder)this.getHerdLeader()).setHerdSize(((IHerder)this.getHerdLeader()).getHerdSize() + 1);
    }

    default void separateFromHerdLeader() {
        ((IHerder)this.getHerdLeader()).setHerdSize(((IHerder)this.getHerdLeader()).getHerdSize() - 1);
        this.setHerdLeader(null);
    }

    default void manageHerding() {
        if (this.isHerdLeader() && !this.getHerder().world.isRemote && this.getHerder().world.rand.nextInt(200) == 1) {
            if (this.getHerder().world.getEntitiesWithinAABB(this.getHerder().getClass(), this.herdBoundingBox()).size() <= 1) {
                this.setHerdSize(1);
            }
        }
    }

    default AxisAlignedBB herdBoundingBox() {
        return this.getHerder().getEntityBoundingBox().grow(12D);
    }

    default boolean isHerdLeader() {
        return this.getHerdSize() > 1;
    }

    default boolean canAddToHerd() {
        return this.isHerdLeader() && this.getHerdSize() < this.maxHerdSize() && this.canDoHerding();
    }

    default boolean isNearHerdLeader() {
        return this.getHerder().getDistanceSq(this.getHerdLeader()) <= 144;
    }

    default void addCreatureToHerd(@Nonnull Stream<RiftCreature> stream) {
        try {
            stream.limit(this.maxHerdSize() - this.getHerdSize())
                    .filter(creature -> creature != this && creature instanceof IHerder)
                    .forEach(creature -> ((IHerder)creature).addToHerdLeader(this.getHerder()));
        }
        catch (Exception e) {}
    }

    default double followRange() {
        return 0.5D;
    }

    default void followLeader() {
        if (this.hasHerdLeader()) {
            if (!this.getHerder().getEntityBoundingBox().intersects(this.getHerdLeader().getEntityBoundingBox().grow(this.followRange()))) {
                this.getHerder().getNavigator().tryMoveToEntityLiving(this.getHerdLeader(), 1D);
            }
        }
    }

    default int maxHerdSize() {
        return 5;
    }

    class HerdData implements IEntityLivingData {
        public final RiftCreature herdLeader;

        public HerdData(@Nonnull RiftCreature creature) {
            this.herdLeader = creature;
        }
    }
}
