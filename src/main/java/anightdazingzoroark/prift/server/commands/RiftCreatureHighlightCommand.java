package anightdazingzoroark.prift.server.commands;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.command.*;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class RiftCreatureHighlightCommand extends CommandBase {
    @Override
    public String getName() {
        return "prifthighlightcreatures";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "priftcommands.highlight_creatures.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            World world = sender.getEntityWorld();
            if (!world.isRemote) {
                List<RiftCreature> riftCreatures = world.getEntities(RiftCreature.class, new Predicate<RiftCreature>() {
                    @Override
                    public boolean apply(@Nullable RiftCreature riftCreature) {
                        return true;
                    }
                });
                for (RiftCreature creature : riftCreatures) creature.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 600));
                notifyCommandListener(sender, this, "priftcommands.highlight_creatures.highlight_success", riftCreatures.size());
            }
        }
        else throw new WrongUsageException("priftcommands.bleed.usage", new Object[0]);
    }
}
