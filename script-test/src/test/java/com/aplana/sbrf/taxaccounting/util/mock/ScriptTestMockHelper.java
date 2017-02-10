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

    ImportService mockImportService();

    RefBookService mockRefBookService();

    RefBookPersonService mockRefBookPersonService();

    FiasRefBookService mockFiasRefBookService();

    DepartmentFormTypeService mockDepartmentFormTypeService();

    RefBookFactory mockRefBookFactory();

    DepartmentReportPeriodService mockDepartmentReportPeriodService();

    FormTypeService mockFormTypeService();

    DeclarationService mockDeclarationService();

    TransactionHelper mockTransactionHelper();

    NdflPersonService mockNdflPersonService();

    DataRowHelper getDataRowHelper();

    RefBookDataProvider getRefBookDataProvider();

    Map<Long, Map<String, RefBookValue>> getRefBookAllRecords(Long refBookId);

    ImportFiasDataService mockImportFiasDataService();

    RaschsvPersSvStrahLicService mockRaschsvPersSvStrahLicService();

    RaschsvObyazPlatSvService mockRaschsvObyazPlatSvService();

    RaschsvUplPerService mockRaschsvUplPerService();

    RaschsvUplPrevOssService mockRaschsvUplPrevOssService();

    RaschsvSvOpsOmsService mockRaschsvSvOpsOmsService();

    RaschsvOssVnmService mockRaschsvOssVnmService();

    RaschsvRashOssZakService mockRaschsvRashOssZakService();

    RaschsvVyplFinFbService mockRaschsvVyplFinFbService();

    RaschsvPravTarif31427Service mockRaschsvPravTarif31427Service();

    RaschsvPravTarif51427Service mockRaschsvPravTarif51427Service();

    RaschsvPravTarif71427Service mockRaschsvPravTarif71427Service();

    RaschsvSvPrimTarif91427Service mockRaschsvSvPrimTarif91427Service();

    RaschsvSvPrimTarif22425Service mockRaschsvSvPrimTarif22425Service();

    RaschsvSvPrimTarif13422Service mockRaschsvSvPrimTarif13422Service();

    RaschsvSvnpPodpisantService mockRaschsvSvnpPodpisantService();

    FiasRefBookService fiasRefBookService();
}
