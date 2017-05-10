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
                templateUrl: '/dialogs/alert.html',
                transclude: true,
                replace: true,
                scope: {
                    onClick: '&',
                    type: '@'
                },
                link: function (scope, iElement, iAttrs) {
                    scope.type = scope.type || "warning";
                    iElement.addClass(scope.type);
                    if (scope.type === "success") {
                        iElement.find('.icon').addClass("success");
                    }

                    if (scope.type === "error") {
                        iElement.find('.icon').addClass("error");
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

                    $timeout(function () {
                        scope.close();
                    }, scope.type === "success" ? 3000 : (scope.type === "notification" ? 2000 : 30000));

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
        .factory("$alertService", ['$compile', '$rootScope', '$uibModal', '$injector', '$filter', '$timeout',
            function ($compile, $rootScope, $uibModal, $injector, $filter, $timeout) {
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
             * @param clickCallback текстовое представление функции, которая должна быть вызвана при клике по сообщению
             * @param scope Функция clickCallback будет скомпилирована в этом scope
             */
            alertService.showClickable = function (text, type, clickCallback, scope) {
                angular.element(alertContainer).empty();
                safeApply(function () {
                    text = text.replace(new RegExp("\n", "g"), "<br>");
                    var element = $compile(
                        '<div ' +
                        '   data-aplana-alert ' +
                        '   data-close-on-click="true" ' +
                        '   data-on-click="' + clickCallback + '" ' +
                        '   data-type="' + type + '">' +
                            text +
                        '</div>'
                    )(scope);

                    $timeout(function () {
                        angular.element(alertContainer).append(element)
                    }, 250);
                });
            };

            /**
             * Функция отображает сообщение aplana-alert с возможностью получить дополнительную информацию
             * @param text текст сообщения
             * @param details дополнительная информация
             * @param type тип сообщения. Возможны значения: error, success, warning(по умолчанию)
             */
            alertService.showClickableDetails = function (type, text, details) {
                var header = text + '<p class="btn-link">' + $filter('translate')('common.error.message.seemore') + '</p>';
                var scope = $rootScope.$new()
                scope.openDetails = function () {
                     $injector.invoke(
                         function ($uibModal) {
                             $uibModal.open({
                                 animation: false,
                                 templateUrl: '/dialogs/details.html',
                                 controller: 'showDetailsCtrl',
                                 size: 'lg',
                                 backdrop: 'static',
                                 resolve: {
                                     errorInfo: function () {
                                         return {
                                             text: text,
                                             details: details}
                                     }
                                 }
                            })
                         }
                     )
                 }
                alertService.showClickable(header, type, 'openDetails()', scope);
            };

            /**
             * Функция отображает сообщение aplana-alert
             * @param text текст сообщения
             * @param type тип сообщения. Возможны значения: error, success, warning(по умолчанию)
             */
            alertService.show = function (text, type) {
                angular.element(alertContainer).empty();
                safeApply(function () {
                    text = text.replace(new RegExp("\n", "g"), "<br>");
                    var element = $compile(
                        "<div data-aplana-alert data-type='" + (type || "warning") + "'>" + text +
                            "</div>"
                    )($rootScope);

                    $timeout(function () {
                        angular.element(alertContainer).append(element)
                    }, 250);
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
        }])
        // контролер для модального окна со списком серверных ошибок
        .controller('showDetailsCtrl', function ( $scope,  $uibModalInstance, errorInfo) {
                $scope.errorInfo = errorInfo
                $scope.ok = function () {
                    $uibModalInstance.close('ok')
                }
            }
        )
        .run(['$templateCache','$interpolate',function($templateCache,$interpolate){
            $templateCache.put('/dialogs/alert.html',
                '<div class="aplanaAlert" data-ng-click="onClick()">' +
                '<div>' +
                '    <button type="button" class="close" data-ng-click="close()">&times;</button>' +
                '</div>' +
                '<div class="icon">' +
                '    </div>' +
                '    <div class="text">' +
                '    <div data-ng-transclude></div>' +
                '</div>' +
                '</div>');
            $templateCache.put('/dialogs/details.html',
                '<div>' +
                '    <div class="modal-header dialog-header-error">' +
                '        <h4 class="modal-title">{{ errorInfo.text }}</h4>' +
                '    </div>' +
                '    <div class="modal-body">' +
                '        <pre class="stack-trace">{{ errorInfo.details }}</pre>' +
                '    </div>' +
                '    <div class="modal-footer">' +
                '        <button class="btn btn-danger" type="button" ng-click="ok()" translate="common.button.close"></button>' +
                '    </div>' +
                '</div>');
        }]);
}());

