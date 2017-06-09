(function () {
    'use strict';

    angular.module('app.header', [
        'ui.router',
        'userData',
        'ng.deviceDetector'
    ])
        .directive('appHeader', function () {
            return {
                templateUrl: 'client/js/main/header.html',
                controller: 'MainMenuController'
            };
        })
        .directive('tree', function() {
            return {
                restrict: "E",
                replace: true,
                scope: {
                    tree: '='
                },
                templateUrl: 'client/js/main/tree.html'
            };
        })
        .directive('leaf', function($compile) {
            return {
                restrict: "E",
                replace: true,
                scope: {
                    leaf: "="
                },
                templateUrl: 'client/js/main/leaf.html',
                link: function(scope, element, attrs) {
                    if (angular.isArray(scope.leaf.subtree)) {
                        element.append("<tree tree='leaf.subtree'></tree>");
                        element.addClass('dropdown-submenu');
                        $compile(element.contents())(scope);
                    }
                }
            };
        })
        .controller('MainMenuController', [
            '$scope', '$translate', '$http', 'USER_DATA', '$rootScope', 'deviceDetector',
            function ($scope, $translate, $http, USER_DATA, $rootScope, deviceDetector) {
                /**
                 * Обновляет информацию о текущем пользователе
                 */
                var updateCurrentUserInfo = function () {
                    var data = USER_DATA;
                    // Формируем строку ФИО
                    $scope.security.userTitle = USER_DATA.name;
                };

                $http.get('controller/rest/configService/getConfig').then(
                    function (response) {
                        $scope.gwtMode = response.data.gwtMode;
                        $scope.version = response.data.project_properties.version;
                        $scope.revision = response.data.project_properties.revision;
                        $scope.serverName = response.data.project_properties.serverName;
                        $scope.browser = deviceDetector.browser + " " + deviceDetector.browser_version;
                        $scope.security.userDep = response.data.department;
                        $scope.aboutHref = "Main.jsp" + $scope.gwtMode + "#!about";

                        $scope.treeTaxes = [{
                            name: "НДФЛ",
                            subtree: [{
                                name: "Формы",
                                href: "Main.jsp" + $scope.gwtMode + "#!declarationList;nType=NDFL"
                            }, {
                                name: "Ведение периодов",
                                href: "Main.jsp" + $scope.gwtMode + "#!periods;nType=NDFL"
                            }, {
                                name: "Настройки подразделений",
                                href: "Main.jsp" + $scope.gwtMode + "#!departmentConfigProperty;nType=NDFL"
                            }, {
                                name: "Назначение форм",
                                href: "Main.jsp" + $scope.gwtMode + "#!destination;nType=NDFL;isForm=false"
                            }, {
                                name: "Отчетность",
                                href: "Main.jsp" + $scope.gwtMode + "#!declarationList;nType=NDFL;isReports=true"
                            }]
                        }, {
                            name: "Сервис",
                            subtree: [{
                                name: "Загрузить файлы",
                                href: "Main.jsp" + $scope.gwtMode + "#!uploadTransportData"
                            }]
                        }, {
                            name: "Общие параметры",
                            href: "Main.jsp" + $scope.gwtMode + "#!commonParameter"
                        }];

                        $scope.treeNsi = [{
                            name: "Справочники",
                            href: "Main.jsp" + $scope.gwtMode + "#!refbooklist"
                        }];

                        $scope.treeAdministration = [{
                            name: "Список блокировок",
                            href: "Main.jsp" + $scope.gwtMode + "#!lockList"
                        }, {
                            name: "Журнал аудита",
                            href: "Main.jsp" + $scope.gwtMode + "#!audit"
                        }, {
                            name: "Список пользователей",
                            href: "Main.jsp" + $scope.gwtMode + "#!members"
                        }, {
                            name: "Конфигурационные параметры",
                            href: "Main.jsp" + $scope.gwtMode + "#!configuration"
                        }, {
                            name: "Планировщик задач",
                            href: "Main.jsp" + $scope.gwtMode + "#!taskList"
                        }, {
                            name: "Настройки",
                            subtree: [{
                                name: "Макеты налоговых форм",
                                href: "Main.jsp" + $scope.gwtMode + "#!declarationTemplateList"
                            }, {
                                name: "Справочники",
                                href: "Main.jsp" + $scope.gwtMode + "#!refbooklistadmin"
                            }, {
                                name: "Сбросить кэш",
                                href: "controller/actions/cache/clear-cache"
                            }, {
                                name: "Экспорт макетов",
                                href: "controller/actions/formTemplate/downloadAll"
                            }, {
                                name: "Импорт скриптов",
                                href: "Main.jsp" + $scope.gwtMode + "#!scriptsImport"
                            }]
                        }];

                        $scope.treeManual = [{
                            name: "Руководство пользователя",
                            href: "resources/help_ndfl.pdf"
                        }, {
                            name: "Руководство настройщика макета",
                            href: "resources/help_conf.pdf"
                        }];
                    }
                );

                $scope.security = {};

                $scope.security.userTitle = '';
                updateCurrentUserInfo();

                /**
                 * Выйти из системы. Убить сессию
                 */
                    // Выход из системы
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