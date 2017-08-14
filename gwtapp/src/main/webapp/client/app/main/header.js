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
            '$scope', '$state', '$translate', '$http', '$rootScope', 'deviceDetector', '$filter', 'ConfigResource', 'NotificationResource', 'appModals', 'amountCasesFormatterFilter', 'APP_CONSTANTS', 'PermissionChecker',
            function ($scope, $state, $translate, $http, $rootScope, deviceDetector, $filter, ConfigResource, NotificationResource, appModals, amountCasesFormatterFilter, APP_CONSTANTS, PermissionChecker) {

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
                        $scope.serverName = data.versionInfoProperties.serverName;
                        $scope.browser = deviceDetector.browser + " " + deviceDetector.browser_version;
                        $scope.aboutHref = "Main.jsp" + $scope.gwtMode + "#!about";

                        var subtree = [];
                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL)) {
                            subtree.push({
                                name: $filter('translate')('menu.taxes.ndfl.forms'),
                                href: "Main.jsp" + $scope.gwtMode + "#!declarationList;nType=NDFL"
                            });
                        }
                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_SETTINGS)) {
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

                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_REPORTS)) {
                            subtree.push({
                                name: $filter('translate')('menu.taxes.ndfl.accountability'),
                                href: "Main.jsp" + $scope.gwtMode + "#!declarationList;nType=NDFL;isReports=true"
                            });
                        }

                        //Задаем ссылки для главного меню
                        $scope.treeTaxes = [{
                            name: $filter('translate')('menu.taxes.ndfl'),
                            subtree: subtree
                        }, {
                            name: $filter('translate')('menu.taxes.service'),
                            subtree: [{
                                name: $filter('translate')('menu.taxes.service.loadFiles'),
                                onClick: function () {
                                    $state.go('uploadTransportData');
                                }
                            }]
                        }];
                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_GENERAL)) {
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
                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_BLOCK_AND_AUDIT)) {
                            $scope.treeAdministration.push({
                                name: $filter('translate')('menu.administration.blockList'),
                                href: "Main.jsp" + $scope.gwtMode + "#!lockList"
                            }, {
                                name: $filter('translate')('menu.administration.auditLog'),
                                href: "Main.jsp" + $scope.gwtMode + "#!audit"
                            });
                        }

                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_USERS)) {
                            $scope.treeAdministration.push({
                                name: $filter('translate')('menu.administration.usersList'),
                                href: "Main.jsp" + $scope.gwtMode + "#!members"
                            });
                        }

                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_CONFIG)) {
                            $scope.treeAdministration.push({
                                name: $filter('translate')('menu.administration.configParams'),
                                href: "Main.jsp" + $scope.gwtMode + "#!configuration"
                            }, {
                                name: $filter('translate')('menu.administration.taskManager'),
                                href: "Main.jsp" + $scope.gwtMode + "#!taskList"
                            });
                        }

                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_SETTINGS)) {
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
                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_MANUAL_USER)) {
                            $scope.treeManual.push({
                                name: $filter('translate')('menu.manuals.manualUser'),
                                href: "resources/help_ndfl.pdf"
                            });
                        }
                        if (PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.VIEW_MANUAL_DESIGNER)) {
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