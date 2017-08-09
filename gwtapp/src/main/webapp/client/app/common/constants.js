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