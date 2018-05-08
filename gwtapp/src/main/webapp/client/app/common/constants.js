(function () {
    'use strict';

    angular
    /**
     * @Description Модуль констант
     */
        .module('app.constants', [])

        .constant('APP_CONSTANTS', {
            COMMON: {
                PAGINATION: [100, 500, 1000]
            },
            USER_PERMISSION: {
                VIEW_TAXES_NDFL: 1 << 0,
                VIEW_TAXES_NDFL_SETTINGS: 1 << 1,
                VIEW_TAXES_NDFL_REPORTS: 1 << 2,
                VIEW_TAXES_GENERAL: 1 << 3,
                VIEW_ADMINISTRATION_BLOCK: 1 << 4,
                VIEW_ADMINISTRATION_CONFIG: 1 << 5,
                VIEW_ADMINISTRATION_SETTINGS: 1 << 6,
                VIEW_MANUAL_USER: 1 << 7,
                VIEW_MANUAL_DESIGNER: 1 << 8,
                VIEW_JOURNAL: 1 << 9,
                CREATE_DECLARATION_REPORT: 1 << 10,
                CREATE_DECLARATION_PRIMARY: 1 << 11,
                CREATE_DECLARATION_CONSOLIDATED: 1 << 12,
                CREATE_UPLOAD_REPORT: 1 << 13,
                HANDLING_FILE: 1 << 14,
                UPLOAD_FILE: 1 << 15,
                EDIT_GENERAL_PARAMS: 1 << 16,
                VIEW_REF_BOOK: 1 << 17,
                EDIT_REF_BOOK: 1 << 18,
                VIEW_NSI: 1 << 19,
                VIEW_TAXES: 1 << 20,
                VIEW_ADMINISTRATION_USERS: 1 << 21,
                EDIT_DECLARATION_TYPES_ASSIGNMENT: 1 << 22,
                OPEN_DEPARTMENT_REPORT_PERIOD: 1 << 23
            },
            USER_ROLE: {
                N_ROLE_OPER: "N_ROLE_OPER",
                N_ROLE_CONTROL_UNP: "N_ROLE_CONTROL_UNP",
                N_ROLE_CONTROL_NS: "N_ROLE_CONTROL_NS",
                N_ROLE_CONF: "N_ROLE_CONF",
                N_ROLE_ADMIN: "N_ROLE_ADMIN",
                F_ROLE_OPER: "F_ROLE_OPER",
                F_ROLE_CONTROL_UNP: "F_ROLE_CONTROL_UNP",
                F_ROLE_CONTROL_NS: "F_ROLE_CONTROL_NS",
                F_ROLE_CONF: "F_ROLE_CONF",
                ROLE_ADMIN: "ROLE_ADMIN"
            },
            REFBOOK: {
                DEPARTMENT: 30,
                DECLARATION_TYPE: 207,
                ASNU: 900,
                ATTACH_FILE_TYPE: 934,
                DECLARATION_DATA_TYPE_REF_BOOK: 931
            },
            DECLARATION_PERMISSION: {
                CREATE: 1 << 0,
                VIEW: 1 << 1,
                UPDATE_PERSONS_DATA: 1 << 2,
                CHECK: 1 << 3,
                ACCEPTED: 1 << 4,
                DELETE: 1 << 5,
                RETURN_TO_CREATED: 1 << 6,
                EDIT_ASSIGNMENT: 1 << 7,
                DOWNLOAD_REPORTS: 1 << 8,
                SHOW: 1 << 9,
                IMPORT_EXCEL: 1 << 10,
                EDIT: 1 << 13
            },
            DOC_STATE: {
                ACCEPTED: {id: 21123700, knd: 1166002, name: 'Принят'},
                REFUSED: {id: 21123800, knd: 1166006, name: 'Отклонен'},
                REVISION: {id: 21124000, knd: 1166009, name: 'Требует уточнения'},
                SUCCESSFUL: {id: 21123900, knd: 1166007, name: 'Успешно отработан'},
                ERROR: {id: 21124100, name: 'Ошибка'}
            },
            STATE: {
                CREATED: {id: 1, name: "Создана"},
                PREPARED: {id: 2, name: "Подготовлена"},
                ACCEPTED: {id: 3, name: "Принята"},
                NOT_EXIST: {id: 4, name: "Не создана"}
            },
            DECLARATION_FILE_PERMISSION: {
                DELETE: 1 << 0
            },
            REPORT_PERIOD_PERMISSION: {
                EDIT: 1 << 0
            },
            DEPARTMENT_REPORT_PERIOD_PERMISSION: {
                EDIT: 1 << 0,
                OPEN: 1 << 1,
                DELETE: 1 << 2,
                CLOSE: 1 << 3,
                OPEN_CORRECT: 1 << 4,
                DEADLINE: 1 << 5
            },
            DEPARTMENT_PERMISSION: {
                EDIT: 1 << 0
            },
            LOCK_DATA_PERMISSION: {
                VIEW: 1 << 0,
                DELETE: 1 << 1
            },
            CONFIGURATION_PERMISSION: {
                VIEW: 1 << 0,
                EDIT: 1 << 1,
                REMOVE: 1 << 2,
                DEFAULT: 1 << 3,
                CREATE: 1 << 4
            },
            NDFL_DECLARATION_KIND: {
                ADDITIONAL: {id: 1, name: "Выходная"},
                CONSOLIDATED: {id: 2, name: "Консолидированная"},
                PRIMARY: {id: 3, name: "Первичная"},
                SUMMARY: {id: 4, name: "Сводная"},
                UNP: {id: 5, name: "Форма УНП"},
                CALCULATED: {id: 6, name: "Расчетная"},
                REPORTS: {id: 7, name: "Отчетная"}
            },
            CORRETION_TAG: {
                ALL: {id: 0, name: "Все периоды"},
                ONLY_PRIMARY: {id: 1, name: "Только не корректирующие"},
                ONLY_CORRECTIVE: {id: 2, name: "Только корректирующие"}
            },
            //Временное решение https://jira.aplana.com/browse/SBRFNDFL-2133, убрать в рамках TODO: https://jira.aplana.com/browse/SBRFNDFL-2358
            DECLARATION_TYPE: {
                RNU_NDFL_PRIMARY: {id: 100, name: "РНУ НДФЛ (первичная)"},
                RNU_NDFL_CONSOLIDATED: {id: 101, name: "РНУ НДФЛ (консолидированная)"},
                REPORT_2_NDFL_1: {id: 102, name: "2-НДФЛ (1)"},
                REPORT_6_NDFL: {id: 103, name: "6-НДФЛ"},
                REPORT_2_NDFL_2: {id: 104, name: "2-НДФЛ (2)"}
            },
            SUBREPORT_ALIAS_CONSTANTS: {
                RNU_NDFL_PERSON_DB: "rnu_ndfl_person_db", // РНУ НДФЛ по физическому лицу
                RNU_NDFL_PERSON_ALL_DB: "rnu_ndfl_person_all_db", // РНУ НДФЛ по всем ФЛ
                REPORT_KPP_OKTMO: "report_kpp_oktmo", // Реестр сформированной отчетности
                REPORT_2NDFL: "report_2ndfl" // 2-НДФЛ (1) по физическому лицу
            },
            CREATE_ASYNC_TASK_STATUS: {
                NOT_EXIST_XML: "NOT_EXIST_XML", //не существует XML
                EXIST: "EXIST", //существует/задача успешно завершена
                LOCKED: "LOCKED", //есть блокировка
                EXIST_TASK: "EXIST_TASK", //существуют задачи, которые будут удалены при выполнении данной
                CREATE: "CREATE" //создана новая задача
            },
            USER_STORAGE: {
                NAME: "USER_STORAGE",
                KEYS: {
                    LAST_SELECTED_PERIOD: "LAST_SELECTED_PERIOD"
                }
            },
            ATTACHE_FILE_TYPE: {
                DEFAULT_TYPE_ID: 21657700
            },
            PERSON_SEARCH_FIELDS: {
                LAST_NAME: {alias: "lastName", length: 36, label: "Фамилия"},
                FIRST_NAME: {alias: "firstName", length: 36, label: "Имя"},
                MIDDLE_NAME: {alias: "middleName", length: 36, label: "Отчество"},
                INN: {alias: "inn", length: 50, label: "ИНН"},
                ID_DOC: {alias: "idDocNumber", length: 25, label: "№ ДУЛ"},
                REF_NUMBER: {alias: "refNumber", length: 10, label: "Номер справки"}
            },
            PERSON_SEARCH_FIELDS_RNU: {
                INP: {alias: "inp", length: 25, label: "ИНП"},
                SNILS: {alias: "snils", length: 14, label: "СНИЛС"},
                LAST_NAME: {alias: "lastName", length: 36, label: "Фамилия"},
                FIRST_NAME: {alias: "firstName", length: 36, label: "Имя"},
                MIDDLE_NAME: {alias: "middleName", length: 36, label: "Отчество"},
                INN: {alias: "inn", length: 50, label: "ИНН"},
                ID_DOC: {alias: "idDocNumber", length: 25, label: "№ ДУЛ"}
            },
            REPORT_PERIOD_STATUS: {
                OPEN: "OPEN", // Открыт
                CLOSE: "CLOSE", // Закрыт
                NOT_EXIST: "NOT_EXIST", // Не существует
                CORRECTION_PERIOD_ALREADY_EXIST: "CORRECTION_PERIOD_ALREADY_EXIST", // Существуют корректирующие периоды
                INVALID: "INVALID",
                CORRECTION_PERIOD_LAST_OPEN: "CORRECTION_PERIOD_LAST_OPEN",//есть более поздний открытый корректирующий период
                CORRECTION_PERIOD_NOT_CLOSE: "CORRECTION_PERIOD_NOT_CLOSE", //текущий не закрыт
                EXISTS_OPEN_CORRECTION_PERIOD_BEFORE: "EXISTS_OPEN_CORRECTION_PERIOD_BEFORE" // есть более ранний открытый корректирующий период
            },
            CONFIGURATION_PARAM_TAB: {
                COMMON_PARAM: "commonParam",
                ASYNC_PARAM: "asyncParam"
            },
            ASYNC_HANDLER_CLASS_NAME: {
                UPLOAD_REFBOOK_ASYNC_TASK: "UploadRefBookAsyncTask"
            },
            DATE_ZERO: {
                AS_DATE: "1901-01-01",
                AS_STRING: "00.00.0000"
            },
            VERSIONED_OBJECT_STATUS: {
                NORMAL: "NORMAL",
                DELETED: "DELETED",
                DRAFT: "DRAFT",
                FAKE: "FAKE"
            },
            DECLARATION_CHECK_CODE: {
                RNU_VALUE_CONDITION: "000-0007-00001",
                RNU_CITIZENSHIP: "001-0001-00002",
                RNU_SECTION_3_10: "003-0001-00002",
                RNU_SECTION_3_10_2: "003-0001-00003",
                RNU_SECTION_3_16: "003-0001-00006",
                RNU_SECTION_2_15: "004-0001-00004",
                RNU_SECTION_2_16: "004-0001-00005",
                RNU_SECTION_2_17: "004-0001-00006",
                RNU_SECTION_2_21: "004-0001-00010"
            },
            REFBOOK_TYPE: {
                LINEAR: 0,
                HIERARCHICAL: 1
            },
            REFBOOK_EDITING: {
                IS_READ_ONLY: "Только для чтения",
                NOT_IS_READ_ONLY: "Редактируемый"
            },
            REFBOOK_ALIAS: {
                BUSINESS_ID_ALIAS: "record_id",
                RECORD_VERSION_FROM_ALIAS: "record_version_from",
                RECORD_VERSION_TO_ALIAS: "record_version_to"
            }
        });
}());