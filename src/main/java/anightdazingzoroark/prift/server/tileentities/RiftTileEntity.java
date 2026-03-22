package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class RiftTileEntity extends TileEntity {
    protected final HashMap<String, ImmutablePair<AbstractPropertyValue<?>, Boolean>> propertyValueMap = new HashMap<>();
    protected final HashMap<String, RiftInventoryHandler> inventoryMap = new HashMap<>();

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

    //-----getting inventory-----
    public RiftInventoryHandler getInventory(String key) {
        return this.inventoryMap.get(key);
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
}
