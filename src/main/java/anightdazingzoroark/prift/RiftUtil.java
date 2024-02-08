package anightdazingzoroark.prift;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.CreatureDiet;
import anightdazingzoroark.prift.server.enums.EggTemperature;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.*;

public class RiftUtil {
    public static final double gravity = 0.08D;

    public static boolean entityAtLocation(EntityLivingBase entityLivingBase, BlockPos pos, double radius) {
        return entityLivingBase.getEntityBoundingBox().grow(radius).contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static RiftCreature getCreatureFromUUID(World world, UUID uuid) {
        if (uuid == null) return null;
        if (!world.isRemote) {
            for (RiftCreature creature : world.getEntities(RiftCreature.class, new Predicate<RiftCreature>() {
                @Override
                public boolean apply(@Nullable RiftCreature input) {
                    return true;
                }
            })) {
                if (creature.getUUID().equals(uuid)) return creature;
            }
        }
        return null;
    }

    public static boolean blockExposedToSky(World world, BlockPos pos) {
        for (int y = pos.getY() + 1; y < 256; y++) {
            BlockPos aboveBlockPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!world.isAirBlock(aboveBlockPos)) {
                return false;
            }
        }
        return true;
    }

    public static String[] removeElementFromArray(String[] array, String element) {
        int size = array.length;
        for(int i = 0; i < size; i++) {
            if (element == array[i]) {
                for(int j = i; j < (size - 1); j++)
                    array[j] = array[j+1];
                size--;
                i--;
            }
        }
        return array;
    }

    public static boolean blockInOreDicType(Block block, String oreDicType) {
        NonNullList<ItemStack> blocksInOreDicType = OreDictionary.getOres(oreDicType);
        for (ItemStack item : blocksInOreDicType) {
            if (item.getItem().equals(new ItemStack(block).getItem())) return true;
        }
        return false;
    }

    public static boolean itemInOreDicType(Item item, String oreDicType) {
        List<Item> itemsInOreDicType = new ArrayList<>();
        for (ItemStack itemStack : OreDictionary.getOres(oreDicType)) {
            itemsInOreDicType.add(itemStack.getItem());
        }
        for (Item itemToFind : itemsInOreDicType) {
            if (itemToFind.equals(item)) return true;
        }
        return false;
    }

    public static boolean blockWeakerThanDirt(Block block, IBlockState blockState) {
        List<String> oreDicList = new ArrayList<>();
        List<String> blockList = new ArrayList<>();
        boolean flag = false;
        for (String entry : GeneralConfig.weakerThanDirt) {
            if (entry.contains("oreDic:")) {
                oreDicList.add(entry.replace("oreDic:", ""));
            }
            if (entry.contains("block:")) {
                blockList.add(entry.replace("block:", ""));
            }
        }
        for (String oreDicEntry : oreDicList) {
            if (!flag) flag = blockInOreDicType(block, oreDicEntry);
        }
        for (String blockEntry : blockList) {
            int blockIdFirst = blockEntry.indexOf(":");
            int blockIdSecond = blockEntry.indexOf(":", blockIdFirst + 1);
            int blockData = Integer.parseInt(blockEntry.substring(blockIdSecond + 1));
            if (!flag) flag = Block.getBlockFromName(blockEntry.substring(0, blockIdSecond)).equals(block) && (blockData == -1 || block.getMetaFromState(blockState) == blockData);
        }
        return flag;
    }

    public static boolean blockWeakerThanWood(Block block, IBlockState blockState) {
        List<String> oreDicList = new ArrayList<>();
        List<String> blockList = new ArrayList<>();
        boolean flag = false;
        for (String entry : GeneralConfig.weakerThanWood) {
            if (entry.contains("oreDic:")) {
                oreDicList.add(entry.replace("oreDic:", ""));
            }
            if (entry.contains("block:")) {
                blockList.add(entry.replace("block:", ""));
            }
        }
        for (String oreDicEntry : oreDicList) {
            if (!flag) flag = blockInOreDicType(block, oreDicEntry);
        }
        for (String blockEntry : blockList) {
            int blockIdFirst = blockEntry.indexOf(":");
            int blockIdSecond = blockEntry.indexOf(":", blockIdFirst + 1);
            int blockData = Integer.parseInt(blockEntry.substring(blockIdSecond + 1));
            if (!flag) flag = Block.getBlockFromName(blockEntry.substring(0, blockIdSecond)).equals(block) && (blockData == -1 || block.getMetaFromState(blockState) == blockData);
        }
        return flag || blockWeakerThanDirt(block, blockState);
    }

    public static boolean blockWeakerThanStone(Block block, IBlockState blockState) {
        List<String> oreDicList = new ArrayList<>();
        List<String> blockList = new ArrayList<>();
        boolean flag = false;
        for (String entry : GeneralConfig.weakerThanStone) {
            if (entry.contains("oreDic:")) {
                oreDicList.add(entry.replace("oreDic:", ""));
            }
            if (entry.contains("block:")) {
                blockList.add(entry.replace("block:", ""));
            }
        }
        for (String oreDicEntry : oreDicList) {
            if (!flag) flag = blockInOreDicType(block, oreDicEntry);
        }
        for (String blockEntry : blockList) {
            int blockIdFirst = blockEntry.indexOf(":");
            int blockIdSecond = blockEntry.indexOf(":", blockIdFirst + 1);
            int blockData = Integer.parseInt(blockEntry.substring(blockIdSecond + 1));
            if (!flag) flag = Block.getBlockFromName(blockEntry.substring(0, blockIdSecond)).equals(block) && (blockData == -1 || block.getMetaFromState(blockState) == blockData);
        }
        return flag || blockWeakerThanDirt(block, blockState) || blockWeakerThanDirt(block, blockState);
    }

    public static boolean isEnergyRegenItem(Item item, CreatureDiet diet) {
        List itemList = new ArrayList<>();
        if (diet == CreatureDiet.HERBIVORE || diet == CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (diet == CreatureDiet.CARNIVORE || diet == CreatureDiet.PISCIVORE || diet == CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);

        for (Object itemEntry : itemList) {
            if (itemEntry instanceof String) {
                String itemEntryStr = (String) itemEntry;
                int first = itemEntryStr.indexOf(":");
                int second = itemEntryStr.indexOf(":", first + 1);
                int third = itemEntryStr.indexOf(":", second + 1);
                int itemData = Integer.parseInt(itemEntryStr.substring(second + 1, third));
                if (Item.getByNameOrId(itemEntryStr.substring(0, second)).equals(item) && (itemData == -1 || itemData == new ItemStack(item).getMetadata())) return true;
            }
        }
        return false;
    }

    public static int getEnergyRegenItemValue(Item item, CreatureDiet diet) {
        List<String> itemList = new ArrayList<>();
        if (diet == CreatureDiet.HERBIVORE || diet == CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (diet == CreatureDiet.CARNIVORE || diet == CreatureDiet.PISCIVORE || diet == CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);

        for (String itemEntry : itemList) {
            int first = itemEntry.indexOf(":");
            int second = itemEntry.indexOf(":", first + 1);
            int third = itemEntry.indexOf(":", second + 1);
            int itemData = Integer.parseInt(itemEntry.substring(second + 1, third));
            if (Item.getByNameOrId(itemEntry.substring(0, second)).equals(item) && (itemData == -1 || itemData == new ItemStack(item).getMetadata())) {
                return Integer.parseInt(itemEntry.substring(third + 1));
            }
        }
        return 0;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean checkInMountItemWhitelist(Item item) {
        List<String> oreDicList = new ArrayList<>();
        List<String> itemList = new ArrayList<>();
        boolean flag = false;
        for (String entry : GeneralConfig.mountOverrideWhitelistItems) {
            if (entry.contains("oreDic:")) {
                oreDicList.add(entry.replace("oreDic:", ""));
            }
            if (entry.contains("item:")) {
                itemList.add(entry.replace("item:", ""));
            }
        }
        for (String oreDicEntry : oreDicList) {
            if (!flag) flag = RiftUtil.itemInOreDicType(item, oreDicEntry);
        }
        for (String itemEntry : itemList) {
            int itemIdFirst = itemEntry.indexOf(":");
            int itemIdSecond = itemEntry.indexOf(":", itemIdFirst + 1);
            int itemData = Integer.parseInt(itemEntry.substring(itemIdSecond + 1));
            if (!flag) flag = Item.getByNameOrId(itemEntry.substring(0, itemIdSecond)).equals(item) && (itemData == -1 || itemData == new ItemStack(item).getMetadata());
        }
        return flag;
    }

    public static void drawTexturedModalRect(float x, float y, int texX, int texY, int width, int height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        double z = 0.0D;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, z).tex((texX * f), (texY + height) * f1).endVertex();
        bufferbuilder.pos((x + width), y + height, z).tex((texX + width) * f, (texY + height) * f1).endVertex();
        bufferbuilder.pos((x + width), y, z).tex((texX + width) * f,(texY * f1)).endVertex();
        bufferbuilder.pos(x, y, z).tex((texX * f), (texY * f1)).endVertex();
        tessellator.draw();
    }

    public static float setModelScale(RiftCreature creature, float min, float max) {
        return clamp(((max - min)/(24000)) * (creature.getAgeInTicks() - 24000f) + max, min, max);
    }

    public static int randomInRange(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public static double randomInRange(double min, double max) {
        return min + new Random().nextDouble() * (max - min);
    }

    public static EntityLivingBase findClosestEntity(EntityLivingBase baseEntity, List<EntityLivingBase> entities) {
        if (baseEntity == null || entities == null || entities.isEmpty()) return null;

        Optional<EntityLivingBase> closest = entities.stream()
                .min((entity1, entity2) -> {
                    double dist1 = baseEntity.getDistanceSq(entity1);
                    double dist2 = baseEntity.getDistanceSq(entity2);
                    return Double.compare(dist1, dist2);
                });

        return closest.orElse(null);
    }

    public static EggTemperature getCorrespondingTempFromBiome(World world, BlockPos blockPos) {
        float temperature = world.getBiome(blockPos).getTemperature(blockPos);
        if (temperature < 0f) return EggTemperature.VERY_COLD;
        else if (temperature >= 0f && temperature < 0.5f) return EggTemperature.COLD;
        else if (temperature >= 0.5f && temperature < 1f) return EggTemperature.NEUTRAL;
        else if (temperature >= 1f && temperature < 1.5f) return EggTemperature.WARM;
        else if (temperature >= 1.5f) return EggTemperature.VERY_WARM;
        else return EggTemperature.NEUTRAL;
    }
}
