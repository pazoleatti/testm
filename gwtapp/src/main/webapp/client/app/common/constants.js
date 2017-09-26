(function () {
    'use strict';

    angular
    /**
     * @Description Модуль констант
     */
        .module('app.constants', [])

        .constant('APP_CONSTANTS', {
            USER_PERMISSION: {
                VIEW_TAXES_NDFL: 1 << 0,
                VIEW_TAXES_NDFL_SETTINGS: 1 << 1,
                VIEW_TAXES_NDFL_REPORTS: 1 << 2,
                VIEW_TAXES_GENERAL: 1 << 3,
                VIEW_ADMINISTRATION_BLOCK_AND_AUDIT: 1 << 4,
                VIEW_ADMINISTRATION_USERS: 1 << 5,
                VIEW_ADMINISTRATION_CONFIG: 1 << 6,
                VIEW_ADMINISTRATION_SETTINGS: 1 << 7,
                VIEW_MANUAL_USER: 1 << 8,
                VIEW_MANUAL_DESIGNER: 1 << 9,
                VIEW_JOURNAL: 1 << 10,
                CREATE_DECLARATION_REPORT: 1 << 11,
                CREATE_DECLARATION_PRIMARY: 1 << 12,
                CREATE_DECLARATION_CONSOLIDATED: 1 << 13,
                CREATE_UPLOAD_REPORT: 1 << 14
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
                F_ROLE_CONF: "F_ROLE_CONF"
            },
            REFBOOK: {
                DEPARTMENT: 30,
                DECLARATION_TYPE: 207,
                ASNU: 900,
                ATTACH_FILE_TYPE: 934,
                PERIOD: "reportPeriod"
            },
            DECLARATION_PERMISSION: {
                CREATE: 1 << 0,
                CALCULATE: 1 << 1,
                CHECK: 1 << 2,
                ACCEPTED: 1 << 3,
                DELETE: 1 << 4,
                RETURN_TO_CREATED: 1 << 5
            },

            DOC_STATE: {
                ACCEPTED: {id: 21123700, knd: 1166002, name: 'Принят'},
                REFUSED: {id: 21123800, knd: 1166006, name: 'Отклонен'},
                REVISION: {id: 21124000, knd: 1166009, name: 'Требует уточнения'},
                SUCCESSFUL: {id: 21123900, knd: 1166007, name: 'Успешно отработан'}
            },

            NDFL_STATE: {
                CREATED: {id: 1, name: "Создана"},
                PREPARED: {id: 2, name: "Подготовлена"},
                ACCEPTED: {id: 3, name: "Принята"}
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
                ONLY_CORRECTIVE: {id: 2, name: "Только корректирующие"},
            }
        });
}());