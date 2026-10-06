package anightdazingzoroark.prift.server.commands;

import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiftBleedCommand extends CommandBase {
    @Override
    @NotNull
    public String getName() {
        return "priftbleed";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    @NotNull
    public String getUsage(@NotNull ICommandSender sender) {
        return "priftcommands.bleed.usage";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
        if (args.length < 2 || args.length > 3) throw new WrongUsageException(this.getUsage(sender));

        EntityLivingBase target = getEntity(server, sender, args[0], EntityLivingBase.class);
        int duration = parseInt(args[1], 1, Integer.MAX_VALUE / 20) * 20;
        int strength = args.length == 3 ? parseInt(args[2], 0) : 0;
        OtherEntityProperties properties = OtherEntityProperties.get(target);
        if (properties == null) throw new CommandException("priftcommands.bleed.cannot_bleed", target.getDisplayName());

        properties.setBleeding(strength, duration);
        notifyCommandListener(sender, this, "priftcommands.bleed.successful", target.getDisplayName());
    }

    @Override
    @NotNull
    public List<String> getTabCompletions(
            @NotNull MinecraftServer server, @NotNull ICommandSender sender,
            String @NotNull [] args, @Nullable BlockPos targetPosition
    ) {
        if (args.length == 1) {
            List<String> targets = new ArrayList<>(List.of(server.getOnlinePlayerNames()));
            Collections.addAll(targets, "@p", "@r", "@a", "@e", "@s");
            return getListOfStringsMatchingLastWord(args, targets);
        }
        if (args.length == 2) return getListOfStringsMatchingLastWord(args, "1", "5", "10", "30", "60");
        if (args.length == 3) return getListOfStringsMatchingLastWord(args, "0", "1", "2", "3", "4");
        return List.of();
    }

    @Override
    public boolean isUsernameIndex(String @NotNull [] args, int index) {
        return index == 0;
    }
}
