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
                CREATE_DEPARTMENT_CONFIG: 1 << 27,
                EXPORT_PERSONS: 1 << 28,
                EXPORT_DEPARTMENT_CONFIG: 1 << 29,
                IMPORT_DEPARTMENT_CONFIG: 1 << 30,
                VIEW_ADMINISTRATION: Math.pow(2, 31),
                TAX_NOTIFICATION: Math.pow(2, 32),
                _2NDFL_FL: Math.pow(2, 33),
                VIEW_TAXES_NDFL_FORMS: Math.pow(2, 34)
            },
            PERSON_PERMISSION: {
                VIEW: 1 << 0,
                VIEW_VIP_DATA: 1 << 1,
                EDIT: 1 << 2
            },
            USER_ROLE: {
                N_ROLE_OPER: "N_ROLE_OPER",
                N_ROLE_CONTROL_UNP: "N_ROLE_CONTROL_UNP",
                N_ROLE_CONTROL_NS: "N_ROLE_CONTROL_NS",
                N_ROLE_EDITOR_FL: "N_ROLE_EDITOR_FL",
                N_ROLE_CONF: "N_ROLE_CONF",
                N_ROLE_ADMIN: "N_ROLE_ADMIN",
                ROLE_ADMIN: "ROLE_ADMIN"
            },
            REFBOOK: {
                ASNU: 900,
                ATTACH_FILE_TYPE: 934,
                COUNTRY: 10,
                DECLARATION_DATA_TYPE_REF_BOOK: 931,
                DECLARATION_TYPE: 207,
                DEPARTMENT: 30,
                INCOME_CODE: 922,
                OKTMO: 96,
                PERIOD_CODE: 8,
                PERSON: 904,
                PERSON_ADDRESS: 901,
                PRESENT_PLACE: 924,
                REORGANIZATION: 928,
                SIGNATORY_MARK: 35,
                TAXPAYER_STATUS: 903,
                DOC_TYPE: 360,
                ID_DOC: 902,
                KNF_TYPE: 909,
                DEDUCTION_MARK: 927,
                DOC_STATE: 929,
                REPORT_PERIOD_IMPORT: 1040
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
                CONSOLIDATE: 1 << 12,
                EDIT: 1 << 13,
                UPDATE_DOC_STATE: 1 << 14,
                PERSON_VIEW: 1 << 16
            },
            USER_ACTIVITY: {
                YES: {id: 1, name: 'Да'},
                NO: {id: 2, name: 'Нет'}
            },
            PERSON_IMPORTANCE: {
                VIP: {id: 1, name: 'VIP'},
                NOT_VIP: {id: 2, name: 'Не VIP'}
            },
            SHOW_VERSIONS: {
                BY_DATE: {id: 1, name: "На дату"},
                ALL: {id: 2, name: "Все версии"}
            },
            SHOW_DUPLICATES: {
                NO: {id: 1, name: "Не отображать дубликаты"},
                ONLY_DUPLICATES: {id: 2, name: "Отображать только дубликаты"},
                ALL_RECORDS: {id: 3, name: "Все записи"}
            },
            STATE: {
                CREATED: {id: 1, name: "Создана"},
                PREPARED: {id: 2, name: "Подготовлена"},
                ACCEPTED: {id: 3, name: "Принята"},
                ISSUED: {id: 4, name: "Выдана"},
                NOT_EXIST: {id: 999, name: "Не создана"}
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
                REPORTS: {id: 7, name: "Отчетная"},
                REPORTS_FL: {id: 8, name: "Отчетная ФЛ"}
            },
            CORRECTION_TAG: {
                ALL: {id: 0, name: "Все периоды"},
                ONLY_PRIMARY: {id: 1, name: "Только не корректирующие"},
                ONLY_CORRECTIVE: {id: 2, name: "Только корректирующие"}
            },
            DECLARATION_TYPE: {
                RNU_NDFL_PRIMARY: {id: 100, name: "РНУ НДФЛ (первичная)"},
                RNU_NDFL_CONSOLIDATED: {id: 101, name: "РНУ НДФЛ (консолидированная)"},
                REPORT_2_NDFL_1: {id: 102, name: "2-НДФЛ (1)"},
                REPORT_6_NDFL: {id: 103, name: "6-НДФЛ"},
                REPORT_2_NDFL_2: {id: 104, name: "2-НДФЛ (2)"},
                REPORT_2_NDFL_FL: {id: 105, name: "2-НДФЛ (ФЛ)"}
            },
            SUBREPORT_ALIAS_CONSTANTS: {
                RNU_NDFL_PERSON_DB: "rnu_ndfl_person_db", // РНУ НДФЛ по физическому лицу
                RNU_NDFL_PERSON_ALL_DB: "rnu_ndfl_person_all_db", // РНУ НДФЛ по всем ФЛ
                RNU_RATE_REPORT: "rnu_rate_report", // Отчет Карманниковой: Отчет в разрезе ставок
                RNU_PAYMENT_REPORT: "rnu_payment_report", // Отчет Карманниковой: Отчет в разрезе платёжных поручений
                RNU_NDFL_2_6_DATA_XLSX_REPORT: "rnu_ndfl_2_6_data_xlsx_report", // спецотчет Данные для включения в разделы 2-НДФЛ и 6-НДФЛ (Excel)
                RNU_NDFL_2_6_DATA_TXT_REPORT: "rnu_ndfl_2_6_data_txt_report", // спецотчет Данные для включения в разделы 2-НДФЛ и 6-НДФЛ (txt)
                RNU_NDFL_DETAIL_REPORT: "rnu_ndfl_detail_report", // Спецотчет "Детализация – доходы, вычеты, налоги"
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
                LAST_NAME: {alias: "lastName", length: 60, label: "Фамилия"},
                FIRST_NAME: {alias: "firstName", length: 60, label: "Имя"},
                MIDDLE_NAME: {alias: "middleName", length: 60, label: "Отчество"},
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
            PERSON_SEARCH_FIELDS_ORIGINAL_DUPLICATES: {
                LAST_NAME: {alias: "lastName", length: 60, label: "Фамилия"},
                FIRST_NAME: {alias: "firstName", length: 60, label: "Имя"},
                MIDDLE_NAME: {alias: "middleName", length: 60, label: "Отчество"},
                RECORD_ID: {alias: "recordId", length: 10, label: "ИД ФЛ"},
                INN: {alias: "inn", length: 50, label: "ИНН"},
                ID_DOC: {alias: "idDocNumber", length: 25, label: "№ ДУЛ"},
                SNILS: {alias: "snils", length: 14, label: "СНИЛС"}
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
                REPORT_PERIOD_YEAR_MAX: "REPORT_PERIOD_YEAR_MAX",
                DOCUMENTS_SENDING_ENABLED: "DOCUMENTS_SENDING_ENABLED"
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
            },
            MODE: {
                VIEW: "VIEW",
                CREATE: "CREATE",
                EDIT: "EDIT",
                ORIGINAL: "ORIGINAL",
                DUPLICATE: "DUPLICATE"
            },
            KNF_TYPE: {
                ALL: {id: 1, name: "КНФ по всем данным"},
                BY_NONHOLDING_TAX: {id: 2, name: "КНФ по неудержанному налогу"},
                BY_KPP: {id: 3, name: "КНФ по обособленному подразделению"},
                BY_PERSON: {id: 4, name: "КНФ по ФЛ"},
                FOR_APP2: {id: 5, name: "КНФ для Приложения 2"}
            },
            TAX_REFUND_REFLECT_MODE: {
                NORMAL: {
                    id: 1,
                    name: "Показывать в строке 090 Раздела 1",
                    shortname: "В строке 090 Раздела 1",
                    enumName: "NORMAL"
                },
                AS_NEGATIVE_WITHHOLDING_TAX: {
                    id: 2,
                    name: "Учитывать возврат как отрицательное удержание в Разделе 2",
                    shortname: "Отрицательное удержание в Разделе 2",
                    enumName: "AS_NEGATIVE_WITHHOLDING_TAX"
                }
            },
            REPORT_FORM_CREATION_MODE: {
                BY_ALL_DATA: {id: 1, name: "По всем данным", enumName: "BY_ALL_DATA"},
                BY_NEW_DATA: {id: 2, name: "По новым данным", enumName: "BY_NEW_DATA"},
                UNACCEPTED_BY_FNS: {id: 3, name: "Для отчетных форм, не принятых ФНС", enumName: "UNACCEPTED_BY_FNS"}
            },
            NEGATIVE_SUMS_SIGN: {
                FROM_CURRENT_FORM: {id: 0, name: "Из текущей формы", enumName: "FROM_CURRENT_FORM"},
                FROM_PREV_FORM: {id: 1, name: "Из предыдущей формы", enumName: "FROM_PREV_FORM"}
            },
            TRANSPORT_MESSAGE_TYPE: {
                0: "Исходящее",
                1: "Входящее"
            },
            TRANSPORT_MESSAGE_CONTENT_TYPE: {
                0: "Неизвестно",
                1: "Квитанция о приёме",
                2: "Уведомление об отказе",
                3: "Уведомление об уточнении",
                4: "Извещение о вводе",
                5: "Реестр принятых документов",
                6: "Протокол приёма 2-НДФЛ",
                7: "Сообщение об ошибке",
                8: "Технологическая квитанция",
                11: "6-НДФЛ",
                12: "2-НДФЛ (1)",
                13: "2-НДФЛ (2)"
            },

            // Вид формы (SBRFNDFL-8318)
//            TRANSPORT_MESSAGE_DECLARATION_TYPE: {
//                102: "2-НДФЛ (1)",
//                104: "2-НДФЛ (2)",
//                103: "6-НДФЛ"
//            },

            TRANSPORT_MESSAGE_STATE: {
                1: "Подтверждено",
                2: "Ошибка",
                3: "Отправлено",
                4: "Получено",
                5: "Дубликат"
            },
            OPERATOR: {
                FILLED: {id: 1, name: 'Заполнено', enumName: 'FILLED', unary: true},
                BLANK: {id: 2, name: 'Не заполнено', enumName: 'BLANK', unary: true},
                HIGHER: {id: 3, name: 'Больше', enumName: 'HIGHER'},
                LOWER: {id: 4, name: 'Меньше', enumName: 'LOWER'},
                EQUAL: {id: 5, name: 'Равно', enumName: 'EQUAL'},
                UNEQUAL: {id: 6, name: 'Не равно', enumName: 'UNEQUAL'}
            }
        });
}());
