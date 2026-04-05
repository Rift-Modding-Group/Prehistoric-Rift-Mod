package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxDataHelper;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import com.cleanroommc.modularui.factory.GuiFactories;
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
import org.jetbrains.annotations.NotNull;

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
    public @NotNull BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(FACING).getIndex();
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y) enumfacing = EnumFacing.NORTH;
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing enumfacing = placer.getHorizontalFacing().getOpposite();
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return new RiftTileEntityCreatureBox();
    }

    @Override
    public void onBlockPlacedBy(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
        if (!world.isRemote && placer instanceof EntityPlayer) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (!(tileEntity instanceof RiftTileEntityCreatureBox teCreatureBox)) return;
            teCreatureBox.setOwner((EntityPlayer) placer);
            teCreatureBox.setUniqueID(UUID.randomUUID());
            CreatureBoxDataHelper.addCreatureBoxPos(pos, (EntityPlayer) placer);
        }
    }

    @Override
    public void breakBlock(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        if (!worldIn.isRemote) CreatureBoxDataHelper.removeCreatureBoxPos(pos);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
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
            else GuiFactories.tileEntity().open(playerIn, pos);
        }
        return true;
    }
}
