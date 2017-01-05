package com.aplana.sbrf.taxaccounting.util.mock;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.*;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;

import java.util.Map;

/**
 * Хэлпер подготовки заглушек для тестирования скриптов
 *
 * @author Levykin
 */
public interface ScriptTestMockHelper {
    FormDataService mockFormDataService();
    ReportPeriodService mockReportPeriodService();
    DepartmentService mockDepartmentService();
    BookerStatementService mockBookerStatementService();
    ImportService mockImportService();
    RefBookService mockRefBookService();
    DepartmentFormTypeService mockDepartmentFormTypeService();
    RefBookFactory mockRefBookFactory();
    DepartmentReportPeriodService  mockDepartmentReportPeriodService();
    FormTypeService mockFormTypeService();
    DeclarationService mockDeclarationService();
    TransactionHelper mockTransactionHelper();
    NdflPersonService mockNdflPersonService();
    DataRowHelper getDataRowHelper();
    RefBookDataProvider getRefBookDataProvider();
    DeclarationService getDeclarationService();
    Map<Long, Map<String, RefBookValue>> getRefBookAllRecords(Long refBookId);

    RaschsvPersSvStrahLicService mockRaschsvPersSvStrahLicService();
    RaschsvObyazPlatSvService mockRaschsvObyazPlatSvService();
    RaschsvUplPerService mockRaschsvUplPerService();
    RaschsvUplPrevOssService mockRaschsvUplPrevOssService();
    RaschsvSvOpsOmsService mockRaschsvSvOpsOmsService();
    RaschsvOssVnmService mockRaschsvOssVnmService();
    RaschsvRashOssZakService mockRaschsvRashOssZakService();
}
