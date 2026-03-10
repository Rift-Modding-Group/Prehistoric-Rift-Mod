package anightdazingzoroark.prift.propertySystem.propertyStorage;

import anightdazingzoroark.prift.propertySystem.networking.PropertiesNetworking;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.*;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class AbstractEntityProperties<E extends Entity> {
    protected final HashMap<String, ImmutablePair<AbstractPropertyValue<?>, Boolean>> propertyValueMap = new HashMap<>();
    @NotNull
    private final String propertyName;
    private final E entityHolder;

    public AbstractEntityProperties() {
        this.propertyName = "";
        this.entityHolder = null;
    }

    public AbstractEntityProperties(@NotNull String propertyName, @NotNull E entityHolder) {
        this.registerDefaults(entityHolder);
        this.propertyName = propertyName;
        this.entityHolder = entityHolder;
    }

    //-----initialization-----
    protected abstract void registerDefaults(E entity);

    //-----register related methods-----
    public void register(AbstractPropertyValue<?> value) {
        this.register(value, true);
    }

    public void register(AbstractPropertyValue<?> value, boolean persist) {
        this.propertyValueMap.put(value.getKey(), new ImmutablePair<>(value, persist));

        //sync to client afterwards from server
        this.syncToClient(value);
    }

    //-----methods relating to putting values-----
    //universal setter and getter
    private <I extends AbstractPropertyValue<?>> I getExistingProperty(String key) {
        //check if key exists
        if (!this.propertyValueMap.containsKey(key)) {
            throw new UnsupportedOperationException("Key "+key+" does not exist in property "+this.getPropertyName()+"!");
        }

        return (I) this.propertyValueMap.get(key).left;
    }

    public <I> void set(String key, I value) {
        //check if key corresponds to value
        AbstractPropertyValue<I> propertyValue = this.getExistingProperty(key);

        if (propertyValue.getHeldClass() != value.getClass()) {
            throw new UnsupportedOperationException("Key "+key+" does not represent given value!");
        }

        //now set as usual
        propertyValue.setValue(value);
        boolean persist = this.propertyValueMap.get(key).right;
        this.propertyValueMap.put(key, new ImmutablePair<>(propertyValue, persist));
        this.syncToClient(propertyValue);
    }

    public <I> I get(String key) {
        AbstractPropertyValue<I> propertyValue = this.getExistingProperty(key);
        return propertyValue.getValue();
    }

    //sync to client from server
    private void syncToClient(AbstractPropertyValue<?> value) {
        if (this.entityHolder != null && !this.entityHolder.world.isRemote) {
            PropertiesNetworking.sendDelta(
                    this.entityHolder,
                    this.propertyName,
                    value.getKey(),
                    this.writeOneToNBT(value.getKey())
            );
        }
    }

    //-----general getters-----
    public boolean has(String key) {
        return this.propertyValueMap.containsKey(key);
    }

    //-----holder related-----
    public @NotNull String getPropertyName() {
        return this.propertyName;
    }

    public @NotNull E getEntityHolder() {
        return this.entityHolder;
    }

    //-----nbt related stuff-----
    //save all properties
    public NBTTagCompound writeAllToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair : this.propertyValueMap.values()) {
            if (propertyValuePair.right) propertyValuePair.left.writeToNBT(tag);
        }
        return tag;
    }

    //load all properties
    public void readAllFromNBT(NBTTagCompound tag) {
        for (ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair : this.propertyValueMap.values()) {
            if (propertyValuePair.right) propertyValuePair.left.readFromNBT(tag);
        }
    }

    //save one property by key (for delta sync)
    public NBTTagCompound writeOneToNBT(String key) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair = this.propertyValueMap.get(key);
        if (propertyValuePair == null) return nbtTagCompound;
        AbstractPropertyValue<?> propertyValue = propertyValuePair.left;
        if (propertyValue != null) propertyValue.writeToNBT(nbtTagCompound);
        return nbtTagCompound;
    }

    //read one property by key (for delta sync)
    public void readOneFromNBT(NBTTagCompound tag, String key) {
        ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair = this.propertyValueMap.get(key);
        if (propertyValuePair == null) return;
        AbstractPropertyValue<?> propertyValue = propertyValuePair.left;
        if (propertyValue != null) propertyValue.readFromNBT(tag);
    }
}
