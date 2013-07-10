package com.aplana.sbrf.taxaccounting.web.widget.datarow.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created with IntelliJ IDEA.
 * User: Comp-1
 * Date: 10.07.13
 * Time: 15:03
 * To change this template use File | Settings | File Templates.
 */
public interface CellModifiedEventHandler extends EventHandler {
	void onCellModified(CellModifiedEvent event);
}
