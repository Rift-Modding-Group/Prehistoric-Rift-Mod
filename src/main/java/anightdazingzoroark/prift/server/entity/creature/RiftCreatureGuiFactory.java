package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RiftCreatureGuiFactory extends AbstractUIFactory<RiftCreatureGuiData> {
    public static final RiftCreatureGuiFactory INSTANCE = new RiftCreatureGuiFactory();

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
        GuiManager.open(this, new RiftCreatureGuiData(serverPlayer, creatureNBT), serverPlayer);
    }

    @Override
    @NotNull
    public IGuiHolder<RiftCreatureGuiData> getGuiHolder(RiftCreatureGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getCreature()), "Creature is not a GUI holder!");
    }

    @Override
    public void writeGuiData(RiftCreatureGuiData data, PacketBuffer buffer) {
        if (!(data.getCreature() instanceof RiftCreature creature)) {
            throw new IllegalArgumentException("Only deployed RiftCreature instances can currently open this UI!");
        }
        buffer.writeInt(creature.getEntityId());
    }

    @Override
    @NotNull
    public RiftCreatureGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        Entity entity = player.world.getEntityByID(buffer.readInt());
        if (!(entity instanceof RiftCreature creature)) {
            throw new IllegalArgumentException("Creature entity for UI does not exist!");
        }
        return new RiftCreatureGuiData(player, creature);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, RiftCreatureGuiData data) {
        if (!(data.getCreature() instanceof RiftCreature creature)) return false;
        return super.canInteractWith(player, data)
                && creature.isEntityAlive()
                && creature.world == player.world
                && player.getDistanceSq(creature.posX, creature.posY, creature.posZ) <= 64D;
    }
}
