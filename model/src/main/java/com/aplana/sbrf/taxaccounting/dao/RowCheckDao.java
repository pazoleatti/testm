package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

public interface RowCheckDao {
	List<RowCheck> getFormRowChecks(int formId);
}
