(function () {
    'use strict';

    /**
     * @description Модуль для главной страницы
     */

    angular.module('app.header', [
        'ui.router',
        'userData',
        'ng.deviceDetector'
    ])
        .directive('appHeader', function () {
            return {
                templateUrl: 'client/app/main/header.html',
                controller: 'MainMenuController'
            };
        })
        .controller('MainMenuController', [
            '$scope', '$translate', '$http', 'USER_DATA', '$rootScope', 'deviceDetector', '$filter',
            function ($scope, $translate, $http, USER_DATA, $rootScope, deviceDetector, $filter) {
                /**
                 * @description Обновляет информацию о текущем пользователе
                 */
                var updateCurrentUserInfo = function () {
                    // Формируем строку ФИО
                    $scope.security.userTitle = USER_DATA.name;
                };

                /**
                 * @description Получаем необходимые настройки с сервера
                 * @param {{project_properties}} response
                 */
                $http.get('controller/rest/configService/getConfig').then(
                    function (response) {
                        $scope.gwtMode = response.data.gwtMode;
                        $scope.version = response.data.project_properties.version;
                        $scope.revision = response.data.project_properties.revision;
                        $scope.serverName = response.data.project_properties.serverName;
                        $scope.browser = deviceDetector.browser + " " + deviceDetector.browser_version;
                        $scope.security.userDep = response.data.department;
                        $scope.aboutHref = "Main.jsp" + $scope.gwtMode + "#!about";

                        //Задаем ссылки для главного меню
                        $scope.treeTaxes = [{
                            name: $filter('translate')('menu.taxes.ndfl'),
                            subtree: [{
                                name: $filter('translate')('menu.taxes.ndfl.forms'),
                                href: "Main.jsp" + $scope.gwtMode + "#!declarationList;nType=NDFL"
                            }, {
                                name: $filter('translate')('menu.taxes.ndfl.maintenanceOfPeriods'),
                                href: "Main.jsp" + $scope.gwtMode + "#!periods;nType=NDFL"
                            }, {
                                name: $filter('translate')('menu.taxes.ndfl.settingsUnits'),
                                href: "Main.jsp" + $scope.gwtMode + "#!departmentConfigProperty;nType=NDFL"
                            }, {
                                name: $filter('translate')('menu.taxes.ndfl.formAssignment'),
                                href: "Main.jsp" + $scope.gwtMode + "#!destination;nType=NDFL;isForm=false"
                            }, {
                                name: $filter('translate')('menu.taxes.ndfl.accountability'),
                                href: "Main.jsp" + $scope.gwtMode + "#!declarationList;nType=NDFL;isReports=true"
                            }]
                        }, {
                            name: $filter('translate')('menu.taxes.service'),
                            subtree: [{
                                name: $filter('translate')('menu.taxes.service.loadFiles'),
                                href: "Main.jsp" + $scope.gwtMode + "#!uploadTransportData"
                            }]
                        }, {
                            name: $filter('translate')('menu.taxes.commonParameters'),
                            href: "Main.jsp" + $scope.gwtMode + "#!commonParameter"
                        }];

                        $scope.treeNsi = [{
                            name: $filter('translate')('menu.nsi.refbooks'),
                            href: "Main.jsp" + $scope.gwtMode + "#!refbooklist"
                        }];

                        $scope.treeAdministration = [{
                            name: $filter('translate')('menu.administration.blockList'),
                            href: "Main.jsp" + $scope.gwtMode + "#!lockList"
                        }, {
                            name: $filter('translate')('menu.administration.auditLog'),
                            href: "Main.jsp" + $scope.gwtMode + "#!audit"
                        }, {
                            name: $filter('translate')('menu.administration.usersList'),
                            href: "Main.jsp" + $scope.gwtMode + "#!members"
                        }, {
                            name: $filter('translate')('menu.administration.configParams'),
                            href: "Main.jsp" + $scope.gwtMode + "#!configuration"
                        }, {
                            name: $filter('translate')('menu.administration.taskManager'),
                            href: "Main.jsp" + $scope.gwtMode + "#!taskList"
                        }, {
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
                                href: "controller/actions/formTemplate/downloadAll"
                            }, {
                                name: $filter('translate')('menu.administration.settings.importingScripts'),
                                href: "Main.jsp" + $scope.gwtMode + "#!scriptsImport"
                            }]
                        }];

                        $scope.treeManual = [{
                            name: $filter('translate')('menu.manuals.manualUser'),
                            href: "resources/help_ndfl.pdf"
                        }, {
                            name: $filter('translate')('menu.manuals.manualLayoutDesigner'),
                            href: "resources/help_conf.pdf"
                        }];
                    }
                );

                $scope.security = {};

                $scope.security.userTitle = '';
                updateCurrentUserInfo();

                //noinspection JSValidateJSDoc
                /**
                 * @description Выход из системы
                 * @param USER_DATA.login
                 */
                $scope.logout = function () {
                    $.ajax({
                        type: "GET",
                        url: "j_spring_security_logout",
                        noalert: true
                    }).always(function () {
                        $.ajax({
                            type: "GET",
                            url: "controller/actions/clearAuthenticationCache",
                            username: USER_DATA.login,
                            password: 'logout' + (new Date()).getTime().toString(),
                            noalert: true
                        }).always(function() {
                            document.location = "logout";
                        });
                    });
                };
            }]);
}());