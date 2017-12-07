(function () {
    'use strict';
    angular.module('aplana.splitter', ['aplana.utils'])
        .directive('aplanaSplitter',['AplanaUtils', '$document', function(AplanaUtils, $document) {
            return {
                restrict: 'A',
                scope: {
                    splitter: '@', // ориентация  vertical / horizontal
                    splitterThick: '@',  // толщина: высота-ширина
                    splitterLeftTop: '@', // айди левого компонента или верхнего
                    splitterRightBottom: '@', // айди левого компонента или верхнего
                    splitterMax: '@',  // максимально на сколько можно подвинуть
                    splitterMin: '@',  // минимально на сколько можно подвинуть
                    splitterStart: '@',  // начальная позиция. нужна для инициализации, двигает компонент splitterLeftTop
                    splitterResizing: '@'  // возможность динамически менять ширину элемента. по умолчанию false
                },
                transclude: true,
                templateUrl: AplanaUtils.templatePath + 'splitter/splitter.html',

                link: function ($scope, $element) {

                    // иконка для элемента сворачивания.
                    $scope.iconClassHide = $scope.splitter === 'vertical' ? 'splitter-icon-chevron-left' : 'splitter-icon-chevron-up';

                    if (!angular.isDefined($scope.splitterResizing)){
                        $scope.splitterResizing = false;
                    }

                    if (!angular.isDefined($scope.splitterLeftTop)) {
                        throw new Error('Не удалось определить объект splitterLeftTop');
                    }

                    if (!angular.isDefined($scope.splitterRightBottom)) {
                        throw new Error('Не удалось определить объект splitterRightBottom');
                    }

                    function init() {
                        // если нет ресайза, то курсор меняется на обычный
                        if (!$scope.splitterResizing){
                            $element.css({
                                cursor: 'default'
                            });
                        }

                        if ($scope.splitter === 'vertical') {
                            //$scope.styleClickElement = {
                            //    height: $scope.splitterThick,
                            //    width: $scope.splitterThick,
                            //    'margin-top': '5px'
                            //};

                            // установка в начальное состояние
                            $element.css({
                                left: $scope.splitterStart + 'px',
                                width: $scope.splitterThick
                            });
                            angular.element('#' + $scope.splitterLeftTop).width($scope.splitterStart);
                        } else {
                            //$scope.styleClickElement = {
                            //    height: $scope.splitterThick,
                            //    width: $scope.splitterThick,
                            //    'margin-left': '5px'
                            //};

                            // установка в начальное состояние
                            $element.css({
                                top: $scope.splitterStart + 'px',
                                height: $scope.splitterThick
                            });
                            angular.element('#' + $scope.splitterLeftTop).height($scope.splitterStart);
                        }
                    }

                    init();

                    $element.on('mousedown', function (event) {
                        event.preventDefault();

                        if ($scope.splitterResizing) {
                            $document.on('mousemove', mouseMove);
                            $document.on('mouseup', mouseUp);
                        }
                    });

                    // клик по вертикальному сплитеру   скрыть/показать
                    function updateCssElementVertical(x) {
                        if ($scope.splitterMax && x > $scope.splitterMax) {
                            x = parseInt($scope.splitterMax);
                        }

                        $element.css({left: x + 'px'});
                        //angular.element('#' + $scope.splitterRightBottom).css({left: (x + parseInt($scope.splitterThick)) + 'px'});
                    }

                    $scope.onClick = function () {
                        var el = angular.element('#' + $scope.splitterLeftTop);
                        //el.toggle();

                        if ($scope.splitter === 'vertical') {
                            updateCssElementVertical(el.is(':visible') ? el.outerWidth() : 0);
                            $scope.iconClassHide = el.is(':visible') ? 'splitter-icon-chevron-left' : 'splitter-icon-chevron-right';
                        } else {
                            $scope.iconClassHide = el.is(':visible') ? 'splitter-icon-chevron-up' : 'splitter-icon-chevron-down';
                        }
                    };

                    function mouseMove(event) {

                        // если елемент скрыт, но двигается  сам сплитер
                        var el = angular.element('#' + $scope.splitterLeftTop);
                        if (!el.is(':visible')) {
                            el.show();

                            // смена иконки
                            if ($scope.splitter === 'vertical') {
                                $scope.iconClassHide = el.is(':visible') ? 'splitter-icon-chevron-left' : 'splitter-icon-chevron-right';
                            } else {
                                $scope.iconClassHide = el.is(':visible') ? 'splitter-icon-chevron-up' : 'splitter-icon-chevron-down';
                            }
                        }

                        if ($scope.splitter === 'vertical') {
                            var x = event.pageX - angular.element('#' + $scope.splitterLeftTop).offset().left;

                            // проверяем границы
                            if ($scope.splitterMax && x > $scope.splitterMax) {
                                x = parseInt($scope.splitterMax);
                            }

                            if ($scope.splitterMin && x < $scope.splitterMin) {
                                x = parseInt($scope.splitterMin);
                            }

                            // апдейт позиций
                            $element.css({left: x + 'px'});
                            angular.element('#' + $scope.splitterLeftTop).css({width: x + 'px'});
                            //angular.element('#' + $scope.splitterRightBottom).css({
                            //    left: (x + parseInt($scope.splitterThick)) + 'px'
                            //});

                        } else {
                            var y = event.pageY - angular.element('#'+$scope.splitterLeftTop).offset().top;
                            if ($scope.splitterMax && y > $scope.splitterMax) {
                                y = parseInt($scope.splitterMax);
                            }
                            if ($scope.splitterMin && y < $scope.splitterMin) {
                                y = parseInt($scope.splitterMin);
                            }
                            angular.element('#' + $scope.splitterLeftTop).css({height: y + 'px'});
                        }
                    }

                    function mouseUp() {
                        if ($scope.splitterResizing) {
                            $document.unbind('mousemove', mouseMove);
                            $document.unbind('mouseup', mouseUp);
                        }
                    }
                }
            };
        }]);
}());
