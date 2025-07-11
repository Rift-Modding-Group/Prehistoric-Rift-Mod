package anightdazingzoroark.prift.client.ui.elements;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiftGuiScrollableSectionContents {
    private final List<Element> list = new ArrayList<>();
    private int totalHeight;

    private void getTotalHeight() {}

    public RiftGuiScrollableSectionContents addTextElement(TextElement element) {
        this.list.add(element);
        return this;
    }

    public RiftGuiScrollableSectionContents addImageElement(ImageElement element) {
        this.list.add(element);
        return this;
    }

    public RiftGuiScrollableSectionContents addButtonElement(ButtonElement element) {
        this.list.add(element);
        return this;
    }

    public RiftGuiScrollableSectionContents addTabElement(TabElement element) {
        this.list.add(element);
        return this;
    }

    public List<Element> getContents() {
        return this.list;
    }

    public static class Element {}

    public static class TextElement extends Element {
        private String contents;
        private int textColor = 0x000000;

        public TextElement setContents(String value) {
            this.contents = value;
            return this;
        }

        public TextElement setTextColor(int color) {
            this.textColor = color;
            return this;
        }

        public String getContents() {
            return this.contents;
        }

        public int getTextColor() {
            return this.textColor;
        }
    }

    public static class ImageElement extends Element {
        private ResourceLocation imageLocation;
        private int[] imageSize;
        private float imageScale = 1f;

        public ImageElement setImageLocation(ResourceLocation imageLocation) {
            this.imageLocation = imageLocation;
            return this;
        }

        public ResourceLocation getImageLocation() {
            return this.imageLocation;
        }

        public ImageElement setImageSize(int width, int height) {
            this.imageSize = new int[]{width, height};
            return this;
        }

        public int[] getImageSize() {
            return this.imageSize;
        }

        public ImageElement setImageScale(float scale) {
            this.imageScale = scale;
            return this;
        }

        public float getImageScale() {
            return this.imageScale;
        }
    }

    public static class ButtonElement extends Element {
        private String name = ""; //display name
        private String id = ""; //id, which is to be used for performing operations or changing stuff among other things
        private int[] size = {60, 20};
        private int bottomSpace;

        public ButtonElement setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return this.name;
        }

        public ButtonElement setId(String id) {
            this.id = id;
            return this;
        }

        public String getId() {
            return this.id;
        }

        public ButtonElement setSize(int width, int height) {
            this.size = new int[]{width, height};
            return this;
        }

        public int[] getSize() {
            return this.size;
        }

        public ButtonElement setBottomSpaceSize(int value) {
            this.bottomSpace = value;
            return this;
        }

        public int getBottomSpaceSize() {
            return this.bottomSpace;
        }
    }

    public static class TabElement extends Element {
        private String id = "";
        private final Map<String, Element> tabContents = new HashMap<>();

        public TabElement setId(String id) {
            this.id = id;
            return this;
        }

        public String getId() {
            return this.id;
        }

        public TabElement addTab(String name, Element contents) {
            this.tabContents.put(name, contents);
            return this;
        }

        public Map<String, Element> getTabContents() {
            return this.tabContents;
        }
    }

    public static class ItemListElement extends Element {
        private String headerText;
        private int headerTextColor = 0x000000;
    }
}
