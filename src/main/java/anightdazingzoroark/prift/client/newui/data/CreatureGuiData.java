package anightdazingzoroark.prift.client.newui.data;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBTKeyword;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class CreatureGuiData extends GuiData {
    //data type stuff
    public final DataType dataType;
    private RiftCreature creature;
    private SelectedCreatureInfo selectedCreatureInfo;

    //ui related stuff
    public boolean isMoveSwitchingUI = false;
    public SelectedMoveInfo selectedMoveInfoUI;
    public SelectedMoveInfo.SwapInfo moveSwapInfoUI = new SelectedMoveInfo.SwapInfo();

    public CreatureGuiData(EntityPlayer player, RiftCreature creature) {
        super(player);
        this.creature = creature;
        this.dataType = DataType.CREATURE;
    }

    public CreatureGuiData(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo) {
        super(player);
        this.selectedCreatureInfo = selectedCreatureInfo;
        this.dataType = DataType.SELECTION;
    }

    public CreatureGuiData(EntityPlayer player, NBTTagCompound nbtTagCompound) {
        super(player);
        this.dataType = DataType.values()[nbtTagCompound.getInteger("DataType")];
        if (this.dataType == DataType.CREATURE) {
            this.creature = (RiftCreature) player.world.getEntityByID(nbtTagCompound.getInteger("CreatureId"));
        }
        else if (this.dataType == DataType.SELECTION) {
            this.selectedCreatureInfo = new SelectedCreatureInfo(nbtTagCompound.getCompoundTag("SelectionInfo"));
        }
    }

    public Object getGuiHolder() {
        if (this.dataType == DataType.CREATURE) return this.creature;
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo;
        return null;
    }

    public CreatureNBT getCreatureNBT() {
        if (this.dataType == DataType.CREATURE) return new CreatureNBT(this.creature);
        else if (this.dataType == DataType.SELECTION) {
            return PlayerTamedCreaturesHelper.getCreatureNBTFromSelected(this.getPlayer(), this.selectedCreatureInfo, false);
        }
        return new CreatureNBT();
    }

    public String getName(boolean includeLevel) {
        if (this.dataType == DataType.CREATURE) return this.creature.getName(includeLevel);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getCreatureName(includeLevel);
        }
        return "";
    }

    public int getLevel() {
        if (this.dataType == DataType.CREATURE) return this.creature.getLevel();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getCreatureLevel();
        }
        return -1;
    }

    public RiftCreatureType getCreatureType() {
        if (this.dataType == DataType.CREATURE) return this.creature.creatureType;
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getCreatureType();
        }
        return null;
    }

    public float[] getHealth() {
        if (this.dataType == DataType.CREATURE) {
            return new float[]{this.creature.getHealth(), this.creature.getMaxHealth()};
        }
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getCreatureHealth();
        }
        return new float[]{0f, 0f};
    }

    public int[] getEnergy() {
        if (this.dataType == DataType.CREATURE) {
            return new int[]{this.creature.getEnergy(), this.creature.getMaxEnergy()};
        }
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getCreatureEnergy();
        }
        return new int[]{0, 0};
    }

    public int[] getXP() {
        if (this.dataType == DataType.CREATURE) {
            return new int[]{this.creature.getXP(), this.creature.getMaxXP()};
        }
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getCreatureXP();
        }
        return new int[]{0, 0};
    }

    public int getAgeInDays() {
        if (this.dataType == DataType.CREATURE) return this.creature.getAgeInDays();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getAgeInDays();
        }
        return 0;
    }

    public String getAcquisitionInfoString() {
        if (this.dataType == DataType.CREATURE) return this.creature.getAcquisitionInfo().acquisitionInfoString();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getAcquisitionInfoString();
        }
        return "";
    }

    public boolean isSitting() {
        if (this.dataType == DataType.CREATURE) return this.creature.isSitting();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.isSitting();
        }
        return false;
    }

    public void setSitting(boolean value) {
        if (this.dataType == DataType.CREATURE) this.creature.setSitting(value);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                NBTTagCompound newNBTParam = CreatureNBTKeyword.SITTING.setValue(value);
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }

    public boolean isTurretMode() {
        if (this.dataType == DataType.CREATURE) return this.creature.isTurretMode();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.isTurretMode();
        }
        return false;
    }

    public void setTurretMode(boolean value) {
        if (this.dataType == DataType.CREATURE) this.creature.setTurretMode(value);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                NBTTagCompound newNBTParam = CreatureNBTKeyword.TURRET_MODE.setValue(value);
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }

    public boolean canEnterTurretMode() {
        if (this.dataType == DataType.CREATURE) return this.creature.canEnterTurretMode();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.canEnterTurretMode();
        }
        return false;
    }

    public TurretModeTargeting getTurretTargeting() {
        if (this.dataType == DataType.CREATURE) return this.creature.getTurretTargeting();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getTurretTargeting();
        }
        return null;
    }

    public void setTurretModeTargeting(TurretModeTargeting value) {
        if (this.dataType == DataType.CREATURE) this.creature.setTurretModeTargeting(value);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                NBTTagCompound newNBTParam = CreatureNBTKeyword.TURRET_TARGETING.setValue((byte) value.ordinal());
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }

    public PlayerTamedCreatures.DeploymentType getDeploymentType() {
        if (this.dataType == DataType.CREATURE) return this.creature.getDeploymentType();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getDeploymentType();
        }
        return null;
    }

    public TameBehaviorType getTameBehavior() {
        if (this.dataType == DataType.CREATURE) return this.creature.getTameBehavior();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getTameBehavior();
        }
        return null;
    }

    public void setTameBehavior(TameBehaviorType value) {
        if (this.dataType == DataType.CREATURE) this.creature.setTameBehavior(value);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                NBTTagCompound newNBTParam = CreatureNBTKeyword.TAME_BEHAVIOR.setValue((byte) value.ordinal());
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }

    //-----inventory and gear related stuff starts here-----
    public boolean gearSlotChangeable() {
        if (this.dataType == DataType.CREATURE) {
            return this.creature.getPassengers().isEmpty();
        }
        return true;
    }

    public CreatureInventoryHandler getCreatureInventory() {
        if (this.dataType == DataType.CREATURE) return this.creature.creatureInventory;
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                RiftCreatureType creatureType = creatureNBT.getCreatureType();
                CreatureInventoryHandler creatureInventoryHandler = new CreatureInventoryHandler(creatureType.inventorySlots);
                creatureInventoryHandler.deserializeNBT(creatureNBT.getInventoryNBT());
                return creatureInventoryHandler;
            }
        }

        return null;
    }

    public CreatureGearHandler getCreatureGear() {
        if (this.dataType == DataType.CREATURE) return this.creature.creatureGear;
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                RiftCreatureType creatureType = creatureNBT.getCreatureType();
                CreatureGearHandler creatureGearHandler = new CreatureGearHandler(creatureType);
                creatureGearHandler.deserializeNBT(creatureNBT.getGearNBT());
                return creatureGearHandler;
            }
        }
        return null;
    }

    public void setSaddled(boolean value) {
        if (this.dataType == DataType.CREATURE) this.creature.setSaddled(value);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                NBTTagCompound newNBTParam = CreatureNBTKeyword.SADDLED.setValue(value);
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }

    public boolean isSaddled() {
        if (this.dataType == DataType.CREATURE) return this.creature.isSaddled();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) return creatureNBT.isSaddled();
        }
        return false;
    }

    public void setLargeWeapon(RiftLargeWeaponType value) {
        if (this.dataType == DataType.CREATURE) this.creature.setLargeWeapon(value);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                NBTTagCompound newNBTParam = CreatureNBTKeyword.LARGE_WEAPON_TYPE.setValue((byte) value.ordinal());
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }
    //-----inventory and gear related stuff starts here-----

    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();

        toReturn.setInteger("DataType", this.dataType.ordinal());
        if (this.dataType == DataType.CREATURE) toReturn.setInteger("CreatureId", this.creature.getEntityId());
        else if (this.dataType == DataType.SELECTION) toReturn.setTag("SelectionInfo", this.selectedCreatureInfo.getNBT());

        return toReturn;
    }

    //-----move related stuff starts here-----
    public FixedSizeList<CreatureMove> getLearnedMoves() {
        if (this.dataType == DataType.CREATURE) return this.creature.getLearnedMoves();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getLearnedMoves();
        }
        return new FixedSizeList<>(3);
    }

    public void setLearnedMoves(FixedSizeList<CreatureMove> learnedMoves) {
        if (this.dataType == DataType.CREATURE) this.creature.setLearnedMoves(learnedMoves);
    }

    public void changeLearnedMove(int pos, CreatureMove move) {
        if (this.dataType == DataType.CREATURE) this.creature.changeLearnedMove(pos, move);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                FixedSizeList<CreatureMove> newLearnedMoves = creatureNBT.getLearnedMoves();
                newLearnedMoves.set(pos, move);
                NBTTagCompound newLearnedMovesNBT = MoveListUtil.getNBTFromFixedSizeListCreatureMove(newLearnedMoves);
                NBTTagCompound newNBTParam = CreatureNBTKeyword.LEARNED_MOVES.setValue(newLearnedMovesNBT);
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }

    public void removeLearnedMove(int pos) {
        if (this.dataType == DataType.CREATURE) this.creature.removeLearnedMove(pos);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                FixedSizeList<CreatureMove> newLearnedMoves = creatureNBT.getLearnedMoves();
                newLearnedMoves.remove(pos);
                NBTTagCompound newLearnedMovesNBT = MoveListUtil.getNBTFromFixedSizeListCreatureMove(newLearnedMoves);
                NBTTagCompound newNBTParam = CreatureNBTKeyword.LEARNED_MOVES.setValue(newLearnedMovesNBT);
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }

    public List<CreatureMove> getLearnableMoves() {
        if (this.dataType == DataType.CREATURE) return this.creature.getLearnableMoves();
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            return creatureNBT.getLearnableMoves();
        }
        return new ArrayList<>();
    }

    public void changeLearnableMove(int pos, CreatureMove move) {
        if (this.dataType == DataType.CREATURE) this.creature.changeLearnableMove(pos, move);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                List<CreatureMove> newLearnableMoves = creatureNBT.getLearnableMoves();
                newLearnableMoves.set(pos, move);
                NBTTagCompound newLearnableMovesNBT = MoveListUtil.getNBTFromListCreatureMove(newLearnableMoves);
                NBTTagCompound newNBTParam = CreatureNBTKeyword.LEARNABLE_MOVES.setValue(newLearnableMovesNBT);
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }

    public void addLearnableMove(CreatureMove move) {
        if (this.dataType == DataType.CREATURE) this.creature.addLearnableMove(move);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                List<CreatureMove> newLearnableMoves = creatureNBT.getLearnableMoves();
                newLearnableMoves.add(move);
                NBTTagCompound newLearnableMovesNBT = MoveListUtil.getNBTFromListCreatureMove(newLearnableMoves);
                NBTTagCompound newNBTParam = CreatureNBTKeyword.LEARNABLE_MOVES.setValue(newLearnableMovesNBT);
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }

    public void removeLearnableMove(int pos) {
        if (this.dataType == DataType.CREATURE) this.creature.removeLearnableMove(pos);
        else if (this.dataType == DataType.SELECTION) {
            CreatureNBT creatureNBT = this.getCreatureNBT();
            if (!creatureNBT.nbtIsEmpty()) {
                List<CreatureMove> newLearnableMoves = creatureNBT.getLearnableMoves();
                newLearnableMoves.remove(pos);
                NBTTagCompound newLearnableMovesNBT = MoveListUtil.getNBTFromListCreatureMove(newLearnableMoves);
                NBTTagCompound newNBTParam = CreatureNBTKeyword.LEARNABLE_MOVES.setValue(newLearnableMovesNBT);
                PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), newNBTParam, this.selectedCreatureInfo);
            }
        }
    }
    //-----move related stuff ends here-----

    public enum DataType {
        CREATURE,
        SELECTION
    }
}
