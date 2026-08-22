package anightdazingzoroark.prift.server.item;

import anightdazingzoroark.prift.client.RiftCreativeTabs;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiftCreatureSpawnEggItem extends Item {
    private static final String CREATURE_KEY = "CreatureType";

    public RiftCreatureSpawnEggItem() {
        super();
        this.setCreativeTab(RiftCreativeTabs.spawnEggsTab);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    //static helper class for creature name in use by stack
    public static String getCreatureName(ItemStack stack) {
        if (!stack.hasTagCompound()) return RiftCreatureRegistry.DEFAULT_CREATURE;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(CREATURE_KEY)) return RiftCreatureRegistry.DEFAULT_CREATURE;

        String creatureName = tag.getString(CREATURE_KEY);
        return RiftCreatureRegistry.hasCreatureBuilder(creatureName) ? creatureName : RiftCreatureRegistry.DEFAULT_CREATURE;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(getCreatureName(stack));
        String creatureName = builder != null ? builder.getLocalizedName() : "???";
        return I18n.format("item.spawn_egg.name", creatureName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag tooltipFlag) {
        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(getCreatureName(stack));
        String creatureName = builder != null ? builder.getName() : "unknown";
        tooltip.add(TextFormatting.GRAY + I18n.format("entity."+creatureName+".spawn_description"));
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;

        List<String> creatureNames = new ArrayList<>(RiftCreatureRegistry.getCreatureNames());
        Collections.sort(creatureNames);

        for (String creatureName : creatureNames) {
            //define stack for creature and add it
            ItemStack stack = new ItemStack(RiftItems.CREATURE_SPAWN_EGG);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(CREATURE_KEY, creatureName);
            stack.setTagCompound(tag);
            items.add(stack);
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(getCreatureName(stack));
        if (builder == null) return EnumActionResult.FAIL;

        BlockPos spawnPos = pos.offset(facing);
        boolean blocked = !world.isAirBlock(spawnPos) && !world.getBlockState(spawnPos).getBlock().isReplaceable(world, spawnPos);
        if (blocked) return EnumActionResult.FAIL;

        if (!world.isRemote) {
            RiftCreature creature = RiftCreatureRegistry.createCreature(world, builder.getName());

            creature.setLocationAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, world.rand.nextFloat() * 360f, 0f);
            creature.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(creature)), null);
            creature.enablePersistence();
            world.spawnEntity(creature);
        }

        if (!player.capabilities.isCreativeMode) stack.shrink(1);

        return EnumActionResult.SUCCESS;
    }
}
