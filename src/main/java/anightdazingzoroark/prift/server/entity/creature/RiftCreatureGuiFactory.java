package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.RiftCreatureUI;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiData;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiFactory;
import anightdazingzoroark.prift.util.RiftUtil;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

public class RiftCreatureGuiFactory extends AbstractUIFactory<RiftCreatureGuiData> {
    public static final RiftCreatureGuiFactory INSTANCE = new RiftCreatureGuiFactory();
    private static final IGuiHolder<RiftCreatureGuiData> STORED_CREATURE_HOLDER = new IGuiHolder<>() {
        @Override
        public ModularPanel buildUI(
                RiftCreatureGuiData data, PanelSyncManager syncManager, UISettings settings
        ) {
            return RiftCreatureUI.show(data, syncManager, settings);
        }

        @Override
        public ModularScreen createScreen(RiftCreatureGuiData data, ModularPanel mainPanel) {
            return new ModularScreen(RiftInitialize.MODID, mainPanel);
        }
    };

    private RiftCreatureGuiFactory() {
        super(RiftInitialize.MODID + ":creature");
    }

    public void open(@NotNull EntityPlayer player, @NotNull RiftCreature creature) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(creature);
        if (!creature.isEntityAlive()) throw new IllegalArgumentException("Can't open a dead creature's UI!");
        if (player.world != creature.world) throw new IllegalArgumentException("Creature must be in the same dimension as the player!");

        EntityPlayerMP serverPlayer = verifyServerSide(player);
        GuiManager.open(this, new RiftCreatureGuiData(serverPlayer, creature), serverPlayer);
    }

    public void open(@NotNull EntityPlayer player, @NotNull CreatureNBT creatureNBT) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(creatureNBT);
        if (creatureNBT.nbtTagCompound().isEmpty()) throw new IllegalArgumentException("CreatureNBT cannot be empty!");

        EntityPlayerMP serverPlayer = verifyServerSide(player);
        PlayerPartyProperties playerParty = PlayerPartyProperties.get(serverPlayer);
        if (playerParty == null) throw new IllegalArgumentException("Player does not have party data!");
        int partyIndex = IntStream.range(0, playerParty.getCreatureStorage().getSize())
                .filter(index -> {
                    CreatureNBT storedCreature = playerParty.getCreatureStorage().getCreature(index);
                    return !storedCreature.nbtTagCompound().isEmpty()
                            && storedCreature.getUniqueID().equals(creatureNBT.getUniqueID());
                })
                .findFirst().orElse(-1);
        if (partyIndex < 0) throw new IllegalArgumentException("Stored creature is not in the player's party!");
        GuiManager.open(this, new RiftCreatureGuiData(serverPlayer, creatureNBT, partyIndex, 0), serverPlayer);
    }

    @Override
    @NotNull
    public IGuiHolder<RiftCreatureGuiData> getGuiHolder(RiftCreatureGuiData data) {
        IGuiHolder<RiftCreatureGuiData> creatureHolder = castGuiHolder(data.getCreature());
        return creatureHolder == null ? STORED_CREATURE_HOLDER : creatureHolder;
    }

    @Override
    public void writeGuiData(RiftCreatureGuiData data, PacketBuffer buffer) {
        buffer.writeBoolean(data.isBoxed());
        if (data.isBoxed()) {
            buffer.writeByte(data.getBoxIndex());
            buffer.writeByte(data.getBoxSlot());
            buffer.writeByte(data.getInitialPage());
            buffer.writeBlockPos(Objects.requireNonNull(data.getBoxPosition()));
            buffer.writeUniqueId(data.getCreature().getUniqueID());
        }
        else {
            buffer.writeBoolean(data.isPartyMember());
            if (data.isPartyMember()) {
                buffer.writeByte(data.getPartyIndex());
            }
            else if (data.getCreature() instanceof RiftCreature creature) {
                buffer.writeInt(creature.getEntityId());
            }
            else {
                throw new IllegalArgumentException("Stored creatures must be opened from a party position!");
            }
            buffer.writeByte(data.getInitialPage());
        }
    }

    @Override
    @NotNull
    public RiftCreatureGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            int box = buffer.readUnsignedByte();
            int slot = buffer.readUnsignedByte();
            int page = buffer.readUnsignedByte();
            BlockPos position = buffer.readBlockPos();
            UUID expectedCreature = buffer.readUniqueId();
            PlayerCreatureBoxProperties boxes = PlayerCreatureBoxProperties.get(player);
            if (boxes == null || box >= PlayerCreatureBoxProperties.BOX_COUNT || slot >= PlayerCreatureBoxProperties.BOX_SIZE
                    || !CreatureBoxGuiFactory.INSTANCE.canInteractWith(player, new CreatureBoxGuiData(player, position, box))
            ) {
                throw new IllegalArgumentException("Invalid creature box access!");
            }
            CreatureNBT creature = boxes.getBox(box).getCreature(slot);
            if (creature.nbtTagCompound().isEmpty() || !creature.isOwner(player) || !creature.getUniqueID().equals(expectedCreature)) {
                throw new IllegalArgumentException("Box position does not contain the expected owned creature!");
            }
            return new RiftCreatureGuiData(player, new CreatureNBT(creature.nbtTagCompound().copy()), box, slot, page, position);
        }
        else {
            boolean partyMember = buffer.readBoolean();
            if (partyMember) {
                int partyIndex = buffer.readByte();
                int initialPage = buffer.readByte();
                PlayerPartyProperties playerParty = PlayerPartyProperties.get(player);
                if (playerParty == null || partyIndex < 0 || partyIndex >= PlayerPartyProperties.MAX_SIZE) {
                    throw new IllegalArgumentException("Invalid party position for creature UI!");
                }
                CreatureNBT storedCreature = playerParty.getCreatureStorage().getCreature(partyIndex);
                if (storedCreature.nbtTagCompound().isEmpty() || !storedCreature.isOwner(player)) {
                    throw new IllegalArgumentException("Party position does not contain an owned creature!");
                }
                Entity correspondingEntity = RiftUtil.getEntityWithUUID(player.world, storedCreature.getUniqueID());
                IRiftCreature creature = correspondingEntity instanceof RiftCreature deployedCreature
                        ? deployedCreature : storedCreature;
                return new RiftCreatureGuiData(player, creature, partyIndex, initialPage);
            }

            Entity entity = player.world.getEntityByID(buffer.readInt());
            int initialPage = buffer.readByte();
            if (!(entity instanceof RiftCreature creature)) {
                throw new IllegalArgumentException("Creature entity for UI does not exist!");
            }
            return new RiftCreatureGuiData(player, creature, -1, initialPage);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, RiftCreatureGuiData data) {
        if (data.isBoxed()) {
            PlayerCreatureBoxProperties boxes = PlayerCreatureBoxProperties.get(player);
            if (boxes == null || data.getBoxIndex() >= PlayerCreatureBoxProperties.BOX_COUNT
                    || data.getBoxSlot() < 0 || data.getBoxSlot() >= PlayerCreatureBoxProperties.BOX_SIZE
                    || data.getBoxPosition() == null
                    || !CreatureBoxGuiFactory.INSTANCE.canInteractWith(player,
                    new CreatureBoxGuiData(player, data.getBoxPosition(), data.getBoxIndex()))) return false;
            CreatureNBT creature = boxes.getBox(data.getBoxIndex()).getCreature(data.getBoxSlot());
            return !creature.nbtTagCompound().isEmpty() && creature.isOwner(player)
                    && creature.getUniqueID().equals(data.getCreature().getUniqueID());
        }
        if (data.isPartyMember()) {
            PlayerPartyProperties playerParty = PlayerPartyProperties.get(player);
            if (playerParty == null || data.getPartyIndex() >= playerParty.getCreatureStorage().getSize()) return false;
            CreatureNBT storedCreature = playerParty.getCreatureStorage().getCreature(data.getPartyIndex());
            return !storedCreature.nbtTagCompound().isEmpty()
                    && storedCreature.isOwner(player)
                    && storedCreature.getUniqueID().equals(data.getCreature().getUniqueID());
        }
        if (!(data.getCreature() instanceof RiftCreature creature)) return false;
        return super.canInteractWith(player, data)
                && creature.isEntityAlive()
                && creature.world == player.world
                && player.getDistanceSq(creature.posX, creature.posY, creature.posZ) <= 64D;
    }
}
