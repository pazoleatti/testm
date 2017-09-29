(function () {
    'use strict';

    /**
     * @description Модуль для страницы Планировщик задач
     */
    angular.module('app.schedulerTaskList', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('schedulerTaskList', {
                url: '/administration/schedulerTaskList',
                templateUrl: 'client/app/administration/schedulerTaskList.html',
                controller: 'schedulerTaskListCtrl'
            });
        }])

        /**
         * @description Контроллер страницы "Планировщик задач"
         */
        .controller('schedulerTaskListCtrl', ['$scope', '$filter', 'schedulerTaskResource', '$http',
            function ($scope, $filter, schedulerTaskResource, $http) {

                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.taskListGrid.ctrl.refreshGrid(page);
                };

                $scope.taskListGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: schedulerTaskResource,
                        height: 250,
                        colNames: [
                            $filter('translate')('taskList.title.number'),
                            $filter('translate')('taskList.title.name'),
                            $filter('translate')('taskList.title.status'),
                            $filter('translate')('taskList.title.schedule'),
                            $filter('translate')('taskList.title.editDate'),
                            $filter('translate')('taskList.title.lastStartDate'),
                            $filter('translate')('taskList.title.nextStartDate')],
                        colModel: [
                            {name: 'id', index: 'id', width: 60, key: true},
                            {name: 'name', index: 'task_name', width: 170},
                            {name: 'state', index: 'active', width: 200},
                            {name: 'schedule', index: 'schedule', width: 175},
                            {
                                name: 'modificationDate',
                                index: 'modification_date',
                                width: 240,
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'lastFireTime',
                                index: 'last_fire_date',
                                width: 240,
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'nextFireTime',
                                index: 'next_fire_time',
                                width: 190,
                                formatter: $filter('dateFormatter')
                            }
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
                 * @description Запуск выполнения по расписанию
                 */
                $scope.activate = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/schedulerTask/activate",
                        params: {
                            ids: getIds()
                        }
                    }).then(function () {
                        $scope.refreshGrid(1);
                    });
                };

                /**
                 * @description Остановка выполнения по расписанию
                 */
                $scope.deactivate = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/schedulerTask/deactivate",
                        params: {
                            ids: getIds()
                        }
                    }).then(function () {
                        $scope.refreshGrid(1);
                    });
                };

                /**
                 * @description Изменения активности задачи
                 */
                function getIds() {
                    var ids = [];
                    for (var i = 0; i < $scope.taskListGrid.value.length; i++) {
                        ids.push($scope.taskListGrid.value[i].id);
                    }
                    return ids;
                }
            }])

}());