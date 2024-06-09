package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockSemiManualBase;
import mysticalmechanics.util.Misc;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public abstract class TileEntitySemiManualBase extends TileEntity implements IAnimatable, ITickable {
    private final AnimationFactory factory = new AnimationFactory(this);
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            TileEntitySemiManualBase.this.markDirty();
        }
    };
    private boolean playResetAnim;
    private int resetAnimTime;

    @Override
    public void update() {
        //manage reset anim
        if (!this.world.isRemote) {
            if (this.canDoResetAnim()) {
                this.setResetAnimTime(this.getResetAnimTime() + 1);
                if (this.getResetAnimTime() >= 10) {
                    this.setPlayResetAnim(false);
                    this.setResetAnimTime(0);
                }
            }
        }
    }

    public EnumFacing getFacing() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        return state.getValue(BlockSemiManualBase.FACING);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("items")) {
            this.itemStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        }
        this.playResetAnim = compound.getBoolean("playResetAnim");
        this.resetAnimTime = compound.getInteger("resetAnimTime");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("items", this.itemStackHandler.serializeNBT());
        compound.setBoolean("playResetAnim", this.playResetAnim);
        compound.setInteger("resetAnimTime", this.resetAnimTime);
        return compound;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemStackHandler);
        return super.getCapability(capability, facing);
    }

    public boolean canDoResetAnim() {
        return this.playResetAnim;
    }

    public void setPlayResetAnim(boolean value) {
        this.playResetAnim = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public int getResetAnimTime() {
        return this.resetAnimTime;
    }

    public void setResetAnimTime(int value) {
        this.resetAnimTime = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
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
        if (tag.hasKey("items")) {
            this.itemStackHandler.deserializeNBT((NBTTagCompound) tag.getTag("items"));
        }
        this.playResetAnim = tag.getBoolean("playResetAnim");
        this.resetAnimTime = tag.getInteger("resetAnimTime");
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    public ItemStack getInputItem() {
        IItemHandler itemHandler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (itemHandler != null) return itemHandler.getStackInSlot(0);
        return null;
    }

    public TileEntitySemiManualTopBase getTopTEntity() {
        return (TileEntitySemiManualTopBase)this.world.getTileEntity(this.pos.up());
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "reset", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent animationEvent) {
                if (canDoResetAnim()) {
                    animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("animation.semi_manual_extractor.release", false));
                    return PlayState.CONTINUE;
                }
                animationEvent.getController().clearAnimationCache();
                return PlayState.STOP;
            }
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos().add(-1, 0, -1), this.getPos().add(1, 2, 1));
    }
}
