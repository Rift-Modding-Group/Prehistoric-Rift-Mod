package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Megapiranha extends RiftWaterCreature implements IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/megapiranha"));
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Megapiranha(World worldIn) {
        super(worldIn, RiftCreatureType.MEGAPIRANHA);
        this.setSize(0.5f, 0.75f);
        this.experienceValue = 3;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.speed = 0.35D;
        this.waterSpeed = 4D;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);

        this.headPart = new RiftCreaturePart(this, 0, 0, 0, 0.25f,  0.375f, 1f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this, true));
        this.tasks.addTask(1, new EntityAIAvoidEntity<>(this, Sarcosuchus.class, 8.0F, 4.0D, 4D));
        this.tasks.addTask(3, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(4, new RiftHerdMemberFollow(this));
        this.tasks.addTask(5, new RiftWanderWater(this, 1.0D));
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

    @Override
    public float[] ageScaleParams() {
        return new float[]{1f, 1f};
    }

    //move related stuff starts here
    @Override
    public List<CreatureMove> learnableMoves() {
        return Collections.singletonList(CreatureMove.BITE);
    }

    @Override
    public List<CreatureMove> initialMoves() {
        return Collections.singletonList(CreatureMove.BITE);
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.JAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(1D)
                .defineChargeUpToUseLength(1.5D)
                .defineRecoverFromUseLength(2.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .finalizePoints()
        );
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 2f;
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
