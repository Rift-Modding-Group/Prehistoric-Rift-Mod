package anightdazingzoroark.prift.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class RiftControls {
    public static KeyBinding openJournal;
    public static KeyBinding toggleAttackOrBlockBreak;

    public static void init() {
        openJournal = new KeyBinding("key.rift.open_journal", Keyboard.KEY_J, "key.categories.gameplay");
        toggleAttackOrBlockBreak = new KeyBinding("key.prift.toggle", Keyboard.KEY_C, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(openJournal);
    }
}
