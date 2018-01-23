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
                $scope.departmentReportPeriod = angular.copy($shareData.period);
                $scope.departmentReportPeriod.correctionDate = new Date();

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
                    if (ValidationUtils.checkYearInterval($scope.correctPeriod.reportPeriod.taxPeriod.year , $scope.departmentReportPeriod.correctionDate.substr(0, 4))) {
                        $http({
                            method: "POST",
                            url: "controller/actions/departmentReportPeriod/openCorrectPeriod",
                            params: {
                                departmentReportPeriod: JSON.stringify({
                                    id: $scope.departmentReportPeriod.id,
                                    reportPeriod: $scope.correctPeriod.reportPeriod,
                                    correctionDate: $scope.departmentReportPeriod.correctionDate,
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

