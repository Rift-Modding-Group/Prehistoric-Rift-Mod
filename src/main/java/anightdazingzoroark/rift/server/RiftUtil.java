package anightdazingzoroark.rift.server;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

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
            return item.getItem() == new ItemStack(block).getItem();
        }
        return false;
    }

    public static boolean blockWeakerThanWood(Block block) {
        return blockInOreDicType(block, "logWood") || blockInOreDicType(block, "plankWood") || blockInOreDicType(block, "slabWood") || blockInOreDicType(block, "stairWood") || blockInOreDicType(block, "fenceWood") || blockInOreDicType(block, "fenceGateWood") || blockInOreDicType(block, "doorWood") || blockInOreDicType(block, "treeSapling") || blockInOreDicType(block, "treeLeaves") || blockInOreDicType(block, "vine") || blockInOreDicType(block, "dirt") || blockInOreDicType(block, "grass") || blockInOreDicType(block, "gravel") || blockInOreDicType(block, "sand") || blockInOreDicType(block, "torch") || blockInOreDicType(block, "workbench") || blockInOreDicType(block, "blockSlime") || blockInOreDicType(block, "blockGlassColorless") || blockInOreDicType(block, "blockGlass") || blockInOreDicType(block, "paneGlassColorless") || blockInOreDicType(block, "paneGlass") || blockInOreDicType(block, "wool") || blockInOreDicType(block, "chestWood");
    }
}
