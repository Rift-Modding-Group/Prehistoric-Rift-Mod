package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;

public class Coelacanth extends RiftWaterCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/coelacanth"));
    public Coelacanth(World worldIn) {
        super(worldIn, RiftCreatureType.COELACANTH);
        this.minCreatureHealth = 6D;
        this.maxCreatureHealth = 6D;
        this.setSize(0.5f, 1f);
        this.experienceValue = 3;
        this.speed = 0.5D;
    }

    protected void initEntityAI() {
        this.tasks.addTask(3, new RiftHerdMemberFollow(this));
        this.tasks.addTask(4, new RiftWanderWater(this, 1.0D));
    }

    @Override
    public void resetParts(float scale) {}

    @Override
    public boolean canDoHerding() {
        return this.isInWater();
    }

    @Override
    public float getRenderSizeModifier() {
        return 0;
    }

    @Override
    public float getEyeHeight() { return this.height * 0.05f; }

    @Override
    public Vec3d riderPos() {
        return null;
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
    public boolean isAmphibious() {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return true;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {

    }
}
