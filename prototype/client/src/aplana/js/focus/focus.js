/**
 * aplana-focus (Фокусировка)
 * Модуль создан для предоставления возможности фокусировки элемента.
 * http://localhost:8080/#/aplana_focus
 */
(function () {
    'use strict';

    angular.module('aplana.focus', [])
        .directive('aplanaFocus', ['$timeout', function ($timeout) {
            return {
                restrict: 'A',
                scope: { trigger: '@aplanaFocus' },
                link: function (scope, element) {
                    scope.$watch('trigger', function (value) {
                        if (value === "true" || Number(value) === 0) {
                            $timeout(function () {    //a timing workaround hack
                                element[0].focus();
                            });
                        }
                    });
                }
            };
        }]);
}());