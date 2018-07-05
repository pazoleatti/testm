(function () {
    'use strict';
    angular.module('aplana.splitter', ['aplana.utils'])
        .directive('aplanaSplitter', ['AplanaUtils', '$document', function (AplanaUtils, $document) {
            return {
                restrict: 'A',
                scope: {
                    splitter: '@', // ориентация  vertical / horizontal
                    splitterThick: '@',  // толщина: высота-ширина
                    splitterLeftTop: '@', // айди левого компонента или верхнего
                    splitterRightBottom: '@', // айди левого компонента или верхнего
                    splitterMajorSide: '@', // сторона, размеры которой будут изменяться при движении сплиттера
                    splitterContainer: '@',  // контейнер, ограничивающий движение сплиттера
                    splitterMax: '@',  // максимально на сколько можно подвинуть
                    splitterMin: '@',  // минимально на сколько можно подвинуть
                    splitterStart: '@',  // начальная позиция. нужна для инициализации, двигает компонент splitterMajorSide
                    splitterResizing: '@'  // возможность динамически менять ширину элемента. по умолчанию false
                },
                transclude: true,
                templateUrl: AplanaUtils.templatePath + 'splitter/splitter.html',

                link: function ($scope, $element) {

                    // иконка для элемента сворачивания.
                    $scope.iconClassHide = $scope.splitter === 'vertical' ? 'splitter-icon-chevron-left' : 'splitter-icon-chevron-up';

                    if (angular.isUndefined($scope.splitterResizing)) {
                        $scope.splitterResizing = false;
                    }
                    if (angular.isUndefined($scope.splitterMajorSide)) {
                        $scope.splitterMajorSide = $scope.splitterLeftTop;
                    }

                    if (!angular.isDefined($scope.splitterLeftTop)) {
                        throw new Error('Не удалось определить объект splitterLeftTop');
                    }

                    if (!angular.isDefined($scope.splitterRightBottom)) {
                        throw new Error('Не удалось определить объект splitterRightBottom');
                    }

                    function init() {
                        // если нет ресайза, то курсор меняется на обычный
                        if (!$scope.splitterResizing) {
                            $element.css({
                                cursor: 'default'
                            });
                        }

                        // установка в начальное состояние
                        if ($scope.splitter === 'vertical') {
                            $element.css({
                                width: $scope.splitterThick
                            });
                            angular.element('#' + $scope.splitterMajorSide).width($scope.splitterStart);
                        } else {
                            $element.css({
                                height: $scope.splitterThick
                            });
                            angular.element('#' + $scope.splitterMajorSide).height($scope.splitterStart);
                        }
                        updateSplitterIcon();
                    }

                    init();

                    var startHeight, pY,
                        moving = false;

                    $element.on('mousedown', function (event) {
                        event.preventDefault();

                        startHeight = angular.element('#' + $scope.splitterMajorSide)[0].offsetHeight;
                        pY = event.pageY;

                        if ($scope.splitterResizing) {
                            $document.on('mousemove', mouseMove);
                            $document.on('mouseup', mouseUp);
                        }
                        moving = false;
                    });

                    $scope.onClick = function () {
                        if (!moving) {
                            var el = angular.element('#' + $scope.splitterMajorSide);
                            el.toggle();
                            updateSplitterIcon();
                        }
                    };

                    // смена иконки
                    function updateSplitterIcon() {
                        var el = angular.element('#' + $scope.splitterMajorSide);
                        if ($scope.splitter === 'vertical') {
                            $scope.iconClassHide = ($scope.splitterMajorSide === $scope.splitterRightBottom) !== el.is(':visible') ? 'splitter-icon-chevron-left' : 'splitter-icon-chevron-right';
                        } else {
                            $scope.iconClassHide = ($scope.splitterMajorSide === $scope.splitterRightBottom) !== el.is(':visible') ? 'splitter-icon-chevron-up' : 'splitter-icon-chevron-down';
                        }
                    }

                    function mouseMove(event) {
                        moving = true;

                        // если елемент скрыт, но двигается  сам сплитер
                        var el = angular.element('#' + $scope.splitterMajorSide);
                        if (!el.is(':visible')) {
                            el.show();
                            updateSplitterIcon();
                        }

                        if ($scope.splitter === 'vertical') {
                            // TODO
                        } else {
                            var my = (event.pageY - pY);

                            $scope.$root.$broadcast('UPDATE_GIRD_HEIGHT');

                            var height = Math.max(Math.min(startHeight - my, angular.element($scope.splitterContainer).height() - $element.height()), 0);

                            angular.element('#' + $scope.splitterMajorSide).css({
                                height: height,
                                maxHeight: angular.element($scope.splitterContainer).height()
                            });
                        }
                    }

                    function mouseUp() {
                        if ($scope.splitterResizing) {
                            $document.unbind('mousemove', mouseMove);
                            $document.unbind('mouseup', mouseUp);
                        }
                    }

                    $scope.$root.$on('WINDOW_RESIZED_MSG', function () {
                        angular.element('#' + $scope.splitterMajorSide).css({
                            maxHeight: angular.element($scope.splitterContainer).height() - $element.height()
                        });
                    });
                }
            };
        }]);
}());
