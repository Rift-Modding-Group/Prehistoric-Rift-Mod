package anightdazingzoroark.rift;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.server.enums.CreatureDiet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

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

    public static boolean blockWeakerThanWood(Block block) {
        List<String> oreDicList = new ArrayList<>();
        List<String> blockList = new ArrayList<>();
        for (String entry : RiftConfig.weakerThanWood) {
            if (entry.contains("oreDic:")) {
                oreDicList.add(entry.replace("oreDic:", ""));
            }
            if (entry.contains("block:")) {
                blockList.add(entry.replace("block:", ""));
            }
        }
        for (String oreDicEntry : oreDicList) {
            if (blockInOreDicType(block, oreDicEntry)) return true;
        }
        for (String blockEntry : blockList) {
            if (Block.getBlockFromName(blockEntry).equals(block)) return true;
        }
        return false;
    }

    public static boolean isEnergyRegenItem(Item item, CreatureDiet diet) {
        List<String> itemList = new ArrayList<>();
        String itemName = Item.REGISTRY.getNameForObject(item).toString();
        if (diet == CreatureDiet.HERBIVORE || diet == CreatureDiet.FUNGIVORE) {
            for (int i = 0; i < RiftConfig.herbivoreRegenEnergyFoods.length; i++) {
                int first = RiftConfig.herbivoreRegenEnergyFoods[i].indexOf(":");
                int second = RiftConfig.herbivoreRegenEnergyFoods[i].indexOf(":", first + 1);
                itemList.add(RiftConfig.herbivoreRegenEnergyFoods[i].substring(0, second));
            }
        }
        else if (diet == CreatureDiet.CARNIVORE || diet == CreatureDiet.PISCIVORE || diet == CreatureDiet.INSECTIVORE) {
            for (int i = 0; i < RiftConfig.carnivoreRegenEnergyFoods.length; i++) {
                int first = RiftConfig.carnivoreRegenEnergyFoods[i].indexOf(":");
                int second = RiftConfig.carnivoreRegenEnergyFoods[i].indexOf(":", first + 1);
                itemList.add(RiftConfig.carnivoreRegenEnergyFoods[i].substring(0, second));
            }
        }
        return itemList.contains(itemName);
    }

    public static int getEnergyRegenItemValue(Item item, CreatureDiet diet) {
        List<String> itemList = new ArrayList<>();
        List<Integer> valueList = new ArrayList<>();
        String itemName = Item.REGISTRY.getNameForObject(item).toString();
        int value;
        if (diet == CreatureDiet.HERBIVORE || diet == CreatureDiet.FUNGIVORE) {
            for (int i = 0; i < RiftConfig.herbivoreRegenEnergyFoods.length; i++) {
                int first = RiftConfig.herbivoreRegenEnergyFoods[i].indexOf(":");
                int second = RiftConfig.herbivoreRegenEnergyFoods[i].indexOf(":", first + 1);
                itemList.add(RiftConfig.herbivoreRegenEnergyFoods[i].substring(0, second));
                valueList.add(Integer.parseInt(RiftConfig.herbivoreRegenEnergyFoods[i].substring(second + 1)));
            }
        }
        else if (diet == CreatureDiet.CARNIVORE || diet == CreatureDiet.PISCIVORE || diet == CreatureDiet.INSECTIVORE) {
            for (int i = 0; i < RiftConfig.carnivoreRegenEnergyFoods.length; i++) {
                int first = RiftConfig.carnivoreRegenEnergyFoods[i].indexOf(":");
                int second = RiftConfig.carnivoreRegenEnergyFoods[i].indexOf(":", first + 1);
                itemList.add(RiftConfig.carnivoreRegenEnergyFoods[i].substring(0, second));
                valueList.add(Integer.parseInt(RiftConfig.carnivoreRegenEnergyFoods[i].substring(second + 1)));
            }
        }

        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).equals(itemName)) return valueList.get(i);
        }
        return 0;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
