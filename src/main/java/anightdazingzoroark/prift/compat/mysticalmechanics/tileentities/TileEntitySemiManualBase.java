package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockSemiManualBase;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.BooleanPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.DoublePropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.IntegerPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.StringPropertyValue;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityContainer;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public abstract class TileEntitySemiManualBase extends RiftTileEntityContainer implements IAnimatable, ITickable, ISidedInventory {
    private final AnimationFactory factory = new AnimationFactory(this);

    @Override
    public void registerValues() {
        this.registerValue(new BooleanPropertyValue("PlayResetAnim", false));
        this.registerValue(new IntegerPropertyValue("ResetAnimTime", 0));
    }

    @Override
    public void registerInventories() {
        this.registerInventory("Input", 1);
        this.registerInventorySiding("Input", SideInvInteraction.INSERT, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST);
    }

    @Override
    public void registerFluidTanks() {}

    @Override
    public void update() {
        //manage reset anim
        if (this.world.isRemote) return;
        if (this.canDoResetAnim()) {
            this.setResetAnimTime(this.getResetAnimTime() + 1);
            if (this.getResetAnimTime() >= 10) {
                this.setPlayResetAnim(false);
                this.setResetAnimTime(0);
            }
        }
    }

    public EnumFacing getFacing() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        return state.getValue(BlockSemiManualBase.FACING);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == EnumFacing.DOWN) return (T) new SidedInvWrapper(this, EnumFacing.DOWN);
            else if (facing == EnumFacing.UP)  return (T) new SidedInvWrapper(this, EnumFacing.UP);
            else if (facing == EnumFacing.EAST)  return (T) new SidedInvWrapper(this, EnumFacing.EAST);
            else if (facing == EnumFacing.WEST)  return (T) new SidedInvWrapper(this, EnumFacing.WEST);
            else if (facing == EnumFacing.NORTH)  return (T) new SidedInvWrapper(this, EnumFacing.NORTH);
            else if (facing == EnumFacing.SOUTH)  return (T) new SidedInvWrapper(this, EnumFacing.SOUTH);
            else return (T) new SidedInvWrapper(this, EnumFacing.NORTH);
        }
        return super.getCapability(capability, facing);
    }

    public boolean canDoResetAnim() {
        return this.getValue("PlayResetAnim");
    }

    public void setPlayResetAnim(boolean value) {
        this.setValue("PlayResetAnim", value);
    }

    public int getResetAnimTime() {
        return this.getValue("ResetAnimTime");
    }

    public void setResetAnimTime(int value) {
        this.setValue("ResetAnimTime", value);
    }

    //inventory stuff starts here
    public RiftInventoryHandler getInputInventory() {
        return this.getInventory("Input");
    }

    public ItemStack getInputItem() {
        return this.getInputInventory().getStackInSlot(0);
    }
    //inventory stuff ends here

    public TileEntitySemiManualTopBase getTopTEntity() {
        TileEntity tileEntity = this.world.getTileEntity(this.pos.up());
        if (!(tileEntity instanceof TileEntitySemiManualTopBase teSemiManualTop)) return null;
        return teSemiManualTop;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "reset", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent animationEvent) {
                if (canDoResetAnim()) {
                    animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("animation.semi_manual_extractor.release", LoopType.PLAY_ONCE));
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