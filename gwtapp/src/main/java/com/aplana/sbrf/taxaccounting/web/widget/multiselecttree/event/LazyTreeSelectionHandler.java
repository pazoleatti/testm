package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event;

import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.LazyTreeItem;
import com.google.gwt.event.shared.EventHandler;

public interface LazyTreeSelectionHandler<T extends LazyTreeItem> extends EventHandler {
    void onSelected(LazyTreeSelectionEvent<T> event);
}
