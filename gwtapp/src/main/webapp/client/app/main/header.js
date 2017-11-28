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
        'app.commonParams',
        'app.formatters',
        'app.constants',
        'app.permissionUtils'
    ])
        .directive('appHeader', function () {
            return {
                templateUrl: 'client/app/main/header.html?v=${buildUuid}',
                controller: 'MainMenuController'
            };
        })
        .controller('MainMenuController', [
            '$scope', '$state', '$translate', '$http', '$rootScope', 'deviceDetector', '$filter', 'ConfigResource', 'NotificationResource', '$aplanaModal', 'amountCasesFormatterFilter',
            function ($scope, $state, $translate, $http, $rootScope, deviceDetector, $filter, ConfigResource, NotificationResource, $aplanaModal, amountCasesFormatterFilter) {

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
                                href: $state.href('ndflJournal')
                            });
                        }
                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_SETTINGS)) {
                            subtree.push({
                                name: $filter('translate')('menu.taxes.ndfl.maintenanceOfPeriods'),
                                href: $state.href('reportPeriod')
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
                                href: $state.href('ndflReportJournal')
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
                            href: "",
                            subtree: [{
                                name: $filter('translate')('menu.taxes.service.loadFiles'),
                                href: $state.href('uploadTransportData')
                            }]
                        });
                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_GENERAL)) {
                            $scope.treeTaxes.push({
                                name: $filter('translate')('menu.taxes.commonParameters'),
                                href: $state.href('commonParams')
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
                                href: $state.href('lockDataList')
                            }, {
                                name: $filter('translate')('menu.administration.asyncTaskList'),
                                href: $state.href('asyncTaskList')
                            });
                        }

                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_USERS)) {
                            $scope.treeAdministration.push({
                                name: $filter('translate')('menu.administration.userList'),
                                href: "Main.jsp" + $scope.gwtMode + "#!members"
                            });
                        }

                        if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_CONFIG)) {
                            $scope.treeAdministration.push({
                                name: $filter('translate')('menu.administration.configParams'),
                                href: "Main.jsp" + $scope.gwtMode + "#!configuration"
                            }, {
                                name: $filter('translate')('menu.administration.schedulerTaskList'),
                                href: $state.href('schedulerTaskList')
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
                    sessionStorage.clear();

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
                    $aplanaModal.open({
                        title: $filter('translate')('notifications.title.listNotifications'),
                        templateUrl: 'client/app/main/notifications.html?v=${buildUuid}',
                        controller: 'notificationsCtrl',
                        windowClass: 'modal800'
                    });
                };

                $scope.updateNotificationCount = function () {
                    NotificationResource.query({
                        projection: 'count',
                        nooverlay: true
                    }, function (data) {
                        if (parseInt(data.notifications_count) !== 0) {
                            $scope.notificationsCount = parseInt(data.notifications_count);
                        } else {
                            $scope.notificationsCount = 0;
                        }

                        if ($scope.notificationsCount > 0) {
                            $scope.notificationsCountClass = "new-message";
                        }
                        else {
                            $scope.notificationsCountClass = "new-message empty-message";
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