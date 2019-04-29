package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Содержит перечень по операциям для которых устанавливается блокировка. Необходим поскольку сейчас блокировка устанавливается
 * в зависимости от операции, а не от типа асинхронной задачи. В будущем не исключено, что необходимость в этом перечислении отпадет.
 * Содержит поля соответствующие идентификатору типа асинхронной задачи и описанию операции. Для синхронных операций идентификатор типа асинхронной задачи равен null.
 */
@Getter
@AllArgsConstructor
public enum OperationType {

    EXCEL_DEC(AsyncTaskType.EXCEL_DEC, "Формирование xlsx"),
    PDF_DEC(AsyncTaskType.PDF_DEC, "Предварительный просмотр налоговой формы"),
    IDENTIFY_PERSON(AsyncTaskType.IDENTIFY_PERSON, "Идентификация ФЛ"),
    CONSOLIDATE(AsyncTaskType.CONSOLIDATE, "Консолидация данных в налоговую форму"),
    CHECK_DEC(AsyncTaskType.CHECK_DEC, "Проверка налоговой формы"),
    ACCEPT_DEC(AsyncTaskType.ACCEPT_DEC, "Принятие налоговой формы"),
    LOAD_TRANSPORT_FILE(AsyncTaskType.LOAD_TRANSPORT_FILE, "Загрузка данных из ТФ xml"),
    RNU_NDFL_PERSON_DB(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование отчета РНУ НДФЛ по ФЛ"),
    RNU_NDFL_PERSON_ALL_DB(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование отчета РНУ НДФЛ по всем ФЛ"),
    REPORT_KPP_OKTMO(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование \"Реестр сформированной отчетности\""),
    RNU_RATE_REPORT(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование отчета \"Отчет в разрезе ставок\""),
    RNU_PAYMENT_REPORT(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование отчета \"Отчет в разрезе платежных поручений\""),
    RNU_NDFL_DETAIL_REPORT(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование отчета \"Детализация - доходы, вычеты, налоги\""),
    RNU_NDFL_2_6_DATA_XLSX_REPORT(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование отчета \"Данные для включения в разделы 2-НДФЛ и 6-НДФЛ\""),
    RNU_NDFL_2_6_DATA_TXT_REPORT(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование файла выгрузки \"Данные для включения в разделы 2-НДФЛ и 6-НДФЛ\""),
    REPORT_2NDFL1(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование 2НДФЛ (1) по ФЛ"),
    REPORT_2NDFL2(AsyncTaskType.SPECIFIC_REPORT_DEC, "Формирование 2НДФЛ (2) по ФЛ"),
    DECLARATION_2NDFL1(AsyncTaskType.CREATE_FORMS_DEC, "Формирование ОНФ 2НДФЛ 1"),
    DECLARATION_2NDFL2(AsyncTaskType.CREATE_FORMS_DEC, "Формирование ОНФ 2НДФЛ 2"),
    DECLARATION_6NDFL(AsyncTaskType.CREATE_FORMS_DEC, "Формирование ОНФ 6НДФЛ"),
    IMPORT_DECLARATION_EXCEL(AsyncTaskType.IMPORT_DECLARATION_EXCEL, "Загрузка данных из Excel файла"),
    EXCEL_TEMPLATE_DEC(AsyncTaskType.EXCEL_TEMPLATE_DEC, "Формирование шаблона ТФ  в Excel"),
    DELETE_DEC(AsyncTaskType.DELETE_DEC, "Удаление налоговой формы"),
    UPDATE_PERSONS_DATA(AsyncTaskType.UPDATE_PERSONS_DATA, "Обновление данных физических лиц в налоговой форме"),
    EXPORT_REPORTS(AsyncTaskType.EXPORT_REPORTS, "Выгрузка отчетности"),
    UPDATE_DOC_STATE(AsyncTaskType.UPDATE_DOC_STATE, "Изменение состояния ЭД"),
    CREATE_NOTIFICATIONS_LOGS(AsyncTaskType.CREATE_NOTIFICATIONS_LOGS, "Выгрузка протоколов по оповещениям"),
    RETURN_DECLARATION(null, "Возврат формы из подготовлена/принята в создана"),
    EDIT(null, "Редактирование строки налоговой формы"),
    EDIT_FILE(null, "Добавление/удаление файлов/комментариев");

    /**
     * Идентификатор типа асинхронной задачи
     */
    private AsyncTaskType asyncTaskType;
    private String name;

    public static OperationType getOperationTypeBySubreport(String subreportAlias) {
        switch (subreportAlias) {
            case SubreportAliasConstants.REPORT_KPP_OKTMO:
                return REPORT_KPP_OKTMO;
            case SubreportAliasConstants.RNU_NDFL_PERSON_ALL_DB:
                return RNU_NDFL_PERSON_ALL_DB;
            case SubreportAliasConstants.RNU_NDFL_PERSON_DB:
                return RNU_NDFL_PERSON_DB;
            case SubreportAliasConstants.RNU_RATE_REPORT:
                return RNU_RATE_REPORT;
            case SubreportAliasConstants.RNU_PAYMENT_REPORT:
                return RNU_PAYMENT_REPORT;
            case SubreportAliasConstants.RNU_NDFL_DETAIL_REPORT:
                return RNU_NDFL_DETAIL_REPORT;
            case SubreportAliasConstants.RNU_NDFL_2_6_DATA_TXT_REPORT:
                return RNU_NDFL_2_6_DATA_TXT_REPORT;
            case SubreportAliasConstants.RNU_NDFL_2_6_DATA_XLSX_REPORT:
                return RNU_NDFL_2_6_DATA_XLSX_REPORT;
            case SubreportAliasConstants.REPORT_2NDFL1:
                return REPORT_2NDFL1;
            case SubreportAliasConstants.REPORT_2NDFL2:
                return REPORT_2NDFL2;
            default:
                throw new IllegalArgumentException("unknown subreport alias " + subreportAlias);
        }
    }

    public static OperationType getOperationByDeclarationTypeId(Integer declarationTypeId) {
        switch (declarationTypeId) {
            case DeclarationType.NDFL_2_1:
                return DECLARATION_2NDFL1;
            case DeclarationType.NDFL_2_2:
                return DECLARATION_2NDFL2;
            case DeclarationType.NDFL_6:
                return DECLARATION_6NDFL;
            default:
                throw new IllegalArgumentException("unknown declarationTypeId " + declarationTypeId);
        }
    }

}
