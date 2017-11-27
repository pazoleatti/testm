(function () {
    'use strict';

    /**
     * @description Модуль для создания отчетных периодов
     */

    angular.module('app.reportPeriodModal', ['ui.router', 'app.rest', 'app.modals'])


        .controller('reportPeriodCtrlModal', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', 'DepartmentResource', '$shareData', 'DepartmentReportPeriodCheckerResource', '$http', '$logPanel', 'LogEntryResource',
            'ReportPeriodTypeResource', '$dialogs',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, DepartmentResource, $shareData, DepartmentReportPeriodCheckerResource, $http, $logPanel, LogEntryResource, ReportPeriodTypeResource,
                      $dialogs) {

                $scope.isAdd = $shareData.isAdd;

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {
                        yearStart: new Date().getFullYear(),
                        yearEnd: new Date().getFullYear(),
                        department: null
                    },
                    isClear: false,
                    filterName: 'filter'
                };

                $scope.period = {
                    reportPeriod: {
                        taxPeriod: {},
                        dictPeriod: null
                    }
                };

                if ($scope.isAdd) {
                    $scope.title = "Открытие периода";
                    $scope.department = $shareData.department;
                } else {
                    $scope.title = "Редактирование периода";
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
                                    content: $filter('translate')('title.reopenPeriod'),
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick: function () {
                                        createQuery();
                                    }
                                });
                            }else {
                                createQuery();
                            }
                        });
                    } else {
                        DepartmentReportPeriodCheckerResource.query({
                            projection: 'checkHasNotAccepted',
                            id: $shareData.period.id
                        }, function (logger) {
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
                                                $modalInstance.close();
                                            }
                                        });
                                    }
                                });
                        });
                    }
                };

                var createQuery = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentReportPeriod/open",
                        params: {
                            departmentReportPeriod: JSON.stringify({
                                reportPeriod: {
                                    dictTaxPeriodId: $scope.period.reportPeriod.dictPeriod.id,
                                    taxPeriod: {
                                        year: $scope.period.reportPeriod.taxPeriod.year
                                    }
                                },
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

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    $modalInstance.dismiss('Canceled');
                };
            }
        ])
    ;


}());