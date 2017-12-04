(function () {
    'use strict';

    /**
     * @description конфигурация модального окна выбора типа отчетного периода
     */

    angular.module('app.openCorrectPeriodModal', ['ui.router', 'app.rest'])

        .filter('correctPeriodFormatter', function () {
            return function (entity) {
                return entity.id ? entity.taxPeriod.year + ": " + entity.name : "";
            };
        })

        .controller('openCorrectCtrlModal', ['$scope', '$shareData', '$http', '$modalInstance', '$logPanel',
            function ($scope, $shareData, $http, $modalInstance, $logPanel) {

                $scope.department = $shareData.department;
                $scope.correctionDate = $shareData.period.correctionDate;
                $scope.departmentReportPeriod = $shareData.period;

                $scope.correctPeriod = {
                    reportPeriod : null
                };

                /**
                 * @description Обработчик кнопки "Открыть"
                 **/
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentReportPeriod/openCorrectPeriod",
                        params: {
                            filter: JSON.stringify({
                                id: $scope.departmentReportPeriod.id,
                                reportPeriod: $scope.correctPeriod.reportPeriod,
                                correctionDate: $scope.correctionDate,
                                departmentId: $scope.department.id
                            })
                        }
                    }).then(function (response) {
                        if (response.data) {
                            $logPanel.open('log-panel-container', response.data);
                            $modalInstance.close();
                        }
                    });
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    $modalInstance.dismiss('Canceled');
                };

            }]);
}());
