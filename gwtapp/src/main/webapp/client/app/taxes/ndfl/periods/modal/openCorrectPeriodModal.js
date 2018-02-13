(function () {
    'use strict';

    /**
     * @description Модуль модального окна создания корректирующего периода
     */

    angular.module('app.openCorrectPeriodModal', ['ui.router', 'app.rest'])

    /**
     * @description  Контроллер модального окна открытия корректирующего перода
     */
        .controller('openCorrectCtrlModal', ['$scope', '$shareData', '$http', '$modalInstance', '$logPanel', 'ValidationUtils', '$dialogs', '$filter', 'APP_CONSTANTS',
            function ($scope, $shareData, $http, $modalInstance, $logPanel, ValidationUtils, $dialogs, $filter, APP_CONSTANTS) {

                $scope.department = $shareData.department;
                $scope.departmentReportPeriod = angular.copy($shareData.period);
                $scope.departmentReportPeriod.correctionDate = new Date();

                $scope.correctPeriod = {
                    reportPeriod: {
                        id: $shareData.period.reportPeriodId,
                        taxPeriod: {
                            year: $shareData.period.year
                        },
                        name: $shareData.period.name
                    }
                };

                /**
                 * @description Обработчик кнопки "Открыть"
                 **/
                $scope.save = function () {
                    if (ValidationUtils.checkYearInterval($scope.correctPeriod.reportPeriod.taxPeriod.year, $scope.departmentReportPeriod.correctionDate.substr(0, 4))) {
                        $http({
                            method: "POST",
                            url: "controller/rest/departmentReportPeriod/status?projection=checkCorrectPeriod",
                            params: {
                                departmentReportPeriod: JSON.stringify({
                                    id: $scope.departmentReportPeriod.id,
                                    reportPeriod: $scope.correctPeriod.reportPeriod,
                                    correctionDate: $scope.departmentReportPeriod.correctionDate,
                                    departmentId: $scope.department.id
                                })
                            }
                        }).then(function (status) {
                            if (status.data){
                                switch (status.data){
                                    case APP_CONSTANTS.REPORT_PERIOD_STATUS.CORRECTION_PERIOD_LAST_OPEN:
                                        $dialogs.errorDialog({
                                            content: $filter('translate')('reportPeriod.error.openCorrectionPeriod.last.text', {
                                                correctDate: new Date($scope.departmentReportPeriod.correctionDate).format("dd.mm.yyyy")
                                            })
                                        });
                                        break;
                                    case APP_CONSTANTS.REPORT_PERIOD_STATUS.EXISTS_OPEN_CORRECTION_PERIOD_BEFORE:
                                        $dialogs.errorDialog({
                                            content: $filter('translate')('reportPeriod.error.openCorrectionPeriod.before.text', {
                                                correctDate: new Date($scope.departmentReportPeriod.correctionDate).format("dd.mm.yyyy")
                                            })
                                        });
                                        break;
                                    case APP_CONSTANTS.REPORT_PERIOD_STATUS.CLOSE:
                                        var confirmDialogModalInstance = $dialogs.confirmDialog({
                                            title: $filter('translate')('reportPeriod.confirm.openCorrectionPeriod.reopenPeriod.title'),
                                            content: $filter('translate')('reportPeriod.confirm.openCorrectionPeriod.reopenPeriod.text', {
                                                correctDate: new Date($scope.departmentReportPeriod.correctionDate).format("dd.mm.yyyy")
                                            }),
                                            okBtnCaption: $filter('translate')('common.button.yes'),
                                            cancelBtnCaption: $filter('translate')('common.button.no'),
                                            okBtnClick: function () {
                                                confirmDialogModalInstance.close();
                                                $scope.openCorrectPeriod();
                                            }
                                        });
                                        break;
                                    case APP_CONSTANTS.REPORT_PERIOD_STATUS.OPEN:
                                        $dialogs.errorDialog({
                                            content: $filter('translate')('reportPeriod.error.openCorrectionPeriod.alreadyOpen.text', {
                                                correctDate: new Date($scope.departmentReportPeriod.correctionDate).format("dd.mm.yyyy")
                                            })
                                        });
                                        break;
                                    default:
                                        $scope.openCorrectPeriod();
                                }
                            }
                        });
                    } else {
                        $dialogs.errorDialog({
                            content: $filter('translate')('reportPeriod.error.openCorrectionPeriod.smallCorrectionYear')
                        });
                    }
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('reportPeriod.confirm.openCorrectionPeriod.title'),
                        content: $filter('translate')('reportPeriod.confirm.openCorrectionPeriod.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.dismiss();
                        }
                    });
                };

                /**
                 * @description метод, выполняющий запрос на сервер для открытия коррекционного периода
                 */
                $scope.openCorrectPeriod = function () {
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
                };

            }]);
}());

