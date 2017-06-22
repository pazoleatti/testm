/**
 * aplana-alert (Сообщение об ошибке)
 * Директива aplana-alert служит для отображения всплывающего сообщения с ошибкой в левом нижнем углу экрана
 * http://localhost:8080/#/aplana_alert
 */
(function () {
    'use strict';

    /* Директива для AplanaAlert */
    angular.module("aplana.alert", ['aplana.utils'])
        .directive('aplanaAlert', ['AplanaUtils', '$timeout', function (AplanaUtils, $timeout) {
            return {
                restrict: 'EA',
                templateUrl: AplanaUtils.templatePath + 'alert/alert.html',
                transclude: true,
                replace: true,
                scope: {
                    onClick: '&',
                    type: '@'
                },
                link: function (scope, iElement) {
                    scope.type = scope.type || "warning";
                    iElement.addClass(scope.type);
                    if (scope.type === "success") {
                        iElement.find('.icon').addClass("success");
                    }

                    if (scope.type === "error") {
                        iElement.find('.icon').addClass("error");
                    } else {
                        $timeout(function () {
                            scope.close();
                        }, scope.type === "success" ? 3000 : 30000);
                    }

                    if (scope.type === "warning") {
                        iElement.find('.icon').addClass("warning");
                    }

                    if (scope.type === "error_validation") {
                        iElement.find('.icon').addClass("error_validation");
                    }

                    if (scope.type === "notification") {
                        iElement.find('.icon').addClass("icon-exclamation");
                    }

                    scope.close = function () {
                        iElement.remove();
                    };
                }
            };
        }])
        .directive('closeOnClick', function () {
            return {
                restrict: 'EA',
                link: function (scope, iElement) {
                    iElement.bind("click", function () {
                        iElement.remove();
                    });
                }
            };
        })
        .factory("$alertService", ['$compile', '$rootScope', function ($compile, $rootScope) {
            var alertService = {};

            // безопасный apply. в тестах apply требуется, при реальном использовании - не всегда
            function safeApply(fn) {
                var phase = $rootScope.$$phase;
                if (phase === '$apply' || phase === '$digest') {
                    if (fn && (typeof(fn) === 'function')) {
                        fn();
                    }
                } else {
                    $rootScope.$apply(fn);
                }
            }

            function getOrCreateAlertContainer() {
                if (document.getElementById('alertContainer') == null) {
                    var _alertContainer = $compile("<div id='alertContainer'></div>")($rootScope);
                    angular.element(document.body).append(_alertContainer);
                    return _alertContainer;
                } else {
                    return $('#alertContainer');
                }
            }

            function initAlertContainer(_alertContainer) {
                // отступ сверху, например для того чтобы не перекрывать верхнее меню
                var topOffest = 60;
                $(window).resize(function () {
                    _alertContainer.css("max-height", $(window).height() - topOffest);
                });
                _alertContainer.css("max-height", $(window).height() - topOffest);
            }

            var alertContainer = getOrCreateAlertContainer();
            initAlertContainer(alertContainer);

            /**
             * Функция отображает сообщение aplana-alert
             * @param text текст сообщения
             * @param type тип сообщения. Возможны значения: error, success, warning(по умолчанию)
             * @param clickCallback текстовое представление функции, которая должна быть вызывана при клике по сообщению
             * @param scope Функция clickCallback будет скомпилирована в этом scope
             */
            alertService.showClickable = function (text, type, clickCallback, scope) {
                safeApply(function () {
                    text = text.replace(new RegExp("\n", "g"), "<br>");
                    var element = $compile(
                        "<div data-aplana-alert data-close-on-click='true' data-on-click='" + clickCallback + "' data-type='" + (type || "warning") + "'>" + text +
                            "</div>"
                    )(scope);

                    angular.element(alertContainer).append(element);
                });
            };

            /**
             * Функция отображает сообщение aplana-alert
             * @param text текст сообщения
             * @param type тип сообщения. Возможны значения: error, success, warning(по умолчанию)
             */
            alertService.show = function (text, type) {
                safeApply(function () {
                    text = text.replace(new RegExp("\n", "g"), "<br>");
                    var element = $compile(
                        "<div data-aplana-alert data-type='" + (type || "warning") + "'>" + text +
                            "</div>"
                    )($rootScope);

                    angular.element(alertContainer).append(element);
                });
            };

            alertService.error = function (text) {
                alertService.show(text, "error");
            };

            alertService.warning = function (text) {
                alertService.show(text, "warning");
            };

            alertService.success = function (text) {
                alertService.show(text, "success");
            };

            alertService.notification = function (text) {
                alertService.show(text, "notification");
            };
            return  alertService;
        }]);
}());

