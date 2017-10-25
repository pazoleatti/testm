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

        .controller('reportPeriodCtrl', ['$scope', '$filter', 'ReportPeriodResource', 'DepartmentResource', 'appModals', 'LogEntryResource', '$logPanel', 'PermissionChecker', '$http',
            function ($scope, $filter, ReportPeriodResource, DepartmentResource, appModals, LogEntryResource, $logPanel, PermissionChecker, $http) {

                DepartmentResource.query({}, function (department) {
                    $scope.department = department;
                });

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

                $scope.$watch('searchFilter.params.department', function () {
                    $scope.departmentId = $scope.searchFilter.params.department ? $scope.searchFilter.params.department.id : null;
                });

                $scope.reportPeriodGrid = {
                    ctrl: {},
                    value: [],
                    gridName: 'reportPeriodGrid',
                    options: {
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
                        height: 200,
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
                                sortable: false
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
                        multiselect: false
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
                    appModals.create('client/app/taxes/ndfl/periods/modal/ndflReportPeriodModal.html', 'reportPeriodCtrlModal',
                        {
                            isAdd: true,
                            department: $scope.department
                        }
                    );
                };

                /**
                 * @description Открытие модального окна создания периода
                 */
                $scope.editPeriod = function () {
                    appModals.create('client/app/taxes/ndfl/periods/modal/ndflReportPeriodModal.html', 'reportPeriodCtrlModal',
                        {
                            isAdd: false,
                            period: $scope.reportPeriodGrid.value[0],
                            department: $scope.department
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

                /**
                 * @description Проверка доступности действий с открытым периодом
                 */
                $scope.isOpen = function () {
                    if ($scope.reportPeriodGrid.value[0]) {
                        return $scope.reportPeriodGrid.value[0].isActive === 1;
                    }else {
                        return false;
                    }
                };

                /**
                 * @description Проверка доступности действий с закрытым периодом
                 */
                $scope.isClose = function () {
                    if ($scope.reportPeriodGrid.value[0]) {
                        return $scope.reportPeriodGrid.value[0].isActive === 0;
                    }else {
                        return false;
                    }
                };

                /** Проверка, может ли текущий пользоватеть выполнить операцию над выделенными налоговыми формами
                * @param permission
                */
                $scope.checkPermissionForGridValue = function (permission) {
                        return PermissionChecker.check(permission);
                };

                /**
                 * @description Удаление выбранного отчетного периода для подразделения
                 */
                $scope.deletePeriod = function () {
                    ReportPeriodResource.delete({id: $scope.reportPeriodGrid.value[0].id},
                    function () {
                        $scope.refreshGrid(1);
                    });
                };

                /**
                 * @description Открытие модального окна окрытия корректирующего периода
                 */
                $scope.openCorrectPeriod = function () {
                    appModals.create('client/app/taxes/ndfl/periods/modal/openCorrectPeriodModal.html', 'openCorrectCtrlModal',
                        {
                            period: $scope.reportPeriodGrid.value[0],
                            department: $scope.department
                        });
                };
            }]);

}());