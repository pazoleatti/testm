package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;

/**
 * Get a cell value from a record.
 * 
 * @param <C> the cell type
 */
public interface ValueGetter<C> {
	C getValue(FormDataSearchResultItem contact);
}