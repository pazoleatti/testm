(function () {
    'use strict';

    /**
     * @description Модуль для создания отчетных периодов
     */

    angular.module('app.reportPeriodModal', ['ui.router', 'app.rest', 'app.modals'])


        .controller('reportPeriodCtrlModal', ['$scope', '$filter', 'RefBookValuesResource', 'APP_CONSTANTS', '$uibModalInstance', 'DepartmentResource', 'appModals', 'ReportPeriodResource', '$http', '$logPanel', 'data', 'LogEntryResource',
            function ($scope, $filter, RefBookValuesResource, APP_CONSTANTS, $uibModalInstance, DepartmentResource, appModals, ReportPeriodResource, $http, $logPanel, data, LogEntryResource) {

                $scope.isAdd = data.isAdd;

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
                        taxPeriod: {
                            year: null
                        }
                    }
                };

                if ($scope.isAdd) {
                    $scope.title = "Открытие периода";
                    $scope.department = data.department;
                } else {
                    $scope.title = "Редактирование периода";
                    $scope.period.id = data.period.id;
                    $scope.department = data.department;
                    $scope.period.reportPeriod.taxPeriod.year = data.period.year;
                    $scope.period.reportPeriod.name = data.period.name;
                }


                /**
                 * @description загрузчик данных для select'a
                 */
                $scope.periodSelect = {
                    options: {
                        data: {
                            results: [],
                            text: $filter('reportPeriodFormatter')
                        },
                        formatSelection: $filter('reportPeriodFormatter'),
                        formatResult: $filter('reportPeriodFormatter'),
                        multiple: false,
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };

                /**
                 * @description Обработчик кнопки "Создать"
                 */
                $scope.save = function () {
                    if ($scope.isAdd) {
                        $http({
                            method: "POST",
                            url: "controller/rest/reportPeriods/newPeriod",
                            params: {
                                departmentReportPeriod: JSON.stringify({
                                    reportPeriod: {
                                        dictTaxPeriodId: $scope.period.reportPeriod.id,
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
                                $uibModalInstance.close();
                            }
                        });
                    }else {
                        ReportPeriodResource.query({
                            projection: 'checkNotAccepted',
                            id: data.period.id
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
                                            url: "controller/rest/reportPeriods/updatePeriod",
                                            params: {
                                                departmentReportPeriod: JSON.stringify({
                                                    id: $scope.period.id,
                                                    reportPeriod: {
                                                        dictTaxPeriodId: $scope.period.reportPeriod.id,
                                                        taxPeriod: {
                                                            year: $scope.period.reportPeriod.taxPeriod.year
                                                        }
                                                    }
                                                })
                                            }
                                        }).then(function (response) {
                                            if (response.data) {
                                                $logPanel.open('log-panel-container', response.data);
                                                $uibModalInstance.close();
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
                                    $uibModalInstance.dismiss('Canceled');
                                };

                                /**
                                 * @description Открытие модального окна создания периода
                                 */
                                $scope.openPeriodType = function () {
                                    var modal = appModals.create('client/app/taxes/ndfl/periods/modal/selectReportPeriodModal.html', 'reportPeriodTypeCtrlModal');
                                    modal.result.then(function (response) {
                                        if (response.length !== 0) {
                                            var taxPeriod = $scope.period.reportPeriod.taxPeriod;
                                            $scope.period.reportPeriod = response[0];
                                            $scope.period.reportPeriod.taxPeriod = taxPeriod;
                                        }
                                    });
                                };
                            }
                        ])
                        ;


                    }());