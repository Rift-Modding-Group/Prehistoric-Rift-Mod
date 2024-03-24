package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentTranslation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileEntityLeadPoweredCrank extends TileEntity implements IAnimatable, ITickable {
    private final AnimationFactory factory = new AnimationFactory(this);
    private boolean hasLead = false;
    private RiftCreature worker;
    private NBTTagCompound workerNBT;

    public void update() {
        System.out.println(this.worker);
        //for worker
        if (this.workerNBT != null && this.world != null && this.worker == null) {
            this.findWorker();
        }

        //for hasLead client sync

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("hasLead", this.hasLead);

        if (this.worker != null) {
            if (this.workerNBT == null) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                UUID uuid = this.worker.getUniqueID();
                nbtTagCompound.setUniqueId("workerUUID", uuid);
                this.workerNBT = nbtTagCompound;
            }
            compound.setTag("workerNBT", this.workerNBT);
        }

        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.hasLead = compound.getBoolean("hasLead");

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

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.hasLead = tag.getBoolean("hasLead");
        if (this.hasLead) this.workerNBT = tag.getCompoundTag("workerNBT");
    }

    //for gettin worker when loadin world
    private void findWorker() {
        UUID uuid = this.workerNBT.getUniqueId("workerUUID");
        RiftCreature tentWorker = (RiftCreature)RiftUtil.getEntityFromUUID(this.world, uuid);
        if (tentWorker != null) this.setWorker(tentWorker);
    }

    public RiftCreature getWorker() {
        return this.worker;
    }

    @Override
    public void registerControllers(AnimationData animationData) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}