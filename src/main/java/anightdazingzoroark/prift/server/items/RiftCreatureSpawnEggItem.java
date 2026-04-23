package anightdazingzoroark.prift.server.items;

import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiftCreatureSpawnEggItem extends Item {
    private static final String CREATURE_KEY = "CreatureType";
    private static final String DEFAULT_CREATURE = "tyrannosaurus";

    public RiftCreatureSpawnEggItem() {
        super();
        this.setCreativeTab(RiftCreativeTabs.spawnEggsTab);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    //static helper class for creature name in use by stack
    public static String getCreatureName(ItemStack stack) {
        if (!stack.hasTagCompound()) return DEFAULT_CREATURE;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(CREATURE_KEY)) return DEFAULT_CREATURE;

        String creatureName = tag.getString(CREATURE_KEY);
        return RiftCreatureRegistry.creatureBuilderMap.containsKey(creatureName) ? creatureName : DEFAULT_CREATURE;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(getCreatureName(stack));
        String creatureName = builder != null ? builder.getLocalizedName() : "???";
        return I18n.format("item.spawn_egg.name", creatureName);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;

        List<String> creatureNames = new ArrayList<>(RiftCreatureRegistry.creatureBuilderMap.keySet());
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
            RiftCreatureNew creature = this.createCreature(world, builder);
            if (creature == null) return EnumActionResult.FAIL;

            creature.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(creature)), null);
            creature.setLocationAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, world.rand.nextFloat() * 360f, 0f);
            creature.enablePersistence();
            world.spawnEntity(creature);
        }

        if (!player.capabilities.isCreativeMode) stack.shrink(1);

        return EnumActionResult.SUCCESS;
    }

    private RiftCreatureNew createCreature(World world, RiftCreatureBuilder builder) {
        try {
            return builder.getCreatureClass().getConstructor(World.class).newInstance(world);
        }
        catch (Exception e) {
            return null;
        }
    }
}
