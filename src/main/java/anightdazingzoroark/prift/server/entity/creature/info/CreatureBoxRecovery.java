package anightdazingzoroark.prift.server.entity.creature.info;

import anightdazingzoroark.prift.server.config.RiftGeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Recovery timestamps travel with the creature, so moving between boxes cannot reset its progress.
 * */
public class CreatureBoxRecovery {
    public static final String TAG = "BoxRecovery";

    public static long currentTime(@NotNull EntityPlayer player) {
        MinecraftServer server = player.getServer();
        World clockWorld = server == null ? player.world : server.getWorld(0);
        return clockWorld.getTotalWorldTime();
    }

    /**
     * Returns whether health or stamina changed and the visible box needs a fresh snapshot.
     * */
    public static boolean update(@NotNull CreatureNBT creature, long currentTime) {
        if (creature.nbtTagCompound().isEmpty()) return false;
        NBTTagCompound recovery = creature.nbtTagCompound().getCompoundTag(TAG);
        long previousTime = recovery.hasKey("LastUpdate") ? recovery.getLong("LastUpdate") : currentTime;
        long elapsedTicks = Math.max(0L, currentTime - previousTime);
        boolean changed = creature.advanceInactiveTicks(elapsedTicks);
        recovery.setLong("LastUpdate", Math.max(currentTime, previousTime));
        if (creature.getHealth() <= 0f) {
            if (!recovery.hasKey("ReviveAt")) {
                recovery.setLong("ReviveAt", currentTime + Math.max(0L, RiftGeneralConfig.creatures.boxRevivalTimeSeconds) * 20L);
            }
            if (currentTime >= recovery.getLong("ReviveAt") && creature.getMaxHealth() > 0f) {
                creature.nbtTagCompound().setFloat("Health", creature.getMaxHealth());
                creature.nbtTagCompound().setShort("DeathTime", (short) 0);
                creature.nbtTagCompound().setShort("HurtTime", (short) 0);
                recovery.removeTag("ReviveAt");
                changed = true;
            }
        }
        else recovery.removeTag("ReviveAt");
        creature.nbtTagCompound().setTag(TAG, recovery);
        return changed;
    }

    public static int remainingSeconds(@NotNull CreatureNBT creature, long currentTime) {
        if (creature.nbtTagCompound().isEmpty() || creature.getHealth() > 0f) return 0;
        NBTTagCompound recovery = creature.nbtTagCompound().getCompoundTag(TAG);
        if (!recovery.hasKey("ReviveAt")) return 0;
        long deadline = recovery.getLong("ReviveAt");
        if (currentTime >= deadline) return 0;
        // All visible timers change on the same world-second boundary, regardless of deposit tick.
        // Keep the final partial second visible until the exact revival deadline has passed.
        long seconds = Math.floorDiv(deadline, 20L) - Math.floorDiv(currentTime, 20L);
        return (int) Math.clamp(seconds, 1L, Integer.MAX_VALUE);
    }
}