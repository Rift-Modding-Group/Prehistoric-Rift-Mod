package anightdazingzoroark.prift.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class RiftControls {
    public static KeyBinding openParty;
    public static KeyBinding toggleAttackOrBlockBreak;
    public static KeyBinding switchUpwards;
    public static KeyBinding switchDownwards;
    public static KeyBinding quickSummonAndDismiss;
    public static KeyBinding descend;

    //for mouse buttons cause they aren't define in org.lwjgl.input, only for utility purposes
    public static final int LEFT_MOUSE = -100;
    public static final int RIGHT_MOUSE = -99;
    public static final int MIDDLE_MOUSE = -98;

    public static void init() {
        openParty = new KeyBinding("key.prift.open_party", Keyboard.KEY_J, "key.categories.gameplay");
        toggleAttackOrBlockBreak = new KeyBinding("key.prift.toggle_block_break", Keyboard.KEY_C, "key.categories.gameplay");
        switchUpwards = new KeyBinding("key.prift.switch_party_mem_up", Keyboard.KEY_UP, "key.categories.gameplay");
        switchDownwards = new KeyBinding("key.prift.switch_party_mem_down", Keyboard.KEY_DOWN, "key.categories.gameplay");
        quickSummonAndDismiss = new KeyBinding("key.prift.quick_summon_dismiss", Keyboard.KEY_K, "key.categories.gameplay");
        descend = new KeyBinding("key.prift.descend", Keyboard.KEY_X, "key.categories.gameplay");

        ClientRegistry.registerKeyBinding(openParty);
        ClientRegistry.registerKeyBinding(toggleAttackOrBlockBreak);
        ClientRegistry.registerKeyBinding(switchUpwards);
        ClientRegistry.registerKeyBinding(switchDownwards);
        ClientRegistry.registerKeyBinding(quickSummonAndDismiss);
        ClientRegistry.registerKeyBinding(descend);
    }
}
