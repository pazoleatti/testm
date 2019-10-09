package com.aplana.sbrf.taxaccounting.service.component.lock.locker;

import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.service.impl.component.lock.MainLockKeyGeneratorImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Данные для тестирования взаимосвязей блокировок.
 * Их следует поддерживать в постоянном соответствии с постановкой (000) Взаимосвязь блокировок
 * https://conf.aplana.com/pages/viewpage.action?pageId=41017175
 */
class LocksRelations {

    /**
     * Идентификатор формы, используемый в константах
     */
    static final long DECLARATION_ID = 1;
    /**
     * Кол-во всех блокировок (см. нумерацию в постановке).
     */
    static final int LOCKS_COUNT = 34;

    // Все возможные виды блокировок, в значениях ID заменен на значение DECLARATION_ID (даже для TEMPLATE, т.к. при вызове один алгоритм)
    private static final String DECLARATION_DATA_ID_IMPORT_TF_DECLARATION = "DECLARATION_DATA_1_IMPORT_TF_DECLARATION";
    private static final String IMPORT_DECLARATION_EXCEL_ID = "IMPORT_DECLARATION_EXCEL_1";
    private static final String DECLARATION_DATA_ID_IDENTIFY_PERSON = "DECLARATION_DATA_1_IDENTIFY_PERSON";
    private static final String DECLARATION_DATA_ID_UPDATE_PERSONS_DATA = "DECLARATION_DATA_1_UPDATE_PERSONS_DATA";
    private static final String DECLARATION_DATA_ID_CHECK_DECLARATION = "DECLARATION_DATA_1_CHECK_DECLARATION";
    private static final String DECLARATION_DATA_ID_EDIT = "DECLARATION_DATA_1_EDIT";
    private static final String DECLARATION_DATA_ID_ACCEPT_DECLARATION = "DECLARATION_DATA_1_ACCEPT_DECLARATION";
    private static final String DECLARATION_DATA_ID_RETURN_DECLARATION = "DECLARATION_DATA_1_RETURN_DECLARATION";
    private static final String DECLARATION_DATA_ID_CONSOLIDATE = "DECLARATION_DATA_1_CONSOLIDATE";
    private static final String DECLARATION_DATA_ID_EDIT_FILE = "DECLARATION_DATA_1_EDIT_FILE";
    private static final String DECLARATION_DATA_ID_DELETE_DECLARATION = "DECLARATION_DATA_1_DELETE_DECLARATION";
    private static final String DECLARATION_DATA_ID_XLSX = "DECLARATION_DATA_1_XLSX";
    private static final String DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB = "DECLARATION_DATA_1_RNU_NDFL_PERSON_DB";
    private static final String DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB = "DECLARATION_DATA_1_RNU_NDFL_PERSON_ALL_DB";
    private static final String EXCEL_TEMPLATE_DECLARATION_ID = "EXCEL_TEMPLATE_DECLARATION_1";
    private static final String DECLARATION_DATA_ID_RNU_RATE_REPORT = "DECLARATION_DATA_1_RNU_RATE_REPORT";
    private static final String DECLARATION_DATA_ID_RNU_PAYMENT_REPORT = "DECLARATION_DATA_1_RNU_PAYMENT_REPORT";
    private static final String DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT = "DECLARATION_DATA_1_RNU_NDFL_DETAIL_REPORT";
    private static final String DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT = "DECLARATION_DATA_1_RNU_NDFL_2_6_DATA_XLSX_REPORT";
    private static final String DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT = "DECLARATION_DATA_1_RNU_NDFL_2_6_DATA_TXT_REPORT";
    private static final String DECLARATION_DATA_ID_PDF = "DECLARATION_DATA_1_PDF";
    private static final String EXPORT_REPORTS_ID = "EXPORT_REPORTS_1";
    private static final String DECLARATION_DATA_ID_REPORT_2NDFL1 = "DECLARATION_DATA_1_REPORT_2NDFL1";
    private static final String DECLARATION_DATA_ID_REPORT_2NDFL2 = "DECLARATION_DATA_1_REPORT_2NDFL2";
    private static final String DECLARATION_DATA_ID_REPORT_KPP_OKTMO = "DECLARATION_DATA_1_REPORT_KPP_OKTMO";
    private static final String DECLARATION_TEMPLATE_ID_2NDFL1 = "DECLARATION_TEMPLATE_1_2NDFL1";
    private static final String DECLARATION_TEMPLATE_ID_2NDFL2 = "DECLARATION_TEMPLATE_1_2NDFL2";
    private static final String DECLARATION_TEMPLATE_ID_6NDFL = "DECLARATION_TEMPLATE_1_6NDFL";
    private static final String DECLARATION_TEMPLATE_ID_2NDFL_FL = "DECLARATION_TEMPLATE_1_2NDFL_FL";
    private static final String DECLARATION_DATA_ID_CHANGE_STATUS = "DECLARATION_DATA_1_CHANGE_STATUS";
    private static final String DECLARATION_DATA_ID_SEND_EDO = "DECLARATION_DATA_1_SEND_EDO";
    private static final String DECLARATION_DATA_ID_TRANSFER = "DECLARATION_DATA_1_TRANSFER_null";
    private static final String DECLARATION_DATA_ID_REPORT_LINK_DECLARATION = "DECLARATION_DATA_1_REPORT_LINK_DECLARATION";
    private static final String DECLARATION_TEMPLATE_ID_2NDFL_NEW = "DECLARATION_TEMPLATE_1_2NDFL_NEW";

    /**
     * Массив из всех блокировок
     */
    static final String[] ALL_LOCKS = {
            DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
            IMPORT_DECLARATION_EXCEL_ID,
            DECLARATION_DATA_ID_IDENTIFY_PERSON,
            DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
            DECLARATION_DATA_ID_CHECK_DECLARATION,
            DECLARATION_DATA_ID_EDIT,
            DECLARATION_DATA_ID_ACCEPT_DECLARATION,
            DECLARATION_DATA_ID_RETURN_DECLARATION,
            DECLARATION_DATA_ID_CONSOLIDATE,
            DECLARATION_DATA_ID_EDIT_FILE,
            DECLARATION_DATA_ID_DELETE_DECLARATION,
            DECLARATION_DATA_ID_XLSX,
            DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB,
            DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB,
            EXCEL_TEMPLATE_DECLARATION_ID,
            DECLARATION_DATA_ID_RNU_RATE_REPORT,
            DECLARATION_DATA_ID_RNU_PAYMENT_REPORT,
            DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT,
            DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT,
            DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT,
            DECLARATION_DATA_ID_PDF,
            EXPORT_REPORTS_ID,
            DECLARATION_DATA_ID_REPORT_2NDFL1,
            DECLARATION_DATA_ID_REPORT_2NDFL2,
            DECLARATION_DATA_ID_REPORT_KPP_OKTMO,
            DECLARATION_TEMPLATE_ID_2NDFL1,
            DECLARATION_TEMPLATE_ID_2NDFL2,
            DECLARATION_TEMPLATE_ID_6NDFL,
            DECLARATION_TEMPLATE_ID_2NDFL_FL,
            DECLARATION_DATA_ID_CHANGE_STATUS,
            DECLARATION_DATA_ID_SEND_EDO,
            DECLARATION_DATA_ID_TRANSFER,
            DECLARATION_DATA_ID_REPORT_LINK_DECLARATION,
            DECLARATION_TEMPLATE_ID_2NDFL_NEW
    };

    /**
     * Соответствие блокировок операциям, используемым в алгоритме блокировки.
     *
     * @see MainLockKeyGeneratorImpl#generateLockKey(Map, OperationType)
     */
    static final Map<String, OperationType> OPERATION_BY_LOCK = new HashMap<>();

    static {
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_IMPORT_TF_DECLARATION, OperationType.LOAD_TRANSPORT_FILE);
        OPERATION_BY_LOCK.put(IMPORT_DECLARATION_EXCEL_ID, OperationType.IMPORT_DECLARATION_EXCEL);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_IDENTIFY_PERSON, OperationType.IDENTIFY_PERSON);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_UPDATE_PERSONS_DATA, OperationType.UPDATE_PERSONS_DATA);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_CHECK_DECLARATION, OperationType.CHECK_DEC);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_ACCEPT_DECLARATION, OperationType.ACCEPT_DEC);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_DELETE_DECLARATION, OperationType.DELETE_DEC);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_CONSOLIDATE, OperationType.CONSOLIDATE);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_XLSX, OperationType.EXCEL_DEC);
        OPERATION_BY_LOCK.put(EXCEL_TEMPLATE_DECLARATION_ID, OperationType.EXCEL_TEMPLATE_DEC);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_PDF, OperationType.PDF_DEC);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_RETURN_DECLARATION, OperationType.RETURN_DECLARATION);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_EDIT, OperationType.EDIT);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_EDIT_FILE, OperationType.EDIT_FILE);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB, OperationType.RNU_NDFL_PERSON_DB);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB, OperationType.RNU_NDFL_PERSON_ALL_DB);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_REPORT_KPP_OKTMO, OperationType.REPORT_KPP_OKTMO);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_RNU_RATE_REPORT, OperationType.RNU_RATE_REPORT);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_RNU_PAYMENT_REPORT, OperationType.RNU_PAYMENT_REPORT);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT, OperationType.RNU_NDFL_DETAIL_REPORT);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT, OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT, OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_REPORT_2NDFL1, OperationType.REPORT_2NDFL1);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_REPORT_2NDFL2, OperationType.REPORT_2NDFL2);
        OPERATION_BY_LOCK.put(DECLARATION_TEMPLATE_ID_2NDFL1, OperationType.DECLARATION_2NDFL1);
        OPERATION_BY_LOCK.put(DECLARATION_TEMPLATE_ID_2NDFL2, OperationType.DECLARATION_2NDFL2);
        OPERATION_BY_LOCK.put(DECLARATION_TEMPLATE_ID_6NDFL, OperationType.DECLARATION_6NDFL);
        OPERATION_BY_LOCK.put(DECLARATION_TEMPLATE_ID_2NDFL_FL, OperationType.DECLARATION_2NDFL_FL);
        OPERATION_BY_LOCK.put(EXPORT_REPORTS_ID, OperationType.EXPORT_REPORTS);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_CHANGE_STATUS, OperationType.UPDATE_DOC_STATE);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_SEND_EDO, OperationType.SEND_EDO);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_TRANSFER, OperationType.TRANSFER);
        OPERATION_BY_LOCK.put(DECLARATION_DATA_ID_REPORT_LINK_DECLARATION, OperationType.EXCEL_UNLOAD_LIST);
        OPERATION_BY_LOCK.put(DECLARATION_TEMPLATE_ID_2NDFL_NEW, OperationType.CREATE_ANNULMENT_2NDFL);
    }

    /**
     * Взаимоисключение блокировок, таблица в постановке.
     * <p>
     * Ключ = "Блокировка, которую необходимо установить"
     * Значение = список значений из столбца "Мешающие блокировки для установки"
     */
    static final Map<String, List<String>> CONFLICTING_LOCKS = new HashMap<>();

    // Удобно просто скопировать столбец из таблицы, потом через Ctrl+R заменить "< ID >" на "ID", расставить запятые.
    static {
        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                Arrays.asList(
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_CHECK_DECLARATION,
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_XLSX,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB,
                        EXCEL_TEMPLATE_DECLARATION_ID,
                        DECLARATION_DATA_ID_RNU_RATE_REPORT,
                        DECLARATION_DATA_ID_RNU_PAYMENT_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT
                )
        );

        CONFLICTING_LOCKS.put(
                IMPORT_DECLARATION_EXCEL_ID,
                Arrays.asList(
                        EXCEL_TEMPLATE_DECLARATION_ID,
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        DECLARATION_DATA_ID_CHECK_DECLARATION,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB,
                        DECLARATION_DATA_ID_RNU_PAYMENT_REPORT,
                        DECLARATION_DATA_ID_RNU_RATE_REPORT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_XLSX,
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_EDIT
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_IDENTIFY_PERSON,
                Arrays.asList(
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        DECLARATION_DATA_ID_CHECK_DECLARATION,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB,
                        DECLARATION_DATA_ID_RNU_PAYMENT_REPORT,
                        DECLARATION_DATA_ID_RNU_RATE_REPORT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_XLSX,
                        EXCEL_TEMPLATE_DECLARATION_ID,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_EDIT
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                Arrays.asList(
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        DECLARATION_DATA_ID_CHECK_DECLARATION,
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_REPORT_KPP_OKTMO,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB,
                        DECLARATION_DATA_ID_RNU_PAYMENT_REPORT,
                        DECLARATION_DATA_ID_RNU_RATE_REPORT,
                        DECLARATION_DATA_ID_XLSX,
                        DECLARATION_TEMPLATE_ID_2NDFL1,
                        DECLARATION_TEMPLATE_ID_2NDFL2,
                        DECLARATION_TEMPLATE_ID_6NDFL,
                        DECLARATION_TEMPLATE_ID_2NDFL_FL,
                        EXCEL_TEMPLATE_DECLARATION_ID,
                        IMPORT_DECLARATION_EXCEL_ID
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_CHECK_DECLARATION,
                Arrays.asList(
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_CHANGE_STATUS
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_EDIT,
                Arrays.asList(
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        DECLARATION_DATA_ID_CHECK_DECLARATION,
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_REPORT_KPP_OKTMO,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB,
                        DECLARATION_DATA_ID_RNU_PAYMENT_REPORT,
                        DECLARATION_DATA_ID_RNU_RATE_REPORT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_XLSX,
                        DECLARATION_TEMPLATE_ID_2NDFL1,
                        DECLARATION_TEMPLATE_ID_2NDFL2,
                        DECLARATION_TEMPLATE_ID_6NDFL,
                        DECLARATION_TEMPLATE_ID_2NDFL_FL,
                        EXCEL_TEMPLATE_DECLARATION_ID,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        IMPORT_DECLARATION_EXCEL_ID
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                Arrays.asList(
                        DECLARATION_DATA_ID_CHECK_DECLARATION,
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_CHANGE_STATUS
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_RETURN_DECLARATION,
                Arrays.asList(
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        DECLARATION_DATA_ID_CHECK_DECLARATION,
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_CHANGE_STATUS,
                        DECLARATION_DATA_ID_SEND_EDO
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_CONSOLIDATE,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_RETURN_DECLARATION
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_EDIT_FILE,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_DELETE_DECLARATION,
                Arrays.asList(
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        DECLARATION_DATA_ID_CHECK_DECLARATION,
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_EDIT_FILE,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_PDF,
                        DECLARATION_DATA_ID_REPORT_2NDFL1,
                        DECLARATION_DATA_ID_REPORT_2NDFL2,
                        DECLARATION_DATA_ID_REPORT_KPP_OKTMO,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB,
                        DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB,
                        DECLARATION_DATA_ID_RNU_PAYMENT_REPORT,
                        DECLARATION_DATA_ID_RNU_RATE_REPORT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_XLSX,
                        DECLARATION_TEMPLATE_ID_2NDFL1,
                        DECLARATION_TEMPLATE_ID_2NDFL2,
                        DECLARATION_TEMPLATE_ID_6NDFL,
                        DECLARATION_TEMPLATE_ID_2NDFL_FL,
                        EXCEL_TEMPLATE_DECLARATION_ID,
                        EXPORT_REPORTS_ID,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_CHANGE_STATUS,
                        DECLARATION_DATA_ID_SEND_EDO,
                        DECLARATION_DATA_ID_REPORT_LINK_DECLARATION,
                        DECLARATION_TEMPLATE_ID_2NDFL_NEW
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_XLSX,
                Arrays.asList(
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        IMPORT_DECLARATION_EXCEL_ID
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_RNU_NDFL_PERSON_DB,
                Arrays.asList(
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        IMPORT_DECLARATION_EXCEL_ID
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_RNU_NDFL_PERSON_ALL_DB,
                Arrays.asList(
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        IMPORT_DECLARATION_EXCEL_ID
                )
        );

        CONFLICTING_LOCKS.put(
                EXCEL_TEMPLATE_DECLARATION_ID,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_EDIT
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_RNU_RATE_REPORT,
                Arrays.asList(
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_CONSOLIDATE
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_RNU_PAYMENT_REPORT,
                Arrays.asList(
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_CONSOLIDATE
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_RNU_NDFL_DETAIL_REPORT,
                Arrays.asList(
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_CONSOLIDATE
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_XLSX_REPORT,
                Arrays.asList(
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_CONSOLIDATE
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_RNU_NDFL_2_6_DATA_TXT_REPORT,
                Arrays.asList(
                        DECLARATION_DATA_ID_IMPORT_TF_DECLARATION,
                        IMPORT_DECLARATION_EXCEL_ID,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_CONSOLIDATE
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_PDF,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION
                )
        );

        CONFLICTING_LOCKS.put(
                EXPORT_REPORTS_ID,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_REPORT_2NDFL1,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_REPORT_2NDFL2,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_REPORT_KPP_OKTMO,
                Arrays.asList(
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_TEMPLATE_ID_2NDFL1,
                Arrays.asList(
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_TEMPLATE_ID_2NDFL2,
                Arrays.asList(
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_TEMPLATE_ID_6NDFL,
                Arrays.asList(
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_TEMPLATE_ID_2NDFL_FL,
                Arrays.asList(
                        DECLARATION_DATA_ID_CONSOLIDATE,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_EDIT,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_CHANGE_STATUS,
                Arrays.asList(
                        DECLARATION_DATA_ID_ACCEPT_DECLARATION,
                        DECLARATION_DATA_ID_CHECK_DECLARATION,
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_SEND_EDO
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_SEND_EDO,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_CHANGE_STATUS
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_TRANSFER,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION,
                        DECLARATION_DATA_ID_IDENTIFY_PERSON,
                        DECLARATION_DATA_ID_RETURN_DECLARATION,
                        DECLARATION_DATA_ID_UPDATE_PERSONS_DATA,
                        DECLARATION_DATA_ID_CONSOLIDATE
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_DATA_ID_REPORT_LINK_DECLARATION,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION
                )
        );

        CONFLICTING_LOCKS.put(
                DECLARATION_TEMPLATE_ID_2NDFL_NEW,
                Arrays.asList(
                        DECLARATION_DATA_ID_DELETE_DECLARATION
                )
        );
    }

    private LocksRelations() {
    }
}
