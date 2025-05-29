package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Coelacanth extends RiftWaterCreature implements IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/coelacanth"));
    private RiftCreaturePart bodyFront0;
    private RiftCreaturePart bodyFront1;
    private RiftCreaturePart bodyFront2;
    private RiftCreaturePart bodyBack0;
    private RiftCreaturePart bodyBack1;
    private RiftCreaturePart bodyBack2;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Coelacanth(World worldIn) {
        super(worldIn, RiftCreatureType.COELACANTH);
        this.setSize(0.5f, 0.5f);
        this.experienceValue = 3;
        this.speed = 0.5D;

        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0f, 0.5f, 1f, 1f);
        this.bodyFront0 = new RiftCreaturePart(this, 1.1f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyFront1 = new RiftCreaturePart(this, 0.72f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyFront2 = new RiftCreaturePart(this, 0.36f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyBack0 = new RiftCreaturePart(this, -1.1f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyBack1 = new RiftCreaturePart(this, -0.72f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyBack2 = new RiftCreaturePart(this, -0.36f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.bodyPart,
            this.bodyFront0,
            this.bodyFront1,
            this.bodyFront2,
            this.bodyBack0,
            this.bodyBack1,
            this.bodyBack2
        };
    }

    protected void initEntityAI() {
        this.tasks.addTask(3, new RiftHerdMemberFollow(this));
        this.tasks.addTask(4, new RiftWanderWater(this, 1.0D));
    }

    @Override
    public void updateParts() {
        super.updateParts();
    }

    @Override
    public boolean canDoHerding() {
        return this.isInWater();
    }

    public RiftCreature getHerder() {
        return this;
    }

    public RiftCreature getHerdLeader() {
        return this.herdLeader;
    }

    public void setHerdLeader(RiftCreature creature) {
        this.herdLeader = creature;
    }

    public int getHerdSize() {
        return this.herdSize;
    }

    public void setHerdSize(int value) {
        this.herdSize = value;
    }

    public double followRange() {
        return 2D;
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(1, Collections.singletonList(CreatureMove.BOUNCE));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        return Collections.emptyMap();
    }
    //move related stuff ends here

    @Override
    public float[] ageScaleParams() {
        return new float[]{1f, 1f};
    }

    public float attackWidth() {
        return 0;
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public boolean isAmphibious() {
        return false;
    }

    public boolean canFlop() {
        return true;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }
}
