(function () {
    'use strict';

    /**
     * @description Модуль для страницы "Список пользователей"
     */
    angular.module('app.usersList', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('usersList', {
                url: '/administration/users',
                templateUrl: 'client/app/administration/users.html',
                controller: 'usersCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_USERS)) {
                            $state.go("/");
                        }
                    }
                ]
            });
        }])

        /**
         * @description Контроллер страницы "Список пользователей"
         */
        .controller('usersCtrl', ['$scope', '$filter', '$window', 'usersResource', 'APP_CONSTANTS',
            function ($scope, $filter, $window, usersResource, APP_CONSTANTS) {

                $scope.refreshGrid = function (page) {
                    $scope.usersGrid.ctrl.refreshGrid(page);
                };

                /**
                 * Инициализация объекта фильтра.
                 */
                $scope.searchFilter = {
                    params: {},
                    filterName: 'usersFilter'
                };

                /**
                 * Строковое представление содержимого фильтра.
                 * @returns {string} содержимое фильтра в виде JSON-строки
                 */
                $scope.filterParams = function () {
                    return JSON.stringify({
                        departmentIds: $filter('idExtractor')($scope.searchFilter.params.departments),
                        roleIds: $filter('idExtractor')($scope.searchFilter.params.roles),
                        active: $filter('activityAttributeFormatter')($scope.searchFilter.params.active),
                        userName: $scope.searchFilter.params.userName
                    });
                };

                /**
                 * Инициализация таблицы пользователей
                 */
                $scope.usersGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: usersResource,
                        requestParameters: function () {
                            return {
                                filter: $scope.filterParams()
                            }
                        },
                        colNames: [
                            $filter('translate')('users.title.name'),
                            $filter('translate')('users.title.login'),
                            $filter('translate')('users.title.email'),
                            $filter('translate')('users.title.active'),
                            $filter('translate')('users.title.department'),
                            $filter('translate')('users.title.role'),
                            $filter('translate')('users.title.asnu')
                        ],
                        colModel: [
                            {
                                name: 'name',
                                width: 150,
                                classes: 'grid-cell-white-space'
                            },
                            {
                                name: 'login',
                                width: 150
                            },
                            {
                                name: 'email',
                                width: 250
                            },
                            {
                                name: 'active',
                                width: 80,
                                formatter: $filter('yesNoFormatter')
                            },
                            {
                                name: 'depName',
                                width: 250,
                                classes: 'grid-cell-white-space'
                            },
                            {
                                name: 'roles',
                                width: 400,
                                classes: 'grid-cell-white-space'
                            },
                            {
                                name: 'asnu',
                                width: 500,
                                classes: 'grid-cell-white-space'
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION
                    }
                };

                /**
                 * Метод для выгрузки данных в xlsx.
                 */
                $scope.downloadXlsx = function () {
                    $window.location = 'controller/rest/users/xlsx?filter=' + $scope.filterParams();
                };
            }
        ])
}());