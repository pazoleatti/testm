(function () {
    'use strict';

    /**
     * @description Модуль для страницы Планировщик задач
     */
    angular.module('app.updateSchedulerTask', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('updateSchedulerTask', {
                url: '/administration/schedulerTaskList/updateSchedulerTask/{idTaskScheduler}',
                templateUrl: 'client/app/administration/updateSchedulerTask.html',
                controller: 'updateSchedulerTaskCtrl'
            });
        }])

        /**
         * @description Контроллер страницы "Планировщик задач"
         */
        .controller('updateSchedulerTaskCtrl', ['$scope','$state', '$filter', 'schedulerTaskResource', '$http',
            '$dialogs', 'APP_CONSTANTS', '$stateParams','updateScheduleTask',
            function ($scope, $state, $filter, schedulerTaskResource, $http, $dialogs, APP_CONSTANTS, $stateParams, updateScheduleTask) {
                /**
                 * @description Инициализация первичных данных на странице
                 */
                function initPage() {
                    updateScheduleTask.query({
                        idTaskScheduler: $stateParams.idTaskScheduler
                    }, function (data) {
                        $scope.updateSchedulerTask = data;
                    });

                    //преобразование json формата
                   var taskScheduleHelpUpdateParam =  $filter('translate')('taskScheduleHelpUpdateParam');
                    $scope.taskScheduleHelpUpdateParam =  taskScheduleHelpUpdateParam.toString().replace(/\\n/g, '<br/>');

                }

                //переход обратно к планировщику
                $scope.cancel = function() {
                    $state.go('schedulerTaskList');
                };

                /**
                 * @description обновление задачи планировщика
                 */
                $scope.actionsUpdateSchedulerfunction = function() {
                    // проверка валидности введенного cron выражения
                    $http({
                        method: "POST",
                        url: "controller/action/schedulerTaskData/validateCron",
                        params: {
                            cronString: $scope.updateSchedulerTask.schedule
                        }
                    }).then(function (response) {
                        if (!response.data) {
                            $dialogs.errorDialog({
                                content: $filter('translate')('taskScheduler.validation.error')
                            });
                        } else {
                            // сохранение параметра
                            $http({
                                method: "POST",
                                url: "controller/rest/schedulerTaskData/update",
                                params: {
                                    schedulerTaskModel: $scope.updateSchedulerTask
                                }
                            }).then(function() {
                                $state.go('schedulerTaskList');
                            });
                        }
                    });
                };

                initPage();

            }]);

}());
