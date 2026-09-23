package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.message.RiftLibMessageWrapper;

public class RiftMessages {
    public static final RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> WRAPPER = new RiftLibMessageWrapper<>(RiftInitialize.MODID);
}
