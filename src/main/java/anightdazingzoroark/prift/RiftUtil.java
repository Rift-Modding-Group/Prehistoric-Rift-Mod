package anightdazingzoroark.prift;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.enums.EggTemperature;
import com.teamderpy.shouldersurfing.client.ShoulderInstance;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RiftUtil {
    public static final double gravity = 0.08D;
    public static final UUID nilUUID = new UUID(0L, 0L);

    public static EntityLivingBase getEntityFromUUID(World world, UUID uuid) {
        if (world != null) {
            for (Entity entity : world.getLoadedEntityList()) {
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                    if (entityLivingBase.getUniqueID().equals(uuid)) {
                        return entityLivingBase;
                    }
                }
            }
        }
        return null;
    }

    public static double highestWaterPos(RiftCreature creature) {
        double maxHeight = creature.world.getActualHeight() - creature.getPosition().getY();
        if (creature.isInWater()) {
            for (int i = 0; i <= maxHeight; i++) {
                BlockPos pos = creature.getPosition().add(0, i, 0);
                if (!creature.world.getBlockState(pos).getMaterial().isLiquid()) return creature.getPosition().getY() + i - 1;
            }
        }
        return 0D;
    }

    public static boolean isUsingSSR() {
        if (Loader.isModLoaded(RiftInitialize.SSR_MOD_ID)) {
            return ShoulderInstance.getInstance().doShoulderSurfing();
        }
        return false;
    }

    public static boolean isRidingBoat(EntityLivingBase entity) {
        return entity.isRiding() && (entity.getRidingEntity() instanceof EntityBoat);
    }

    public static boolean entityAtLocation(EntityLivingBase entityLivingBase, BlockPos pos, double radius) {
        return entityLivingBase.getEntityBoundingBox().grow(radius).contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
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

    public static boolean biomeTagMatchFromList(List<String> list, Biome biome) {
        for (String tag : list) {
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(tag))) return true;
        }
        return false;
    }

    public static List<String> creatureTargets(String[] whiteList) {
        return creatureTargets(whiteList, new String[]{}, false);
    }

    public static List<String> creatureTargets(String[] whiteList, String[] blackList, boolean useCUniversal) {
        List<String> finalTargets = new ArrayList<>();
        List<String> wLList = Arrays.asList(whiteList);
        List<String> bLList = Arrays.asList(blackList);
        List<String> baseTargetList = new ArrayList<>(Arrays.asList(GeneralConfig.universalCarnivoreTargets));
        if (useCUniversal) {
            baseTargetList.removeIf(bLList::contains);
            finalTargets = Stream.concat(wLList.stream(), baseTargetList.stream()).collect(Collectors.toList());
        }
        else return wLList;
        return finalTargets;
    }

    public static MobSize getMobSize(EntityLivingBase entity) {
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

    public static boolean isAppropriateSize(EntityLivingBase entity, MobSize size) {
        return getMobSize(entity).ordinal() <= size.ordinal();
    }

    public static void drawCenteredString(FontRenderer fontRenderer, String string, int xPos, int yPos, int wrapWidth, int textColor) {
        List<String> lines = fontRenderer.listFormattedStringToWidth(string, wrapWidth);
        int currentY = yPos;

        for (String line : lines) {
            int lineWidth = fontRenderer.getStringWidth(line);
            int lineX = xPos + (wrapWidth - lineWidth) / 2;
            fontRenderer.drawString(line, lineX, currentY, textColor);
            currentY += fontRenderer.FONT_HEIGHT;
        }
    }

    public static boolean entityIsUnderwater(EntityLivingBase entityLivingBase) {
        BlockPos highestWaterPos = entityLivingBase.getPosition().add(0, Math.ceil(entityLivingBase.height), 0);
        return entityLivingBase.world.getBlockState(highestWaterPos).getMaterial() == Material.WATER;
    }
}
