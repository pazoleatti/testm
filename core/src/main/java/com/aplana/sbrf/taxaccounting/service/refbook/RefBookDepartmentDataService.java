package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

import java.util.List;

public interface RefBookDepartmentDataService {
    List<RefBookDepartment> fetchDepartments();
}
