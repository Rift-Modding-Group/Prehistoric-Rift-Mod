package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.item.RiftItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftCreativeTabs {
    public static final CreativeTabs creativeItemsTab = new CreativeTabs(RiftInitialize.MODID) {
        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack createIcon() {
            Item tributeItem = RiftItems.getTributeItem(RiftCreatureRegistry.DEFAULT_CREATURE);
            return new ItemStack(tributeItem == null ? Items.BONE : tributeItem);
        }

        @Override
        public String getTranslationKey() {
            return "itemGroup.prift_items";
        }
    };

    public static final CreativeTabs spawnEggsTab = new CreativeTabs(RiftInitialize.MODID) {
        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack createIcon() {
            if (RiftItems.CREATURE_SPAWN_EGG == null) return new ItemStack(Items.BONE);

            ItemStack stack = new ItemStack(RiftItems.CREATURE_SPAWN_EGG);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("CreatureType", RiftCreatureRegistry.DEFAULT_CREATURE);
            stack.setTagCompound(tag);
            return stack;
        }

        @Override
        public String getTranslationKey() {
            return "itemGroup.prift_spawn_eggs";
        }
    };
}