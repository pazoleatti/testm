(function () {
    'use strict';

    /**
     * @description Модуль модального окна оповещений
     */
    angular.module('app.application2', [])
    /**
     * @description Контроллер модального окна оповещений
     */
        .controller('application2Ctrl', ['$scope', '$modalInstance', '$http', '$logPanel',
            function ($scope, $modalInstance, $http, $logPanel) {

                $scope.reportYear = new Date().getFullYear() - 1;

                /**
                 * Запуск задачи формирования Приложения 2
                 */
                $scope.create = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/createApplication2",
                        params: {
                            reportYear: $scope.reportYear
                        }
                    }).success(function (response) {
                        $logPanel.open('log-panel-container', response);
                        $modalInstance.close(response);
                    })
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss();
                };
            }
        ])
}());