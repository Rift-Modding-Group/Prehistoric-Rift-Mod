package anightdazingzoroark.prift.server.player;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.AbstractEntityProperties;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.RiftLibProperty;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.IntegerPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.ObjectPropertyValue;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerPartyProperties extends AbstractEntityProperties<EntityPlayer> {
    public static final String PROPERTY_NAME = "PlayerParty";
    public static final int MAX_SIZE = 6;

    public PlayerPartyProperties(@NotNull String propertyName, @NotNull EntityPlayer entityHolder) {
        super(propertyName, entityHolder);
    }

    @Nullable
    public static PlayerPartyProperties get(@Nullable EntityPlayer player) {
        if (player == null) return null;
        return RiftLibProperty.getProperty(PROPERTY_NAME, player);
    }

    @Override
    protected void registerDefaults(EntityPlayer entity) {
        this.register(new ObjectPropertyValue<>(
                "Creatures", new CreatureStorage(MAX_SIZE), CreatureStorage.class,
                CreatureStorage::getAsNBT,
                nbtBase -> {
                    CreatureStorage creatureStorage = new CreatureStorage(MAX_SIZE);
                    if (nbtBase instanceof NBTTagCompound nbtTagCompound) creatureStorage.readFromNBT(nbtTagCompound);
                    return creatureStorage;
                }
        ));
        this.register(new IntegerPropertyValue("SelectedPosition", 0));
    }

    @NotNull
    public CreatureStorage getCreatureStorage() {
        return this.get("Creatures");
    }

    public boolean addPartyMember(@NotNull RiftCreature creature) {
        if (this.getEntityHolder().world.isRemote) return false;

        CreatureStorage creatureStorage = this.getCreatureStorage();
        for (int index = 0; index < creatureStorage.getSize(); index++) {
            if (!creatureStorage.getCreature(index).nbtTagCompound().isEmpty()) continue;

            creature.setDeploymentType(RiftCreatureEnums.CreatureDeployment.PARTY);
            this.storeCreatureAt(index, creature);
            return true;
        }
        return false;
    }

    public void updatePartyMember(@NotNull RiftCreature creature) {
        if (this.getEntityHolder().world.isRemote) return;

        CreatureStorage creatureStorage = this.getCreatureStorage();
        for (int index = 0; index < creatureStorage.getSize(); index++) {
            CreatureNBT storedCreature = creatureStorage.getCreature(index);
            if (storedCreature.nbtTagCompound().isEmpty() || !storedCreature.getUniqueID().equals(creature.getUniqueID())) continue;

            this.storeCreatureAt(index, creature);
            return;
        }
    }

    public int getSelectedPosition() {
        return this.get("SelectedPosition");
    }

    public void selectPreviousPartyMember() {
        int selectedPosition = this.getSelectedPosition();
        this.set("SelectedPosition", selectedPosition > 0 ? selectedPosition - 1 : MAX_SIZE - 1);
    }

    public void selectNextPartyMember() {
        int selectedPosition = this.getSelectedPosition();
        this.set("SelectedPosition", selectedPosition + 1 < MAX_SIZE ? selectedPosition + 1 : 0);
    }

    public void toggleSelectedPartyMember() {
        if (this.getEntityHolder().world.isRemote) return;

        int selectedPosition = this.getSelectedPosition();
        CreatureNBT storedCreature = this.getCreatureStorage().getCreature(selectedPosition);
        if (storedCreature.nbtTagCompound().isEmpty() || !storedCreature.isOwner(this.getEntityHolder())) return;

        Entity correspondingEntity = RiftUtil.getEntityWithUUID(this.getEntityHolder().world, storedCreature.getUniqueID());
        if (storedCreature.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY) {
            if (correspondingEntity instanceof RiftCreature creature) {
                creature.setDeploymentType(RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE);
                this.storeCreatureAt(selectedPosition, creature);
                creature.setDead();
            }
            else {
                storedCreature.setDeploymentType(RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE);
                CreatureStorage creatureStorage = this.getCreatureStorage();
                creatureStorage.setCreature(selectedPosition, storedCreature);
                this.set("Creatures", creatureStorage);
            }
            this.getEntityHolder().sendStatusMessage(new TextComponentTranslation("party.warning.dismiss_success"), false);
            return;
        }

        if (storedCreature.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE || storedCreature.getHealth() <= 0f) {
            if (storedCreature.getHealth() <= 0f) {
                this.getEntityHolder().sendStatusMessage(new TextComponentTranslation("party.warning.cannot_summon_dead"), false);
            }
            return;
        }

        BlockPos positionBelowPlayer = this.getEntityHolder().getPosition().down();
        Material materialBelowPlayer = this.getEntityHolder().world.getBlockState(positionBelowPlayer).getMaterial();
        if (materialBelowPlayer == Material.AIR && !this.getEntityHolder().isRiding() || materialBelowPlayer == Material.LAVA) {
            this.getEntityHolder().sendStatusMessage(new TextComponentTranslation("party.warning.cannot_summon"), false);
            return;
        }

        storedCreature.setDeploymentType(RiftCreatureEnums.CreatureDeployment.PARTY);
        RiftCreature creature = RiftCreatureRegistry.createCreature(
                this.getEntityHolder().world, storedCreature.getCreatureType().getName()
        );
        creature.readFromNBT(storedCreature.nbtTagCompound().copy());
        creature.setPosition(this.getEntityHolder().posX, this.getEntityHolder().posY, this.getEntityHolder().posZ);
        if (!this.getEntityHolder().world.spawnEntity(creature)) {
            storedCreature.setDeploymentType(RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE);
            this.getEntityHolder().sendStatusMessage(new TextComponentTranslation("party.warning.cannot_summon"), false);
            return;
        }

        CreatureStorage creatureStorage = this.getCreatureStorage();
        creatureStorage.setCreature(selectedPosition, storedCreature);
        this.set("Creatures", creatureStorage);
        this.getEntityHolder().sendStatusMessage(new TextComponentTranslation("party.warning.summon_success"), false);
    }

    private void storeCreatureAt(int index, @NotNull RiftCreature creature) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        creature.writeToNBT(nbtTagCompound);

        CreatureStorage creatureStorage = this.getCreatureStorage();
        creatureStorage.setCreature(index, new CreatureNBT(nbtTagCompound));
        this.set("Creatures", creatureStorage);
    }
}
