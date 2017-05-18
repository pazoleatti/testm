(function () {
    'use strict';

    angular.module('app.header', [
        'ui.router',
        'aplana.dropdownMenu',
        'sbrfNdfl.Constants',
        'userData'
    ])
        .directive('appHeader', function () {
            return {
                templateUrl: 'js/main/header.html',
                controller: 'MainMenuController'
            }
        })
        .controller('MainMenuController', [
            '$scope', '$translate', '$http', 'USER_DATA',
            function ($scope, $translate, $http, USER_DATA) {
                $scope.mainMenu = [];

                /**
                 * Обновляет информацию о текущем пользователе
                 */
                var updateCurrentUserInfo = function () {
                    var data = USER_DATA;
                    // Формируем строку ФИО
                    $scope.security.userTitle = data.user.fullShortName;
                };

                $scope.mainMenu = [];

                var menuItem = {id: 'menu-production', title: 'menu.taxes', items: []};
                menuItem.items.push({title: 'menu.taxes.ndfl', sref: 'ndflForms'});
                $scope.mainMenu.push(menuItem);

                $scope.mainMenu.forEach(function (item) {
                    // Добавляем стрелочку выпадающего меню (\u25BE - маленькая, \u25BС - крупная)
                    $translate(item.title).then(function (txt) {
                        item.title = txt + ' \u25BE'
                    });
                    // Выставляем ссылки по умолчанию для пустых подпунктов главного меню
                    item.items.forEach(function (submenu) {
                        if (submenu.sref === null && submenu.action === null && submenu.href === null) {
                            submenu.href = '#';
                        }
                        $translate(submenu.title).then(function (txt) {
                            submenu.title = txt;
                        })
                    })
                });
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