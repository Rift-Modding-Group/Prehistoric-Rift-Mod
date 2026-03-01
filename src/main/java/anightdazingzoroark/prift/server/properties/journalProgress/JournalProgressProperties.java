package anightdazingzoroark.prift.server.properties.journalProgress;

import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.propertyValues.JournalEncounteredCreaturesValue;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JournalProgressProperties extends AbstractEntityProperties {
    public JournalProgressProperties(@NotNull String key, @NotNull Entity entityHolder) {
        super(key, entityHolder);
    }

    @Override
    protected void registerDefaults(Entity entity) {
        this.put(new JournalEncounteredCreaturesValue("EncounteredCreatures", new HashMap<>()));
    }

    //-----direct progress editing and getting-----
    public HashMap<RiftCreatureType, Boolean> getEncounteredCreatures() {
        return (HashMap<RiftCreatureType, Boolean>) this.getProperty("EncounteredCreatures").getValue();
    }

    public void setEncounteredCreatures(HashMap<RiftCreatureType, Boolean> encounteredCreatures) {
        this.put(new JournalEncounteredCreaturesValue("EncounteredCreatures", encounteredCreatures));
    }

    //-----indirect editing and getting-----
    public void discoverCreature(RiftCreatureType creatureType) {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        encounteredCreatures.put(creatureType, false);
        this.setEncounteredCreatures(encounteredCreatures);
    }

    public void unlockCreature(RiftCreatureType creatureType) {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        encounteredCreatures.put(creatureType, true);
        this.setEncounteredCreatures(encounteredCreatures);
    }

    public void clearCreature(RiftCreatureType creatureType) {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        encounteredCreatures.remove(creatureType);
        this.setEncounteredCreatures(encounteredCreatures);
    }

    public List<RiftCreatureType.CreatureCategory> getUnlockedCategories() {
        List<RiftCreatureType.CreatureCategory> toReturn = new ArrayList<>();
        for (RiftCreatureType.CreatureCategory category : RiftCreatureType.CreatureCategory.values()) {
            if (!category.equals(RiftCreatureType.CreatureCategory.ALL)) {
                for (RiftCreatureType creatureType : this.getEncounteredCreatures().keySet()) {
                    if (creatureType.getCreatureCategory().equals(category) && !toReturn.contains(creatureType.getCreatureCategory())) {
                        toReturn.add(creatureType.getCreatureCategory());
                    }
                }
            }
            else if (!this.getEncounteredCreatures().isEmpty()) toReturn.add(category);
        }
        return toReturn;
    }

    public void resetEntries() {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        encounteredCreatures.clear();
        this.setEncounteredCreatures(encounteredCreatures);
    }

    public void unlockAllEntries() {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        for (RiftCreatureType creatureType : RiftCreatureType.values()) {
            encounteredCreatures.put(creatureType, true);
        }
        this.setEncounteredCreatures(encounteredCreatures);
    }
}
