package anightdazingzoroark.rift.server.commands;

import anightdazingzoroark.rift.server.entity.RiftEntityProperties;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
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
        return "riftbleed";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "riftcommands.bleed.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 1) throw new WrongUsageException("riftcommands.bleed.usage", new Object[0]);
        else if (args.length == 2) {
            Entity target = getEntity(server, sender, args[0]);
            if (target instanceof EntityLivingBase) {
                RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(target, RiftEntityProperties.class);
                properties.setBleeding(0, Integer.parseInt(args[1]) * 20);
            }
            else throw new WrongUsageException("riftcommands.bleed.cannot_bleed", new Object[0]);
        }
        else if (args.length == 3) {
            Entity target = getEntity(server, sender, args[0]);
            if (target instanceof EntityLivingBase) {
                RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(target, RiftEntityProperties.class);
                properties.setBleeding(Integer.parseInt(args[2]), Integer.parseInt(args[1]) * 20);
            }
            else throw new WrongUsageException("riftcommands.bleed.cannot_bleed", new Object[0]);
        }
    }
}
