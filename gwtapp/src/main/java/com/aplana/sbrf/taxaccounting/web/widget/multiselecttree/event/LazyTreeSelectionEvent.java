package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.LazyTreeItem;
import com.google.gwt.event.shared.GwtEvent;

public class LazyTreeSelectionEvent<T extends LazyTreeItem> extends GwtEvent<LazyTreeSelectionHandler<T>> {

    /**
     * Handler type.
     */
    private static Type<LazyTreeSelectionHandler<?>> TYPE;

    /**
     * Fires a selection event on all registered handlers in the handler
     * manager.If no such handlers exist, this method will do nothing.
     *
     * @param <T> the selected item type
     * @param source the source of the handlers
     * @param selectedItem the selected item
     */
    public static <T extends LazyTreeItem> void fire(HasLazyTreeSelectionHandlers<T> source, T selectedItem) {
        if (TYPE != null) {
            LazyTreeSelectionEvent<T> event = new LazyTreeSelectionEvent<T>(selectedItem);
            source.fireEvent(event);
        }
    }

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<LazyTreeSelectionHandler<?>> getType() {
        if (TYPE == null) {
            TYPE = new Type<LazyTreeSelectionHandler<?>>();
        }
        return TYPE;
    }

    private final T selectedItem;

    /**
     * Creates a new selection event.
     *
     * @param selectedItem selected item
     */
    protected LazyTreeSelectionEvent(T selectedItem) {
        this.selectedItem = selectedItem;
    }

    // The instance knows its BeforeSelectionHandler is of type I, but the TYPE
    // field itself does not, so we have to do an unsafe cast here.
    @SuppressWarnings("unchecked")
    @Override
    public final Type<LazyTreeSelectionHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    /**
     * Gets the selected item.
     *
     * @return the selected item
     */
    public T getSelectedItem() {
        return selectedItem;
    }

    @Override
    protected void dispatch(LazyTreeSelectionHandler<T> handler) {
        handler.onSelected(this);
    }
}
