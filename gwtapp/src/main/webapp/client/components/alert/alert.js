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
        .factory("$alertService", ['$compile', '$rootScope', '$injector', '$filter', function ($compile, $rootScope, $injector, $filter) {
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
                if (document.getElementById('alertContainer') === null) {
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
             * Функция отображает сообщение aplana-alert с возможностью получить дополнительную информацию
             * @param text текст сообщения
             * @param stackTrace дополнительная информация
             * @param type тип сообщения. Возможны значения: error, success, warning(по умолчанию)
             */
            alertService.showClickableDetails = function (type, text, stackTrace) {
                var header = text + '<p class="btn-link">' + $filter('translate')('common.error.message.seemore') + '</p>';
                var scope = $rootScope.$new();
                scope.openDetails = function () {
                    $injector.invoke(
                        function ($uibModal) {
                            $uibModal.open({
                                animation: false,
                                templateUrl: '/dialogs/stackTrace.html',
                                controller: 'showStackTraceCtrl',
                                size: 'lg',
                                backdrop: 'static',
                                resolve: {
                                    errorInfo: function () {
                                        return {
                                            text: text,
                                            stackTrace: stackTrace
                                        }
                                    }
                                }
                            })
                        }
                    )
                };
                alertService.showClickable(header, type, 'openDetails()', scope);
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
            return alertService;
        }])
        // контролер для модального окна со списком серверных ошибок
        .controller('showStackTraceCtrl', function ($scope, $uibModalInstance, errorInfo) {
                $scope.header = errorInfo.text;
                $scope.stackTrace = errorInfo.stackTrace;

                if (!Array.isArray($scope.stackTrace.exceptionCause)) {
                    $scope.stackTrace.exceptionCause = [$scope.stackTrace.exceptionCause];
                }
                $scope.ok = function () {
                    $uibModalInstance.close('ok')
                }
            }
        )
        .run(['$templateCache', '$interpolate', function ($templateCache, $interpolate) {
            $templateCache.put('/dialogs/stackTrace.html',
                '<div>' +
                '    <div class="modal-header dialog-header-error">' +
                '        <h4 class="modal-title">{{ header }}</h4>' +
                '    </div>' +
                '    <div class="modal-body">' +
                '       <div>' +
                '           <span>' +
                '               {{:: stackTrace.additionInfo.serverDate| date: "dd.MM.yyyy HH:mm:ss"}} {{:: stackTrace.exceptionCause[0].errorClass}}: {{::stackTrace.exceptionCause[0].message | translate}}' +
                '           </span>' +
                '       </div>' +
                '       <div class="stack-trace">' +
                '           <div data-ng-repeat="arrayStack in stackTrace.exceptionCause track by $index" >' +
                '               <div>' +
                '                   <span data-ng-if="!$first" style="font-weight: bold;">' +
                '                       Cause by: {{::arrayStack.errorClass}}: {{:: arrayStack.message |translate }}' +
                '                   </span>' +
                '                   <div style="padding-left: 10px;">' +
                '                       <div data-ng-repeat="item in arrayStack.serverException track by $index" >' +
                '                           at {{:: item }}' +
                '                       </div>' +
                '                   </div>' +
                '               </div>' +
                '           </div>' +
                '       </div>' +
                '    </div>' +
                '    <div class="modal-footer">' +
                '        <button class="btn btn-default" type="button" ng-click="ok()" translate="button.close"></button>' +
                '    </div>' +
                '</div>'
            )
            ;
        }]);
}());

