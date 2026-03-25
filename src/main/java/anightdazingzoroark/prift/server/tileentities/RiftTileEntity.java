package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

public abstract class RiftTileEntity extends TileEntity {
    protected final HashMap<String, ImmutablePair<AbstractPropertyValue<?>, Boolean>> propertyValueMap = new HashMap<>();
    protected final HashMap<String, RiftInventoryHandler> inventoryMap = new HashMap<>();
    protected final InventorySideInfo inventorySideInfo = new InventorySideInfo();

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
    protected void registerInventorySiding(@NotNull String key, boolean extract, @NotNull EnumFacing... sides) {
        //check if the inventory already exists, if it doesn't, skip
        if (!this.inventoryMap.containsKey(key)) {
            throw new UnsupportedOperationException("Inventory "+key+" does not exist in this tile entity!");
        }

        this.inventorySideInfo.addKey(key, this.inventoryMap.get(key).getSlots(), extract, sides);
    }

    //-----getting inventory-----
    public RiftInventoryHandler getInventory(String key) {
        return this.inventoryMap.get(key);
    }

    //-----inventory operations that take advantage of inventorySidingMap, for use in anything that has ISidedInventory-----

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

    //for info on restricting inventory extraction via pipes or hoppers or whatever depending on the side
    //of the tile entity it was placed next to
    protected static class InventorySideInfo {
        private final HashMap<String, ImmutableTriple<Integer, Boolean, List<EnumFacing>>> inventorySideOrder = new HashMap<>();
        private final HashMap<EnumFacing, List<Integer>> enumIndexes = new HashMap<>();
        private final HashMap<Integer, Boolean> canExtractIndexes = new HashMap<>();
        private final List<String> orderedKeys = new ArrayList<>();

        public void addKey(String key, int inventorySize, boolean extract, EnumFacing[] directions) {
            this.orderedKeys.add(key);
            this.inventorySideOrder.put(key, new ImmutableTriple<>(inventorySize, extract, Arrays.asList(directions)));
        }

        //must be run at the end of registerInventories() after registering all inventories
        public void finalizeInfo() {
            for (Map.Entry<String, ImmutableTriple<Integer, Boolean, List<EnumFacing>>> entry : this.inventorySideOrder.entrySet()) {
                for (EnumFacing facing : entry.getValue().getRight()) {
                    int[] indexesAtSide = this.getSlotsForSide(facing);

                    //deal with enumIndexes, which is for indexes that correspond to a direction
                    //check if facing as key exists in enumIndexes
                    //if yes, then perform appending
                    //otherwise, add it
                    if (this.enumIndexes.containsKey(facing)) {
                        List<Integer> oldIndexes = this.enumIndexes.get(facing);
                        if (indexesAtSide.length > 0) {
                            oldIndexes.addAll(Arrays.stream(indexesAtSide).boxed().toList());
                            this.enumIndexes.put(facing, oldIndexes);
                        }
                    }
                    else {
                        if (indexesAtSide.length > 0) {
                            this.enumIndexes.put(facing, Arrays.stream(indexesAtSide).boxed().toList());
                        }
                    }

                    //deal with canExtractIndexes, which deals with whether or not an index can be extracted from or inserted into
                    for (int indexAtSide : indexesAtSide) {
                        this.canExtractIndexes.put(indexAtSide, entry.getValue().getMiddle());
                    }
                }

            }
        }

        private int[] getSlotsForSide(EnumFacing direction) {
            int[] toReturn = new int[]{};
            int accumulatedSize = 0;
            for (String key : this.orderedKeys) {
                ImmutableTriple<Integer, Boolean, List<EnumFacing>> sideInfo = this.inventorySideOrder.get(key);

                //add to accumulative size and skip if triple doesn't contain direction
                if (!sideInfo.getRight().contains(direction)) {
                    accumulatedSize += sideInfo.getLeft();
                    continue;
                }

                //create a new array (as a stream) whose integer values are between accumulated size and the ideal last pos
                IntStream sidesStream = IntStream.range(accumulatedSize, accumulatedSize + sideInfo.getLeft());

                //now append to toReturn
                toReturn = IntStream.concat(sidesStream, Arrays.stream(toReturn)).toArray();

                //add to accumulative size
                accumulatedSize += sideInfo.getLeft();
            }
            return toReturn;
        }
    }
}
