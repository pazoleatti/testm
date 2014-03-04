package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event;

import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.LazyTreeItem;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasLazyTreeSelectionHandlers<T extends LazyTreeItem> extends HasHandlers {

    HandlerRegistration addLazyTreeSelectionHandler(LazyTreeSelectionHandler<T> handler);
}