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
                VIEW_MANUAL_DESIGNER: 1 << 9
            },
            REFBOOK: {
                DEPARTMENT: 30,
                DECLARATION_TYPE: 207,
                ASNU: 900,
                ATTACH_FILE_TYPE: 934,
                PERIOD: reportPeriod
            },
            DECLARATION_PERMISSION: {
                CREATE: 1 << 0,
                CALCULATE: 1 << 1,
                CHECK: 1 << 2,
                ACCEPTED: 1 << 3,
                DELETE: 1 << 4,
                RETURN_TO_CREATED: 1 << 5
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