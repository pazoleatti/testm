(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "НДФЛ - Ведение периодов"
     */
    angular.module('app.reportPeriod',
        ['ui.router',
            'app.logPanel',
            'app.rest',
            'app.reportPeriodModal',
            'app.openCorrectPeriodModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('reportPeriod', {
                url: '/taxes/reportPeriod',
                templateUrl: 'client/app/taxes/ndfl/periods/reportPeriod.html',
                controller: 'reportPeriodCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_SETTINGS)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        /**
         * @description Контроллер формы "Ведение периодов"
         */
        .controller('reportPeriodCtrl', ['$scope', '$filter', 'DepartmentReportPeriodResource', 'CommonParamResource', 'LogEntryResource', '$logPanel', 'PermissionChecker', '$http', 'APP_CONSTANTS', '$aplanaModal',
            'ValidationUtils', '$dialogs', '$q', '$rootScope',
            function ($scope, $filter, DepartmentReportPeriodResource, CommonParamResource, LogEntryResource, $logPanel, PermissionChecker, $http, APP_CONSTANTS, $aplanaModal,
                      ValidationUtils, $dialogs, $q) {

                var defaultDepartment = null;
                $scope.yearMin = 2003;
                $scope.yearMax = 2100;//TODO сообщения валидатора не обновляются если изменить параметр и вернутся на страницу
                CommonParamResource.query({
                    codes: [APP_CONSTANTS.CONFIGURATION_PARAM.REPORT_PERIOD_YEAR_MIN, APP_CONSTANTS.CONFIGURATION_PARAM.REPORT_PERIOD_YEAR_MAX],
                    projection: "allByEnums"
                }, function (configurationsByCode) {
                    $scope.yearMin = configurationsByCode[APP_CONSTANTS.CONFIGURATION_PARAM.REPORT_PERIOD_YEAR_MIN].value;
                    $scope.yearMax = configurationsByCode[APP_CONSTANTS.CONFIGURATION_PARAM.REPORT_PERIOD_YEAR_MAX].value;
                });

                if (!$scope.searchFilter) {
                    $scope.searchFilter = {
                        ajaxFilter: [],
                        params: {
                            yearStart: new Date().getFullYear(),
                            yearEnd: new Date().getFullYear(),
                            department: null
                        },
                        isClear: false,
                        filterName: 'filter',
                        hideExtendedFilter: false
                    };
                }

                $scope.reportPeriodGrid = {
                    init: function (ctrl) {
                        ctrl.gridComplete = function () {
                            var cm = $scope.reportPeriodGrid.options.colModel;
                            var headers = $scope.reportPeriodGrid.ctrl.grid[0].grid.headers;
                            for (var index = 1; index < headers.length; index++) {// index=0 - parent col
                                var header = headers[index];
                                var cmi = cm[index - 1], colName = cmi.name;
                                if (!cmi.sortable && colName !== 'rn' && colName !== 'cb' && colName !== 'subgrid') {
                                    $('div.ui-jqgrid-sortable', header.el).css({cursor: "default"});
                                }
                            }
                        };
                    },
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "local",
                        rowNum: 1000,
                        colNames: [
                            $filter('translate')('reportPeriod.grid.period'),
                            $filter('translate')('reportPeriod.grid.state'),
                            $filter('translate')('reportPeriod.grid.formType'),
                            $filter('translate')('reportPeriod.grid.correctionDate')
                        ],
                        colModel: [
                            {
                                name: 'name',
                                index: 'name',
                                sortable: false,
                                width: 450,
                                formatter: function (cellValue, options, row) {
                                    return $filter("translate")("reportPeriod.grid.period.value", {
                                        code: row.code,
                                        periodName: row.name,
                                        correction: row.correctionDate ? $filter("translate")("reportPeriod.grid.period.value.correction") : ""
                                    });
                                }
                            },
                            {
                                name: 'isActive',
                                index: 'isActive',
                                width: 140,
                                formatter: $filter('activeStatusPeriodFormatter')
                            },
                            {
                                name: 'taxFormTypeId',
                                index: 'taxFormTypeId',
                                width: 140,
                                formatter: $filter('taxFormTypeFormatter')
                            },
                            {
                                name: 'correctionDate',
                                index: 'correctionDate',
                                width: 110,
                                formatter: $filter('dateFormatter')
                            }
                        ],
                        hidegrid: false,
                        groupConfig: {
                            level: 1,
                            colModel: {
                                formatter: $filter('groupFormatter'),
                                sorttype: $filter('groupSortCustomType')('id', 'parent.year', 'dictTaxPeriodId', 'parent.year')
                            },
                            noTree: {
                                groupObject: 'parent',
                                groupId: 'parent.id',
                                groupColumnModel: {'name': 'name'}
                            }
                        }
                    }
                };

                $scope.$watch("searchFilter.params.department", function (newValue, oldValue) {
                    if (!!newValue && (!oldValue || newValue.id !== oldValue.id)) {
                        if (!defaultDepartment) {
                            defaultDepartment = newValue;
                        }
                        $scope.initGrid();
                    }
                });

                /**
                 *@description инициализирует данные в гриде
                 */
                $scope.initGrid = function () {
                    $scope.periodGridData = [];
                    var taxFormTypeId = $scope.searchFilter.params.formType ? $scope.searchFilter.params.formType.id : null;
                    DepartmentReportPeriodResource.query({
                        filter: JSON.stringify({
                            yearStart: $scope.searchFilter.params.yearStart,
                            yearEnd: $scope.searchFilter.params.yearEnd,
                            reportPeriod: taxFormTypeId ? { reportPeriodTaxFormTypeId: taxFormTypeId } : null,
                            departmentId: $scope.searchFilter.params.department.id
                        })
                    }, function (response) {
                        var array = [];
                        angular.forEach(response, function (period) {
                            var periodTmp = {};
                            angular.copy(period, periodTmp);
                            array.push(periodTmp);
                        });
                        $scope.periodGridData = array;
                        $scope.reportPeriodGrid.ctrl.refreshGridData($scope.periodGridData);
                    });
                };

                /**
                 * @description Обновление грида
                 */
                $scope.refreshGrid = function () {
                    $scope.initGrid();
                };


                /**
                 * @description Поиск по фильтру
                 */
                $scope.searchByFilter = function () {
                    if (!ValidationUtils.checkYearInterval($scope.searchFilter.params.yearStart, $scope.searchFilter.params.yearEnd)) {
                        $dialogs.errorDialog({
                            content: $filter('translate')('common.validation.interval.year')
                        });
                        return;
                    }
                    $scope.searchFilter.ajaxFilter = [];
                    $scope.searchFilter.fillFilterParams();
                    $scope.initGrid();
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
                        department: defaultDepartment
                    };

                    /* убираем надпись "Сброс" */
                    $scope.isClear = false;

                    $scope.searchByFilter();
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
                    $aplanaModal.open({
                        title: $filter('translate')('reportPeriod.pils.openPeriod'),
                        templateUrl: 'client/app/taxes/ndfl/periods/modal/createReportPeriodModal.html',
                        controller: 'reportPeriodCtrlModal',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    department: $scope.searchFilter.params.department,
                                    isAdd: true,
                                    yearMin: $scope.yearMin,
                                    yearMax: $scope.yearMax
                                };
                            }
                        }
                    }).result.then(function (res) {
                        $scope.searchFilter.params.yearStart = res;
                        $scope.searchFilter.params.yearEnd = res;
                        $scope.refreshGrid();
                    });
                };

                /**
                 * @description Закрыть период
                 */
                $scope.closePeriod = function (skipHasNotAcceptedCheck) {
                    $logPanel.close();
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentReportPeriod/" + $scope.reportPeriodGrid.value[0].id + "/close",
                        params: {
                            skipHasNotAcceptedCheck: !!skipHasNotAcceptedCheck
                        }
                    }).then(function (response) {
                        if (response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        if (response.data.error) {
                            if (!response.data.fatal) {
                                $dialogs.confirmDialog({
                                    title: $filter('translate')('reportPeriod.confirm.closePeriod'),
                                    content: response.data.error,
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick: function () {
                                        $scope.closePeriod(true);
                                    }
                                });
                            } else {
                                $dialogs.errorDialog({content: response.data.error});
                            }
                        } else {
                            $scope.refreshGrid();
                        }
                    });
                };

                /** Проверка, может ли текущий пользоватеть выполнить операцию над выделенным налоговыми периодами
                 * @param permission
                 * @return boolean возможность управления
                 */
                $scope.checkPermissionForGridValue = function (permission) {
                    if ($scope.reportPeriodGrid.value && $scope.reportPeriodGrid.value.length > 0) {
                        return PermissionChecker.check($scope.reportPeriodGrid.value[0], permission);
                    }
                };

                /**
                 * @description Удаление выбранного отчетного периода для подразделения
                 */
                $scope.deletePeriod = function () {
                    $logPanel.close();
                    $dialogs.confirmDialog({
                        title: $filter('translate')('reportPeriod.confirm.deletePeriod.title'),
                        content: $filter('translate')('reportPeriod.confirm.deletePeriod.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/departmentReportPeriod/delete",
                                params: {
                                    departmentReportPeriodId: $scope.reportPeriodGrid.value[0].id
                                }
                            }).then(function (response) {
                                if (response.data.uuid) {
                                    $logPanel.open('log-panel-container', response.data.uuid);
                                }
                                if (response.data.error) {
                                    $dialogs.errorDialog({content: response.data.error});
                                } else {
                                    $scope.refreshGrid();
                                }
                            });
                        }
                    });
                };

                /**
                 * @description Переоткрытие выбранного периода
                 */
                $scope.onReopenPeriod = function () {
                    $logPanel.close();
                    reopenPeriod($scope.reportPeriodGrid.value[0].id);
                };

                function reopenPeriod(departmentReportPeriodId) {
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentReportPeriod/reopen",
                        params: {
                            departmentReportPeriodId: departmentReportPeriodId
                        }
                    }).then(function (response) {
                        if (response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        if (response.data.error) {
                            $dialogs.errorDialog({content: response.data.error});
                        } else {
                            $scope.refreshGrid();
                        }
                    });
                }

                /**
                 * @description Открытие модального окна окрытия корректирующего периода
                 */
                $scope.openCorrectPeriod = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('reportPeriod.pils.correctPeriod'),
                        templateUrl: 'client/app/taxes/ndfl/periods/modal/openCorrectPeriodModal.html',
                        controller: 'openCorrectCtrlModal',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    department: $scope.searchFilter.params.department,
                                    selectedPeriod: $scope.reportPeriodGrid.value[0]
                                };
                            }
                        }
                    }).result.then(function () {
                        $scope.refreshGrid();
                    });
                };

            }]);

}());