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

                $scope.yearMin = 2003;
                $scope.yearMax = 2100;//TODO сообщения валидатора не обновляются если изменить параметр и вернутся на страницу
                CommonParamResource.query({
                    codes: [APP_CONSTANTS.CONFIGURATION_PARAM.REPORT_PERIOD_YEAR_MIN, APP_CONSTANTS.CONFIGURATION_PARAM.REPORT_PERIOD_YEAR_MAX],
                    projection: "allByCodes"
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
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "local",
                        rowNum: 1000,
                        colNames: [
                            $filter('translate')('reportPeriod.grid.period'),
                            $filter('translate')('reportPeriod.grid.state'),
                            $filter('translate')('reportPeriod.grid.correctionDate')
                        ],
                        colModel: [
                            {name: 'name', index: 'name', sortable: false, width: 300},
                            {
                                name: 'isActive',
                                index: 'isActive',
                                width: 140,
                                formatter: $filter('activeStatusPeriodFormatter')
                            },
                            {
                                name: 'correctionDate',
                                index: 'correctionDate',
                                width: 250,
                                formatter: $filter('dateFormatter')
                            }
                        ],
                        viewrecords: true,
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
                        $scope.initGrid();
                    }
                });

                /**
                 *@description инициализирует данные в гриде
                 */
                $scope.initGrid = function () {
                    $scope.periodGridData = [];
                    DepartmentReportPeriodResource.query({
                        filter: JSON.stringify({
                            yearStart: $scope.searchFilter.params.yearStart,
                            yearEnd: $scope.searchFilter.params.yearEnd,
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
                        department: $scope.searchFilter.params.defaultDepartment
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
                                    isAdd: true
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
                $scope.closePeriod = function () {
                    $logPanel.close();
                    // проверяем на наличие заблокированных форм на редактировании
                    $scope.checkHasBlocked().then(function (response) {
                        $scope.createLogPanel(response).then(function (response) {
                            if (response) {
                                $dialogs.errorDialog({
                                    content: $filter('translate')('reportPeriod.error.closePeriod.hasBlocked.text')
                                });
                            } else {
                                // проверка на наличие форм в статусе отличном от "Принята"
                                $scope.checkHasNotAccepted().then(function (response) {
                                    $scope.createLogPanel(response).then(function (response) {
                                        if (response) {
                                            $dialogs.confirmDialog({
                                                title: $filter('translate')('reportPeriod.confirm.closePeriod.hasNotAccepted.title'),
                                                content: $filter('translate')('reportPeriod.confirm.closePeriod.hasNotAccepted.text'),
                                                okBtnCaption: $filter('translate')('common.button.yes'),
                                                cancelBtnCaption: $filter('translate')('common.button.no'),
                                                okBtnClick: function () {
                                                    doClosePeriod();
                                                }
                                            });
                                        } else {
                                            doClosePeriod();
                                        }
                                    });
                                });
                            }
                        });
                    });
                };

                function doClosePeriod() {
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentReportPeriod/" + $scope.reportPeriodGrid.value[0].id + "/close"
                    }).then(function (response) {
                        if (response.data) {
                            $logPanel.open('log-panel-container', response.data);
                            $scope.refreshGrid();
                        }
                    });
                }

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
                            doDeletePeriod();
                        }
                    });
                };

                function doDeletePeriod() {
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentReportPeriod/delete",
                        params: {
                            departmentReportPeriodId: $scope.reportPeriodGrid.value[0].id
                        }
                    }).then(function (response) {
                        $logPanel.open('log-panel-container', response.data);
                        $scope.refreshGrid();
                    });
                }

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
                        if (response.data) {
                            $logPanel.open('log-panel-container', response.data);
                        }
                        $scope.refreshGrid();
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

                /**
                 * @description Проверяет на наличие деклараций в статусе отличном от "Принята"
                 */
                $scope.checkHasNotAccepted = function () {
                    $scope.checkHasNotAcceptedDefer = $q.defer();
                    $http({
                        method: "POST",
                        url: "controller/rest/departmentReportPeriod/" + $scope.reportPeriodGrid.value[0].id + "?projection=checkHasNotAccepted"
                    }).then(function (logger) {
                        if (logger.status === 200) {
                            $scope.checkHasNotAcceptedDefer.resolve(logger.data);
                        } else {
                            $scope.checkHasNotAcceptedDefer.reject("Fail connect");
                        }
                    });
                    return $scope.checkHasNotAcceptedDefer.promise;
                };

                /**
                 * Формирует окно оповещений
                 * @param uuid - идентификатор логов
                 */
                $scope.createLogPanel = function (uuid) {
                    $scope.createLogPanelDefer = $q.defer();
                    if (uuid) {
                        LogEntryResource.query({
                                uuid: uuid,
                                projection: 'count'
                            },
                            function (data) {
                                if ((data.ERROR + data.WARNING) !== 0) {
                                    $logPanel.open('log-panel-container', uuid);
                                    $scope.createLogPanelDefer.resolve(true);
                                } else {
                                    $scope.createLogPanelDefer.resolve(false);
                                }
                            });
                    } else {
                        $scope.createLogPanelDefer.resolve(false);
                    }
                    return $scope.createLogPanelDefer.promise;
                };

                /**
                 * @description Проверяет на наличие деклараций на редактировании
                 * @return признак блокировки периода формой
                 */
                $scope.checkHasBlocked = function () {
                    $scope.checkHasBlockedDefer = $q.defer();
                    $http({
                        method: "POST",
                        url: "controller/rest/departmentReportPeriod/" + $scope.reportPeriodGrid.value[0].id + "?projection=checkHasBlockedDeclaration"
                    }).then(function (logger) {
                        if (logger.status === 200) {
                            $scope.checkHasBlockedDefer.resolve(logger.data);
                        } else {
                            $scope.checkHasBlockedDefer.reject("Fail connect");
                        }
                    });
                    return $scope.checkHasBlockedDefer.promise;
                };

            }]);

}());