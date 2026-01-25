package anightdazingzoroark.prift.client.newui.custom;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

//this is similar to PagedWidget, but the key difference is that it actively
//removes pages when switching instead of just hiding and showing
public class DynamicPagedWidget<W extends DynamicPagedWidget<W>> extends Widget<W> {
    private final List<IWidget> pages = new ArrayList<>();
    private final ParentWidget<?> parentWidget = new ParentWidget<>().coverChildren().debugName("dynamicPage");
    private IWidget currentPage;
    private int currentPageIndex = 0;
    private IntConsumer onPageChange;

    @Override
    public void afterInit() {
        this.setPage(this.currentPageIndex);
    }

    public W onPageChange(@Nullable IntConsumer onPageChange) {
        this.onPageChange = onPageChange;
        return getThis();
    }

    public void setPage(int page) {
        if (page < 0 || page >= this.pages.size()) {
            throw new IndexOutOfBoundsException("Setting page of " + this + " to " + page + " failed. Only values from 0 to " + (this.pages.size() - 1) + " are allowed.");
        }
        this.currentPageIndex = page;
        if (this.currentPage != null) {
            this.parentWidget.remove(this.currentPage);
        }
        this.currentPage = this.pages.get(this.currentPageIndex);
        this.parentWidget.child(this.currentPage);
        this.parentWidget.scheduleResize();

        if (this.onPageChange != null) {
            this.onPageChange.accept(page);
        }
    }

    public void nextPage() {
        if (++this.currentPageIndex == this.pages.size()) {
            this.currentPageIndex = 0;
        }
        this.setPage(this.currentPageIndex);
    }

    public void previousPage() {
        if (--this.currentPageIndex == -1) {
            this.currentPageIndex = this.pages.size() - 1;
        }
        this.setPage(this.currentPageIndex);
    }

    public List<IWidget> getPages() {
        return this.pages;
    }

    public IWidget getCurrentPage() {
        return this.currentPage;
    }

    public int getCurrentPageIndex() {
        return this.currentPageIndex;
    }

    @Override
    public List<IWidget> getChildren() {
        return Collections.singletonList(this.parentWidget);
    }

    public W initialPage(int page) {
        if (!isValid()) {
            this.currentPageIndex = page;
        }
        return getThis();
    }

    public W addPage(IWidget widget) {
        this.pages.add(widget);
        //widget.setEnabled(false);
        return getThis();
    }

    public W controller(Controller controller) {
        controller.setPagedWidget(this);
        return getThis();
    }

    public static class Controller {

        private DynamicPagedWidget<?> pagedWidget;

        public boolean isInitialised() {
            return this.pagedWidget != null && this.pagedWidget.isValid();
        }

        private void validate() {
            if (!isInitialised()) {
                throw new IllegalStateException("PagedWidget controller does not have a valid PagedWidget! Current PagedWidget: " + this.pagedWidget);
            }
        }

        private void setPagedWidget(DynamicPagedWidget<?> pagedWidget) {
            this.pagedWidget = pagedWidget;
        }

        public void setPage(int page) {
            validate();
            this.pagedWidget.setPage(page);
        }

        public void nextPage() {
            validate();
            this.pagedWidget.nextPage();
        }

        public void previousPage() {
            validate();
            this.pagedWidget.previousPage();
        }

        public IWidget getActivePage() {
            validate();
            return this.pagedWidget.getCurrentPage();
        }

        public int getActivePageIndex() {
            validate();
            return this.pagedWidget.getCurrentPageIndex();
        }
    }
}
