package anightdazingzoroark.prift.client.ui.data;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CreatureGuiFactory extends AbstractUIFactory<CreatureGuiData> {
    public static CreatureGuiFactory INSTANCE = new CreatureGuiFactory();

    public static Opener create() {
        return new Opener();
    }

    private int pageToOpenTo;
    private SelectedCreatureInfo.MenuOpenedFrom menuOpenedFrom;
    private BlockPos lastCreatureBoxPos = BlockPos.ORIGIN;

    protected CreatureGuiFactory() {
        super(RiftInitialize.MODID+":creature");
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
        GuiManager.open(this, new CreatureGuiData(player, entity, this.menuOpenedFrom, this.pageToOpenTo, this.lastCreatureBoxPos), (EntityPlayerMP) player);
    }

    public void open(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo) {
        Objects.requireNonNull(selectedCreatureInfo);
        GuiManager.open(this, new CreatureGuiData(player, selectedCreatureInfo, this.pageToOpenTo), (EntityPlayerMP) player);
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
                player.world == creature.world &&
                creature.isEntityAlive();
    }

    public static class Opener {
        private final CreatureGuiFactory toOpen = INSTANCE;

        private Opener() {}

        public Opener setPageToOpenTo(int page) {
            this.toOpen.pageToOpenTo = page;
            return this;
        }

        public Opener setMenuOpenedFrom(SelectedCreatureInfo.MenuOpenedFrom menuOpenedFrom) {
            this.toOpen.menuOpenedFrom = menuOpenedFrom;
            return this;
        }

        public Opener setLastCreatureBox(BlockPos pos) {
            this.toOpen.lastCreatureBoxPos = pos;
            return this;
        }

        public CreatureGuiFactory build() {
            return INSTANCE;
        }
    }
}
