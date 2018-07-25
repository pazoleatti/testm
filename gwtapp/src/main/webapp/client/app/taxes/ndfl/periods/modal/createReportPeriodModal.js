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

                $scope.period = {
                    reportPeriod: {
                        taxPeriod: {
                            year: new Date().getFullYear()
                        },
                        dictPeriod: undefined
                    }
                };
                $scope.title = $filter('translate')('reportPeriod.pils.openPeriod');
                $scope.department = $shareData.department;

                /**
                 * @description Обработчик кнопки "Создать"
                 */
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "controller/rest/departmentReportPeriod/status",
                        params: {
                            dictTaxPeriodId: $scope.period.reportPeriod.dictPeriod.id,
                            year: $scope.period.reportPeriod.taxPeriod.year,
                            departmentId: $scope.department.id
                        }
                    }).then(function (response) {
                        if (response.data && response.data === APP_CONSTANTS.REPORT_PERIOD_STATUS.CLOSE) {
                            // переоткрытие закрытого периода
                            $dialogs.confirmDialog({
                                title: $filter('translate')('title.confirm'),
                                content: $filter('translate')('reportPeriod.confirm.openPeriod.reopenPeriod.text'),
                                okBtnCaption: $filter('translate')('common.button.yes'),
                                cancelBtnCaption: $filter('translate')('common.button.no'),
                                okBtnClick: function () {
                                    $http({
                                        method: "POST",
                                        url: "controller/actions/departmentReportPeriod/open",
                                        params: {
                                            departmentReportPeriod: JSON.stringify({
                                                departmentId: $scope.department.id,
                                                reportPeriod: {
                                                    dictTaxPeriodId: $scope.period.reportPeriod.dictPeriod.id,
                                                    taxPeriod: {
                                                        year: $scope.period.reportPeriod.taxPeriod.year
                                                    }
                                                }
                                            })
                                        }
                                    }).then(function (response) {
                                        if (response.data) {
                                            $logPanel.open('log-panel-container', response.data);
                                            $modalInstance.close($scope.period.reportPeriod.taxPeriod.year);
                                        }
                                    });
                                }
                            });
                        } else {
                            if (response.data && response.data === APP_CONSTANTS.REPORT_PERIOD_STATUS.OPEN) {
                                $dialogs.errorDialog({
                                    content: $filter('translate')('reportPeriod.error.openPeriod.alreadyOpen')
                                });
                            } else {
                                if (response.data && response.data === APP_CONSTANTS.REPORT_PERIOD_STATUS.CORRECTION_PERIOD_ALREADY_EXIST) {
                                    $dialogs.errorDialog({
                                        content: $filter('translate')('reportPeriod.error.openPeriod.hasCorrectionPeriod')
                                    });
                                } else {
                                    $http({
                                        method: "POST",
                                        url: "controller/actions/departmentReportPeriod/open",
                                        params: {
                                            departmentReportPeriod: JSON.stringify({
                                                departmentId: $scope.department.id,
                                                reportPeriod: {
                                                    dictTaxPeriodId: $scope.period.reportPeriod.dictPeriod.id,
                                                    taxPeriod: {
                                                        year: $scope.period.reportPeriod.taxPeriod.year
                                                    }
                                                }
                                            })
                                        }
                                    }).then(function (response) {
                                        if (response.data) {
                                            $logPanel.open('log-panel-container', response.data);
                                            $modalInstance.close($scope.period.reportPeriod.taxPeriod.year);
                                        }
                                    });
                                }
                            }
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
        ])
    ;


}());