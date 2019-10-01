(function () {
    'use strict';

    /**
     * @description Контроллер создания отчетного периода
     */

    angular.module('app.reportPeriodModal', ['ui.router', 'app.rest'])

    /**
     * @description Контроллер создания отчетного периода
     */
        .controller('reportPeriodCtrlModal', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', '$shareData', '$http', '$logPanel', 'LogEntryResource', '$dialogs',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, $shareData, $http, $logPanel, LogEntryResource, $dialogs) {
                $scope.title = $filter('translate')('reportPeriod.pils.openPeriod');

                $scope.yearMin = $shareData.yearMin;
                $scope.yearMax = $shareData.yearMax;

                // Данные формы
                $scope.form = {department: undefined, year: new Date().getFullYear(), dictPeriod: undefined};

                /**
                 * @description Обработчик кнопки "Создать"
                 */
                $scope.save = function () {
                    $logPanel.close();
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentReportPeriod/open",
                        params: {
                            departmentReportPeriod: JSON.stringify({
                                departmentId: $scope.form.department.id,
                                reportPeriod: {
                                    dictTaxPeriodId: $scope.form.dictPeriod.id,
                                    taxPeriod: {
                                        year: $scope.form.year
                                    }
                                }
                            })
                        }
                    }).then(function (response) {
                        if (response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        if (response.data.error) {
                            $dialogs.errorDialog({content: response.data.error});
                        } else {
                            $modalInstance.close($scope.form.year);
                        }
                    });
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('reportPeriod.confirm.openPeriod.title'),
                        content: $filter('translate')('reportPeriod.confirm.openPeriod.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.dismiss();
                        }
                    });
                };
            }
        ]);
}());