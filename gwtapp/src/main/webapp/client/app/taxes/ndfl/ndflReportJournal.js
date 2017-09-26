(function () {
    'use strict';

    /**
     * @description Модуль для работы с формой "Отчетность"
     */
    angular.module('app.ndflReportJournal', ['ui.router', 'app.createReport'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflReportJournal', {
                url: '/taxes/ndflReportJournal',
                templateUrl: 'client/app/taxes/ndfl/ndflReportJournal.html',
                controller: 'ndflReportJournalCtrl',
                resolve: {
                    periods: function (APP_CONSTANTS, RefBookValuesResource) {
                        return RefBookValuesResource.query({
                            refBookId: APP_CONSTANTS.REFBOOK.PERIOD,
                            projection: "allPeriods"
                        }, function (data) {
                            return data;
                        }).$promise;
                    },
                    openPeriods: function (APP_CONSTANTS, RefBookValuesResource) {
                        return RefBookValuesResource.query({
                            refBookId: APP_CONSTANTS.REFBOOK.PERIOD,
                            projection: "openPeriods"
                        }, function (data) {
                            return data;
                        }).$promise;
                    },
                    declarationTypes: function (APP_CONSTANTS, RefBookValuesResource) {
                        return RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.DECLARATION_TYPE}, function (data) {
                            return data;
                        }).$promise;
                    }
                }
            });
        }])

        .controller('ndflReportJournalCtrl', ['$scope', '$rootScope', 'ShowToDoDialog', '$filter', 'DeclarationDataResource', '$http', '$logPanel', 'appModals', 'APP_CONSTANTS',
            'periods', 'openPeriods', 'declarationTypes',
            function ($scope, $rootScope, $showToDoDialog, $filter, DeclarationDataResource, $http, $logPanel, appModals, APP_CONSTANTS,
                      periods, openPeriods, declarationTypes) {
                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.ndflReportJournalGrid.ctrl.refreshGrid(page);
                };

                $scope.createReport = function () {
                    appModals.create('client/app/taxes/ndfl/createReport.html', 'createReportCtrl',
                        {
                            periods: openPeriods,
                            latestSelectedPeriod: $scope.latestSelectedPeriod
                        }, {size: 'md'});
                };
                $scope.downloadReport = function () {
                    $showToDoDialog();
                };
                $scope.check = function () {
                    var ids = [];
                    _.each($scope.ndflReportJournalGrid.value, function (element) {
                        ids.push(element.declarationDataId);
                    });
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/checkDeclarationDataList",
                        params: {
                            declarationDataIds: ids
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };
                $scope.delete = function () {
                    var ids = [];
                    _.each($scope.ndflReportJournalGrid.value, function (element) {
                        ids.push(element.declarationDataId);
                    });
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/deleteDeclarationDataList",
                        params: {
                            declarationDataIds: ids
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        $scope.refreshGrid(1);
                    });
                };
                $scope.accept = function () {
                    var ids = [];
                    _.each($scope.ndflReportJournalGrid.value, function (element) {
                        ids.push(element.declarationDataId);
                    });
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/acceptDeclarationDataList",
                        params: {
                            declarationDataIds: ids
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        $scope.refreshGrid(1);
                    });
                };
                $scope.returnToCreated = function () {
                    appModals.inputMessage($filter('translate')('title.indicateReasonForReturn'), $filter('translate')('title.reasonForReturn'))
                        .result.then(function (text) {
                        var ids = [];
                        _.each($scope.ndflReportJournalGrid.value, function (element) {
                            ids.push(element.declarationDataId);
                        });
                        $http({
                            method: "POST",
                            url: "controller/actions/declarationData/returnToCreatedDeclarationDataList",
                            params: {
                                declarationDataIds: ids,
                                reasonForReturn: text
                            }
                        }).then(function (response) {
                            if (response.data && response.data.uuid && response.data.uuid !== null) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                            $scope.refreshGrid(1);
                        });
                    });
                };

                function getIds(list) {
                    if (list) {
                        return list.map(function (elem) {
                            return elem.id;
                        });
                    } else {
                        return [];
                    }
                }

                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');
                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.ctrlReportGrid.refreshGrid(page);
                };

                var correctionTags = [APP_CONSTANTS.CORRETION_TAG.ONLY_CORRECTIVE, APP_CONSTANTS.CORRETION_TAG.ONLY_PRIMARY, APP_CONSTANTS.CORRETION_TAG.ALL];
                var docStates = [APP_CONSTANTS.DOC_STATE.ACCEPTED, APP_CONSTANTS.DOC_STATE.REFUSED, APP_CONSTANTS.DOC_STATE.REVISION, APP_CONSTANTS.DOC_STATE.SUCCESSFUL];
                var defaultCorrectionTag = APP_CONSTANTS.CORRETION_TAG.ALL;

                function getCorrectionTag() {
                    switch ($scope.searchFilter.params.correctionTag) {
                        case APP_CONSTANTS.CORRETION_TAG.ALL:
                            return undefined;
                        case APP_CONSTANTS.CORRETION_TAG.ONLY_PRIMARY:
                            return false;
                        case APP_CONSTANTS.CORRETION_TAG.ONLY_CORRECTIVE:
                            return true;
                    }
                    return undefined;
                }

                var declarationStates = [APP_CONSTANTS.NDFL_STATE.CREATED, APP_CONSTANTS.NDFL_STATE.PREPARED, APP_CONSTANTS.NDFL_STATE.ACCEPTED];

                $scope.searchFilter = {
                    params: {
                        correctionTag: defaultCorrectionTag
                    },
                    ajaxFilter: [],
                    isClear: false,
                    filterName: 'ndflReportsFilter'
                };

                // Флаг отображения кнопки "Сбросить"
                $scope.searchFilter.isClearByFilterParams = function () {
                    var needToClear = false;
                    angular.forEach($scope.searchFilter.params, function (value, key) {
                        if (key === 'correctionTag') {
                            needToClear = needToClear || value.id !== defaultCorrectionTag.id;
                        } else if (value != null) {
                            if (Array.isArray(value) || typeof(value) === "string" || value instanceof String) {
                                needToClear = needToClear || value.length > 0;
                            } else {
                                needToClear = true;
                            }
                        }
                    });
                    $scope.searchFilter.isClear = needToClear;
                };

                $scope.searchFilter.resetFilterParams = function () {
                    $scope.searchFilter.params.correctionTag = defaultCorrectionTag;
                };

                $scope.$watch('searchFilter.params.periods', function (periods) {
                    if (periods.length > 0) {
                        $scope.latestSelectedPeriod = periods[periods.length - 1];
                    } else {
                        $scope.latestSelectedPeriod = null;
                    }
                });

                $scope.correctionTagSelect = {
                    options: {
                        data: {
                            results: correctionTags,
                            text: $filter('nameFormatter')
                        },
                        formatSelection: $filter('nameFormatter'),
                        formatResult: $filter('nameFormatter')
                    }
                };

                $scope.periodSelect = {
                    options: {
                        data: {
                            results: periods,
                            text: $filter('periodFormatter')
                        },
                        formatSelection: $filter('periodFormatter'),
                        formatResult: $filter('periodFormatter'),
                        multiple: true,
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };

                $scope.departmentsSelect = {
                    options: {
                        ajax: {
                            url: "controller/rest/refBookValues/30?projection=allDepartments",
                            quietMillis: 200,
                            data: function (term, page) {
                                return {
                                    name: term,
                                    pagingParams: JSON.stringify({count: 50, page: page})
                                };
                            },
                            results: function (data, page) {
                                var more = (page * 50) < data.records;
                                return {results: data.rows, more: more};
                            }
                        },
                        formatSelection: $filter('nameFormatter'),
                        formatResult: $filter('nameFormatter'),
                        multiple: true,
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };


                $scope.declarationTypeSelect = {
                    options: {
                        data: {
                            results: declarationTypes,
                            text: $filter('nameFormatter')
                        },
                        formatSelection: $filter('nameFormatter'),
                        formatResult: $filter('nameFormatter'),
                        multiple: true,
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };

                $scope.docStateSelect = {
                    options: {
                        data: {
                            results: docStates,
                            text: $filter('nameFormatter')
                        },
                        formatSelection: $filter('nameFormatter'),
                        formatResult: $filter('nameFormatter'),
                        multiple: true,
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };

                $scope.stateSelect = {
                    options: {
                        data: {
                            results: declarationStates,
                            text: $filter('nameFormatter')
                        },
                        formatSelection: $filter('nameFormatter'),
                        formatResult: $filter('nameFormatter'),
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };

                $scope.ndflReportJournalGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: DeclarationDataResource,
                        requestParameters: function () {
                            return {
                                projection: 'declarations',
                                filter: JSON.stringify({
                                    docStateIds: getIds($scope.searchFilter.params.docStates),
                                    departmentIds: getIds($scope.searchFilter.params.departments),
                                    formKindIds: [APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id],
                                    declarationDataId: $scope.searchFilter.params.declarationNumber,
                                    declarationTypeIds: getIds($scope.searchFilter.params.declarationTypes),
                                    formState: $scope.searchFilter.params.state ? $scope.searchFilter.params.state.id : undefined,
                                    fileName: $scope.searchFilter.params.file,
                                    note: $scope.searchFilter.params.note,
                                    oktmo: $scope.searchFilter.params.oktmo,
                                    taxOrganKpp: $scope.searchFilter.params.kpp,
                                    taxOrganCode: $scope.searchFilter.params.codeNo,
                                    correctionTag: getCorrectionTag(),
                                    reportPeriodIds: getIds($scope.searchFilter.params.periods)
                                })
                            };
                        },
                        height: 250,
                        colNames: [
                            $filter('translate')('title.numberDeclaration'),
                            $filter('translate')('title.formKind'),
                            $filter('translate')('title.department'),
                            $filter('translate')('title.period'),
                            $filter('translate')('title.state'),
                            $filter('translate')('title.kpp'),
                            $filter('translate')('title.oktmo'),
                            $filter('translate')('title.codeNO'),
                            $filter('translate')('title.docState'),
                            $filter('translate')('title.dateAndTimeCreate'),
                            $filter('translate')('title.creator'),
                            $filter('translate')('title.xmlFile'),
                            $filter('translate')('title.note')],
                        colModel: [
                            {name: 'declarationDataId', index: 'declarationDataId', width: 110, key: true},
                            {name: 'declarationType', index: 'declarationType', width: 170, formatter: linkformatter},
                            {name: 'department', index: 'department', width: 200},
                            {name: 'reportPeriod', index: 'reportPeriod', width: 175},
                            {name: 'state', index: 'state', width: 175},
                            {name: 'kpp', index: 'kpp', width: 85},
                            {name: 'oktmo', index: 'oktmo', width: 80},
                            {name: 'taxOrganCode', index: 'taxOrganCode', width: 70},
                            {name: 'docState', index: 'docState', width: 130},
                            {
                                name: 'creationDate',
                                index: 'creationDate',
                                width: 205,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {name: 'creationUserName', index: 'creationUserName', width: 130},
                            {name: 'fileName', index: 'fileName', width: 200, formatter: linkFileFormatter},
                            {name: 'note', index: 'note', width: 200}
                        ],
                        rowNum: 20,
                        rowList: [10, 20, 30, 50, 100],
                        sortname: 'declarationDataId',
                        viewrecords: true,
                        sortorder: "desc",
                        hidegrid: false,
                        multiselect: true
                    }
                };

                /**
                 * @description инициализирует грид
                 * @param ctrl контроллер грида
                 */
                $scope.initOurGrid = function (ctrl) {
                    $scope.ctrlReportGrid = ctrl;
                    var grid = ctrl.getGrid();
                    grid.setGridParam({
                        onSelectRow: function () {
                            $scope.selectedItems = ctrl.getAllSelectedRows();
                            $scope.$apply();
                        },
                        onSelectAll: function () {
                            $scope.selectedItems = ctrl.getAllSelectedRows();
                            $scope.$apply();
                        }
                    });
                };

                /**
                 * @description форматтер для поля 'Вид налоговой формы' для перехода на конкретную НФ
                 * @param cellValue значение ячейки
                 * @param options данные таблицы
                 */
                function linkformatter(cellValue, options) {
                    return "<a href='index.html#/taxes/ndfl/" + options.rowId + "'>" + cellValue + "</a>";
                }

                /**
                 * @description форматтер для поля 'Файл ТФ' для получения файла ТФ
                 * @param cellValue значение ячейки
                 * @param options данные таблицы
                 */
                function linkFileFormatter(cellValue, options) {
                    if (!cellValue) {
                        cellValue = '';
                    }
                    return "<a target='_blank' href='controller/rest/declarationData/" + options.rowId + "/xml'>" + cellValue + "</a>";
                }
            }])
}());