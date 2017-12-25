(function () {
    'use strict';

    /**
     * @description Модуль для создания и редактирования отчетных периодов
     */

    angular.module('app.reportPeriodModal', ['ui.router', 'app.rest'])


    /**
     * @description Контроллер создания и редактирования отчетных периодов
     */
        .controller('reportPeriodCtrlModal', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', 'DepartmentResource', '$shareData', 'DepartmentReportPeriodCheckerResource', '$http', '$logPanel', 'LogEntryResource',
            'ReportPeriodTypeResource', '$dialogs', 'OpenPeriodResource',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, DepartmentResource, $shareData, DepartmentReportPeriodCheckerResource, $http, $logPanel, LogEntryResource, ReportPeriodTypeResource,
                      $dialogs, OpenPeriodResource) {

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
                        id: $shareData.period.dictTaxPeriodId
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
                            if (response.data && response.data === APP_CONSTANTS.REPORT_PERIOD_STATUS.CLOSE){
                                $dialogs.confirmDialog({
                                    title: $filter('translate')('title.confirm'),
                                    content: $filter('translate')('reportPeriod.confirm.openPeriod.reopenPeriod.text'),
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick: function () {
                                        OpenPeriodResource.query({
                                            reportPeriod: {
                                                dictTaxPeriodId: $scope.period.reportPeriod.dictPeriod.id,
                                                taxPeriod: {
                                                    year: $scope.period.reportPeriod.taxPeriod.year
                                                }
                                            },
                                            departmentId: $scope.department.id
                                        }, function (response) {
                                            if (response.data) {
                                                $logPanel.open('log-panel-container', response.data);
                                                $modalInstance.close();
                                            }
                                        });
                                    }
                                });
                            }else {
                                if (response.data && response.data === APP_CONSTANTS.REPORT_PERIOD_STATUS.OPEN){
                                    $dialogs.errorDialog({
                                        content: $filter('translate')('reportPeriod.error.openPeriod.alreadyOpen')
                                    });
                                }else {
                                    if (response.data && response.data === APP_CONSTANTS.REPORT_PERIOD_STATUS.CORRECTION_PERIOD_ALREADY_EXIST){
                                        $dialogs.errorDialog({
                                            content: $filter('translate')('reportPeriod.error.openPeriod.hasCorrectionPeriod')
                                        });
                                    }else {
                                        OpenPeriodResource.query({
                                            reportPeriod: {
                                                dictTaxPeriodId: $scope.period.reportPeriod.dictPeriod.id,
                                                taxPeriod: {
                                                    year: $scope.period.reportPeriod.taxPeriod.year
                                                }
                                            },
                                            departmentId: $scope.department.id
                                        }, function (response) {
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
                        $http({
                            method: "POST",
                            url: "controller/rest/departmentReportPeriod/" + $shareData.period.id + "?projection=checkHasNotAccepted"
                        }).then(function (logger) {
                            LogEntryResource.query({
                                    uuid: logger,
                                    projection: 'count'
                                },
                                function (data) {
                                    if ((data.ERROR + data.WARNING) !== 0) {
                                        $logPanel.open('log-panel-container', logger);
                                    } else {
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
                                            if (response.data) {
                                                $logPanel.open('log-panel-container', response.data);
                                                $dialogs.errorDialog({
                                                    content: $filter('translate')('reportPeriod.error.editPeriod.text')
                                                });
                                            }
                                        });
                                    }
                                });
                        });
                    }
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    if ($scope.isAdd){
                        $dialogs.confirmDialog({
                            title: $filter('translate')('reportPeriod.confirm.openPeriod.title'),
                            content: $filter('translate')('reportPeriod.confirm.openPeriod.text'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                     $modalInstance.dismiss('Canceled');
                            }
                        });
                    }
                };
            }
        ])
    ;


}());