package anightdazingzoroark.prift.server.properties;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
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
    protected void registerDefaults(@NotNull EntityPlayer entity) {
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

    @Override
    public void onTickProperty() {
        //tick inactive party members to regenerate stamina, recharge movement cooldowns
        //and tick down move cooldowns
        if (this.getEntityHolder().world.isRemote) return;

        CreatureStorage creatureStorage = this.getCreatureStorage();
        for (int index = 0; index < creatureStorage.getSize(); index++) {
            CreatureNBT storedCreature = creatureStorage.getCreature(index);
            if (storedCreature.nbtTagCompound().isEmpty()) continue;

            if (storedCreature.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE) {
                if (storedCreature.getStamina() < storedCreature.getMaxStamina()) {
                    storedCreature.regenerateStaminaInactive();
                    if (storedCreature.getStamina() >= storedCreature.getMaxStamina()) {
                        //needed to update stamina amnt on client
                        this.syncToClientMultiple("Creatures");
                    }
                }
                if (storedCreature.getLeapCooldown() > 0) {
                    storedCreature.setLeapCooldown(storedCreature.getLeapCooldown() - 1);
                }
                if (storedCreature.getSprintCooldown() > 0) {
                    storedCreature.setSprintCooldown(storedCreature.getSprintCooldown() - 1);
                }
                if (storedCreature.getCreatureMoves() != null) {
                    CreatureMoveStorage creatureMoveStorage = storedCreature.getCreatureMoves();
                    creatureMoveStorage.tickCooldowns();
                    storedCreature.setCreatureMoves(creatureMoveStorage);
                }
            }
        }
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
        this.togglePartyMember(this.getSelectedPosition());
    }

    public void togglePartyMember(int selectedPosition) {
        if (this.getEntityHolder().world.isRemote) return;
        if (selectedPosition < 0 || selectedPosition >= MAX_SIZE) return;
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

        if (!this.canSummonAtCurrentPosition()) {
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

    public boolean canSummonAtCurrentPosition() {
        BlockPos positionBelowPlayer = this.getEntityHolder().getPosition().down();
        Material materialBelowPlayer = this.getEntityHolder().world.getBlockState(positionBelowPlayer).getMaterial();
        return (materialBelowPlayer != Material.AIR || this.getEntityHolder().isRiding())
                && materialBelowPlayer != Material.LAVA;
    }

    public void swapPartyMembers(int firstPosition, int secondPosition) {
        if (this.getEntityHolder().world.isRemote) return;
        if (firstPosition < 0 || firstPosition >= MAX_SIZE || secondPosition < 0 || secondPosition >= MAX_SIZE) return;
        if (firstPosition == secondPosition) return;

        CreatureStorage creatureStorage = this.getCreatureStorage();
        CreatureNBT firstCreature = creatureStorage.getCreature(firstPosition);
        creatureStorage.setCreature(firstPosition, creatureStorage.getCreature(secondPosition));
        creatureStorage.setCreature(secondPosition, firstCreature);
        this.set("Creatures", creatureStorage);

        int selectedPosition = this.getSelectedPosition();
        if (selectedPosition == firstPosition) this.set("SelectedPosition", secondPosition);
        else if (selectedPosition == secondPosition) this.set("SelectedPosition", firstPosition);
    }

    public void savePartyMember(int index, @NotNull IRiftCreature creature) {
        if (this.getEntityHolder().world.isRemote || index < 0 || index >= MAX_SIZE) return;
        CreatureNBT currentCreature = this.getCreatureStorage().getCreature(index);
        if (currentCreature.nbtTagCompound().isEmpty() || !currentCreature.getUniqueID().equals(creature.getUniqueID())) return;
        if (!creature.isOwner(this.getEntityHolder())) return;

        if (creature instanceof RiftCreature deployedCreature) {
            this.storeCreatureAt(index, deployedCreature);
            return;
        }
        if (creature instanceof CreatureNBT storedCreature) {
            CreatureStorage creatureStorage = this.getCreatureStorage();
            creatureStorage.setCreature(index, storedCreature);
            this.set("Creatures", creatureStorage);
        }
    }

    private void storeCreatureAt(int index, @NotNull RiftCreature creature) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        creature.writeToNBT(nbtTagCompound);

        CreatureStorage creatureStorage = this.getCreatureStorage();
        creatureStorage.setCreature(index, new CreatureNBT(nbtTagCompound));
        this.set("Creatures", creatureStorage);
    }
}
