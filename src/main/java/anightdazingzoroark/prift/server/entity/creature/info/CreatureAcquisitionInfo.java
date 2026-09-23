package anightdazingzoroark.prift.server.entity.creature.info;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record CreatureAcquisitionInfo(@Nullable AcquisitionMethod acquisitionMethod, long acquisitionTime) {
    public static CreatureAcquisitionInfo fromNBT(@Nullable NBTTagCompound nbt) {
        if (nbt == null || nbt.isEmpty()) return new CreatureAcquisitionInfo(null, 0L);
        else {
            byte acquisitionMethodByte = nbt.getByte("AcquisitionMethod");
            AcquisitionMethod acquisitionMethod = acquisitionMethodByte >= 0 && acquisitionMethodByte < AcquisitionMethod.values().length
                    ? AcquisitionMethod.values()[acquisitionMethodByte]
                    : null;
            long acquisitionTime = nbt.getLong("AcquisitionTime");
            return new CreatureAcquisitionInfo(acquisitionMethod, acquisitionTime);
        }
    }

    public String acquisitionTimeString() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(this.acquisitionTime), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    public String acquisitionInfoString() {
        if (this.acquisitionTime <= 0L || this.acquisitionMethod == null) {
            return I18n.format("acquisition.unknown");
        }
        return I18n.format("acquisition." + this.acquisitionMethod.name().toLowerCase(Locale.ROOT), this.acquisitionTimeString());
    }

    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        //save acquisition method
        byte acquisitionMethodByte = this.acquisitionMethod != null ? (byte) this.acquisitionMethod.ordinal() : -1;
        toReturn.setByte("AcquisitionMethod", acquisitionMethodByte);
        //save acquisition time
        toReturn.setLong("AcquisitionTime", this.acquisitionTime);
        return toReturn;
    }

    public enum AcquisitionMethod {
        TAMED_FROM_WILD,
        HATCHED,
        BORN,
        PURCHASED,
        RECEIVED
    }
}
