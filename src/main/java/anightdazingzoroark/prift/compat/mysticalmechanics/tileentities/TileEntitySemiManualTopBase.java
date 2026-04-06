package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.ConsumerMechCapability;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockSemiManualBase;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualRecipeBase;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.BooleanPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.IntegerPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.StringPropertyValue;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntity;
import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.util.Misc;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public abstract class TileEntitySemiManualTopBase extends RiftTileEntity implements ITickable {
    private final IMechCapability mechPower;

    public TileEntitySemiManualTopBase() {
        super();
        this.mechPower = new ConsumerMechCapability() {
            @Override
            public void onPowerChange() {
                TileEntitySemiManualTopBase.this.markDirty();
            }
        };
    }

    @Override
    public void registerValues() {
        this.registerValue(new IntegerPropertyValue("TimeHeld", 0));
        this.registerValue(new BooleanPropertyValue("MustBeReset", false));
        this.registerValue(new StringPropertyValue("CurrentRecipeId", ""));
    }

    @Override
    public void update() {
        if (!this.world.isRemote) return;
        //get nearby players that will hear the sounds
        AxisAlignedBB hearRange = new AxisAlignedBB(this.getPos().getX() - 8, this.getPos().getY() - 8, this.getPos().getZ() - 8, this.getPos().getX() + 8, this.getPos().getY() + 8, this.getPos().getZ() + 8);
        List<EntityPlayer> playerList = this.world.getEntitiesWithinAABB(EntityPlayer.class, hearRange, null);
        if (this.getPower() > 0 && this.world.rand.nextInt(40) < 2) for (EntityPlayer player : playerList) this.world.playSound(player, this.pos, SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.BLOCKS, 0.75F, this.world.rand.nextFloat() * 0.4F + 0.8F);

        //add smoke particles and make jamming sounds when it needs to reset
        Random rand = new Random();
        double motionX = rand.nextGaussian() * 0.07D;
        double motionY = rand.nextGaussian() * 0.07D;
        double motionZ = rand.nextGaussian() * 0.07D;
        float f = rand.nextFloat() + this.pos.getX();
        float f1 = rand.nextFloat() + this.pos.getY();
        float f2 = rand.nextFloat() + this.pos.getZ();
        if (this.getMustBeReset()) this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f, f1, f2, motionX, motionY, motionZ);
    }

    public EnumFacing getFacing() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        return state.getValue(BlockSemiManualBase.FACING);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && facing == EnumFacing.UP) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && facing == EnumFacing.UP) return (T) this.mechPower;
        return super.getCapability(capability, facing);
    }

    public abstract SemiManualRecipeBase getCurrentRecipe();

    public String getCurrentRecipeId() {
        return this.getValue("CurrentRecipeId");
    }

    public void setCurrentRecipeId(String value) {
        this.setValue("CurrentRecipeId", value);
    }

    public void setCurrentRecipe(SemiManualRecipeBase value) {
        if (value == null) this.setCurrentRecipeId("");
        else this.setCurrentRecipeId(value.getId());
    }

    public int getMaxRecipeTime() {
        //this estimates max time based on power input requires
        //at min power required its the default 15 seconds, but the higher the power the lower
        //the max time is until it reaches 5 seconds, which is 8x the min power
        //note that output is in ticks
        SemiManualRecipeBase currentRecipe = this.getCurrentRecipe();
        if (currentRecipe != null) {
            double minPower = currentRecipe.getMinPower();
            if (minPower <= this.getPower()) {
                double result = RiftUtil.slopeResult(this.getPower(), true, minPower, 8 * minPower, 30, 5);
                return (int) (result * 20);
            }
        }
        return -1;
    }

    public double getPower() {
        return this.mechPower.getPower(null);
    }

    public int getTimeHeld() {
        return this.getValue("TimeHeld");
    }

    public void setTimeHeld(int value) {
        this.setValue("TimeHeld", value);
    }

    public boolean getMustBeReset() {
        return this.getValue("MustBeReset");
    }

    public void setMustBeReset(boolean value) {
        this.setValue("MustBeReset", value);
    }

    public TileEntitySemiManualBase getBottomTEntity() {
        return (TileEntitySemiManualBase) this.world.getTileEntity(this.pos.down());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this, false);
    }
}
