package anightdazingzoroark.prift.client.newui.function;

import anightdazingzoroark.prift.server.capabilities.playerParty.IPlayerParty;

public interface PlayerPartyConsumer {
    void accept(IPlayerParty playerParty);
}
