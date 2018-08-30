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
                OPEN_DEPARTMENT_REPORT_PERIOD: 1 << 23,
                VIEW_TAXES_SERVICE: 1 << 25,
                VIEW_TAXES_CREATE_APPLICATION_2: 1 << 26,
                CREATE_DEPARTMENT_CONFIG: 1 << 27
            },
            USER_ROLE: {
                N_ROLE_OPER: "N_ROLE_OPER",
                N_ROLE_CONTROL_UNP: "N_ROLE_CONTROL_UNP",
                N_ROLE_CONTROL_NS: "N_ROLE_CONTROL_NS",
                N_ROLE_CONF: "N_ROLE_CONF",
                N_ROLE_ADMIN: "N_ROLE_ADMIN",
                ROLE_ADMIN: "ROLE_ADMIN"
            },
            USER_ROLE_OBJECT: {
                ROLE_ADMIN: {id: 5, alias: "ROLE_ADMIN", name: "Администратор"},
                N_ROLE_OPER: {id: 11, alias: "N_ROLE_OPER", name: "Оператор (НДФЛ)"},
                N_ROLE_CONTROL_UNP: {id: 12, alias: "N_ROLE_CONTROL_UNP", name: "Контролёр УНП (НДФЛ)"},
                N_ROLE_CONF: {id: 13, alias: "N_ROLE_CONF", name: "Настройщик (НДФЛ)"},
                N_ROLE_CONTROL_NS: {id: 15, alias: "N_ROLE_CONTROL_NS", name: "Контролёр НС (НДФЛ)"},
                N_ROLE_OPER_1000: {id: 20, alias: "N_ROLE_OPER_1000", name: "АС «SAP» (НДФЛ)"},
                N_ROLE_OPER_2000: {id: 21, alias: "N_ROLE_OPER_2000", name: "АИС «Дивиденд» (НДФЛ)"},
                N_ROLE_OPER_3000: {id: 22, alias: "N_ROLE_OPER_3000", name: "АС «Diasoft Custody 5NT» (НДФЛ)"},
                N_ROLE_OPER_4000: {id: 23, alias: "N_ROLE_OPER_4000", name: "АС «Инфобанк» (НДФЛ)"},
                N_ROLE_OPER_5000: {id: 24, alias: "N_ROLE_OPER_5000", name: "АИС «Депозитарий» (НДФЛ)"},
                N_ROLE_OPER_6000: {id: 25, alias: "N_ROLE_OPER_6000", name: "Материальная выгода. Кредиты_АС «ЕКП» (НДФЛ)"},
                N_ROLE_OPER_6001: {id: 26, alias: "N_ROLE_OPER_6001", name: "Экономическая выгода. Кредиты_АС «ЕКП» (НДФЛ)"},
                N_ROLE_OPER_7000: {id: 27, alias: "N_ROLE_OPER_7000", name: "Экономическая выгода. Карты_ АС «ИПС БК» (НДФЛ)"},
                N_ROLE_OPER_6002: {id: 28, alias: "N_ROLE_OPER_6002", name: "Экономическая выгода. Комиссии_АС «ЕКП» (НДФЛ)"},
                N_ROLE_OPER_6003: {id: 29, alias: "N_ROLE_OPER_6003", name: "Реструктуризация валютных кредитов_АС «ЕКП» (НДФЛ)"},
                N_ROLE_OPER_6004: {id: 30, alias: "N_ROLE_OPER_6004", name: "Прощение долга (амнистия). Кредиты_АС «ЕКП» (НДФЛ)"},
                N_ROLE_OPER_6005: {id: 31, alias: "N_ROLE_OPER_6005", name: "Выплаты клиентам по решениям суда_АС «ЕКП» (НДФЛ)"},
                N_ROLE_OPER_1001: {id: 32, alias: "N_ROLE_OPER_1001", name: "Призы, подарки клиентам_АС «SAP» (НДФЛ)"},
                N_ROLE_OPER_8000: {id: 33, alias: "N_ROLE_OPER_8000", name: "АС «Back Office» (НДФЛ)"},
                N_ROLE_OPER_9000: {id: 34, alias: "N_ROLE_OPER_9000", name: "АС «ЕКС» (НДФЛ)"},
                N_ROLE_OPER_ALL: {id: 35, alias: "N_ROLE_OPER_ALL", name: "Все АСНУ (НДФЛ)"}
            },
            REFBOOK: {
                DEPARTMENT: 30,
                DECLARATION_TYPE: 207,
                ASNU: 900,
                ATTACH_FILE_TYPE: 934,
                DECLARATION_DATA_TYPE_REF_BOOK: 931,
                PERSON: 904,
                OKTMO: 96,
                INCOME_CODE: 922,
                PERSON_ADDRESS: 901,
                TAXPAYER_STATUS: 903,
                PERIOD_CODE: 8,
                PRESENT_PLACE: 924,
                SIGNATORY_MARK: 35,
                REORGANIZATION: 928
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
                IDENTIFY: 1 << 11,
                EDIT: 1 << 13
            },
            USER_ACTIVITY: {
                YES: {id: 1, name: "Да"},
                NO: {id: 2, name: "Нет"}
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
                OPEN: 1 << 0,
                DELETE: 1 << 1,
                CLOSE: 1 << 2,
                OPEN_CORRECT: 1 << 3,
                REOPEN: 1 << 4
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
            CORRECTION_TAG: {
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
                REPORT_2NDFL: "report_2ndfl", // 2-НДФЛ (1) по физическому лицу
                DEPT_NOTICE: "DEPT_NOTICE" // Уведомление о задолженности
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
                ASYNC_PARAM: "asyncParam",
                EMAIL_PARAM: "emailParam"
            },
            CONFIGURATION_PARAM: {
                REPORT_PERIOD_YEAR_MIN: "REPORT_PERIOD_YEAR_MIN",
                REPORT_PERIOD_YEAR_MAX: "REPORT_PERIOD_YEAR_MAX"
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
            REFBOOK_EDITING: {
                IS_READ_ONLY: "Только для чтения",
                NOT_IS_READ_ONLY: "Редактируемый"
            },
            EVENTS: {
                DEPARTMENT_SELECTED: "DEPARTMENT_SELECTED",
                LAST_PERIOD_SELECT: "LAST_PERIOD_SELECT",
                DEPARTMENT_AND_PERIOD_SELECTED: "DEPARTMENT_AND_PERIOD_SELECTED"
            },
            REFBOOK_ALIAS: {
                BUSINESS_ID_ALIAS: "record_id",
                RECORD_VERSION_FROM_ALIAS: "record_version_from",
                RECORD_VERSION_TO_ALIAS: "record_version_to"
            },
            REFBOOK_EM_TO_PX_CONVERSION_INDEX: 20,
            NEGATIVE_VALUE_ADJUSTMENT: {
                NOT_CORRECT: {id: 0, name: "Не корректировать"},
                CORRECT: {id: 1, name: "Корректировать"}
            },
            URM: {
                CURRENT_TB: {id: 1, name: "Данные текущего ТБ", enumName: "CURRENT_TB"},
                OTHERS_TB: {id: 2, name: "Данные других ТБ", enumName: "OTHERS_TB"},
                NONE_TB: {id: 3, name: "Данные, не указанные ни для одного ТБ", enumName: "NONE_TB"}
            },
            DEPARTMENT_CONFIG_RELEVANCE_SELECT: {
                DATE: {id: 1, name: "На дату"},
                ALL: {id: 2, name: "Все настройки"}
            },
            DEPARTMENT_CONFIG_PERMISSION: {
                UPDATE: 1 << 2,
                DELETE: 1 << 3
            }
        });
}());