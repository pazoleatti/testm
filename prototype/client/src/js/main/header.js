(function () {
    'use strict';

    angular.module('mtsUsim.header', [
            'ui.router',
            'aplana.dropdownMenu',
            'mtsUsim.Constants',
            'userData'
        ])
        .directive('mtsUsimHeader', function () {
            return {
                templateUrl: 'js/main/header.html',
                controller: 'mtsUsimTopMenuController'
            }
        })
        .controller('mtsUsimTopMenuController', [
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

                // Справочники
                if (hasRole("REF_BOOK_GUI")) {
                    $scope.mainMenu.push({
                        id: 'menu-refbooks', title: 'entity.RefBooks', items: [
                            {title: 'entity.MacroRegion', href: '#/refbook/MacroRegion'},
                            {title: 'entity.Region', href: '#/refbook/Region'},
                            {title: 'entity.Code', href: '#/refbook/Code'},
                            {title: 'entity.Vendor', href: '#/refbook/Vendor'},
                            {title: 'entity.Specification', href: '#/refbook/Specification'},
                            {title: 'entity.Profile', href: '#/refbook/Profile'},
                            {title: 'entity.CardType', href: '#/refbook/CardType'},
                            {title: 'entity.Tariff', href: '#/refbook/Tariff'},
                            {title: 'entity.Hlr', href: '#/refbook/Hlr'},
                            {title: 'entity.TransportCompany', href: '#/refbook/TransportCompany'},
                            {title: 'entity.AuthAlgorythm', href: '#/refbook/AuthAlgorythm'},
                            {title: 'entity.Price', href: '#/refbook/Price'},
                            {title: 'entity.Delivery', href: '#/refbook/Delivery'}
                        ]
                    });
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