package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class RiftControls {
    public static final int MIDDLE_MOUSE = -98;

    public static final KeyBinding SWITCH_PARTY_MEMBER_UP = new KeyBinding(
            "key.prift.switch_party_mem_up", Keyboard.KEY_UP, RiftInitialize.MODNAME
    );
    public static final KeyBinding SWITCH_PARTY_MEMBER_DOWN = new KeyBinding(
            "key.prift.switch_party_mem_down", Keyboard.KEY_DOWN, RiftInitialize.MODNAME
    );
    public static final KeyBinding DEPLOY_PARTY_MEMBER = new KeyBinding(
            "key.prift.quick_summon_dismiss", Keyboard.KEY_K, RiftInitialize.MODNAME
    );
    public static final KeyBinding TOGGLE_RIDING_MOVE_HOTBAR = new KeyBinding(
            "key.prift.toggle_riding_move_hotbar", Keyboard.KEY_R, RiftInitialize.MODNAME
    );
    public static final KeyBinding TOGGLE_RIDING_BLOCK_BREAK = new KeyBinding(
            "key.prift.toggle_riding_block_break", Keyboard.KEY_C, RiftInitialize.MODNAME
    );
}
