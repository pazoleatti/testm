package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Column;

public interface ColumnDao {
	List<Column> getFormColumns(int formId);
}
