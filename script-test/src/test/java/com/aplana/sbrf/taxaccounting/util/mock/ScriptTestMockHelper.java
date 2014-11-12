package com.aplana.sbrf.taxaccounting.util.mock;

import com.aplana.sbrf.taxaccounting.service.script.FormDataService;
import com.aplana.sbrf.taxaccounting.service.script.ImportService;
import com.aplana.sbrf.taxaccounting.service.script.RefBookService;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;
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
    DataRowHelper getDataRowHelper();
}
