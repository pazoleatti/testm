package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Created by aokunev on 14.08.2017.
 */
public interface RefBookDepartmentDataDao {
    List<RefBookDepartment> fetchDepartments();
}
