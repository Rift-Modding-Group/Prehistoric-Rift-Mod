package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.client.ui.creatureBoxScreen.RiftCreatureBoxScreen;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class RiftCreatureBox extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final int maxDeployableCreatures = 10;

    public RiftCreatureBox() {
        super(Material.ROCK);
        this.setResistance(69420666F);
    }

    @Override
    public BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y) enumfacing = EnumFacing.NORTH;
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing enumfacing = placer.getHorizontalFacing().getOpposite();
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new RiftTileEntityCreatureBox();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote && placer instanceof EntityPlayer) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (!(tileEntity instanceof RiftTileEntityCreatureBox)) return;
            RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) tileEntity;
            teCreatureBox.setOwner((EntityPlayer) placer);
            teCreatureBox.setUniqueID(UUID.randomUUID());
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            RiftTileEntityCreatureBox tileEntity = (RiftTileEntityCreatureBox) worldIn.getTileEntity(pos);

            if (tileEntity == null) return false;

            //for sneaking, note that displaying the bounds is already dealt with in RiftCreatureBoxBorder
            if (playerIn.isSneaking()) {
                //send message to player
                playerIn.sendStatusMessage(new TextComponentTranslation("reminder.show_creature_box_bounds_and_deployed"), false);

                //apply glowing to deployed creatures
                for (int x = 0; x < tileEntity.getDeployedCreatures().size(); x++) {
                    CreatureNBT creatureNBT = tileEntity.getDeployedCreatures().get(x);
                    RiftCreature actualCreature = creatureNBT.findCorrespondingCreature(worldIn);
                    if (actualCreature != null) {
                        actualCreature.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 100));
                    }
                }
            }
            //just open the ui
            else RiftLibUIHelper.showUI(playerIn, new RiftCreatureBoxScreen(pos));
        }
        return true;
    }
}
