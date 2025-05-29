package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.ConsumerMechCapability;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.MillstoneRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.util.Misc;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TileEntityMillstone extends TileEntity implements IAnimatable, ITickable, ISidedInventory {
    private final AnimationFactory factory = new AnimationFactory(this);
    private final IMechCapability mechPower;
    private int timeHeld;
    private double compPerc;
    protected NonNullList<ItemStack> itemStackHandler = NonNullList.<ItemStack>withSize(12, ItemStack.EMPTY);
    private MillstoneRecipe currentRecipe;

    public TileEntityMillstone() {
        this.mechPower = new ConsumerMechCapability() {
            @Override
            public void onPowerChange() {
                TileEntityMillstone.this.markDirty();
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
        }
        else {
            if (this.getPower() > 0) {
                if (this.getCurrentRecipe() == null) {
                    for (MillstoneRecipe recipe : RiftMMRecipes.millstoneRecipes) {
                        if (recipe.matches(this.getPower(), this.getInputItem())) {
                            this.setCurrentRecipe(recipe);
                        }
                    }
                }
                else {
                    if (this.itemToInventoryTest(false, this.currentRecipe.output.matchingStacks[0], 3)) {
                        this.setTimeHeld(this.getTimeHeld() + 1);
                        if (this.getMaxRecipeTime() != 69420666) this.setCompletionPercentage((double)this.getTimeHeld()/(double)this.getMaxRecipeTime());
                        if (this.getTimeHeld() >= this.getMaxRecipeTime()) {
                            this.itemToInventoryTest(true, this.currentRecipe.output.matchingStacks[0], 3);
                            this.getInputItem().shrink(1);
                            this.setTimeHeld(0);
                        }
                    }
                    if (!this.currentRecipe.input.apply(this.getInputItem())) {
                        this.setTimeHeld(0);
                        this.setCompletionPercentage(0);
                        this.setCurrentRecipe(null);
                    }
                }
            }
            else {
                if (this.getCurrentRecipe() != null) {
                    if (!this.currentRecipe.input.apply(this.getInputItem())) {
                        this.setTimeHeld(0);
                        this.setCompletionPercentage(0);
                        this.setCurrentRecipe(null);
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.itemStackHandler = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.itemStackHandler);
        this.mechPower.readFromNBT(compound);
        this.timeHeld = compound.getInteger("timeHeld");
        this.currentRecipe = RiftMMRecipes.getMillstoneRecipe(compound.getString("currentRecipe"));
        this.compPerc = compound.getDouble("completionPercentage");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.itemStackHandler);
        this.mechPower.writeToNBT(compound);
        compound.setInteger("timeHeld", this.timeHeld);
        compound.setString("currentRecipe", this.getCurrentRecipeId());
        compound.setDouble("completionPercentage", this.getCompletionPercentage());
        return compound;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        else if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && facing == EnumFacing.UP) return true;
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
            else return (T) new SidedInvWrapper(this, EnumFacing.UP);
        }
        else if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && facing == EnumFacing.UP) return (T) this.mechPower;
        return super.getCapability(capability, facing);
    }

    public MillstoneRecipe getCurrentRecipe() {
        return this.currentRecipe;
    }

    public String getCurrentRecipeId() {
        if (this.currentRecipe != null) return this.currentRecipe.getId();
        return "";
    }

    public void setCurrentRecipe(MillstoneRecipe value) {
        this.currentRecipe = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public int getMaxRecipeTime() {
        //this estimates max time based on power input requires
        //at min power required its the default 10 seconds, but the higher the power the lower
        //the max time is until it reaches 3 seconds, which is 8x the min power
        //note that output is in ticks
        if (this.currentRecipe != null) {
            double minPower = this.currentRecipe.getMinPower();
            if (minPower <= this.getPower()) {
                double result = -1D / minPower * (this.getPower() - minPower) + 10D;
                return (int) RiftUtil.clamp(result, 5D, 30D) * 20;
            }
        }
        return 69420666;
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

    public double getCompletionPercentage() {
        return this.compPerc;
    }

    public void setCompletionPercentage(double value) {
        this.compPerc = value;
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
        this.itemStackHandler = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, this.itemStackHandler);
        this.mechPower.readFromNBT(tag);
        this.timeHeld = tag.getInteger("timeHeld");
        this.currentRecipe = RiftMMRecipes.getMillstoneRecipe(tag.getString("currentRecipe"));
        this.compPerc = tag.getDouble("completionPercentage");
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this, false);
    }

    //inventory stuff starts here
    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.DOWN) return new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11};
        else if (side == EnumFacing.NORTH || side == EnumFacing.EAST || side == EnumFacing.WEST || side == EnumFacing.SOUTH) return new int[]{0, 1, 2};
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if (index != 0 && index != 1 && index != 2) return direction == EnumFacing.DOWN;
        return true;
    }

    @Override
    public int getSizeInventory() {
        return this.itemStackHandler.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.itemStackHandler) {
            if (!itemstack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.itemStackHandler.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.itemStackHandler, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.itemStackHandler, index);
    }

    public void insertItemToSlot(int slot, ItemStack itemStack) {
        int newCount = this.getStackInSlot(slot).getCount() + itemStack.getCount();
        itemStack.setCount(newCount);
        this.setInventorySlotContents(slot, itemStack);
    }

    public boolean itemToInventoryTest(boolean addItem, ItemStack toAdd, int startInd) {
        //scan for indices to insert into
        List<Integer> amntToInsert = Arrays.asList(new Integer[this.getSizeInventory() - startInd]); //amnt of items to insert into each, for if theres multiple
        amntToInsert = amntToInsert.stream().map(integer -> 0).collect(Collectors.toList());
        int count = toAdd.getCount();
        for (int x = startInd; x < this.getSizeInventory(); x++) {
            if (Ingredient.fromStacks(this.getStackInSlot(x)).apply(toAdd)) {
                if (this.getStackInSlot(x).getCount() + count <= toAdd.getMaxStackSize()) {
                    amntToInsert.set(x - startInd, count);
                    break;
                }
                else {
                    amntToInsert.set(x - startInd, toAdd.getMaxStackSize() - this.getStackInSlot(x).getCount());
                    count = count - (toAdd.getMaxStackSize() - this.getStackInSlot(x).getCount());
                }
            }
            else if (this.getStackInSlot(x).isEmpty()) {
                amntToInsert.set(x - startInd, count);
                break;
            }
        }

        //now insert the item in each valid slot
        //test first tho
        int countDist = amntToInsert.stream().mapToInt(Integer::intValue).sum();
        if (countDist == toAdd.getCount()) {
            if (addItem) {
                for (int x = 0; x < amntToInsert.size(); x++) {
                    if (amntToInsert.get(x) > 0) {
                        ItemStack stackToAdd = toAdd.copy();
                        stackToAdd.setCount(amntToInsert.get(x));
                        this.insertItemToSlot(x + startInd, stackToAdd);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.itemStackHandler.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return !isInvalid() && player.getDistanceSq(this.pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 0 || index == 1 || index == 2;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.itemStackHandler.clear();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public int getUsableInputSlot() {
        for (int x = 0; x < 3; x++) {
            if (!this.getStackInSlot(x).isEmpty()) return x;
        }
        return -1;
    }

    public ItemStack getInputItem() {
        if (this.getUsableInputSlot() > -1) return this.getStackInSlot(this.getUsableInputSlot());
        return ItemStack.EMPTY;
    }
    //inventory stuff ends here

    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
