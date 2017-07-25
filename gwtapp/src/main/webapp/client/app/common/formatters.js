(function () {
    'use strict';

    /**
     * @description Модуль, содержащий форматтеры
     */

    angular.module('app.formatters', [])
    /**
     * @description Фильтр даты
     *
     * @param value - значение, которое необходимо отформатировать
     * @return Дата в формате 'dd.MM.yyyy'
     */
        .filter('dateFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) {
                    return '';
                }
                if (!value.millis) {
                    return $filter('date')(value, 'dd.MM.yyyy');
                }
                else {
                    return $filter('date')(value.millis, 'dd.MM.yyyy');
                }
            };
        }])

        /**
         * @description Фильтр даты и времени
         *
         * @param value - значение, которое необходимо отформатировать
         * @return Дата и время в формате 'dd.MM.yyyy HH:mm:ss'
         */
        .filter('dateTimeFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) {
                    return '';
                }
                if (!value.millis) {
                    return $filter('date')(value, 'dd.MM.yyyy HH:mm:ss');
                }
                else {
                    return $filter('date')(value.millis, 'dd.MM.yyyy HH:mm:ss');
                }
            };
        }]);
}());