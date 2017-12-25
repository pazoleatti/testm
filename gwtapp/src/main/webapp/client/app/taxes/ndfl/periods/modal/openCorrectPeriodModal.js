(function () {
    'use strict';

    /**
     * @description Модуль модального окна создания корректирующего периода
     */

    angular.module('app.openCorrectPeriodModal', ['ui.router', 'app.rest'])

        .filter('correctPeriodFormatter', function () {
            return function (entity) {
                return entity.id ? entity.taxPeriod.year + ": " + entity.name : "";
            };
        })

        /**
         * @description  Контроллер модального окна открытия корректирующего перода
         */
        .controller('openCorrectCtrlModal', ['$scope', '$shareData', '$http', '$modalInstance', '$logPanel', 'ValidationUtils', '$dialogs', '$filter',
            function ($scope, $shareData, $http, $modalInstance, $logPanel, ValidationUtils, $dialogs, $filter) {

                $scope.department = $shareData.department;
                $scope.correctionDate = new Date();
                $scope.departmentReportPeriod = $shareData.period;

                $scope.correctPeriod = {
                    reportPeriod : {
                        id: $shareData.period.reportPeriodId,
                        taxPeriod: {
                            year : $shareData.period.year
                        },
                        name: $shareData.period.name
                    }
                };

                /**
                 * @description Обработчик кнопки "Открыть"
                 **/
                $scope.save = function () {
                    if (ValidationUtils.checkYearInterval($scope.correctPeriod.reportPeriod.taxPeriod.year , $scope.correctionDate.getFullYear())) {
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
                            content: $filter('translate')('reportPeriod.error.openCorrectionPeriod.smallCorrectionYear')
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

