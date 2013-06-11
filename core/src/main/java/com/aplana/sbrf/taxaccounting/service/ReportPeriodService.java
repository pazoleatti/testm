package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.List;

/**
 * Сервис для работы с отчетными периодами
 */
public interface ReportPeriodService {

	ReportPeriod get(int reportPeriodId);

	ReportPeriod getCurrentPeriod(TaxType taxType);

	List<ReportPeriod> listByTaxPeriod(int taxPeriodId);
}
