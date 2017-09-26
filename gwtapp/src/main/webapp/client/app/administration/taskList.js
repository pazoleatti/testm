(function () {
    'use strict';

    /**
     * @description Модуль для страницы Планировщик задач
     */
    angular.module('app.taskList', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('taskList', {
                url: '/administration/taskList',
                templateUrl: 'client/app/administration/taskList.html',
                controller: 'taskListCtrl'
            });
        }])

        /**
         * @description Контроллер страницы "Планировщик задач"
         */
        .controller('taskListCtrl', ['$scope', '$filter', 'taskList', '$http',
            function ($scope, $filter, taskList, $http) {

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
                        angularResource: taskList,
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
                $scope.runTasks = function () {
                    changeStateTasks(true);
                };

                /**
                 * @description Остановка выполнения по расписанию
                 */
                $scope.stopTasks = function () {
                    changeStateTasks(false);
                };

                /**
                 * @description Изменения активности задачи
                 * @param isActive признак активности задачи
                 */
                function changeStateTasks(isActive) {
                    var ids = [];
                    for (var i = 0; i < $scope.taskListGrid.value.length; i++) {
                        ids.push($scope.taskListGrid.value[i].id);
                    }

                    $http({
                        method: "POST",
                        url: "controller/actions/taskList/changeState",
                        params: {
                            ids: ids,
                            isActive: isActive
                        }
                    }).then(function () {
                        $scope.refreshGrid(1);
                    });
                }
            }])

}());