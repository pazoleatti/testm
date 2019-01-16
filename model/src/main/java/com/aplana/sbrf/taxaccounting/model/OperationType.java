package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Содержит перечень по операциям для которых устанавливается блокировка. Необходим поскольку сейчас блокировка устанавливается
 * в зависимости от операции, а не от типа асинхронной задачи. В будущем не исключено, что необходимость в этом перечислении отпадет.
 * Содержит поля соответствующие идентификатору типа асинхронной задачи и описанию операции. Для синхронных операций идентификатор типа асинхронной задачи равен null.
 */
@Getter @AllArgsConstructor
public enum OperationType {

    LOAD_TRANSPORT_FILE(12L, "Загрузка данных из ТФ xml"),
    IMPORT_DECLARATION_EXCEL(30L, "Загрузка данных из Excel файла"),
    IDENTIFY_PERSON(8L, "Идентификация ФЛ"),
    UPDATE_PERSONS_DATA(34L, "Обновление данных физических лиц в налоговой форме"),
    CHECK_DEC(14L, "Проверка налоговой формы"),
    ACCEPT_DEC(15L, "Принятие налоговой формы"),
    DELETE_DEC(32L, "Удаление налоговой формы"),
    CONSOLIDATE(9L, "Консолидация данных в налоговую форму"),
    EXCEL_DEC(5L, "Формирование xlsx"),
    EXCEL_TEMPLATE_DEC(31L, "Формирование шаблона ТФ  в Excel"),
    DECLARATION_2NDFL1(28L, "Формирование ОНФ 2НДФЛ 1"),
    DECLARATION_2NDFL2(28L, "Формирование ОНФ 2НДФЛ 2"),
    DECLARATION_6NDFL(28L, "Формирование ОНФ 6НДФЛ"),
    PDF_DEC(7L, "Выгрузка данных из формы в PDF формате"),
    DEPT_NOTICE_DEC(35L, "Формирование уведомления о задолженности"),
    EXPORT_REPORTS(29L, "Выгрузка отчетности"),
    RETURN_DECLARATION(null, "Возврат формы из подготовлена/принята в создана"),
    EDIT(null, "Редактирование строки налоговой формы"),
    EDIT_FILE(null, "Добавление/удаление файлов/комментариев"),
    RNU_NDFL_PERSON_DB(26L, "Формирование отчета РНУ НДФЛ по ФЛ"),
    RNU_NDFL_PERSON_ALL_DB(26L, "Формирование отчета РНУ НДФЛ по всем ФЛ"),
    REPORT_KPP_OKTMO(26L,"Формирование \"Реестр сформированной отчетности\""),
    RNU_RATE_REPORT(26L,"Формирование отчета \"Отчет в разрезе ставок\""),
    RNU_PAYMENT_REPORT(26L,"Формирование отчета \"Отчет в разрезе платежных поручений\""),
    RNU_NDFL_DETAIL_REPORT(26L, "Формирование отчета \"Детализация - доходы, вычеты, налоги\""),
    RNU_NDFL_2_6_DATA_XLSX_REPORT(26L,"Формирование отчета \"Данные для включения в разделы 2-НДФЛ и 6-НДФЛ\""),
    RNU_NDFL_2_6_DATA_TXT_REPORT(26L, "Формирование файла выгрузки \"Данные для включения в разделы 2-НДФЛ и 6-НДФЛ\""),
    REPORT_2NDFL1(26L, "Формирование 2НДФЛ (1) по ФЛ"),
    REPORT_2NDFL2(26L, "Формирование 2НДФЛ (2) по ФЛ"),
    UPDATE_DOC_STATE(42L, "Изменение состояния ЭД");

    /**
     * Идентификатор типа асинхронной задачи
     */
    private Long asyncTaskTypeId;
    private String name;

    public static OperationType getOperationTypeBySubreport(String subreportAlias) {
        switch (subreportAlias) {
            case SubreportAliasConstants.REPORT_KPP_OKTMO: return REPORT_KPP_OKTMO;
            case SubreportAliasConstants.RNU_NDFL_PERSON_ALL_DB: return RNU_NDFL_PERSON_ALL_DB;
            case SubreportAliasConstants.RNU_NDFL_PERSON_DB: return RNU_NDFL_PERSON_DB;
            case SubreportAliasConstants.RNU_RATE_REPORT: return RNU_RATE_REPORT;
            case SubreportAliasConstants.RNU_PAYMENT_REPORT: return RNU_PAYMENT_REPORT;
            case SubreportAliasConstants.RNU_NDFL_DETAIL_REPORT: return RNU_NDFL_DETAIL_REPORT;
            case SubreportAliasConstants.RNU_NDFL_2_6_DATA_TXT_REPORT: return RNU_NDFL_2_6_DATA_TXT_REPORT;
            case SubreportAliasConstants.RNU_NDFL_2_6_DATA_XLSX_REPORT: return RNU_NDFL_2_6_DATA_XLSX_REPORT;
            case SubreportAliasConstants.REPORT_2NDFL1: return REPORT_2NDFL1;
            case SubreportAliasConstants.REPORT_2NDFL2: return REPORT_2NDFL2;
            case SubreportAliasConstants.DEPT_NOTICE_DEC: return DEPT_NOTICE_DEC;
            default: throw new IllegalArgumentException("unknown subreport alias " + subreportAlias);
        }
    }

    public static OperationType getOperationByDeclarationTypeId(Integer declarationTypeId) {
        switch (declarationTypeId) {
            case DeclarationType.NDFL_2_1: return DECLARATION_2NDFL1;
            case DeclarationType.NDFL_2_2: return DECLARATION_2NDFL2;
            case DeclarationType.NDFL_6: return DECLARATION_6NDFL;
            default: throw new IllegalArgumentException("unknown declarationTypeId " + declarationTypeId);
        }
    }

}
