package anightdazingzoroark.rift;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.enums.CreatureDiet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;
import scala.Int;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.List;

public class RiftUtil {
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

    public static boolean blockWeakerThanWood(Block block, IBlockState blockState) {
        List<String> oreDicList = new ArrayList<>();
        List<String> blockList = new ArrayList<>();
        boolean flag = false;
        for (String entry : RiftConfig.weakerThanWood) {
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
            if (!flag) flag = Block.getBlockFromName(blockEntry.substring(0, blockIdSecond)).equals(block) && (blockData == 32767 || block.getMetaFromState(blockState) == blockData);
        }
        return flag;
    }

    public static boolean isEnergyRegenItem(Item item, CreatureDiet diet) {
        List<String> itemList = new ArrayList<>();
        boolean flag = false;
        if (diet == CreatureDiet.HERBIVORE || diet == CreatureDiet.FUNGIVORE) itemList = Arrays.asList(RiftConfig.herbivoreRegenEnergyFoods);
        else if (diet == CreatureDiet.CARNIVORE || diet == CreatureDiet.PISCIVORE || diet == CreatureDiet.INSECTIVORE) itemList = Arrays.asList(RiftConfig.carnivoreRegenEnergyFoods);

        for (String itemEntry : itemList) {
            int first = itemEntry.indexOf(":");
            int second = itemEntry.indexOf(":", first + 1);
            int third = itemEntry.indexOf(":", second + 1);
            int itemData = Integer.parseInt(itemEntry.substring(second + 1, third));
            if (Item.getByNameOrId(itemEntry.substring(0, second)).equals(item) && (itemData == 32767 || itemData == new ItemStack(item).getMetadata())) return true;
        }
        return false;
    }

    public static int getEnergyRegenItemValue(Item item, CreatureDiet diet) {
        List<String> itemList = new ArrayList<>();
        if (diet == CreatureDiet.HERBIVORE || diet == CreatureDiet.FUNGIVORE) itemList = Arrays.asList(RiftConfig.herbivoreRegenEnergyFoods);
        else if (diet == CreatureDiet.CARNIVORE || diet == CreatureDiet.PISCIVORE || diet == CreatureDiet.INSECTIVORE) itemList = Arrays.asList(RiftConfig.carnivoreRegenEnergyFoods);

        for (String itemEntry : itemList) {
            int first = itemEntry.indexOf(":");
            int second = itemEntry.indexOf(":", first + 1);
            int third = itemEntry.indexOf(":", second + 1);
            int itemData = Integer.parseInt(itemEntry.substring(second + 1, third));
            if (Item.getByNameOrId(itemEntry.substring(0, second)).equals(item) && (itemData == 32767 || itemData == new ItemStack(item).getMetadata())) {
                return Integer.parseInt(itemEntry.substring(third + 1));
            }
        }
        return 0;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean checkInMountItemWhitelist(Item item) {
        List<String> oreDicList = new ArrayList<>();
        List<String> itemList = new ArrayList<>();
        boolean flag = false;
        for (String entry : RiftConfig.mountOverrideWhitelistItems) {
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
            if (!flag) flag = Item.getByNameOrId(itemEntry.substring(0, itemIdSecond)).equals(item) && (itemData == 32767 || itemData == new ItemStack(item).getMetadata());
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

    //note to self: this returns degrees
    //multiply by 0.017453292 or pi rad over 180 to get degrees
    public static float getCreatureHeadYaw(RiftCreature creature, float partialTicks) {
        float f = interpolateRotation(creature.prevRenderYawOffset, creature.renderYawOffset, partialTicks);
        float f1 = interpolateRotation(creature.prevRotationYawHead, creature.rotationYawHead, partialTicks);
        float f2 = f1 - f;

        return f2 * (creature.isBeingRidden() ? 1 : -1);
    }

    public static float getCreatureHeadPitch(RiftCreature creature, float partialTicks) {
        return creature.prevRotationPitch + (creature.rotationPitch - creature.prevRotationPitch) * partialTicks;
    }

    protected static float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f;

        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F) {
            ;
        }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return prevYawOffset + partialTicks * f;
    }
}
