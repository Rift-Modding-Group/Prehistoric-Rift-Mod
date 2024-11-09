package anightdazingzoroark.prift.server.items;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class RiftPlayerCreatureUpgradeItem extends Item {
    private final String type;

    public RiftPlayerCreatureUpgradeItem(String type) {
        this.type = type.toLowerCase(Locale.ROOT);
        this.maxStackSize = 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item."+this.type+".tooltip"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        ItemStack itemstack = player.getHeldItem(handIn);

        switch (this.type) {
            case "player_party_upgrade":
                if (PlayerTamedCreaturesHelper.getPartySizeLevel(player) >= 4) {
                    if (world.isRemote) player.sendStatusMessage(new TextComponentTranslation("reminder.party_level_max"), false);
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
                else {
                    int newLevel = PlayerTamedCreaturesHelper.getPartySizeLevel(player) + 1;
                    PlayerTamedCreaturesHelper.upgradePlayerParty(player, newLevel);
                    if (world.isRemote) player.sendStatusMessage(new TextComponentTranslation("reminder.party_level_upgraded", newLevel), false);
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
            case "player_box_upgrade":
                if (PlayerTamedCreaturesHelper.getBoxSizeLevel(player) >= 4) {
                    if (world.isRemote) player.sendStatusMessage(new TextComponentTranslation("reminder.box_level_max"), false);
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
                else {
                    int newLevel = PlayerTamedCreaturesHelper.getBoxSizeLevel(player) + 1;
                    PlayerTamedCreaturesHelper.upgradePlayerBox(player, newLevel);
                    if (world.isRemote) player.sendStatusMessage(new TextComponentTranslation("reminder.box_level_upgraded", newLevel), false);
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
        }
        return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
    }
}
