package anightdazingzoroark.prift.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RiftUtil {
    @Nullable
    public static Entity getEntityWithUUID(@NotNull World world, @NotNull UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (entity == null) continue;
            if (entity.getUniqueID().equals(uuid)) return entity;
        }
        return null;
    }

    public static boolean entityMatchesID(@NotNull Entity entity, @NotNull String id) {
        //special case for players
        if (entity instanceof EntityPlayer && id.equals("minecraft:player")) return true;

        //normal case
        ResourceLocation entityResource = EntityList.getKey(entity);
        if (entityResource == null) return false;
        return entityResource.toString().equals(id);
    }

    public static boolean itemStackMatchesString(@NotNull ItemStack itemStack, @NotNull String string) {
        ResourceLocation itemResource = Item.REGISTRY.getNameForObject(itemStack.getItem());
        if (itemResource == null) return false;

        ImmutablePair<String, Integer> stackNameAndMeta = getItemStackNameAndMeta(string);
        String testedStackName = itemResource.toString();

        //if metadata is negative, presume wildcard
        if (stackNameAndMeta.getRight() < 0) return testedStackName.equals(stackNameAndMeta.getLeft());
        else return testedStackName.equals(stackNameAndMeta.getLeft()) && itemStack.getMetadata() == stackNameAndMeta.getRight();
    }

    @NotNull
    private static ImmutablePair<String, Integer> getItemStackNameAndMeta(@NotNull String string) {
        int firstColonIndex = string.indexOf(":");
        int secondColonIndex = string.indexOf(":", firstColonIndex);

        //no metadata provided, presume its 0 then
        if (secondColonIndex < 0) return new ImmutablePair<>(string, 0);

        String itemId = string.substring(0, secondColonIndex);
        int metadata = Integer.parseInt(string.substring(secondColonIndex + 1));

        return new ImmutablePair<>(itemId, metadata);
    }
}
