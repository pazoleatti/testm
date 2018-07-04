(function () {
    'use strict';

    /**
     * Директива для элемента изменения масштаба.
     * Позволяет увеличить, уменьшить, растянуть по ширине или задать конкретный масшаб в процентах из выпадающего списка для элемента DOM (например, img)
     * путем изменения атрибутов height и width этого элемента
     */
    angular.module("app.resizer", [])
        .directive('resizer', [function () {
            return {
                restrict: 'EA',
                templateUrl: 'client/app/common/directives/resizer/resizer.html',
                transclude: true,
                replace: true,
                scope: {
                    elementSelector: '@'    // селектор для элемента, масштаб которого будет изменятся
                },
                link: function (scope) {
                    var minScale = 50, maxScale = 200, step = 10, defaultWidth = 500;

                    function setElementWidth(width) {
                        angular.element(scope.elementSelector).css("width", width);
                    }

                    function scaleElement(scale) {
                        setElementWidth(elementWidth * scale / 100 + 'px')
                    }

                    // Элементы выпадающего списка (масштабы)
                    scope.selectValues = [];
                    for (var i = minScale; i <= maxScale; i += step) {
                        scope.selectValues.push(i);
                    }
                    // Выбранный масштаб
                    scope.selectValue = 100;

                    // Размер элемента до масштабирования
                    var elementWidth = angular.element(scope.elementSelector).width();
                    elementWidth = elementWidth ? elementWidth : defaultWidth;

                    /**
                     * Вызывается при изменении значения в выпадашке
                     */
                    scope.onSelectChange = function () {
                        scaleElement(scope.selectValue);
                    };

                    /**
                     * Уменьшить масштаб
                     */
                    scope.onScaleDown = function () {
                        if (scope.selectValue > minScale) {
                            scope.selectValue -= step;
                            scaleElement(scope.selectValue);
                        }
                    };

                    /**
                     * Увеличить масштаб
                     */
                    scope.onScaleUp = function () {
                        if (scope.selectValue < maxScale) {
                            scope.selectValue += step;
                            scaleElement(scope.selectValue);
                        }
                    };

                    /**
                     * Растянуть по ширине
                     */
                    scope.onFitToWidth = function () {
                        setElementWidth("100%");
                    };
                }
            };
        }]);
}());

