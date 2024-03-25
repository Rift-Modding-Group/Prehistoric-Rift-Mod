package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockLeadPoweredCrank;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import mysticalmechanics.api.DefaultMechCapability;
import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.util.Misc;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.UUID;

import static net.minecraft.block.BlockDoor.getFacing;

public class TileEntityLeadPoweredCrank extends TileEntity implements IAnimatable, ITickable {
    private final AnimationFactory factory = new AnimationFactory(this);
    private boolean hasLead = false;
    private int rotation = 0;
    private RiftCreature worker;
    private NBTTagCompound workerNBT;
    public IMechCapability mechPower;

    public TileEntityLeadPoweredCrank() {
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

    public EnumFacing getFacing() {
        IBlockState state = getWorld().getBlockState(getPos());
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
        TileEntity tile = this.world.getTileEntity(getPos().offset(facing));
        if (tile != null) {
            if (tile.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, facing.getOpposite())) {
                if (tile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, facing.getOpposite()).isInput(facing.getOpposite())) {
                    tile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, facing.getOpposite()).setPower(this.mechPower.getPower(facing.getOpposite()), facing.getOpposite());
                }
            }
        }
    }

    public void update() {
        //for worker detection after loading
        if (this.workerNBT != null && this.world != null && this.worker == null) {
            this.findWorker();
        }

        //for updatin power related stuff
        if (this.worker != null && this.world != null) {
            if (!this.world.isRemote) {
                if (this.worker.isMoving(false)) {
//                    System.out.println("power");
                    this.mechPower.setPower(5f, null);
                }
                else {
//                    System.out.println("not moving");
                    this.mechPower.setPower(0f, null);
                }
                this.updateNeighbors();
                this.markDirty();
            }
        }
        else if (this.worker == null && this.world != null) {
            if (!this.world.isRemote) {
//                System.out.println("no worker");
                this.mechPower.setPower(0f, null);
                this.updateNeighbors();
                this.markDirty();
            }
        }
//        System.out.println(this.mechPower);
    }

    public void onBreakCrank() {
        this.mechPower.setPower(0f, null);
        this.updateNeighbors();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        this.mechPower.writeToNBT(compound);
        compound.setBoolean("hasLead", this.hasLead);
        compound.setInteger("rotation", this.rotation);

        if (this.worker != null) {
            if (this.workerNBT == null) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                UUID uuid = this.worker.getUniqueID();
                nbtTagCompound.setUniqueId("workerUUID", uuid);
                this.workerNBT = nbtTagCompound;
            }
            compound.setTag("workerNBT", this.workerNBT);
        }

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        
        this.mechPower.readFromNBT(compound);
        this.hasLead = compound.getBoolean("hasLead");
        this.rotation = compound.getInteger("rotation");

        if (this.getHasLead()) {
            this.workerNBT = compound.getCompoundTag("workerNBT");
        }
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

    public void setRotation(int value) {
        this.rotation = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public int getRotation() {
        return this.rotation;
    }

    public void setHasLead(boolean value) {
        this.hasLead = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public boolean getHasLead() {
        return this.hasLead;
    }

    public void setWorker(RiftCreature creature) {
        this.worker = creature;
        this.worker.setUseWorkstation(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        this.setHasLead(true);

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        UUID uuid = this.worker.getUniqueID();
        nbtTagCompound.setUniqueId("workerUUID", uuid);
        this.workerNBT = nbtTagCompound;

        this.markDirty();
    }

    public void removeWorker() {
        this.worker.clearWorkstation(false);
        this.setHasLead(false);
        this.worker = null;

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setUniqueId("workerUUID", RiftUtil.nilUUID);
        this.workerNBT = nbtTagCompound;

        this.markDirty();
    }

    public RiftCreature getWorker() {
        return this.worker;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.hasLead = tag.getBoolean("hasLead");
        this.rotation = tag.getInteger("rotation");
        if (this.hasLead) this.workerNBT = tag.getCompoundTag("workerNBT");
    }

    //for gettin worker when loadin world
    private void findWorker() {
        UUID uuid = this.workerNBT.getUniqueId("workerUUID");
        RiftCreature tentWorker = (RiftCreature)RiftUtil.getEntityFromUUID(this.world, uuid);
        if (tentWorker != null) this.setWorker(tentWorker);
    }

    @Override
    public void registerControllers(AnimationData animationData) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}