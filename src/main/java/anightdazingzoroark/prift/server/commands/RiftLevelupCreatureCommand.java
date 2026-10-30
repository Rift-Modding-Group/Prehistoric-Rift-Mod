package anightdazingzoroark.prift.server.commands;

import anightdazingzoroark.prift.server.config.RiftGeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RiftLevelupCreatureCommand extends CommandBase {
    @Override
    @NotNull
    public String getName() {
        return "priftlevelupcreature";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    @NotNull
    public String getUsage(@NotNull ICommandSender sender) {
        return "priftcommands.levelupcreature.usage";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
        if (args.length != 2) throw new WrongUsageException(this.getUsage(sender));

        RiftCreature target = getEntity(server, sender, args[0], RiftCreature.class);
        int level = parseInt(args[1], 1, RiftGeneralConfig.creatures.maxLevel);

        float healthFraction = target.getHealth() / target.getMaxHealth();
        float staminaFraction = target.getStamina() / target.getMaxStamina();
        target.setLevel(level);
        target.setXP(0);
        target.getCreatureStats().applyStatsToCreature(target);
        target.setHealth(target.getMaxHealth() * healthFraction);
        target.setStamina(target.getMaxStamina() * staminaFraction);
        target.getCreatureMoves().unlockLevelupMoves(target.getLevel());

        if (target.getOwner() instanceof EntityPlayer player) {
            PlayerPartyProperties playerParty = PlayerPartyProperties.get(player);
            if (playerParty != null) playerParty.updatePartyMember(target);
        }

        notifyCommandListener(sender, this, "priftcommands.levelupcreature.successful", target.getDisplayName(), target.getLevel());
    }

    @Override
    @NotNull
    public List<String> getTabCompletions(
            @NotNull MinecraftServer server, @NotNull ICommandSender sender,
            String @NotNull [] args, @Nullable BlockPos targetPosition
    ) {
        return switch (args.length) {
            case 1 -> getListOfStringsMatchingLastWord(args, "@e", "@s");
            case 2 -> getListOfStringsMatchingLastWord(args, "1", Integer.toString(RiftGeneralConfig.creatures.maxLevel));
            default -> List.of();
        };
    }

    @Override
    public boolean isUsernameIndex(String @NotNull [] args, int index) {
        return index == 0;
    }
}
