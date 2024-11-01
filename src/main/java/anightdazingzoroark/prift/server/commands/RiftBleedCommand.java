package anightdazingzoroark.prift.server.commands;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;

public class RiftBleedCommand extends CommandBase {
    @Override
    public String getName() {
        return "priftbleed";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "priftcommands.bleed.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 1) throw new WrongUsageException("priftcommands.bleed.usage", new Object[0]);
        else if (args.length == 2) {
            Entity target = getEntity(server, sender, args[0]);
            if (target instanceof EntityLivingBase) {
                NonPotionEffectsHelper.setBleeding((EntityLivingBase)target, 0, Integer.parseInt(args[1]) * 20);
                notifyCommandListener(sender, this, "priftcommands.bleed.successful", new Object[] {target.getDisplayName()});
            }
            else throw new WrongUsageException("priftcommands.bleed.cannot_bleed", new Object[0]);
        }
        else if (args.length == 3) {
            Entity target = getEntity(server, sender, args[0]);
            if (target instanceof EntityLivingBase) {
                NonPotionEffectsHelper.setBleeding((EntityLivingBase)target, Integer.parseInt(args[2]), Integer.parseInt(args[1]) * 20);
                notifyCommandListener(sender, this, "priftcommands.bleed.successful", new Object[] {target.getDisplayName()});
            }
            else throw new WrongUsageException("priftcommands.bleed.cannot_bleed", new Object[0]);
        }
    }
}
