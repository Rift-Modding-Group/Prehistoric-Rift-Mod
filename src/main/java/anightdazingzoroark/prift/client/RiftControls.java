package anightdazingzoroark.prift.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class RiftControls {
    public static KeyBinding mountDescend;
    public static KeyBinding mountAscend;
    public static KeyBinding openJournal;

    public static void init() {
        mountDescend = new KeyBinding("key.rift.mount_descend", Keyboard.KEY_C, "key.categories.gameplay");
        mountAscend = new KeyBinding("key.rift.mount_ascend", Keyboard.KEY_SPACE, "key.categories.gameplay");
        openJournal = new KeyBinding("key.rift.open_journal", Keyboard.KEY_J, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(mountDescend);
        ClientRegistry.registerKeyBinding(mountAscend);
        ClientRegistry.registerKeyBinding(openJournal);
    }
}
