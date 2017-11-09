package com.aplana.sbrf.taxaccounting.util.mock;

import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.script.service.*;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;

import java.util.Map;

/**
 * Хэлпер подготовки заглушек для тестирования скриптов
 *
 * @author Levykin
 */
public interface ScriptTestMockHelper {
    ReportPeriodService mockReportPeriodService();

    DepartmentService mockDepartmentService();

    ImportService mockImportService();

    RefBookService mockRefBookService();

    RefBookPersonService mockRefBookPersonService();

    FiasRefBookService mockFiasRefBookService();

    RefBookFactory mockRefBookFactory();

    DepartmentReportPeriodService mockDepartmentReportPeriodService();

    DeclarationService mockDeclarationService();

    TransactionHelper mockTransactionHelper();

    NdflPersonService mockNdflPersonService();

    RefBookDataProvider getRefBookDataProvider();

    Map<Long, Map<String, RefBookValue>> getRefBookAllRecords(Long refBookId);

    ImportFiasDataService mockImportFiasDataService();

    FiasRefBookService fiasRefBookService();

    BlobDataService mockBlobDataService();
}
