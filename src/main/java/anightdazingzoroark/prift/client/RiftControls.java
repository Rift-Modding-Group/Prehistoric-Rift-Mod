package anightdazingzoroark.prift.client;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class RiftControls {
    public static final KeyBinding SWITCH_PARTY_MEMBER_UP = new KeyBinding(
            "key.prift.switch_party_mem_up", Keyboard.KEY_UP, "key.categories.gameplay"
    );
    public static final KeyBinding SWITCH_PARTY_MEMBER_DOWN = new KeyBinding(
            "key.prift.switch_party_mem_down", Keyboard.KEY_DOWN, "key.categories.gameplay"
    );
    public static final KeyBinding DEPLOY_PARTY_MEMBER = new KeyBinding(
            "key.prift.quick_summon_dismiss", Keyboard.KEY_K, "key.categories.gameplay"
    );
}
