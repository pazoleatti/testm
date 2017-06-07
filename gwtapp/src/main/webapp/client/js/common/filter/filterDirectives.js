(function() {
    'use strict';

    angular.module('sbrfNdfl.filterDirectives', [])
        /**
         * Выбор интервала дат от-до
         *
         * Атрибуты:
         *   "ng-label" - css-класс для контейнера с подписью
         *   "ng-from-model" - модель для даты "С"
         *   "ng-to-model" - модель для даты "По"
         */
        .directive('dateFromToFilter', function () {
            return {
                restrict: 'E',
                ngModel: 'ngModel',
                templateUrl: 'client/js/common/filter/dateFromToFilter.html',
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