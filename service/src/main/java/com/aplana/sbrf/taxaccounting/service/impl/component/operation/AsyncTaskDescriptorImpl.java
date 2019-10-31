package com.aplana.sbrf.taxaccounting.service.impl.component.operation;

import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.component.operation.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Делегирует создание описания асинхронной задачи соответствующим бинам создающим описание
 */
@Component
public class AsyncTaskDescriptorImpl implements AsyncTaskDescriptor {

    private CreateReportsAsyncTaskDescriptor createReportsAsyncTaskDescriptor;
    private TransportFileAsyncTaskDescriptor transportFileAsyncTaskDescriptor;
    private DeclarationDataReportingMultiModeAsyncTaskDescriptor declarationDataReportingMultiModeAsyncTaskDescriptor;
    private DeclarationDataService declarationDataService;

    public AsyncTaskDescriptorImpl(CreateReportsAsyncTaskDescriptor createReportsAsyncTaskDescriptor,
                                   TransportFileAsyncTaskDescriptor transportFileAsyncTaskDescriptor,
                                   DeclarationDataReportingMultiModeAsyncTaskDescriptor declarationDataReportingMultiModeAsyncTaskDescriptor,
                                   DeclarationDataService declarationDataService) {
        this.createReportsAsyncTaskDescriptor = createReportsAsyncTaskDescriptor;
        this.transportFileAsyncTaskDescriptor = transportFileAsyncTaskDescriptor;
        this.declarationDataReportingMultiModeAsyncTaskDescriptor = declarationDataReportingMultiModeAsyncTaskDescriptor;
        this.declarationDataService = declarationDataService;
    }

    /**
     * Создать описание асинхронной задачи
     *
     * @param params параметры для формирования описания
     * @return строку описания
     */
    @Override
    public String createDescription(Map<String, Object> params, OperationType operationType) {
        Long declarationDataId = null;
        Integer reportPeriodId = null;
        Integer departmentReportPeriodId = null;
        Integer declarationTypeId = null;
        String fileName = null;
        List<Long> declarationDataIds = null;
        String dates = "";
        if (params.containsKey("declarationDataId")) {
            declarationDataId = (Long) params.get("declarationDataId");
        }
        if (params.containsKey("reportPeriodId")) {
            reportPeriodId = (Integer) params.get("reportPeriodId");
        }
        if (params.containsKey("departmentReportPeriodId")) {
            departmentReportPeriodId = (Integer) params.get("departmentReportPeriodId");
        }
        if (params.containsKey("declarationTypeId")) {
            declarationTypeId = (Integer) params.get("declarationTypeId");
        }
        if (params.containsKey("fileName")) {
            fileName = (String) params.get("fileName");
        }
        if (params.containsKey("declarationDataIds")) {
            declarationDataIds = (List<Long>) params.get("declarationDataIds");
        }
        if (params.containsKey("dates")) {
            dates = (String) params.get("dates");
        }
        if (operationType.equals(OperationType.IMPORT_DECLARATION_EXCEL))
            return createDescription(declarationDataId, "Загрузка данных в ПНФ РНУ НДФЛ");
        else if (operationType.equals(OperationType.IDENTIFY_PERSON))
            return createDescription(declarationDataId, "Идентификация ФЛ");
        else if (operationType.equals(OperationType.UPDATE_PERSONS_DATA))
            return createDescription(declarationDataId, "Обновление данных ФЛ");
        else if (operationType.equals(OperationType.CHECK_DEC))
            return createDescription(declarationDataId, "Проверка налоговой формы");
        else if (operationType.equals(OperationType.ACCEPT_DEC))
            return createDescription(declarationDataId, "Принятие налоговой формы");
        else if (operationType.equals(OperationType.DELETE_DEC))
            return createDescription(declarationDataId, "Удаление налоговой формы");
        else if (operationType.equals(OperationType.CONSOLIDATE))
            return createDescription(declarationDataId, "\"Консолидация\" для формы");
        else if (operationType.equals(OperationType.EXCEL_DEC))
            return createDescription(declarationDataId, "Формирование XLSX-отчета для НФ");
        else if (operationType.equals(OperationType.EXCEL_TEMPLATE_DEC))
            return createDescription(declarationDataId, "Выгрузка данных налоговой формы в виде шаблона ТФ (Excel)");
        else if (operationType.equals(OperationType.EXCEL_UNLOAD_LIST))
            return createDescription(declarationDataId, "Выгрузка списка источники-приемники в xlsx (Excel)");
        else if (operationType.equals(OperationType.PDF_DEC))
            return createDescription(declarationDataId, "Создание формы предварительного просмотра");
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_DB))
            return createDescription(declarationDataId, "Формирование отчета \"РНУ НДФЛ по физическому лицу\"");
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_ALL_DB))
            return createDescription(declarationDataId, "Формирование отчета \"РНУ НДФЛ по всем ФЛ\"");
        else if (operationType.equals(OperationType.REPORT_KPP_OKTMO))
            return createDescription(declarationDataId, "Формирование отчета \"Реестр сформированной отчетности\"");
        else if (operationType.equals(OperationType.RNU_RATE_REPORT))
            return createDescription(declarationDataId, "Формирование отчета \"Отчет в разрезе ставок\"");
        else if (operationType.equals(OperationType.RNU_PAYMENT_REPORT))
            return createDescription(declarationDataId, "Формирование отчета \"Отчет в разрезе платёжных поручений\"");
        else if (operationType.equals(OperationType.RNU_NDFL_DETAIL_REPORT))
            return createDescription(declarationDataId, "Формирование отчета \"Детализация – доходы, вычеты, налоги\"");
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT))
            return createDescription(declarationDataId, "Формирование отчета \"Данные для включения в разделы 2-НДФЛ и 6-НДФЛ\"");
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT))
            return createDescription(declarationDataId, "Формирование файла выгрузки \"Данные для включения в разделы 2-НДФЛ и 6-НДФЛ\"");
        else if (operationType.equals(OperationType.REPORT_2NDFL1))
            return createDescription(declarationDataId, "Формирование отчета \"2-НДФЛ (1) по физическому лицу\"");
        else if (operationType.equals(OperationType.REPORT_2NDFL2))
            return createDescription(declarationDataId, "Формирование отчета \"2-НДФЛ (2) по физическому лицу\"");
        else if (operationType.equals(OperationType.DECLARATION_2NDFL1) || operationType.equals(OperationType.DECLARATION_2NDFL2) || operationType.equals(OperationType.DECLARATION_6NDFL))
            return createReportsAsyncTaskDescriptor.createDescription(departmentReportPeriodId, declarationTypeId);
        else if (operationType.equals(OperationType.DECLARATION_2NDFL_FL))
            return createReportsAsyncTaskDescriptor.createShortDescription(reportPeriodId, declarationTypeId);
        else if (operationType.equals(OperationType.EXPORT_REPORTS))
            return declarationDataReportingMultiModeAsyncTaskDescriptor.createDescription(declarationDataIds, "Выгрузка отчетности");
        else if (operationType.equals(OperationType.LOAD_TRANSPORT_FILE))
            return transportFileAsyncTaskDescriptor.createDescription(fileName);
        else if (operationType.equals(OperationType.UPDATE_DOC_STATE))
            return declarationDataReportingMultiModeAsyncTaskDescriptor.createDescription(declarationDataIds, "Изменение состояния ЭД");
        else if (operationType.equals(OperationType.CREATE_NOTIFICATIONS_LOGS))
            return String.format("Выгрузка протоколов по оповещениям за : %s", dates);
        else if (operationType.equals(OperationType.SEND_EDO))
            return declarationDataReportingMultiModeAsyncTaskDescriptor.createDescription((List<Long>) params.get("noLockDeclarationDataIds"), "Отправка ЭД в ЭДО");
        else if (operationType.equals(OperationType.DELETE_DEC_ROWS))
            return declarationDataAsyncTaskDescriptor.createDescription(declarationDataId, "Удаление строк формы");
        else {
            throw new IllegalArgumentException("Unknown operationType type!");
        }
    }

    private String createDescription(Long declarationDataId, String name) {
        return String.format("%s. Налоговая форма: %s",
                name,
                declarationDataService.getFullDeclarationDescription(declarationDataId));
    }
}
