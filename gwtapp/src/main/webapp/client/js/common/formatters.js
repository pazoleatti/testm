/**
 * Created by akuleshova on 30.05.2017.
 */
(function () {
    'use strict';

    angular.module('app.formatters', [])
        // Фильтр даты в формате dd.MM.yyyy
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
        }]);
}());