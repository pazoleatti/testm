(function () {
    'use strict';

    /**
     * @description Модуль для работы с формой "Отчетность"
     */
    angular.module('app.ndflReportJournal', ['ui.router', 'app.createReport', 'app.updateDocStateModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflReportJournal', {
                url: '/taxes/ndflReportJournal',
                templateUrl: 'client/app/taxes/ndfl/reportForm/ndflReportJournal.html',
                controller: 'ndflReportJournalCtrl',
                params: {uuid: null},
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_REPORTS)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        .controller('ndflReportJournalCtrl', ['$scope', '$state', '$stateParams', '$rootScope', '$filter', 'DeclarationDataResource', '$http',
            '$logPanel', '$aplanaModal', '$dialogs', 'APP_CONSTANTS', 'PermissionChecker', '$webStorage',
            function ($scope, $state, $stateParams, $rootScope, $filter, DeclarationDataResource, $http,
                      $logPanel, $aplanaModal, $dialogs, APP_CONSTANTS, PermissionChecker, $webStorage) {
                $scope.reportCreateAllowed = PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.CREATE_DECLARATION_REPORT);
                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');
                var defaultCorrectionTag = APP_CONSTANTS.CORRECTION_TAG.ALL;

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

                $scope.searchFilter = {
                    params: {
                        correctionTag: defaultCorrectionTag
                    },
                    ajaxFilter: [],
                    isClear: false,
                    filterName: 'ndflReportsFilter',
                    onCreateComplete: function () {
                        $scope.refreshGrid();
                    }
                };
                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.ndflReportJournalGrid.ctrl.refreshGrid(page);
                };

                /**
                 * Показ МО "Создание отчётности"
                 */
                $scope.createReport = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('title.creatingReport'),
                        templateUrl: 'client/app/taxes/ndfl/reportForm/createReport.html',
                        controller: 'createReportCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {};
                            }
                        }
                    }).result.then(function (response) {
                        if (response) {
                            if (response.data && response.data.uuid && response.data.uuid !== null) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                        }
                    });
                };

                /**
                 * Проверка, может ли текущий пользоватеть выполнить операцию над выделенными налоговыми формами
                 * @param permission
                 */
                $scope.checkPermissionForSelectedItems = function (permission) {
                    var selectedItems = $scope.ndflReportJournalGrid.value;
                    if (selectedItems && selectedItems.length > 0) {
                        return selectedItems.every(function (item) {
                            return PermissionChecker.check(item, permission);
                        });
                    } else {
                        return false;
                    }
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

                function getFilter() {
                    return {
                        docStateIds: $filter('idExtractor')($scope.searchFilter.params.docState),
                        departmentIds: $filter('idExtractor')($scope.searchFilter.params.departments),
                        formKindIds: [APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id],
                        declarationDataId: $scope.searchFilter.params.declarationNumber,
                        declarationTypeIds: $filter('idExtractor')($scope.searchFilter.params.declarationTypes),
                        formStates: $filter('idExtractor')($scope.searchFilter.params.states),
                        fileName: $scope.searchFilter.params.file,
                        note: $scope.searchFilter.params.note,
                        oktmo: $scope.searchFilter.params.oktmo,
                        taxOrganKpp: $scope.searchFilter.params.kpp,
                        taxOrganCode: $scope.searchFilter.params.codeNo,
                        correctionTag: $filter('correctionTagFormatter')($scope.searchFilter.params.correctionTag),
                        reportPeriodIds: $filter('idExtractor')($scope.searchFilter.params.periods),
                        correctionNum: $scope.searchFilter.params.correctionNum,
                        creationUserName: $scope.searchFilter.params.creationUserName,
                        creationDateFrom: $filter('dateTimeSerializer')($scope.searchFilter.params.creationDateFrom),
                        creationDateTo: $filter('dateTimeSerializer')($scope.searchFilter.params.creationDateTo)
                    };
                }

                $scope.ndflReportJournalGrid = {
                    ctrl: {},
                    options: {
                        gridName: 'ndflReportJournal',
                        datatype: "angularResource",
                        angularResource: DeclarationDataResource,
                        requestParameters: function () {
                            return {
                                projection: 'declarations',
                                filter: JSON.stringify(getFilter())
                            };
                        },
                        height: 250,
                        colNames: [
                            $filter('translate')('title.numberDeclaration'),
                            $filter('translate')('title.formKind'),
                            $filter('translate')('title.department'),
                            $filter('translate')('title.period'),
                            $filter('translate')('title.state'),
                            $filter('translate')('title.correctionNum'),
                            $filter('translate')('title.kpp'),
                            $filter('translate')('title.oktmo'),
                            $filter('translate')('title.codeNO'),
                            $filter('translate')('title.docState'),
                            $filter('translate')('title.dateAndTimeCreate'),
                            $filter('translate')('title.creator'),
                            $filter('translate')('title.xmlFile'),
                            $filter('translate')('title.note')],
                        colModel: [
                            {name: 'declarationDataId', index: 'declarationDataId', width: 120, key: true},
                            {
                                name: 'declarationType',
                                index: 'declarationType',
                                width: 170,
                                formatter: $filter('linkReportFormatter')
                            },
                            {name: 'department', index: 'department', width: 200},
                            {name: 'reportPeriod', index: 'reportPeriod', width: 175},
                            {name: 'state', index: 'state', width: 100},
                            {name: 'correctionNum', index: 'correction_num', width: 110},
                            {name: 'kpp', index: 'kpp', width: 85},
                            {name: 'oktmo', index: 'oktmo', width: 80},
                            {name: 'taxOrganCode', index: 'taxOrganCode', width: 70},
                            {name: 'docState', index: 'docState', width: 130},
                            {
                                name: 'creationDate',
                                index: 'creationDate',
                                width: 215,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {name: 'creationUserName', index: 'creationUserName', width: 130},
                            {name: 'fileName', index: 'fileName', width: 200, formatter: $filter('linkFileFormatter')},
                            {name: 'note', index: 'note', width: 200}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'declarationDataId',
                        viewrecords: true,
                        sortorder: "desc",
                        hidegrid: false,
                        multiselect: true,
                        disableAutoLoad: true,
                        fullScreen: true,
                        ondblClickRow: function (rowId) {
                            $state.go("ndflReport", {
                                declarationId: rowId
                            });
                        }
                    }
                };
                // Запоминаем самый поздний период для создания отчётности
                // Флаг на загрузку страницы, когда страница загружается -
                // значение последнего выбранного периода не должно сбрасываться
                var isLoadingPage = true;
                $scope.$watch('searchFilter.params.periods', function (selectedPeriods) {
                    if (selectedPeriods && selectedPeriods.length > 0) {
                        $webStorage.set(APP_CONSTANTS.USER_STORAGE.NAME, APP_CONSTANTS.USER_STORAGE.KEYS.LAST_SELECTED_PERIOD, selectedPeriods[selectedPeriods.length - 1], true);
                        isLoadingPage = false;
                    } else {
                        if (!isLoadingPage) {
                            $webStorage.remove(APP_CONSTANTS.USER_STORAGE.NAME, APP_CONSTANTS.USER_STORAGE.KEYS.LAST_SELECTED_PERIOD, true);
                        }
                    }
                });

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function () {
                    var selectedItems = $scope.ndflReportJournalGrid.value;
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/accept",
                        data: $filter('idExtractor')(selectedItems, 'declarationDataId')
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.check = function () {
                    var selectedItems = $scope.ndflReportJournalGrid.value;
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/check",
                        data: $filter('idExtractor')(selectedItems, 'declarationDataId')
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnToCreated = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('title.indicateReasonForReturn'),
                        templateUrl: 'client/app/taxes/ndfl/returnToCreatedDialog.html',
                        controller: 'returnToCreatedCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    msg: $filter('translate')('title.reasonForReturn')
                                };
                            }
                        }
                    }).result.then(function (reason) {
                        var selectedItems = $scope.ndflReportJournalGrid.value;
                        $http({
                            method: "POST",
                            url: "controller/actions/declarationData/returnToCreated",
                            data: $filter('idExtractor')(selectedItems, 'declarationDataId'),
                            params: {
                                reason: reason
                            }
                        });
                    });
                };

                /**
                 * @description Выгрузить отчетность по фильтру
                 */
                $scope.downloadReportsByAccepted = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/downloadReportsByFilter",
                        params: {
                            filter: JSON.stringify({
                                formStates: [APP_CONSTANTS.STATE.ACCEPTED.id],
                                formKindIds: [APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id]
                            })
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * @description Выгрузить отчетность по фильтру
                 */
                $scope.downloadReportsByFilter = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/downloadReportsByFilter",
                        params: {
                            filter: JSON.stringify(getFilter())
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * @description Выгрузить отчетность по фильтру по выбранным формам
                 */
                $scope.downloadReportsBySelected = function () {
                    var selectedItems = $scope.ndflReportJournalGrid.value;
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/downloadReports",
                        data: $filter('idExtractor')(selectedItems, 'declarationDataId')
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * @description Выгрузить отчетность по фильтру
                 */
                $scope.updateDocStateByFilter = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('title.updateDocState'),
                        templateUrl: 'client/app/taxes/ndfl/reportForm/updateDocStateModal.html',
                        controller: 'UpdateDocStateCtrl',
                        windowClass: 'modal600'
                    }).result.then(function (docState) {
                        if (docState) {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/updateDocStateByFilter",
                                params: {
                                    filter: JSON.stringify(getFilter()),
                                    docStateId: docState.id
                                }
                            }).then(function (response) {
                                if (response.data && response.data.uuid) {
                                    $logPanel.open('log-panel-container', response.data.uuid);
                                }
                            });
                        }
                    });
                };

                /**
                 * @description Выгрузить отчетность по фильтру по выбранным формам
                 */
                $scope.updateDocStateBySelected = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('title.updateDocState'),
                        templateUrl: 'client/app/taxes/ndfl/reportForm/updateDocStateModal.html',
                        controller: 'UpdateDocStateCtrl',
                        windowClass: 'modal600'
                    }).result.then(function (docState) {
                        if (docState) {
                            var selectedItems = $scope.ndflReportJournalGrid.value;
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/updateDocState",
                                data: $filter('idExtractor')(selectedItems, 'declarationDataId'),
                                params: {
                                    docStateId: docState.id
                                }
                            }).then(function (response) {
                                if (response.data && response.data.uuid) {
                                    $logPanel.open('log-panel-container', response.data.uuid);
                                }
                            });
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.delete = function () {
                    var selectedItems = $scope.ndflReportJournalGrid.value;
                    $dialogs.confirmDialog({
                        content: $filter('translate')('title.deleteDeclarations'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/delete",
                                data: $filter('idExtractor')(selectedItems, 'declarationDataId')
                            }).then(function (response) {
                                if (response.data && response.data.uuid && response.data.uuid !== null) {
                                    $logPanel.open('log-panel-container', response.data.uuid);
                                }
                            });
                        }
                    });
                };

            }])
        /**
         * @description Форматтер для поля 'Вид налоговой формы' для перехода на конкретную НФ
         * @param cellValue Значение ячейки
         * @param options Данные таблицы
         */
        .filter('linkReportFormatter', function () {
            return function (cellValue, options) {
                return "<a href='index.html#/taxes/ndfl/ndflReport/" + options.rowId + "'>" + cellValue + "</a>";
            };
        })
        /**
         * @description Форматтер для поля 'Файл ТФ' для получения файла ТФ
         * @param cellValue Значение ячейки
         * @param options Данные таблицы
         */
        .filter('linkFileFormatter', function () {
            return function (cellValue, options) {
                if (!cellValue) {
                    cellValue = '';
                }
                return "<a href='controller/rest/declarationData/" + options.rowId + "/xml'>" + cellValue + "</a>";
            };
        });
}());