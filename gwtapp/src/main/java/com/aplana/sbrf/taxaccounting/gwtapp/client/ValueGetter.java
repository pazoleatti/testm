package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.aplana.sbrf.taxaccounting.model.FormData;

/**
 * Get a cell value from a record.
 * 
 * @param <C> the cell type
 */
public interface ValueGetter<C> {
	C getValue(FormData contact);
}