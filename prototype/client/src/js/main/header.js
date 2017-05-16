(function () {
    'use strict';

    angular.module('app.header', [
        'ui.router',
        'sbrfNdfl.Constants',
        'userData'
    ])
        .directive('appHeader', function () {
            return {
                templateUrl: 'js/main/header.html',
                controller: 'MainMenuController'
            }
        })
        .directive('tree', function() {
            return {
                restrict: "E",
                replace: true,
                scope: {
                    tree: '='
                },
                templateUrl: 'js/main/tree.html'
            };
        })
        .directive('leaf', function($compile) {
            return {
                restrict: "E",
                replace: true,
                scope: {
                    leaf: "="
                },
                templateUrl: 'js/main/leaf.html',
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
            '$scope', '$translate', '$http', 'USER_DATA',
            function ($scope, $translate, $http, USER_DATA) {
                /**
                 * Обновляет информацию о текущем пользователе
                 */
                var updateCurrentUserInfo = function () {
                    var data = USER_DATA;
                    // Формируем строку ФИО
                    $scope.security.userTitle = data.user.fullShortName;
                };

                $scope.treeTaxes = [{
                    name: "НДФЛ",
                    subtree: [{
                        name: "Формы",
                        href: "index.html#/taxes/ndfl/forms"
                    }, {
                        name: "Ведение периодов"
                    }, {
                        name: "Настройки подразделений"
                    }, {
                        name: "Назначение форм"
                    }, {
                        name: "Отчетность"
                    }]
                }, {
                    name: "Сервис",
                    subtree: [{
                        name: "Загрузить файлы"
                    }]
                }, {
                    name: "Общие параметры"
                }];

                $scope.treeNsi = [{
                    name: "Справочники"
                }];

                $scope.treeAdministration = [{
                    name: "Список блокировок"
                }, {
                    name: "Журнал аудита"
                }, {
                    name: "Список пользователей"
                }, {
                    name: "Конфигурационные параметры"
                }, {
                    name: "Планировщик задач"
                }, {
                    name: "Настройки",
                    subtree: [{
                        name: "Макеты налоговых форм"
                    }, {
                        name: "Справочники"
                    }, {
                        name: "Сбросить кэш"
                    }, {
                        name: "Экспорт макетов"
                    }, {
                        name: "Импорт скриптов"
                    }]
                }];

                $scope.security = {};

                $scope.security.userTitle = '';
                updateCurrentUserInfo();

                /**
                 * Выйти из системы. Убить сессию
                 */
                $scope.logout = function () {
                    $http.get('rest/service/securityService/logout');
                };
            }]);
}());