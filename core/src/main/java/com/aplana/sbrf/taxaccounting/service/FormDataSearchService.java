package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * Интерфейс, позволяющий пользователю получать данные из базы по запросу
 * @author srybakov
 *
 */
public interface FormDataSearchService {

	List<FormData> findDataByUserIdAndFilter(Long userId, FormDataFilter formDataFilter);

	List<Department> listDepartments();

	List<FormType> listFormTypes();

	List<ReportPeriod> listReportPeriodsByTaxType(TaxType taxType);
}
