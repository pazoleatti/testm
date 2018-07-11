(function () {
    'use strict';

    /**
     * @description Модуль для страницы Список асинхронных задач
     */
    angular.module('app.asyncTaskList', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('asyncTaskList', {
                url: '/administration/async',
                templateUrl: 'client/app/administration/async.html',
                controller: 'asyncCtrl',
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
        .controller('asyncCtrl', ['$scope', '$filter', 'asyncTaskResource', '$http', 'APP_CONSTANTS',
            function ($scope, $filter, asyncTaskResource, $http, APP_CONSTANTS) {

                $scope.searchFilter = {};

                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.asyncGrid.ctrl.refreshGrid(page);
                };

                $scope.asyncGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: asyncTaskResource,
                        requestParameters: function () {
                            return {
                                filter: $scope.searchFilter.filter
                            };
                        },
                        colNames: [
                            $filter('translate')('async.title.number'),
                            $filter('translate')('async.title.createDate'),
                            $filter('translate')('async.title.user'),
                            $filter('translate')('async.title.node'),
                            $filter('translate')('async.title.queue'),
                            $filter('translate')('async.title.state'),
                            $filter('translate')('async.title.stateDate'),
                            $filter('translate')('async.title.description')],
                        colModel: [
                            {name: 'id', index: 'id', width: 65},
                            {
                                name: 'createDate',
                                index: 'create_date',
                                width: 120,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'user', index: 'user', width: 250},
                            {name: 'node', index: 'node', width: 150},
                            {name: 'queue', index: 'queue', width: 250},
                            {name: 'state', index: 'state', width: 250},
                            {
                                name: 'stateDate',
                                index: 'stateDate',
                                width: 200,
                                formatter: $filter('dateTimeFormatter')
                            },
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
                $scope.interruptTask = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/async/interrupt",
                        params: {
                            ids: $filter('idExtractor')($scope.asyncGrid.value)
                        }
                    }).then(function () {
                        $scope.refreshGrid(1);
                    });
                };
            }])

}());