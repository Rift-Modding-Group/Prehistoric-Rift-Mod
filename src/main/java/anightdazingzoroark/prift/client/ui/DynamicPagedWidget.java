package anightdazingzoroark.prift.client.ui;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A better PagedWidget that allows for auto resizing of pages
 */
public class DynamicPagedWidget<W extends DynamicPagedWidget<W>> extends Widget<W> {
    private final List<IWidget> pages = new ArrayList<>();
    private final Flow pageContainer = Flow.column().collapseDisabledChild().widthRel(1f).coverChildrenHeight();
    private int currentPageIndex;

    public DynamicPagedWidget() {}

    @Override
    public void afterInit() {
        this.setPage(this.currentPageIndex);
    }

    public void setPage(int page) {
        if (page < 0 || page >= this.pages.size()) {
            throw new IndexOutOfBoundsException("Setting page of " + this + " to " + page + " failed. Only values from 0 to " + (this.pages.size() - 1) + " are allowed.");
        }
        if (page == this.currentPageIndex && this.pages.get(page).isEnabled()) return;

        this.pages.get(this.currentPageIndex).setEnabled(false);
        this.currentPageIndex = page;
        this.pages.get(page).setEnabled(true);
        this.pageContainer.scheduleResize();
    }

    @Override
    @NotNull
    public List<IWidget> getChildren() {
        return Collections.singletonList(this.pageContainer);
    }

    public W addPage(IWidget widget) {
        this.pages.add(widget);
        widget.setEnabled(false);
        this.pageContainer.child(widget);
        return getThis();
    }

    public W controller(Controller controller) {
        controller.pagedWidget = this;
        return getThis();
    }

    public static class Controller {
        private DynamicPagedWidget<?> pagedWidget;

        public Controller() {}

        public void setPage(int page) {
            if (this.pagedWidget == null || !this.pagedWidget.isValid()) {
                throw new IllegalStateException("Dynamic page controller does not have a valid page container");
            }
            this.pagedWidget.setPage(page);
        }

        public int getActivePageIndex() {
            if (this.pagedWidget == null || !this.pagedWidget.isValid()) {
                throw new IllegalStateException("Dynamic page controller does not have a valid page container");
            }
            return this.pagedWidget.currentPageIndex;
        }
    }
}
