package com.aplana.sbrf.taxaccounting.util.mock;

import com.aplana.sbrf.taxaccounting.service.script.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;

/**
 * Хэлпер подготовки заглушек для тестирования скриптов
 *
 * @author Levykin
 */
public interface ScriptTestMockHelper {
    FormDataService mockFormDataService();
    ReportPeriodService mockReportPeriodService();
    ImportService mockImportService();
    RefBookService mockRefBookService();
    DepartmentFormTypeService mockDepartmentFormTypeService();

    DataRowHelper getDataRowHelper();
}
