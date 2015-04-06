package com.aplana.sbrf.taxaccounting.util.mock;

import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;

/**
 * Хэлпер подготовки заглушек для тестирования скриптов
 *
 * @author Levykin
 */
public interface ScriptTestMockHelper {
    FormDataService mockFormDataService();
    ReportPeriodService mockReportPeriodService();
    DepartmentService mockDepartmentService();
    ImportService mockImportService();
    RefBookService mockRefBookService();
    DepartmentFormTypeService mockDepartmentFormTypeService();
    RefBookFactory mockRefBookFactory();
    DepartmentReportPeriodService  mockDepartmentReportPeriodService();
    FormTypeService mockFormTypeService();
    TransactionHelper mockTransactionHelper();

    DataRowHelper getDataRowHelper();
    RefBookDataProvider getRefBookDataProvider();
    DeclarationService getDeclarationService();

}
