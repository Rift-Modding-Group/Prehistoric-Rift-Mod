package anightdazingzoroark.prift.client.newui.data;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.EntityGuiData;
import com.cleanroommc.modularui.factory.GuiManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CreatureGuiFactory extends AbstractUIFactory<CreatureGuiData> {
    public static CreatureGuiFactory INSTANCE = new CreatureGuiFactory();
    public static CreatureGuiFactory INSTANCE_FROM_PARTY = new CreatureGuiFactory(true);

    public static CreatureGuiFactory openToPage(int page) {
        CreatureGuiFactory toReturn = INSTANCE_FROM_PARTY;
        toReturn.pageToOpenTo = page;
        return toReturn;
    }

    private final boolean openedFromParty;
    private int pageToOpenTo;

    protected CreatureGuiFactory() {
        this(false);
    }

    protected CreatureGuiFactory(boolean openedFromParty) {
        super(RiftInitialize.MODID+":creature");
        this.openedFromParty = openedFromParty;
    }

    private <E extends RiftCreature & IGuiHolder<CreatureGuiData>> void verifyEntity(EntityPlayer player, E entity) {
        Objects.requireNonNull(entity);
        if (!entity.isEntityAlive()) {
            throw new IllegalArgumentException("Can't open dead Entity GUI!");
        }
        else if (player.world != entity.world) {
            throw new IllegalArgumentException("Entity must be in same dimension as the player!");
        }
    }

    public <E extends RiftCreature & IGuiHolder<CreatureGuiData>> void open(EntityPlayer player, E entity) {
        Objects.requireNonNull(player);
        this.verifyEntity(player, entity);
        GuiManager.open(this, new CreatureGuiData(player, entity, this.openedFromParty, this.pageToOpenTo), (EntityPlayerMP) player);
    }

    public void open(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo) {
        Objects.requireNonNull(selectedCreatureInfo);
        GuiManager.open(this, new CreatureGuiData(player, selectedCreatureInfo, this.openedFromParty, this.pageToOpenTo), (EntityPlayerMP) player);
    }

    @Override
    public @NotNull IGuiHolder<CreatureGuiData> getGuiHolder(CreatureGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getGuiHolder()), "Found object is not a gui holder!");
    }

    @Override
    public void writeGuiData(CreatureGuiData guiData, PacketBuffer buffer) {
        ByteBufUtils.writeTag(buffer, guiData.getNBT());
    }

    @Override
    public @NotNull CreatureGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        NBTTagCompound nbtTagCompound = ByteBufUtils.readTag(buffer);
        return new CreatureGuiData(player, Objects.requireNonNull(nbtTagCompound));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, CreatureGuiData guiData) {
        if (guiData.dataType == CreatureGuiData.DataType.SELECTION) return true;
        RiftCreature creature = (RiftCreature) guiData.getGuiHolder();
        return super.canInteractWith(player, guiData) &&
                creature != null &&
                player.getDistanceSq(creature.posX, creature.posY, creature.posZ) <= 64 &&
                player.world == creature.world &&
                creature.isEntityAlive();
    }
}
