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
                controller: 'locksCtrl'
            });
        }])

        /**
         * @description Контроллер страницы "Список блокировок"
         */
        .controller('locksCtrl', ['$scope', '$filter', 'lockDataResource', '$http',
            function ($scope, $filter, lockDataResource, $http) {

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
                        height: 250,
                        colNames: [
                            $filter('translate')('locks.title.dateLock'),
                            $filter('translate')('locks.title.key'),
                            $filter('translate')('locks.title.description'),
                            $filter('translate')('locks.title.user')],
                        colModel: [
                            {
                                name: 'dateLock',
                                index: 'date_lock',
                                width: 240,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'key', index: 'key', width: 175},
                            {name: 'description', index: 'description', width: 175},
                            {name: 'user', index: 'user', width: 175}
                        ],
                        rowNum: 10,
                        rowList: [10, 20, 30],
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
                            ids: $filter('idExtractor')($scope.locksGrid.value)
                        }
                    }).then(function () {
                        $scope.refreshGrid(1);
                    });
                };
            }])

}());