(function () {
    'use strict';

    angular.module('sbrfNdfl.header', [
            'ui.router',
            'aplana.dropdownMenu',
            'sbrfNdfl.Constants',
            'userData'
        ])
        .directive('sbrfNdflHeader', function () {
            return {
                templateUrl: 'js/main/header.html',
                controller: 'sbrfNdflTopMenuController'
            }
        })
        .controller('sbrfNdflTopMenuController', [
            '$rootScope', '$scope', '$translate', '$http', '$log', '$location', '$window', 'USER_DATA', 'aplanaDialogs',
            function ($rootScope, $scope, $translate, $http, $log, $location, $window, USER_DATA, aplanaDialogs) {
                $scope.mainMenu = [];

                /**
                 * Обновляет информацию о текущем пользователе и списке его ролей
                 */
                var updateCurrentUserInfo = function () {
                    var data = USER_DATA;

                    $scope.security.modes = [];
                    angular.extend($scope.security, data);
                    // Формируем строку ФИО
                    $scope.security.userTitle = data.user.fullShortName;
                    // Составляем список режимов
                    data.user.roles.forEach(function (userRole) {
                        if (userRole.macroRegions) {
                            userRole.macroRegions.forEach(function (macroRegion) {
                                $scope.security.modes.push({
                                    role: userRole.role,
                                    macroRegion: macroRegion,
                                    title: userRole.role.name + ' - ' + macroRegion.shortName
                                })
                            })
                        } else {
                            $scope.security.modes.push({
                                role: userRole.role,
                                macroRegion: undefined,
                                title: userRole.role.name
                            })
                        }
                    });
                    // Выбираем текущий режим
                    $scope.security.currentMode = $scope.security.modes[0]
                };

                //$rootScope.$on('$translateChangeSuccess', function () {
                $scope.mainMenu = [];
                var menuItem = {};

                // Производство
                if (hasRole("PRODUCTION_ORDERS_REGION_READ") ||
                    hasRole("PRODUCTION_ORDERS_MR_READ") ||
                    hasRole("PRODUCTION_FILE_READ") ||
                    hasRole("PRODUCTION_LOG_READ") ||
                    hasRole("PRODUCTION_PLAN_READ") ||
                    hasRole("PRODUCTION_SHIPMENT_READ")) {

                    menuItem = {id: 'menu-production', title: 'entity.Production', items: []};

                    // Планирование
                    if (hasRole("PRODUCTION_PLAN_READ")) {
                        menuItem.items.push(
                            {title: 'productionPlanning', sref: 'productionPlanning'}
                        );
                    }
                    $scope.mainMenu.push(menuItem);
                }

                $scope.mainMenu.forEach(function (item) {
                    // Добавляем стрелочку выпадающего меню (\u25BE - маленькая, \u25BС - крупная)
                    $translate(item.title).then(function (txt) {
                        item.title = txt + ' \u25BE'
                    })
                    // Выставляем ссылки по умолчанию для пустых подпунктов главного меню
                    item.items.forEach(function (submenu) {
                        if (submenu.sref == null && submenu.action == null && submenu.href == null) {
                            submenu.href = '#'
                        }
                        $translate(submenu.title).then(function (txt) {
                            submenu.title = txt
                        })
                    })
                    //})
                })
                $scope.security = {}
                $scope.security.currentMode = {}
                $scope.security.userTitle = ''
                updateCurrentUserInfo()

                /**
                 * Сменить режим работы пользователя
                 */
                $scope.setMode = function () {
                    var params = {
                        role: $scope.security.currentMode.role.id
                    };
                    if ($scope.security.currentMode.macroRegion) {
                        params.mr = $scope.security.currentMode.macroRegion.id
                    }
                    $http.get('rest/service/securityService/changeRole', {params: params})
                        .success(function (data) {
                            $location.url('/');
                            $window.location.reload();
                            sessionStorage.clear()
                        })
                };
                /**
                 * Выйти из системы. Убить сессию
                 */
                $scope.logout = function () {
                    $http.get('rest/service/securityService/logout');
                };

                function hasRole(role) {
                    return $.inArray(role, USER_DATA.authorities) !== -1;
                }
            }]);
}());