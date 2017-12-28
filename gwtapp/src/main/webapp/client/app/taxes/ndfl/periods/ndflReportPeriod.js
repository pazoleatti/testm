(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "НДФЛ - Ведение периодов"
     */

    angular.module('app.reportPeriod',
        ['ui.router',
            'app.logPanel',
            'app.rest',
            'app.openCorrectPeriodModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('reportPeriod', {
                url: '/taxes/reportPeriod',
                templateUrl: 'client/app/taxes/ndfl//periods/ndflReportPeriod.html',
                controller: 'reportPeriodCtrl'
            });
        }])

        .filter('reportPeriodFormatter', function () {
            return function (entity) {
                if (entity) {
                    return entity.name;
                } else {
                    return '';
                }
            };
        })



        /**
         * @description Контроллер формы "Ведение периодов"
         */

        .controller('reportPeriodCtrl', ['$scope', '$filter', 'DepartmentReportPeriodResource', 'BankDepartmentResource', 'LogEntryResource', '$logPanel', 'PermissionChecker', '$http', 'APP_CONSTANTS', '$aplanaModal', 'DepartmentReportPeriodCheckerResource',
            'ValidationUtils', '$dialogs', '$q',
            function ($scope, $filter, DepartmentReportPeriodResource, BankDepartmentResource, LogEntryResource, $logPanel, PermissionChecker, $http, APP_CONSTANTS, $aplanaModal, DepartmentReportPeriodCheckerResource,
                      ValidationUtils, $dialogs, $q) {

                BankDepartmentResource.query({}, function (department) {
                    $scope.department = department;
                });
                if (!$scope.searchFilter) {
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
                    options: {
                        datatype: "local",
                        rowNum: 1000,
                        colNames: [
                            $filter('translate')('reportPeriod.grid.period'),
                            $filter('translate')('reportPeriod.grid.state'),
                            $filter('translate')('reportPeriod.grid.deadline'),
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

                $scope.initGrid = function() {
                    $scope.periodGridData = [];
                    DepartmentReportPeriodResource.query({
                        filter: JSON.stringify({
                            yearStart: $scope.searchFilter.params.yearStart,
                            yearEnd: $scope.searchFilter.params.yearEnd,
                            departmentId: $scope.departmentId
                        })
                    }, function (response) {
                        var array = [];
                        angular.forEach(response, function (period) {
                            var periodTmp = {};
                            angular.copy(period, periodTmp);
                            array.push(periodTmp);
                        });
                        // $scope.periodGridData = sortGridData(array);
                        $scope.periodGridData = array;
                        $scope.reportPeriodGrid.ctrl.refreshGridData($scope.periodGridData);
                    });
                };

                var sortGridData = function(array){
                    array = _.sortBy(array, 'correctionDate');
                    array = _.sortBy(array, 'dictTaxPeriodId');
                    array =  _.sortBy(array, 'year');
                    return array;
                };



                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.initGrid();
                };


                /**
                 * @description Поиск по фильтру
                 */
                $scope.submit = function () {
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
                        department: null
                    };

                    /* убираем надпись "Сброс" */
                    $scope.isClear = false;

                    $scope.submit();
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
                        tittle: $filter('translate')('reportPeriod.pils.openPeriod'),
                        templateUrl: 'client/app/taxes/ndfl/periods/modal/ndflReportPeriodModal.html?v=${buildUuid}',
                        controller: 'reportPeriodCtrlModal',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    isAdd: true,
                                    department: $scope.department
                                };
                            }
                        }
                    }).result.then(function () {
                        $scope.refreshGrid(1);
                    });
                };

                /**
                 * @description Открытие модального окна создания периода
                 */
                $scope.editPeriod = function () {
                    $aplanaModal.open({
                        tittle: $filter('translate')('reportPeriod.pils.editPeriod'),
                        templateUrl: 'client/app/taxes/ndfl/periods/modal/ndflReportPeriodModal.html?v=${buildUuid}',
                        controller: 'reportPeriodCtrlModal',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    isAdd: false,
                                    period: $scope.reportPeriodGrid.value[0],
                                    department: $scope.department
                                };
                            }
                        }
                    }).result.then(function () {
                        $scope.refreshGrid(1);
                    });
                };

                /**
                 * @description Закрыть период
                 */
                $scope.closePeriod = function () {
                    $scope.checkHasBlocked().then(function (response) {
                        $scope.createLogPanel(response).then(function (response) {
                            if (response) {
                                $dialogs.errorDialog({
                                    content: $filter('translate')('reportPeriod.error.closePeriod.hasBlocked.text')
                                });
                            } else {
                                $scope.checkHasNotAccepted().then(function (response) {
                                    $scope.createLogPanel(response).then(function (response) {
                                        if (response) {
                                            $dialogs.confirmDialog({
                                                title: $filter('translate')('reportPeriod.confirm.closePeriod.hasNotAccepted.title'),
                                                content: $filter('translate')('reportPeriod.confirm.closePeriod.hasNotAccepted.text'),
                                                okBtnCaption: $filter('translate')('common.button.yes'),
                                                cancelBtnCaption: $filter('translate')('common.button.no'),
                                                okBtnClick: function () {
                                                    $http({
                                                        method: "POST",
                                                        url: "controller/actions/departmentReportPeriod/" + $scope.reportPeriodGrid.value[0].id + "/close"
                                                    }).then(function (response) {
                                                        if (response.data) {
                                                            $logPanel.open('log-panel-container', response.data);
                                                            $scope.refreshGrid(1);
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            $http({
                                                method: "POST",
                                                url: "controller/actions/departmentReportPeriod/" + $scope.reportPeriodGrid.value[0].id + "/close"
                                            }).then(function (response) {
                                                if (response.data) {
                                                    $logPanel.open('log-panel-container', response.data);
                                                    $scope.refreshGrid(1);
                                                }
                                            });
                                        }
                                    });
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
                                $scope.reportPeriodGrid.value.length !== 1) {
                                return false;
                            }
                            return PermissionChecker.check(item, permission);
                        });
                    } else {
                        if (permission === APP_CONSTANTS.DEPARTMENT_REPORT_PERIOD_PERMISSION.OPEN) {
                            return PermissionChecker.check(permission);
                        }
                    }
                };

                /**
                 * @description Удаление выбранного отчетного периода для подразделения
                 */
                $scope.deletePeriod = function () {

                    if (!$scope.reportPeriodGrid.value[0].correctionDate) {
                        $scope.hasCorrectionPeriod().then(function (response) {
                            if (response) {
                                $dialogs.errorDialog({
                                    content: $filter('translate')('reportPeriod.error.deletePeriod.hasCorPeriod.text')
                                });
                            } else {
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
                                                id: $scope.reportPeriodGrid.value[0].id

                                            }
                                        }).then(function (response) {
                                            if (response.data) {
                                                $logPanel.open('log-panel-container', response.data);
                                                $scope.refreshGrid(1);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    } else {
                        $scope.hasLaterCorrectionPeriod().then(function (response) {
                            if (response) {
                                $dialogs.errorDialog({
                                    content: $filter('translate')('reportPeriod.error.deletePeriod.hasLaterCorPeriod.text')
                                });
                            } else {
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
                                                id: $scope.reportPeriodGrid.value[0].id

                                            }
                                        }).then(function (response) {
                                            if (response.data) {
                                                $logPanel.open('log-panel-container', response.data);
                                                $scope.refreshGrid(1);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                };

                /**
                 * @description Открытие модального окна окрытия корректирующего периода
                 */
                $scope.openCorrectPeriod = function () {

                    $aplanaModal.open({
                        title: $filter('translate')('reportPeriod.pils.correctPeriod'),
                        templateUrl: 'client/app/taxes/ndfl/periods/modal/openCorrectPeriodModal.html?v=${buildUuid}',
                        controller: 'openCorrectCtrlModal',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    period: $scope.reportPeriodGrid.value[0],
                                    department: $scope.department
                                };
                            }
                        }
                    }).result.then(function () {
                        $scope.refreshGrid(1);
                    });
                };

                /**
                 * @description Открытие модального окна назначения срока сдачи отчетности
                 */
                $scope.deadlinePeriod = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('reportPeriod.deadline.title') + $scope.reportPeriodGrid.value[0].name + " " + $scope.reportPeriodGrid.value[0].year,
                        templateUrl: 'client/app/taxes/ndfl/periods/modal/deadlinePeriod.html?v=${buildUuid}',
                        controller: 'deadlinePeriodController',
                        windowClass: 'modal500',
                        resolve: {
                            $shareData: function () {
                                return {
                                    period: $scope.reportPeriodGrid.value[0]
                                };
                            }
                        }
                    }).result.then(function () {
                        $scope.refreshGrid(1);
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

                /**
                 * @description Проверяет на наличие коректирующих периодов с более поздней датой сдачи корректировки
                 * @return признак существования корректирующих периодов с более поздней датой сдачи корректировки
                 */
                $scope.hasLaterCorrectionPeriod = function () {
                    $scope.hasLaterCorrectionPeriodDefer = $q.defer();
                    DepartmentReportPeriodResource.query({
                        filter: JSON.stringify({
                            yearStart: $scope.reportPeriodGrid.value[0].year,
                            yearEnd: $scope.reportPeriodGrid.value[0].year,
                            departmentId: $scope.departmentId
                        })
                    }, function (response) {
                        response.rows.forEach(function (item) {
                            if ($scope.reportPeriodGrid.value[0].year === item.year && $scope.reportPeriodGrid.value[0].dictTaxPeriodId === item.dictTaxPeriodId &&
                                $scope.reportPeriodGrid.value[0].correctionDate < item.correctionDate) {
                                $scope.hasLaterCorrectionPeriodDefer.resolve(true);
                            }
                        });
                        $scope.hasLaterCorrectionPeriodDefer.resolve(false);
                    });
                    return $scope.hasLaterCorrectionPeriodDefer.promise;
                };

                /**
                 * @description Проверяет на наличие корректирующего периода
                 * @return признак корректировки
                 */
                $scope.hasCorrectionPeriod = function () {
                    $scope.hasCorrectionPeriodDefer = $q.defer();
                    DepartmentReportPeriodResource.query({
                        filter: JSON.stringify({
                            yearStart: $scope.reportPeriodGrid.value[0].year,
                            yearEnd: $scope.reportPeriodGrid.value[0].year,
                            departmentId: $scope.departmentId
                        })
                    }, function (response) {
                        response.rows.forEach(function (item) {
                            if ($scope.reportPeriodGrid.value[0].year === item.year && $scope.reportPeriodGrid.value[0].dictTaxPeriodId === item.dictTaxPeriodId && item.correctionDate) {
                                $scope.hasCorrectionPeriodDefer.resolve(true);
                            }
                        });
                        $scope.hasCorrectionPeriodDefer.resolve(false);
                    });
                    return $scope.hasCorrectionPeriodDefer.promise;
                };

            }]);

}());