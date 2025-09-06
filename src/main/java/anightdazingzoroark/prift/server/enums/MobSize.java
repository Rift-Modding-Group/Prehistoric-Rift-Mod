package anightdazingzoroark.prift.server.enums;

import anightdazingzoroark.prift.config.GeneralConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Arrays;
import java.util.List;

public enum MobSize {
    VERY_SMALL,
    SMALL,
    MEDIUM,
    LARGE,
    VERY_LARGE;

    public static MobSize safeValueOf(String string) {
        for (MobSize mobSize : MobSize.values()) {
            if (mobSize.name().equalsIgnoreCase(string)) {
                return mobSize;
            }
        }
        return null;
    }

    public boolean isAppropriateSize(Entity entityToCompare) {
        //if hitbox is hit, perform recursion involving the parent
        if (entityToCompare instanceof MultiPartEntityPart) {
            Entity parent = (Entity) ((MultiPartEntityPart) entityToCompare).parent;
            return isAppropriateSize(parent);
        }
        if (entityToCompare != null) return getMobSize(entityToCompare).ordinal() <= this.ordinal();
        else return true;
    }

    public boolean isAppropriateSizeNotEqual(Entity entityToCompare) {
        //if hitbox is hit, perform recursion involving the parent
        if (entityToCompare instanceof MultiPartEntityPart) {
            Entity parent = (Entity) ((MultiPartEntityPart) entityToCompare).parent;
            return isAppropriateSizeNotEqual(parent);
        }
        if (entityToCompare != null) return getMobSize(entityToCompare).ordinal() < this.ordinal();
        else return true;
    }

    public static MobSize getMobSize(Entity entity) {
        List<String> verySmallSize = Arrays.asList(GeneralConfig.verySmallMobs);
        List<String> smallSize = Arrays.asList(GeneralConfig.smallMobs);
        List<String> mediumSize = Arrays.asList(GeneralConfig.mediumMobs);
        List<String> largeSize = Arrays.asList(GeneralConfig.largeMobs);
        List<String> veryLargeSize = Arrays.asList(GeneralConfig.veryLargeMobs);

        if (entity instanceof EntityPlayer) {
            if (verySmallSize.contains("minecraft:player")) return MobSize.VERY_SMALL;
            else if (smallSize.contains("minecraft:player")) return MobSize.SMALL;
            else if (mediumSize.contains("minecraft:player")) return MobSize.MEDIUM;
            else if (largeSize.contains("minecraft:player")) return MobSize.LARGE;
            else if (veryLargeSize.contains("minecraft:player")) return MobSize.VERY_LARGE;
        }
        else {
            String mobString = EntityList.getKey(entity).toString();
            if (verySmallSize.contains(mobString)) return MobSize.VERY_SMALL;
            else if (smallSize.contains(mobString)) return MobSize.SMALL;
            else if (mediumSize.contains(mobString)) return MobSize.MEDIUM;
            else if (largeSize.contains(mobString)) return MobSize.LARGE;
            else if (veryLargeSize.contains(mobString)) return MobSize.VERY_LARGE;
        }

        return MobSize.MEDIUM;
    }
}
