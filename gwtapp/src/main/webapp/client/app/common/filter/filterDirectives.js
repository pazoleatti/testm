(function() {
    'use strict';

    /**
     * @description Модуль, содержащий директивы фильтров
     */

    angular.module('app.filterDirectives', [])
        /**
         * @description Директива для выбора интервала дат от-до
         *
         *   @attr ng-label - css-класс для контейнера с подписью
         *   @attr ng-from-model - модель для даты "С"
         *   @attr ng-to-model - модель для даты "По"
         *   @attr labelWidth - ширина метки в колонках bootstrap'а
         */
        .directive('dateFromToFilter', function () {
            return {
                restrict: 'E',
                ngModel: 'ngModel',
                templateUrl: 'client/app/common/filter/dateFromToFilter.html',
                scope: {
                    ngLabel: '@?',
                    ngFromModel: '=',
                    ngToModel: '=',
                    labelWidth: "@"
                },
                link: function (scope, element, attributes) {
                    scope.ngLabel = angular.isDefined(scope.ngLabel) ? scope.ngLabel : "title.dateFromFilterLabel";
                    scope.dateFromOpened = false;
                    scope.dateToOpened = false;
                    scope.dateFromSelectClick = function () {
                        scope.dateFromOpened = true;
                    };
                    scope.dateToSelectClick = function () {
                        scope.dateToOpened = true;
                        scope.dateToOptions = {
                            minDate: scope.ngFromModel,
                            maxDate: undefined
                        };
                    };
                    // Изменение пропорций метки и поля
                    scope.labelGridClass = 'col-md-3';
                    scope.pickerGridClass = 'col-md-9';
                    if (attributes.labelWidth !== undefined) {
                        var labelWidth = parseInt(attributes.labelWidth);
                        scope.labelGridClass = 'col-md-' + labelWidth;
                        scope.pickerGridClass = 'col-md-' + (12 - labelWidth);
                    }
                }
            };
        });
} ());