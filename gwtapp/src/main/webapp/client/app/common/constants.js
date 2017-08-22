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
                CREATE_DECLARATION_TAX: 1 << 12,
                CREATE_UPLOAD_REPORT: 1 << 13,
                HANDLING_FILE: 1 << 14,
                UPLOAD_FILE: 1 << 15,
                EDIT_GENERAL_PARAMS: 1 << 16,
                VIEW_REF_BOOK: 1 << 17,
                EDIT_REF_BOOK: 1 << 18
            },
            DECLARATION_PERMISSION: {
                CREATE: 1 << 0,
                VIEW: 1 << 1,
                CALCULATE: 1 << 2,
                CHECK: 1 << 3,
                ACCEPTED: 1 << 4,
                DELETE: 1 << 5,
                RETURN_TO_CREATED: 1 << 6,
                EDIT_ASSIGNMENT: 1 << 7
            },
            DECLARATION_FILE_PERMISSION: {
                DELETE: 1 << 0
            },
            REPORT_PERIOD_PERMISSION: {
                EDIT: 1 << 0
            },
            DEPARTMENT_PERMISSION: {
                EDIT: 1 << 0
            },
            LOCK_DATA_PERMISSION: {
                VIEW: 1 << 0,
                DELETE: 1 << 1
            },
            NDFL_STATS: {
                CREATED: "Создана",
                PREPARED: "Подготовлена",
                ACCEPTED: "Принята",
                NOT_EXIST: "Не создана"
            },
            NDFL_FORMKIND: {
                PRIMARY: "Первичная",
                CONSOLIDATED: "Консолидированная",
                SUMMARY: "Сводная",
                UNP: "Форма УНП",
                ADDITIONAL: "Выходная",
                CALCULATED: "Расчетная"
            }
        });
}());