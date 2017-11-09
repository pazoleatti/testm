(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "НДФЛ - Ведение периодов"
     */

    angular.module('app.reportPeriod',
        ['ui.router',
            'app.logPanel',
            'app.modals',
            'app.rest',
            'app.openCorrectPeriodModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('reportPeriod', {
                url: '/taxes/reportPeriod',
                templateUrl: 'client/app/taxes/ndfl//periods/ndflReportPeriod.html',
                controller: 'reportPeriodCtrl'
            });
        }])

        .filter('balanceFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (value && value === 1) {
                    return 'Да';
                } else {
                    return 'Нет';
                }
            };
        }])

        .filter('statusPeriodFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (value && value === 1) {
                    return 'Открыт';
                } else {
                    return 'Закрыт';
                }
            };
        }])

        .filter('reportPeriodFormatter', ['$filter', function ($filter) {
            return function (entity) {
                if (entity) {
                    return entity.name;
                } else {
                    return '';
                }
            };
        }])



        /**
         * @description Контроллер формы "Ведение периодов"
         */

        .controller('reportPeriodCtrl', ['$scope', '$filter', 'ReportPeriodResource', 'DepartmentResource', 'appModals', 'LogEntryResource', '$logPanel', 'PermissionChecker', '$http', 'APP_CONSTANTS',
            function ($scope, $filter, ReportPeriodResource, DepartmentResource, appModals, LogEntryResource, $logPanel, PermissionChecker, $http, APP_CONSTANTS) {

                DepartmentResource.query({}, function (department) {
                    $scope.department = department;
                });
                if(!$scope.searchFilter) {
                    $scope.searchFilter = {
                        ajaxFilter: [],
                        params: {
                            yearStart: new Date().getFullYear(),
                            yearEnd: new Date().getFullYear(),
                            department: $scope.department
                        },
                        isClear: false,
                        filterName: 'filter',
                        hideExtendedFilter: false
                    };
                }

                $scope.reportPeriodGrid = {
                    ctrl: {},
                    value: [],
                    gridName: 'reportPeriodGrid',
                    options: {
                        fullscreen: true,
                        height: '900',
                        datatype: "angularResource",
                        angularResource: ReportPeriodResource,
                        requestParameters: function () {
                            return {
                                projection: 'reportPeriods',
                                filter: JSON.stringify({
                                    yearStart: $scope.searchFilter.params.yearStart,
                                    yearEnd: $scope.searchFilter.params.yearEnd,
                                    departmentId: $scope.departmentId
                                })
                            };
                        },
                        colNames: [
                            $filter('translate')('reportPeriod.modal.year'),
                            $filter('translate')('reportPeriod.grid.period'),
                            $filter('translate')('reportPeriod.grid.state'),
                            $filter('translate')('reportPeriod.grid.deadline'),
                            $filter('translate')('reportPeriod.grid.correctionDate')

                        ],
                        colModel: [
                            {name: 'year', index: 'year', width: 55},
                            {name: 'name', index: 'name', sortable: false, width: 140},
                            {
                                name: 'isActive',
                                index: 'isActive',
                                width: 140,
                                formatter: $filter('statusPeriodFormatter')
                            },
                            {
                                name: 'deadline',
                                index: 'deadline',
                                width: 250,
                                sortable: false,
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'correctionDate',
                                index: 'correctionDate',
                                width: 250,
                                formatter: $filter('dateFormatter')
                            }
                        ],
                        rowNum: 10,
                        rowList: [10, 20, 30],
                        viewrecords: true,
                        sortname: 'year',
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: true
                    }
                };


                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.reportPeriodGrid.ctrl.refreshGrid(page);
                };


                /**
                 * @description Поиск по фильтру
                 */
                $scope.submitSearch = function () {
                    $scope.searchFilter.ajaxFilter = [];
                    $scope.searchFilter.fillFilterParams();
                    $scope.refreshGrid(1);
                    $scope.searchFilter.isClear = !_.isEmpty($scope.searchFilter.ajaxFilter);
                };

                /**
                 * @description сброс фильтра
                 */
                $scope.resetFilterCustom = function () {
                    /* очистка всех инпутов на форме */
                    $scope.searchFilter.params = {
                        yearStart: new Date().getFullYear(),
                        yearEnd: new Date().getFullYear(),
                        department: null
                    };

                    /* убираем надпись "Сброс" */
                    $scope.isClear = false;

                    $scope.submitSearch();
                };

                /**
                 * @description Заполнение ajaxFilter
                 */
                $scope.searchFilter.fillFilterParams = function () {
                    if ($scope.searchFilter.params.yearStart) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "yearStart",
                            value: $scope.searchFilter.params.yearStart
                        });
                    }
                    if ($scope.searchFilter.params.yearEnd) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "yearEnd",
                            value: $scope.searchFilter.params.yearEnd
                        });
                    }
                    if ($scope.searchFilter.params.department) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "departmentId",
                            value: $scope.searchFilter.params.department.id
                        });
                    }
                };

                /**
                 * @description Открытие модального окна создания периода
                 */
                $scope.openPeriod = function () {
                    var modal = appModals.create('client/app/taxes/ndfl/periods/modal/ndflReportPeriodModal.html',
                        'reportPeriodCtrlModal',
                        {isAdd: true,department: $scope.department},
                        {size: 'md'}
                    );
                    modal.result.then(function () {
                        $scope.refreshGrid(1);
                    });
                };

                /**
                 * @description Открытие модального окна создания периода
                 */
                $scope.editPeriod = function () {
                    var modal = appModals.create('client/app/taxes/ndfl/periods/modal/ndflReportPeriodModal.html',
                        'reportPeriodCtrlModal',
                        {isAdd: false, period: $scope.reportPeriodGrid.value[0], department: $scope.department },
                        {size: 'md'}
                    );
                    modal.result.then(function () {
                        $scope.refreshGrid(1);
                    });
                };

                /**
                 * @description Закрыть период
                 */
                $scope.closePeriod = function () {
                    ReportPeriodResource.query({
                        projection: 'checkNotAccepted',
                        id: $scope.reportPeriodGrid.value[0].id
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
                                        url: "controller/rest/reportPeriods/closePeriod",
                                        params: {
                                            filter: JSON.stringify({
                                                id: $scope.reportPeriodGrid.value[0].id
                                            })
                                        }
                                    }).then(function (response) {
                                        if (response.data) {
                                            $logPanel.open('log-panel-container', response.data);
                                            $scope.refreshGrid(1);
                                        }
                                    });
                                }
                            });
                    });
                };

                /** Проверка, может ли текущий пользоватеть выполнить операцию над выделенными налоговыми периодами
                * @param permission
                */
                $scope.checkPermissionForGridValue = function (permission) {
                    if ($scope.reportPeriodGrid.value && $scope.reportPeriodGrid.value.length > 0) {
                        return $scope.reportPeriodGrid.value.every(function (item) {
                            if ((permission === APP_CONSTANTS.DEPARTMENT_REPORT_PERIOD_PERMISSION.EDIT || permission === APP_CONSTANTS.DEPARTMENT_REPORT_PERIOD_PERMISSION.DEADLINE ||
                                permission === APP_CONSTANTS.DEPARTMENT_REPORT_PERIOD_PERMISSION.OPEN_CORRECT || permission === APP_CONSTANTS.DEPARTMENT_REPORT_PERIOD_PERMISSION.CLOSE) &&
                                $scope.reportPeriodGrid.value.length !== 1){
                                return false;
                            }
                            return PermissionChecker.check(item, permission);
                        });
                    }else {
                        if (permission === APP_CONSTANTS.DEPARTMENT_REPORT_PERIOD_PERMISSION.OPEN){
                            return PermissionChecker.check(permission);
                        }
                    }
                };

                /**
                 * @description Удаление выбранного отчетного периода для подразделения
                 */
                $scope.deletePeriod = function () {
                    var ids = [];
                    $scope.reportPeriodGrid.value.forEach(function (item) {
                        ids.push(item.id);
                    });
                    $http({
                        method: "POST",
                        url: "controller/rest/reportPeriods/delete",
                        params: {
                                ids: ids

                        }
                    }).then(function (response) {
                        if (response.data) {
                            $logPanel.open('log-panel-container', response.data);
                            $scope.refreshGrid(1);
                        }
                    });
                };

                /**
                 * @description Открытие модального окна окрытия корректирующего периода
                 */
                $scope.openCorrectPeriod = function () {
                    var modal = appModals.create('client/app/taxes/ndfl/periods/modal/openCorrectPeriodModal.html', 'openCorrectCtrlModal',
                        {
                            period: $scope.reportPeriodGrid.value[0],
                            department: $scope.department
                        });
                    modal.result.then(function () {
                        $scope.refreshGrid(1);
                    });
                };

                $scope.deadlinePeriod = function () {
                    var modal = appModals.create('client/app/taxes/ndfl/periods/modal/deadlinePeriod.html', 'deadlinePeriodController',
                        {
                            period: $scope.reportPeriodGrid.value[0]
                        },
                        {
                            size : "md"
                        });
                    modal.result.then(function () {
                        $scope.refreshGrid(1);
                    });
                };

            }]);

}());