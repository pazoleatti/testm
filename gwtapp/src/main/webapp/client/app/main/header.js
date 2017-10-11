(function () {
    'use strict';

    /**
     * @description Модуль для главной страницы
     */

    angular.module('app.header', [
        'ui.router',
        'ng.deviceDetector',
        'app.notifications',
        'app.uploadTransportData',
        'app.modals',
        'app.formatters',
        'app.constants',
        'app.permissionUtils'
    ])
        .directive('appHeader', function () {
            return {
                templateUrl: 'client/app/main/header.html',
                controller: 'MainMenuController'
            };
        })
        .controller('MainMenuController', [
            '$scope', '$state', '$translate', '$http', '$rootScope', 'deviceDetector', '$filter', 'ConfigResource', 'NotificationResource', 'appModals', 'amountCasesFormatterFilter',
            function ($scope, $state, $translate, $http, $rootScope, deviceDetector, $filter, ConfigResource, NotificationResource, appModals, amountCasesFormatterFilter) {

                $scope.security = {
                    user: $rootScope.user
                };

                /**
                 * @description Получаем необходимые настройки с сервера
                 * @param {{project_properties}} response
                 */
                ConfigResource.query({
                        projection: "configurations"
                    },
                    function (data) {
                        $scope.gwtMode = data.gwtMode;
                        $scope.version = data.versionInfoProperties.version;
                        $scope.revision = data.versionInfoProperties.revision;
                        $scope.serverName = data.serverInfo.serverName;
                        $scope.browser = deviceDetector.browser + " " + deviceDetector.browser_version;
                        $scope.aboutHref = "Main.jsp" + $scope.gwtMode + "#!about";

                        var subtree = [];
                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL)) {
                            subtree.push({
                                name: $filter('translate')('menu.taxes.ndfl.forms'),
                                onClick: function () {
                                    $state.go('ndflJournal');
                                }
                            });
                        }
                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_SETTINGS)) {
                            subtree.push({
                                name: $filter('translate')('menu.taxes.ndfl.maintenanceOfPeriods'),
                                href: "Main.jsp" + $scope.gwtMode + "#!periods;nType=NDFL"
                            });
                            subtree.push({
                                name: $filter('translate')('menu.taxes.ndfl.settingsUnits'),
                                href: "Main.jsp" + $scope.gwtMode + "#!departmentConfigProperty;nType=NDFL"
                            });
                            subtree.push({
                                name: $filter('translate')('menu.taxes.ndfl.formAssignment'),
                                href: "Main.jsp" + $scope.gwtMode + "#!destination;nType=NDFL;isForm=false"
                            });
                        }

                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_REPORTS)) {
                            subtree.push({
                                name: $filter('translate')('menu.taxes.ndfl.accountability'),
                                onClick: function () {
                                    $state.go('ndflReportJournal');
                                }
                            });
                        }

                        //Задаем ссылки для главного меню
                        $scope.treeTaxes = [];
                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL)) {
                            $scope.treeTaxes.push({
                                name: $filter('translate')('menu.taxes.ndfl'),
                                subtree: subtree
                            });
                        }
                        $scope.treeTaxes.push({
                            name: $filter('translate')('menu.taxes.service'),
                            subtree: [{
                                name: $filter('translate')('menu.taxes.service.loadFiles'),
                                onClick: function () {
                                    $state.go('uploadTransportData');
                                }
                            }]
                        });
                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_GENERAL)) {
                            $scope.treeTaxes.push({
                                name: $filter('translate')('menu.taxes.commonParameters'),
                                href: "Main.jsp" + $scope.gwtMode + "#!commonParameter"
                            });
                        }

                        $scope.treeNsi = [{
                            name: $filter('translate')('menu.nsi.refbooks'),
                            href: "Main.jsp" + $scope.gwtMode + "#!refbooklist"
                        }];

                        $scope.treeAdministration = [];
                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_BLOCK)) {
                            $scope.treeAdministration.push({
                                name: $filter('translate')('menu.administration.blockList'),
                                onClick: function () {
                                    $state.go('lockDataList');
                                }
                            });
                        }

                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_CONFIG)) {
                            $scope.treeAdministration.push({
                                name: $filter('translate')('menu.administration.configParams'),
                                href: "Main.jsp" + $scope.gwtMode + "#!configuration"
                            }, {
                                name: $filter('translate')('menu.administration.schedulerTaskList'),
                                onClick: function () {
                                    $state.go('schedulerTaskList');
                                }
                            });
                        }

                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_SETTINGS)) {
                            $scope.treeAdministration.push({
                                name: $filter('translate')('menu.administration.settings'),
                                subtree: [{
                                    name: $filter('translate')('menu.administration.settings.mockOfTaxForms'),
                                    href: "Main.jsp" + $scope.gwtMode + "#!declarationTemplateList"
                                }, {
                                    name: $filter('translate')('menu.administration.settings.refbooks'),
                                    href: "Main.jsp" + $scope.gwtMode + "#!refbooklistadmin"
                                }, {
                                    name: $filter('translate')('menu.administration.settings.resetCache'),
                                    href: "controller/actions/cache/clear-cache"
                                }, {
                                    name: $filter('translate')('menu.administration.settings.exportLayouts'),
                                    href: "controller/actions/declarationTemplate/downloadAll"
                                }, {
                                    name: $filter('translate')('menu.administration.settings.importingScripts'),
                                    href: "Main.jsp" + $scope.gwtMode + "#!scriptsImport"
                                }]
                            });
                        }

                        $scope.treeManual = [];
                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_MANUAL_USER)) {
                            $scope.treeManual.push({
                                name: $filter('translate')('menu.manuals.manualUser'),
                                href: "resources/help_ndfl.pdf"
                            });
                        }
                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_MANUAL_DESIGNER)) {
                            $scope.treeManual.push({
                                name: $filter('translate')('menu.manuals.manualLayoutDesigner'),
                                href: "resources/help_conf.pdf"
                            });
                        }
                    }
                );

                //noinspection JSValidateJSDoc
                /**
                 * @description Выход из системы
                 */
                $scope.logout = function () {
                    // Сообщаем клиентской части системы, что выходим. Если есть несохраненные данные - нужно ловить это сообщение
                    $rootScope.$broadcast('LOGOUT_MSG');

                    $.ajax({
                        type: "GET",
                        url: "j_spring_security_logout",
                        noalert: true
                    }).always(function () {
                        $.ajax({
                            type: "GET",
                            url: "controller/actions/clearAuthenticationCache",
                            username: $scope.security.user.login,
                            password: 'logout' + (new Date()).getTime().toString(),
                            noalert: true
                        }).always(function () {
                            document.location = "logout";
                        });
                    });
                };

                $scope.openNotifications = function () {
                    appModals.create('client/app/main/notifications.html', 'notificationsCtrl');
                };

                $scope.updateNotificationCount = function () {
                    NotificationResource.query({
                        projection: 'count'
                    }, function (data) {
                        if (parseInt(data.notifications_count) !== 0) {
                            $scope.showImage = true;
                            $scope.notificationsCount = data.notifications_count + " "
                                + amountCasesFormatterFilter(parseInt(data.notifications_count), $filter('translate')('notifications.nominative'), $filter('translate')('notifications.singular'), $filter('translate')('notifications.plural'));
                        } else {
                            $scope.showImage = false;
                            $scope.notificationsCount = "Нет оповещений";
                        }
                    });
                };

                $scope.$on("UPDATE_NOTIFICATION_COUNT", function () {
                    $scope.updateNotificationCount();
                });

                $scope.updateNotificationCount();

                setInterval(function () {
                    $scope.updateNotificationCount();
                }, 30000);
            }]);
}());