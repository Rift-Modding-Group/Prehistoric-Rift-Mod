package anightdazingzoroark.prift.client.ui;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class UIUtils {
    public static boolean canEscape(int keyCode) {
        return keyCode == Keyboard.KEY_ESCAPE || Minecraft.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(keyCode);
    }
}
