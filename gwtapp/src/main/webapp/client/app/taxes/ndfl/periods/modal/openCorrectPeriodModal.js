(function () {
    'use strict';

    /**
     * @description Модуль модального окна создания корректирующего периода
     */

    angular.module('app.openCorrectPeriodModal', ['ui.router', 'app.rest'])

    /**
     * @description  Контроллер модального окна открытия корректирующего перода
     */
        .controller('openCorrectCtrlModal', ['$scope', '$shareData', '$http', '$modalInstance', '$logPanel', 'ValidationUtils', '$dialogs', '$filter',
            function ($scope, $shareData, $http, $modalInstance, $logPanel, ValidationUtils, $dialogs, $filter) {

                // Данные формы
                var reportPeriodStr = $filter('departmentReportPeriodFormatter')($shareData.selectedPeriod)
                $scope.form = {department: $shareData.department, correctionDate: undefined,
                    reportPeriod: reportPeriodStr};

                /**
                 * @description Обработчик кнопки "Открыть"
                 **/
                $scope.save = function () {
                    $logPanel.close();
                    if ($scope.form.correctionDate <= $shareData.selectedPeriod.endDate) {
                        $dialogs.errorDialog({
                            content: $filter('translate')('reportPeriod.error.openCorrectionPeriod.smallCorrectionYear', {endDate: new Date($shareData.selectedPeriod.endDate).format("dd.mm.yyyy")})
                        });
                    } else {
                        $http({
                            method: "POST",
                            url: "controller/actions/departmentReportPeriod/openCorrectPeriod",
                            params: {
                                action: JSON.stringify({
                                    departmentReportPeriodId: $shareData.selectedPeriod.id,
                                    correctionDate: $scope.form.correctionDate
                                })
                            }
                        }).then(function (response) {
                            if (response.data.uuid) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                            if (response.data.error) {
                                $dialogs.errorDialog({content: response.data.error});
                            } else {
                                $modalInstance.close();
                            }
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
            }]);
}());

