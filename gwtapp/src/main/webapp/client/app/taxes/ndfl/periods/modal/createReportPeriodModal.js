(function () {
    'use strict';

    /**
     * @description Модуль для создания и редактирования отчетных периодов
     */

    angular.module('app.reportPeriodModal', ['ui.router', 'app.rest'])

    /**
     * @description Контроллер создания и редактирования отчетных периодов
     */
        .controller('reportPeriodCtrlModal', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', '$shareData', '$http', '$logPanel', 'LogEntryResource',
            'ReportPeriodTypeResource', '$dialogs',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, $shareData, $http, $logPanel, LogEntryResource, ReportPeriodTypeResource,
                      $dialogs) {

                $scope.isAdd = $shareData.isAdd;

                $scope.period = {
                    reportPeriod: {
                        taxPeriod: {
                            year: new Date().getFullYear()
                        },
                        dictPeriod: undefined
                    }
                };

                if ($scope.isAdd) {
                    $scope.title = $filter('translate')('reportPeriod.pils.openPeriod');
                    $scope.department = $shareData.department;
                } else {
                    $scope.title = $filter('translate')('reportPeriod.pils.editPeriod');
                    $scope.period.id = $shareData.period.id;
                    $scope.department = $shareData.department;
                    $scope.period.reportPeriod.taxPeriod.year = $shareData.period.year;
                    $scope.period.reportPeriod.name = $shareData.period.name;
                    ReportPeriodTypeResource.query({
                        dictTaxPeriodId: $shareData.period.dictTaxPeriodId
                    }, function (data) {
                        if (data) {
                            $scope.period.reportPeriod.dictPeriod = data;
                        }
                    });
                }


                /**
                 * @description Обработчик кнопки "Создать"
                 */
                $scope.save = function () {
                    if ($scope.isAdd) {
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
                                                $modalInstance.close();
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
                                                $modalInstance.close();
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    } else {
                        // Проверяем статус редактируемого периода
                        $http({
                            method: "POST",
                            url: "controller/rest/departmentReportPeriod/status",
                            params: {
                                dictTaxPeriodId: $shareData.period.dictTaxPeriodId,
                                year: $shareData.period.year,
                                departmentId: $scope.department.id
                            }
                        }).then(function (status) {
                            if (status.data && status.data === APP_CONSTANTS.REPORT_PERIOD_STATUS.CLOSE) {
                                $dialogs.errorDialog({
                                    content: $filter('translate')('reportPeriod.error.editPeriod.alreadyClose.text')
                                });
                                return;
                            } else {
                                if (status.data && status.data === APP_CONSTANTS.REPORT_PERIOD_STATUS.CORRECTION_PERIOD_ALREADY_EXIST) {
                                    $dialogs.errorDialog({
                                        content: $filter('translate')('reportPeriod.error.editPeriod.hasCorPeriod.text')
                                    });
                                    return;
                                }
                            }
                            if ($scope.period.reportPeriod.dictPeriod.id === $shareData.period.dictTaxPeriodId && $scope.period.reportPeriod.taxPeriod.year === $shareData.period.year) {
                                $dialogs.errorDialog({
                                    title: $filter('translate')('reportPeriod.error.editPeriod.noChange.title'),
                                    content: $filter('translate')('reportPeriod.error.editPeriod.noChange.text')
                                });
                                return;
                            }
                            // Проверяем наличие периода в системе
                            $http({
                                method: "POST",
                                url: "controller/rest/departmentReportPeriod/status",
                                params: {
                                    dictTaxPeriodId: $scope.period.reportPeriod.dictPeriod.id,
                                    year: $scope.period.reportPeriod.taxPeriod.year,
                                    departmentId: $scope.department.id
                                }
                            }).then(function (status) {
                                if (status.data && status.data !== APP_CONSTANTS.REPORT_PERIOD_STATUS.NOT_EXIST) {
                                    $dialogs.errorDialog({
                                        content: $filter('translate')('reportPeriod.error.editPeriod.alreadyExist.text')
                                    });
                                    return;
                                }
                                // Сохраняем отредактированный период
                                $http({
                                    method: "POST",
                                    url: "controller/rest/departmentReportPeriod/" + $scope.period.id,
                                    params: {
                                        departmentReportPeriod: JSON.stringify({
                                            id: $scope.period.id,
                                            reportPeriod: {
                                                dictTaxPeriodId: $scope.period.reportPeriod.dictPeriod.id,
                                                taxPeriod: {
                                                    year: $scope.period.reportPeriod.taxPeriod.year
                                                }
                                            }
                                        })
                                    }
                                }).then(function (response) {
                                    // Проверяем на наличие ошибок при сохранении
                                    LogEntryResource.query({
                                            uuid: response.data,
                                            projection: 'count'
                                        }, function (data) {
                                            if ((data.ERROR + data.WARNING) > 0) {
                                                $logPanel.open('log-panel-container', response.data);
                                                $dialogs.errorDialog({
                                                    content: $filter('translate')('reportPeriod.error.editPeriod.text')
                                                });
                                            } else {
                                                $logPanel.open('log-panel-container', response.data);
                                                $modalInstance.close();
                                            }
                                        }
                                    );
                                });
                            });
                        });
                    }
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    if ($scope.isAdd) {
                        $dialogs.confirmDialog({
                            title: $filter('translate')('reportPeriod.confirm.openPeriod.title'),
                            content: $filter('translate')('reportPeriod.confirm.openPeriod.text'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                $modalInstance.dismiss();
                            }
                        });
                    } else {
                        $dialogs.confirmDialog({
                            title: $filter('translate')('reportPeriod.confirm.editPeriod.title'),
                            content: $filter('translate')('reportPeriod.confirm.editPeriod.text'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                $modalInstance.dismiss();
                            }
                        });
                    }
                };
            }
        ])
    ;


}());