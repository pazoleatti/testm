(function() {
    'use strict';

    /**
     * @description Модуль, содержащий компонент для выбора интервала дат
     */

    angular.module('aplana.dateFromToFilter', [])
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
                templateUrl: 'client/components/dateFromToFilter/dateFromToFilter.html',
                scope: {
                    ngLabel: '@?',
                    ngPeriodModel: '=',
                    ngFromParam: '@',
                    ngToParam: '@',
                    labelWidth: "@"
                },
                link: function (scope, element, attributes) {
                    scope.ngLabel = angular.isDefined(scope.ngLabel) ? scope.ngLabel : "title.dateFromFilterLabel";
                    // Изменение пропорций метки и поля
                    scope.labelGridClass = 'span3';
                    scope.pickerGridClass = 'span9';
                    if (attributes.labelWidth !== undefined) {
                        var labelWidth = parseInt(attributes.labelWidth);
                        scope.labelGridClass = 'span' + labelWidth;
                        scope.pickerGridClass = 'span' + (12 - labelWidth);
                    }
                    scope.paragraph = 'standartParagraphForDate';
                    if (attributes.paragraph !== undefined) {
                        var paragraph = attributes.paragraph;
                        scope.paragraph = paragraph;
                    }
                }
            };
        });
} ());