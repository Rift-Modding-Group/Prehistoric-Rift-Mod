package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockLeadPoweredCrank;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.DoublePropertyValue;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntity;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import mysticalmechanics.api.DefaultMechCapability;
import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.util.Misc;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileEntityHandCrank extends RiftTileEntity implements IAnimatable, ITickable {
    private final AnimationFactory factory = new AnimationFactory(this);
    public IMechCapability mechPower;
    private float power;
    private int atMaxPowerTimer;

    public TileEntityHandCrank() {
        this.mechPower = new DefaultMechCapability() {
            @Override
            public void setPower(double value, EnumFacing from) {
                if (from == null)
                    super.setPower(value, null);
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
    public void registerValues() {
        this.registerValue(new DoublePropertyValue("Rotation", 0D));
    }

    @Override
    public void update() {
        if (!this.world.isRemote) {
            if (this.power > 0) {
                if (this.atMaxPowerTimer > 0) {
                    this.mechPower.setPower(this.power, null);
                    this.setPower(this.power);
                    this.atMaxPowerTimer--;
                }
                else {
                    this.mechPower.setPower(this.power, null);
                    this.setPower(this.power - 0.05f);
                }
            }
            else this.mechPower.setPower(0, null);
            this.updateNeighbors();
            this.markDirty();
        }
        if (this.power > 0) {
            if (this.getFacing().equals(EnumFacing.NORTH) || this.getFacing().equals(EnumFacing.WEST) || this.getFacing().equals(EnumFacing.UP)) {
                this.setRotation(this.getRotation() + this.power);
            }
            else this.setRotation(this.getRotation() - this.power);
            if (this.getRotation() >= 360) this.setRotation(this.getRotation() - 360D);
            else if (this.getRotation() < 0D) this.setRotation(this.getRotation() + 360D);
        }
    }

    public void onBreakCrank() {
        this.mechPower.setPower(0f, null);
        this.updateNeighbors();
    }

    public EnumFacing getFacing() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        return state.getValue(BlockLeadPoweredCrank.FACING);
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
        TileEntity tile = world.getTileEntity(getPos().offset(facing));
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

    public double getRotation() {
        return this.getValue("Rotation");
    }

    public void setRotation(double value) {
        this.setValue("Rotation", value);
    }

    public void setMaxPowerTimer(int value) {
        this.atMaxPowerTimer = value;
    }

    public int getAtMaxPowerTimer() {
        return this.atMaxPowerTimer;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this, false);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "rotation", 0, this::rotation));
    }

    private <E extends IAnimatable> PlayState rotation(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.hand_crank.rotation", LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public List<AnimatableValue> createAnimationVariables() {
        return List.of(new AnimatableValue("rotation", 0D));
    }

    @Override
    public List<AnimatableValue> tickAnimationVariables() {
        return List.of(new AnimatableValue("rotation", this.getRotation()));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
