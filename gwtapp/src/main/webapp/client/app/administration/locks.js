(function () {
    'use strict';

    /**
     * @description Модуль для страницы Список блокировок
     */
    angular.module('app.lockDataList', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('lockDataList', {
                url: '/administration/locks',
                templateUrl: 'client/app/administration/locks.html',
                controller: 'locksCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_BLOCK)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        /**
         * @description Контроллер страницы "Список блокировок"
         */
        .controller('locksCtrl', ['$scope', '$http', '$filter', '$logPanel', 'lockDataResource', 'APP_CONSTANTS',
            function ($scope, $http, $filter, $logPanel, lockDataResource, APP_CONSTANTS) {

                $scope.searchFilter = {};

                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.locksGrid.ctrl.refreshGrid(page);
                };

                $scope.locksGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: lockDataResource,
                        requestParameters: function () {
                            return {
                                filter: $scope.searchFilter.filter
                            };
                        },
                        colNames: [
                            $filter('translate')('locks.title.dateLock'),
                            $filter('translate')('locks.title.key'),
                            $filter('translate')('locks.title.user'),
                            $filter('translate')('locks.title.description')],
                        colModel: [
                            {
                                name: 'dateLock',
                                index: 'date_lock',
                                width: 200,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {name: 'key', index: 'key', width: 220},
                            {name: 'user', index: 'user', width: 250},
                            {name: 'description', index: 'description', width: 350}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'id',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: true
                    }
                };

                /**
                 * @description удаление блокировки
                 */
                $scope.deleteLock = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/lock/delete",
                        params: {
                            keys: $filter('idExtractor')($scope.locksGrid.value, 'key')
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        $scope.refreshGrid(1);
                    });
                };

                // Проверка, все ли выбранные блокировки позволено снимать
                $scope.allSelectedAreAllowedToUnlock = function () {
                    var selectedBlocks = $scope.locksGrid.value;
                    if (selectedBlocks && selectedBlocks.length > 0) {
                        for (var i in selectedBlocks) {
                            var block = selectedBlocks[i];
                            if (!block.allowedToUnlock) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return false;
                }
            }])

}());