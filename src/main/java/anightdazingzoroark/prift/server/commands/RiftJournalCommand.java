package anightdazingzoroark.prift.server.commands;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.message.RiftJournalEditAll;
import anightdazingzoroark.prift.server.message.RiftJournalEditOne;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class RiftJournalCommand extends CommandBase {
    @Override
    public String getName() {
        return "priftjournal";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "priftcommands.journal.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (getEntity(server, sender, args[0]) instanceof EntityPlayer) {
            EntityPlayer target = (EntityPlayer)getEntity(server, sender, args[0]);
            if (args.length <= 1) throw new WrongUsageException("priftcommands.journal.usage", new Object[0]);
            else if (args.length == 2) {
                if (args[1].equals("clearall")) {
                    IPlayerJournalProgress journalProgress = target.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
                    //add server side
                    journalProgress.resetEntries();
                    //add client side
                    RiftMessages.WRAPPER.sendToAll(new RiftJournalEditAll(false));

                    notifyCommandListener(sender, this, "priftcommands.journal.clear_all_success", target.getDisplayName());
                }
                else if (args[1].equals("unlockall")) {
                    IPlayerJournalProgress journalProgress = target.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
                    //add server side
                    for (RiftCreatureType creatureType : RiftCreatureType.values()) {
                        if (!journalProgress.getUnlockedCreatures().contains(creatureType)) {
                            journalProgress.unlockCreature(creatureType);
                        }
                    }
                    //add client side
                    RiftMessages.WRAPPER.sendToAll(new RiftJournalEditAll(true));

                    notifyCommandListener(sender, this, "priftcommands.journal.add_all_success", target.getDisplayName());
                }
                else if (args[1].equals("unlock")) throw new WrongUsageException("priftcommands.journal.unlock_entry_blank", new Object[0]);
                else if (args[1].equals("clear")) throw new WrongUsageException("priftcommands.journal.clear_entry_blank", new Object[0]);
                else throw new WrongUsageException("priftcommands.journal.usage", new Object[0]);
            }
            else if (args.length == 3) {
                if (args[1].equals("unlock")) {
                    IPlayerJournalProgress journalProgress = target.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
                    RiftCreatureType creatureType = RiftCreatureType.safeValOf(args[2].toUpperCase());
                    if (creatureType != null) {
                        if (!journalProgress.getUnlockedCreatures().contains(creatureType)) {
                            //add server side
                            journalProgress.unlockCreature(creatureType);
                            //add client side
                            RiftMessages.WRAPPER.sendToAll(new RiftJournalEditOne(creatureType, true));
                            notifyCommandListener(sender, this, "priftcommands.journal.add_success", creatureType.getTranslatedName(), target.getDisplayName());
                        }
                        else throw new CommandException("priftcommands.journal.entry_already_exists", creatureType.getTranslatedName(), target.getDisplayName());
                    }
                    else throw new WrongUsageException("priftcommands.journal.invalid_entry", args[2]);
                }
                else if (args[1].equals("clear")) {
                    IPlayerJournalProgress journalProgress = target.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
                    RiftCreatureType creatureType = RiftCreatureType.safeValOf(args[2].toUpperCase());
                    if (creatureType != null) {
                        if (journalProgress.getUnlockedCreatures().contains(creatureType)) {
                            //add server side
                            journalProgress.clearCreature(creatureType);
                            //add client side
                            RiftMessages.WRAPPER.sendToAll(new RiftJournalEditOne(creatureType, false));
                            notifyCommandListener(sender, this, "priftcommands.journal.clear_success", creatureType.getTranslatedName(), target.getDisplayName());
                        }
                        else throw new CommandException("priftcommands.journal.entry_already_cleared", creatureType.getTranslatedName(), target.getDisplayName());
                    }
                    else throw new WrongUsageException("priftcommands.journal.invalid_entry", args[2]);
                }
                else throw new WrongUsageException("priftcommands.journal.invalid_value", new Object[0]);
            }
        }
        else throw new WrongUsageException("priftcommands.journal.usage", new Object[0]);
    }
}
