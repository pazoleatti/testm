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

        .controller('openCorrectCtrlModal', ['$scope', '$shareData', '$http', '$modalInstance', '$logPanel', 'ValidationUtils', '$dialogs', '$filter',
            function ($scope, $shareData, $http, $modalInstance, $logPanel, ValidationUtils, $dialogs, $filter) {

                $scope.department = $shareData.department;
                $scope.correctionDate = undefined;
                $scope.departmentReportPeriod = $shareData.period;

                $scope.correctPeriod = {
                    reportPeriod : undefined
                };

                /**
                 * @description Обработчик кнопки "Открыть"
                 **/
                $scope.save = function () {
                    if (ValidationUtils.checkDateValidateInterval($scope.correctionDate)) {
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
                    }else {
                        $dialogs.errorDialog({
                            content: $filter('translate')('common.validation.dateInterval')
                        });
                    }
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    $modalInstance.dismiss('Canceled');
                };

            }]);
}());
