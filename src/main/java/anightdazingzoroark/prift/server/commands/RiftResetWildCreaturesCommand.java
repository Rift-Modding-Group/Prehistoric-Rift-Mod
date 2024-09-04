package anightdazingzoroark.prift.server.commands;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class RiftResetWildCreaturesCommand extends CommandBase {
    @Override
    public String getName() {
        return "priftresetwildcreatures";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "priftcommands.reset_wild_creatures.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            World world = sender.getEntityWorld();
            if (!world.isRemote) {
                List<RiftCreature> riftCreatures = world.getEntities(RiftCreature.class, new Predicate<RiftCreature>() {
                    @Override
                    public boolean apply(@Nullable RiftCreature riftCreature) {
                        return riftCreature != null && !riftCreature.isTamed();
                    }
                });
                for (RiftCreature creature : riftCreatures) creature.onKillCommand();
                notifyCommandListener(sender, this, "priftcommands.reset_wild_creatures.reset_success", riftCreatures.size());
            }
        }
        else throw new WrongUsageException("priftcommands.bleed.usage", new Object[0]);
    }
}
