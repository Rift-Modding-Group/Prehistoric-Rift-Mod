package anightdazingzoroark.prift.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class RiftControls {
    public static KeyBinding openParty;
    public static KeyBinding toggleAttackOrBlockBreak;
    public static KeyBinding switchLeftwards;
    public static KeyBinding switchRightwards;

    public static void init() {
        openParty = new KeyBinding("key.prift.open_party", Keyboard.KEY_J, "key.categories.gameplay");
        toggleAttackOrBlockBreak = new KeyBinding("key.prift.toggle_block_break", Keyboard.KEY_C, "key.categories.gameplay");
        switchLeftwards = new KeyBinding("key.prift.switch_party_mem_leftwards", Keyboard.KEY_LEFT, "key.categories.gameplay");
        switchRightwards = new KeyBinding("key.prift.switch_party_mem_rightwards", Keyboard.KEY_RIGHT, "key.categories.gameplay");

        ClientRegistry.registerKeyBinding(openParty);
        ClientRegistry.registerKeyBinding(toggleAttackOrBlockBreak);
        ClientRegistry.registerKeyBinding(switchLeftwards);
        ClientRegistry.registerKeyBinding(switchRightwards);
    }

    public static String getStringFromKeyBinding(KeyBinding keyBinding) {
        if (keyBinding.getKeyCode() == Keyboard.KEY_LEFT) return "<-";
        else if (keyBinding.getKeyCode() == Keyboard.KEY_RIGHT) return "->";

        return keyBinding.getDisplayName();
    }
}
