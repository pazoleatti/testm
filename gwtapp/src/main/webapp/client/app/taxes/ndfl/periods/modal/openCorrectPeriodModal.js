(function () {
    'use strict';

    /**
     * @description конфигурация модального окна выбора типа отчетного периода
     */

    angular.module('app.openCorrectPeriodModal', ['ui.router', 'app.rest', 'app.modals'])

        .filter('correctPeriodFormatter', function () {
            return function (entity) {
                return entity.id ? entity.taxPeriod.year + ": " + entity.name : "";
            };
        })

        .controller('openCorrectCtrlModal', ['$scope', 'data', '$http', '$uibModalInstance', 'appModals', '$logPanel',
            function ($scope, data, $http, $uibModalInstance, appModals, $logPanel) {

                $scope.department = data.department;
                $scope.correctionDate = data.period.correctionDate;
                $scope.departmentReportPeriod = data.period;

                $scope.correctPeriod = {
                    reportPeriod : null
                };

                /**
                 * @description Обработчик кнопки "Открыть"
                 **/
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "controller/rest/reportPeriods/openCorrectPeriod",
                        params: {
                            filter: JSON.stringify({
                                id: $scope.departmentReportPeriod.id,
                                reportPeriod: $scope.correctPeriod.reportPeriod,
                                simpleCorrectionDate: $scope.correctionDate,
                                departmentId: $scope.department.id
                            })
                        }
                    }).then(function (response) {
                        if (response.data) {
                            $logPanel.open('log-panel-container', response.data);
                            $uibModalInstance.close();
                        }
                    });
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    $uibModalInstance.dismiss('Canceled');
                };

            }]);
}());
