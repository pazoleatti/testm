/**
 * aplana-collapse (Показать, спрятать)
 * Директива предназначена для того чтобы показать/спрятать произвольный HTML элемент
 * http://localhost:8080/#/aplana_collapse
 */
(function () {
    'use strict';
    angular.module('aplana.collapse', ['aplana.utils'])
        .directive('aplanaCollapse', [ 'AplanaUtils', '$rootScope', function (AplanaUtils, $rootScope) {
            return {
                restrict: 'A',
                scope: {
                    isCollapsed: '=',
                    label: '=',
                    isAccordion: '='
                },
                replace: true,
                transclude: true,
                templateUrl:
                    // 2 шаблона первый для коллапса(иконка +/- подчёркивание),
                    // второй для аккардиона (рамка, фон, стрелочка вверх/вних)
                    function (tElement, tAttrs) {
                        if (tAttrs.isAccordion) {
                            return AplanaUtils.templatePath + 'collapse/accordion.html';
                        } else {
                            return AplanaUtils.templatePath + 'collapse/collapse.html';
                        }
                    },
                compile: function compile(tElement, tAttrs, transclude) {
                    return function (scope, element, attrs) {
                        if (angular.isUndefined(scope.isCollapsed)) {
                            scope.isCollapsed = true;
                        }

                        if (angular.isUndefined(scope.isCollapsed)) {
                            scope.isAccordion = false;
                        }

                        //элемент transclude должен остаться в своем скопе, по умолчанию под него создастся дочерний скоп в
                        //исходном
                        transclude(scope.$parent, function (clone, scope) {
                            element.find('div[data-ng-hide]').append(clone);
                        });

                        scope.$watch('isCollapsed', function () {
                            $rootScope.$broadcast('COLLAPSE_TOGGLED_MSG');
                            $rootScope.$broadcast("UPDATE_VALIDATION_MESSAGE_POSITION");
                        });

                    };
                }
            };
        } ]);
}());