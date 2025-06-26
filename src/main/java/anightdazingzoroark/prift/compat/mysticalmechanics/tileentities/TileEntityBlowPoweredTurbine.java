package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockBlowPoweredTurbine;
import mysticalmechanics.api.DefaultMechCapability;
import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.util.Misc;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public class TileEntityBlowPoweredTurbine extends TileEntity implements IAnimatable, ITickable {
    private final AnimationFactory factory = new AnimationFactory(this);
    public IMechCapability mechPower;
    private float power;
    private float rotation = 0;

    public TileEntityBlowPoweredTurbine() {
        this.mechPower = new DefaultMechCapability() {
            @Override
            public void setPower(double value, EnumFacing from) {
                if (from == null) super.setPower(value, null);
            }

            @Override
            public boolean isOutput(EnumFacing face) {
                return true;
            }

            @Override
            public boolean isInput(EnumFacing face) {
                return false;
            }

            @Override
            public void onPowerChange(){
                updateNeighbors();
                markDirty();
            }
        };
    }

    @Override
    public void update() {
        if (!this.world.isRemote) {
            if (this.power > 0) {
                this.mechPower.setPower(this.power, null);
                this.setPower(this.power - 0.05f);
            }
            else this.mechPower.setPower(0, null);
            this.updateNeighbors();
            this.markDirty();
        }
        if (this.power > 0) {
            if (this.getFacing().equals(EnumFacing.NORTH) || this.getFacing().equals(EnumFacing.WEST)) this.setRotation(this.rotation + this.power);
            else this.setRotation(this.rotation - this.power);

            if (this.rotation >= 360f) this.setRotation(this.rotation - 360f);
            else if (this.rotation < 0f) this.setRotation(this.rotation + 360f);
        }
    }

    public void onBreakTurbine() {
        this.mechPower.setPower(0f, null);
        this.updateNeighbors();
    }

    public EnumFacing getFacing() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        return state.getValue(BlockBlowPoweredTurbine.FACING);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing){
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && facing == this.getFacing().getOpposite()){
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing){
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && facing == this.getFacing().getOpposite()){
            return (T) this.mechPower;
        }
        return super.getCapability(capability, facing);
    }

    public void updateNeighbors() {
        EnumFacing facing = this.getFacing().getOpposite();
        TileEntity tile = this.world.getTileEntity(getPos().offset(facing));
        if (tile != null) {
            if (tile.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, facing.getOpposite())) {
                if (tile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, facing.getOpposite()).isInput(facing.getOpposite())) {
                    tile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, facing.getOpposite()).setPower(mechPower.getPower(facing.getOpposite()), facing.getOpposite());
                }
            }
        }
    }

    public float getPower() {
        return this.power;
    }

    public void setPower(float value) {
        this.power = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setRotation(float value) {
        this.rotation = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("power", this.power);
        compound.setFloat("rotation", this.rotation);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.power = compound.getFloat("power");
        this.rotation = compound.getInteger("rotation");
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(this.pos, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this, false);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.power = tag.getFloat("power");
        this.rotation = tag.getInteger("rotation");
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "rotation", 0, this::rotation));
    }

    private <E extends IAnimatable> PlayState rotation(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.blow_powered_turbine.rotate", true));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos().add(-1, -1, -1), this.getPos().add(1, 2, 1));
    }
}
