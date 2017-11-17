(function () {
    'use strict';

    /**
     * @description Модуль для страницы Планировщик задач
     */
    angular.module('app.updateSchedulerTask', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('updateSchedulerTask', {
                url: '/administration/schedulerTaskList/updateSchedulerTask/{idTaskScheduler}',
                templateUrl: 'client/app/administration/updateSchedulerTask.html?v=${buildUuid}',
                controller: 'updateSchedulerTaskCtrl'
            });
        }])

        /**
         * @description Контроллер страницы "Планировщик задач"
         */
        .controller('updateSchedulerTaskCtrl', ['$scope','$state', '$filter', 'schedulerTaskResource', '$http', 'APP_CONSTANTS', '$stateParams','updateScheduleTask',
            function ($scope, $state, $filter, schedulerTaskResource, $http, APP_CONSTANTS, $stateParams, updateScheduleTask) {
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
                    href: $state.href('schedulerTaskList');
                }

                //Отправка редактируемого параметра
                $scope.actionsUpdateSchedulerfunction = function() {
                    $http({
                        method: "POST",
                        url: "controller/actions/updateSchedulerTask/",
                        params: {
                             schedulerTaskModel: $scope.updateSchedulerTask
                        }
                    });
                };

                initPage();

            }]);

}());
