package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class RiftTileEntity extends TileEntity {
    protected final HashMap<String, ImmutablePair<AbstractPropertyValue<?>, Boolean>> propertyValueMap = new HashMap<>();
    protected final HashMap<String, RiftInventoryHandler> inventoryMap = new HashMap<>();
    protected final InventorySideContainer inventorySideInfo = new InventorySideContainer();

    public RiftTileEntity() {
        super();
        this.registerValues();
        this.registerInventories();
    }

    //-----initialization and registration of values-----
    public abstract void registerValues();

    //register method
    protected void registerValue(AbstractPropertyValue<?> value) {
        this.registerValue(value, true);
    }

    //persistence means that the data will be saved when the world is unloaded
    protected void registerValue(AbstractPropertyValue<?> value, boolean persist) {
        //check if the property already exists, it it already does, skip
        if (this.propertyValueMap.containsKey(value.getKey())) {
            throw new UnsupportedOperationException("Key "+value.getKey()+" already exists in this tile entity!");
        }

        //add the property
        this.propertyValueMap.put(value.getKey(), new ImmutablePair<>(value, persist));
    }

    //-----methods relating to putting values-----
    //universal setter and getter
    private <I extends AbstractPropertyValue<?>> I getExistingProperty(String key) {
        //check if key exists
        if (!this.propertyValueMap.containsKey(key)) {
            throw new UnsupportedOperationException("Key "+key+" does not exist in this tile entity!");
        }

        return (I) this.propertyValueMap.get(key).left;
    }

    public <I> void setValue(String key, I value) {
        this.setValue(key, value, true);
    }

    public <I> void setValue(String key, I value, boolean syncToClient) {
        //check if key corresponds to value
        AbstractPropertyValue<I> propertyValue = this.getExistingProperty(key);

        if (propertyValue.getHeldClass() != value.getClass()) {
            throw new UnsupportedOperationException("Key "+key+" does not represent given value!");
        }

        //now set as usual
        propertyValue.setValue(value);
        boolean persist = this.propertyValueMap.get(key).right;
        this.propertyValueMap.put(key, new ImmutablePair<>(propertyValue, persist));
        if (syncToClient) this.updateServerData();
    }

    public void setValue(String key, NBTTagCompound nbtTagCompound) {
        //check if key corresponds to value
        AbstractPropertyValue<?> propertyValue = this.getExistingProperty(key);

        //now set as usual
        propertyValue.readFromNBT(nbtTagCompound);
        boolean persist = this.propertyValueMap.get(key).right;
        this.propertyValueMap.put(key, new ImmutablePair<>(propertyValue, persist));
        this.updateServerData();
    }

    //-----general getters-----
    public <I> I getValue(String key) {
        AbstractPropertyValue<I> propertyValue = this.getExistingProperty(key);
        return propertyValue.getValue();
    }

    public boolean hasValue(String key) {
        return this.propertyValueMap.containsKey(key);
    }

    //-----initialization and registration of inventories-----
    public abstract void registerInventories();

    protected void finalizeInventorySidingInfo() {
        this.inventorySideInfo.finalizeInfo();
    }

    protected void registerInventory(String key, int size) {
        //check if the inventory already exists, if it already does, skip
        if (this.inventoryMap.containsKey(key)) {
            throw new UnsupportedOperationException("Inventory "+key+" already exists in this tile entity!");
        }

        this.inventoryMap.put(key, new RiftInventoryHandler(size));
    }

    protected <I extends RiftInventoryHandler> void registerInventory(String key, int size, Class<I> inventoryClass) {
        //check if the inventory already exists, if it already does, skip
        if (this.inventoryMap.containsKey(key)) {
            throw new UnsupportedOperationException("Inventory "+key+" already exists in this tile entity!");
        }

        //try to create using the class
        try {
            RiftInventoryHandler toCreate = inventoryClass.getDeclaredConstructor(Integer.class).newInstance(size);
            this.inventoryMap.put(key, toCreate);
        }
        catch (Exception e) {
            throw new RuntimeException("Inventory class must have a public no-arg actor: " + inventoryClass.getName(), e);
        }
    }

    //for dealing with how inventories can be inserted into or extracted from by hoppers or pipes
    //depending on the side, must be put in registerInventories after registering inventories
    protected void registerInventorySiding(@NotNull String key, SideInvInteraction sideInvInteraction, @NotNull EnumFacing... sides) {
        //check if the inventory already exists, if it doesn't, skip
        if (!this.inventoryMap.containsKey(key)) {
            throw new UnsupportedOperationException("Inventory "+key+" does not exist in this tile entity!");
        }

        this.inventorySideInfo.addKey(key, this.inventoryMap.get(key).getSlots(), sideInvInteraction, sides);
    }

    //-----getting inventory-----
    public RiftInventoryHandler getInventory(String key) {
        return this.inventoryMap.get(key);
    }

    //-----inventory operations that take advantage of inventorySidingMap, for use in anything that has ISidedInventory-----
    public int[] getSlotsAtSide(EnumFacing facing) {
        return this.inventorySideInfo.slotsAndDirections.get(facing).stream().mapToInt(Integer::intValue).toArray();
    }

    public boolean canInsertAtSlot(int index, EnumFacing facing) {
        return this.inventorySideInfo.interactionAtSide.containsKey(facing)
                && this.inventorySideInfo.slotsAndDirections.containsKey(facing)
                && this.inventorySideInfo.interactionAtSide.get(facing) == SideInvInteraction.INSERT
                && this.inventorySideInfo.slotsAndDirections.get(facing).contains(index);
    }

    public boolean canExtractAtSlot(int index, EnumFacing facing) {
        return this.inventorySideInfo.interactionAtSide.containsKey(facing)
                && this.inventorySideInfo.slotsAndDirections.containsKey(facing)
                && this.inventorySideInfo.interactionAtSide.get(facing) == SideInvInteraction.EXTRACT
                && this.inventorySideInfo.slotsAndDirections.get(facing).contains(index);
    }

    public int getTotalSidingInfoSize() {
        return this.inventorySideInfo.totalSize;
    }

    public boolean hasEmptySidedInv() {
        for (String key : this.inventorySideInfo.orderedKeys) {
            if (!this.inventoryMap.get(key).isEmpty()) return false;
        }
        return true;
    }

    public ItemStack getStackAtSidedSlot(int slot) { //slot is relative to InventorySideContainer.totalSize
        ImmutablePair<String, Integer> slotAndInv = this.getSlotAndInv(slot);
        if (slotAndInv == null || slotAndInv.getLeft() == null) return ItemStack.EMPTY;

        return this.inventoryMap.get(slotAndInv.getLeft()).getStackInSlot(slotAndInv.getRight());
    }

    public ItemStack decStackSizeAtSidedSlot(int slot, int count) { //slot is relative to InventorySideContainer.totalSize
        ImmutablePair<String, Integer> slotAndInv = this.getSlotAndInv(slot);
        if (slotAndInv == null || slotAndInv.getLeft() == null) return ItemStack.EMPTY;

        return ItemStackHelper.getAndSplit(
                this.inventoryMap.get(slotAndInv.getLeft()).getItemStackList(),
                slotAndInv.getRight(), count
        );
    }

    public ItemStack removeStackFromSidedSlot(int slot) { //slot is relative to InventorySideContainer.totalSize
        ImmutablePair<String, Integer> slotAndInv = this.getSlotAndInv(slot);
        if (slotAndInv == null || slotAndInv.getLeft() == null) return ItemStack.EMPTY;

        return ItemStackHelper.getAndRemove(
                this.inventoryMap.get(slotAndInv.getLeft()).getItemStackList(),
                slotAndInv.getRight()
        );
    }

    public void setStackAtSidedSlot(int slot, ItemStack stack, int stackLimit) { //slot is relative to InventorySideContainer.totalSize
        ImmutablePair<String, Integer> slotAndInv = this.getSlotAndInv(slot);
        if (slotAndInv == null || slotAndInv.getLeft() == null) return;

        this.inventoryMap.get(slotAndInv.getLeft()).setStackInSlot(slot, stack);

        if (!stack.isEmpty() && stack.getCount() > stackLimit) {
            stack.setCount(stackLimit);
        }
    }

    protected ImmutablePair<String, Integer> getSlotAndInv(int slotRelativeToSidingInfo) {
        String matchedInventory = null;
        int matchedSlot = slotRelativeToSidingInfo;

        if (slotRelativeToSidingInfo >= this.inventorySideInfo.totalSize) return null;

        for (String key : this.inventorySideInfo.orderedKeys) {
            int invSize = this.inventoryMap.get(key).getSlots();
            if (matchedSlot > invSize) {
                matchedSlot =- invSize;
                matchedInventory = key;
            }
            else {
                matchedInventory = key;
                break;
            }
        }
        return new ImmutablePair<>(matchedInventory, matchedSlot);
    }

    //-----saving and updating nbt-----
    protected void updateServerData() {
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        //nbt for properties
        for (ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair : this.propertyValueMap.values()) {
            if (propertyValuePair.right) propertyValuePair.left.readFromNBT(compound);
        }
        //nbt for inventories
        for (Map.Entry<String, RiftInventoryHandler> inventoryEntry : this.inventoryMap.entrySet()) {
            NBTTagCompound inventoryNBT = compound.getCompoundTag(inventoryEntry.getKey());
            inventoryEntry.getValue().deserializeNBT(inventoryNBT);
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        //nbt for properties
        for (ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair : this.propertyValueMap.values()) {
            if (propertyValuePair.right) propertyValuePair.left.writeToNBT(compound);
        }
        //nbt for inventories
        for (Map.Entry<String, RiftInventoryHandler> inventoryEntry : this.inventoryMap.entrySet()) {
            NBTTagCompound inventoryNBT = inventoryEntry.getValue().serializeNBT();
            compound.setTag(inventoryEntry.getKey(), inventoryNBT);
        }
        return compound;
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(this.pos, 1, nbtTag);
    }

    @Override
    public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public @NotNull NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(@NotNull NBTTagCompound compound) {
        this.readFromNBT(compound);
    }

    protected record InventorySideInfo(String key, int size, SideInvInteraction sideInvInteraction, List<EnumFacing> directions) {}

    //for info on restricting inventory extraction via pipes or hoppers or whatever depending on the side
    //of the tile entity it was placed next to
    protected static class InventorySideContainer {
        //non finalized info
        private final List<InventorySideInfo> inventorySideInfos = new ArrayList<>();

        //finalized info
        private int totalSize;
        private final HashMap<String, List<Integer>> displacedIndexes = new HashMap<>();
        private final HashMap<EnumFacing, List<Integer>> slotsAndDirections = new HashMap<>();
        private final HashMap<EnumFacing, SideInvInteraction> interactionAtSide = new HashMap<>();
        private final List<String> orderedKeys = new ArrayList<>();

        public void addKey(String key, int inventorySize, SideInvInteraction sideInvInteraction, EnumFacing[] directions) {
            if (!this.orderedKeys.contains(key)) this.orderedKeys.add(key);
            this.inventorySideInfos.add(new InventorySideInfo(key, inventorySize, sideInvInteraction, new ArrayList<>(Arrays.asList(directions))));
        }

        //must be run at the end of registerInventories() after registering all inventories
        public void finalizeInfo() {
            this.totalSize = this.getTotalSize();
            this.displaceIndexes();
            this.setSlotsAndDirections();
            this.setInteractionAtSide();
        }

        private int getTotalSize() {
            int toReturn = 0;
            List<String> foundKeys = new ArrayList<>();
            for (String key : this.orderedKeys) {
                if (foundKeys.contains(key)) continue;

                List<InventorySideInfo> matches = this.inventorySideInfos.stream()
                        .filter(info -> info.key.equals(key))
                        .toList();
                if (matches.isEmpty()) continue;
                toReturn += matches.getFirst().size;
                foundKeys.add(key);
            }
            return toReturn;
        }

        private void displaceIndexes() {
            int accumulativeSize = 0;
            for (String key : this.orderedKeys) {
                if (this.displacedIndexes.containsKey(key)) continue;

                List<InventorySideInfo> matches = this.inventorySideInfos.stream()
                        .filter(info -> info.key.equals(key))
                        .toList();
                if (matches.isEmpty()) continue;

                InventorySideInfo match = matches.getFirst();
                List<Integer> indexes = IntStream.range(accumulativeSize, accumulativeSize + match.size).boxed().collect(Collectors.toList());
                this.displacedIndexes.put(key, indexes);
                accumulativeSize += match.size;
            }
        }

        private void setSlotsAndDirections() {
            for (Map.Entry<String, List<Integer>> entry : this.displacedIndexes.entrySet()) {
                for (EnumFacing direction : EnumFacing.values()) {
                    //check if direction is listed in an InventorySideInfo
                    boolean infoHasDirection = this.inventorySideInfos.stream().anyMatch(
                            info -> info.directions().contains(direction)
                    );
                    if (!infoHasDirection) {
                        this.slotsAndDirections.put(direction, new ArrayList<>());
                        continue;
                    }

                    //put in slotsAndDirections if it exists
                    //we have to combine with an already existing list if there is one however
                    if (this.slotsAndDirections.containsKey(direction)) {
                        List<Integer> slots = this.slotsAndDirections.get(direction);
                        slots.addAll(entry.getValue());
                        this.slotsAndDirections.put(direction, slots);
                    }
                    else this.slotsAndDirections.put(direction, entry.getValue());
                }
            }
        }

        private void setInteractionAtSide() {
            for (InventorySideInfo invSideInfo : this.inventorySideInfos) {
                for (EnumFacing direction : invSideInfo.directions()) {
                    this.interactionAtSide.put(direction, invSideInfo.sideInvInteraction());
                }
            }
        }
    }

    public enum SideInvInteraction {
        EXTRACT,
        INSERT
    }
}
