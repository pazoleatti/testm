(function () {
    'use strict';

    /**
     * @description Header
     */
    angular.module('app.header', [
        'app.notifications',
        'app.uploadTransportData',
        'app.scriptExecution',
        'app.commonParams',
        'app.formatters',
        'app.constants',
        'app.permissionUtils',
        'app.taxNotification'
    ])
        .directive('appHeader', function () {
            return {
                templateUrl: 'client/app/main/header.html',
                controller: 'HeaderController'
            };
        })
        .controller('HeaderController', [
            '$scope', '$state', '$translate', '$http', '$rootScope', '$interval', '$filter', 'NotificationResource', '$aplanaModal',
            function ($scope, $state, $translate, $http, $rootScope, $interval, $filter, NotificationResource, $aplanaModal) {

                // Ждем пока не загрузятся даныне пользователя и параметры приложения
                var unwatch = $scope.$watchCollection('[user, gwtMode]', function (newValues, oldValues) {
                    if (angular.isDefined(newValues[0]) && angular.isDefined(newValues[1])) {
                        $scope.security = {
                            user: $rootScope.user
                        };

                        buildHeader();
                        unwatch();
                    }
                });

                function buildHeader() {
                    var subtree = [];
                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_FORMS)) {
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
                            href: $state.href('departmentConfig')
                        });
                        subtree.push({
                            name: $filter('translate')('menu.taxes.ndfl.declarationTypeAssignment'),
                            href: $state.href('declarationTypeAssignment')
                        });
                    }

                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_REPORTS)) {
                        subtree.push({
                            name: $filter('translate')('menu.taxes.ndfl.accountability'),
                            href: $state.href('ndflReportJournal')
                        });
                    }

                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION._2NDFL_FL)) {
                        subtree.push({
                            name: $filter('translate')('menu.taxes.ndfl.personsFor2NdflFL'),
                            href: $state.href('personsFor2NdflFL')
                        });
                        subtree.push({
                            name: $filter('translate')('menu.taxes.ndfl.2ndflFLJournal'),
                            href: $state.href('2ndflFLJournal')
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
                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_CREATE_APPLICATION_2)) {
                        $scope.treeTaxes.push({
                            name: $filter('translate')('menu.taxes.application2'),
                            href: $state.href('application2Journal')
                        });
                    }
                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.TAX_NOTIFICATION)) {
                        $scope.treeTaxes.push({
                            name: $filter('translate')('menu.taxes.taxNotification'),
                            onClick: openTaxNotificationModal
                        });
                    }
                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_SERVICE)) {
                        $scope.treeTaxes.push({
                            name: $filter('translate')('menu.taxes.service'),
                            href: "",
                            subtree: [{
                                name: $filter('translate')('menu.taxes.service.loadFiles'),
                                href: $state.href('uploadTransportData')
                            }]
                        });
                    }
                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_GENERAL)) {
                        $scope.treeTaxes.push({
                            name: $filter('translate')('menu.taxes.commonParameters'),
                            href: $state.href('commonParams')
                        });
                    }

                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_NSI)) {
                        $scope.treeNsi = [{
                            name: $filter('translate')('menu.nsi.registryFL'),
                            href: $state.href('registryFL')
                        }, {
                            name: $filter('translate')('menu.nsi.refbooks'),
                            href: $state.href('refBookList')
                        }];
                    }

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

                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_TRANSPORT_MESSAGE_JOURNAL)) {
                        $scope.treeAdministration.push({
                            name: $filter('translate')('menu.administration.transportMessageJournal'),
                            href: $state.href('transportMessageJournal')
                        });
                    }

                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_USERS)) {
                        $scope.treeAdministration.push({
                            name: $filter('translate')('menu.administration.userList'),
                            href: $state.href('usersList')
                        });
                    }

                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_CONFIG)) {
                        $scope.treeAdministration.push({
                            name: $filter('translate')('menu.administration.configParams'),
                            href: $state.href('configParam')
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
                                href: $state.href('declarationTypeJournal')
                            }, {
                                name: $filter('translate')('menu.administration.settings.refbooks'),
                                href: $state.href('refBookConfList')
                            }, {
                                name: $filter('translate')('menu.administration.settings.resetCache'),
                                href: "controller/actions/cache/clear-cache"
                            }, {
                                name: $filter('translate')('menu.administration.settings.exportLayouts'),
                                href: "controller/actions/declarationTemplate/downloadAll"
                            }]
                        });
                    }

                    $scope.treeManual = [];
                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_MANUAL_USER)) {
                        $scope.treeManual.push({
                            name: $filter('translate')('menu.manuals.manualUser'),
                            href: "controller/rest/document?fileName=help_ndfl.pdf",
                            target: "_blank"
                        });
                    }
                    if ($scope.permissionChecker.check($scope.security.user, $scope.APP_CONSTANTS.USER_PERMISSION.VIEW_MANUAL_DESIGNER)) {
                        $scope.treeManual.push({
                            name: $filter('translate')('menu.manuals.manualLayoutDesigner'),
                            href: "controller/rest/document?fileName=help_conf.pdf",
                            target: "_blank"
                        });
                    }
                }

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
                        templateUrl: 'client/app/main/notifications.html',
                        controller: 'notificationsCtrl',
                        windowClass: 'modal1000'
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
                        } else {
                            $scope.notificationsCountClass = "new-message empty-message";
                        }
                    });
                };

                function startUpdateNotificationCountInterval() {
                    if (angular.isDefined($scope.stop)) {
                        return;
                    }
                    $scope.updateNotificationCount();
                    $scope.stop = $interval($scope.updateNotificationCount, 30000);
                }

                function cancelUpdateNotificationCountInterval() {
                    if (angular.isDefined($scope.stop)) {
                        $interval.cancel($scope.stop);
                        $scope.stop = undefined;
                    }
                }

                $scope.$on("AUTHORIZATION_EXPIRED", function () {
                    cancelUpdateNotificationCountInterval();
                });

                $scope.$on("UPDATE_NOTIFICATION_COUNT", function () {
                    $scope.updateNotificationCount();
                });

                $scope.$on('$destroy', function () {
                    cancelUpdateNotificationCountInterval();
                });

                startUpdateNotificationCountInterval();

                var openTaxNotificationModal = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('taxNotification.title.modal'),
                        templateUrl: 'client/app/taxes/taxNotification/taxNotification.html',
                        controller: 'taxNotificationCtrl',
                        windowClass: 'modal200'
                    });
                };
            }]
        );
}());