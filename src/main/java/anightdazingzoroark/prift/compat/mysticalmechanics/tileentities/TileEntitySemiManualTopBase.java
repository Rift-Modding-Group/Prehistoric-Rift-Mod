package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.ConsumerMechCapability;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockSemiManualBase;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualRecipeBase;
import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.util.Misc;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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

public class TileEntitySemiManualTopBase extends TileEntity implements ITickable {
    private SemiManualRecipeBase currentRecipe;
    private final IMechCapability mechPower;
    private int timeHeld;
    private boolean mustBeReset = false;

    public TileEntitySemiManualTopBase() {
        this.mechPower = new ConsumerMechCapability() {
            @Override
            public void onPowerChange() {
                TileEntitySemiManualTopBase.this.markDirty();
            }
        };
    }

    @Override
    public void update() {
        if (this.world.isRemote) {
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

    public SemiManualRecipeBase getCurrentRecipe() {
        return this.currentRecipe;
    }

    public String getCurrentRecipeId() {
        if (this.currentRecipe != null) return this.currentRecipe.getId();
        return "";
    }

    public void setCurrentRecipe(SemiManualRecipeBase value) {
        this.currentRecipe = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public int getMaxRecipeTime() {
        //this estimates max time based on power input requires
        //at min power required its the default 15 seconds, but the higher the power the lower
        //the max time is until it reaches 5 seconds, which is 8x the min power
        //note that output is in ticks
        if (this.currentRecipe != null) {
            double minPower = this.currentRecipe.getMinPower();
            if (minPower <= this.getPower()) {
                double result = -10D / (7D * minPower) * (this.getPower() - minPower) + 15D;
                return (int)RiftUtil.clamp(result, 5D, 30D) * 20;
            }
        }
        return -1;
    }

    public double getPower() {
        return this.mechPower.getPower(null);
    }

    public int getTimeHeld() {
        return this.timeHeld;
    }

    public void setTimeHeld(int value) {
        this.timeHeld = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public boolean getMustBeReset() {
        return this.mustBeReset;
    }

    public void setMustBeReset(boolean value) {
        this.mustBeReset = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public TileEntitySemiManualBase getBottomTEntity() {
        return (TileEntitySemiManualBase)this.world.getTileEntity(this.pos.down());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        this.mechPower.writeToNBT(compound);
        compound.setBoolean("mustBeReset", this.mustBeReset);
        compound.setInteger("timeHeld", this.timeHeld);
        compound.setString("currentRecipe", this.getCurrentRecipeId());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.mechPower.readFromNBT(compound);
        this.mustBeReset = compound.getBoolean("mustBeReset");
        this.timeHeld = compound.getInteger("timeHeld");
        this.currentRecipe = RiftMMRecipes.getSMRecipe(compound.getString("currentRecipe"));
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.mechPower.readFromNBT(tag);
        this.mustBeReset = tag.getBoolean("mustBeReset");
        this.timeHeld = tag.getInteger("timeHeld");
        this.currentRecipe = RiftMMRecipes.getSMRecipe(tag.getString("currentRecipe"));
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this, false);
    }
}
