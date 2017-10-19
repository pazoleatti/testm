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
                params: {uuid: null}
            });
        }])

        .controller('ndflReportJournalCtrl', ['$scope', '$state', '$stateParams', '$rootScope', '$filter', 'DeclarationDataResource', '$http', '$logPanel', 'appModals', 'APP_CONSTANTS', 'PermissionChecker',
            function ($scope, $state, $stateParams, $rootScope, $filter, DeclarationDataResource, $http, $logPanel, appModals, APP_CONSTANTS, PermissionChecker) {
                $scope.reportCreateAllowed = PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.CREATE_DECLARATION_REPORT);
                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');
                var defaultCorrectionTag = APP_CONSTANTS.CORRETION_TAG.ALL;

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

                $scope.searchFilter = {
                    params: {
                        correctionTag: defaultCorrectionTag
                    },
                    ajaxFilter: [],
                    isClear: false,
                    filterName: 'ndflReportsFilter'
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
                    appModals.create('client/app/taxes/ndfl/createReport.html', 'createReportCtrl',
                        {latestSelectedPeriod: $scope.latestSelectedPeriod}, {size: 'md'})
                        .result.then(function (response) {
                        if (response.data && response.data.entityId && response.data.entityId !== null) {
                            $state.go('ndfl', {
                                declarationDataId: response.data.entityId,
                                uuid: response.data.uuid
                            });
                        } else {
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
                    if ($scope.selectedItems && $scope.selectedItems.length > 0) {
                        return $scope.selectedItems.every(function (item) {
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

                /**
                 * @description Инициализация грида
                 * @param ctrl Контроллер грида
                 */
                var init = function (ctrl) {
                    //Установить обработчик выбора строки
                    ctrl.onSelectRow = function () {
                        $scope.selectedItems = ctrl.getAllSelectedRows();
                        $scope.$apply();
                    };

                    //Установить обрабочик выбора всех строк
                    ctrl.onSelectAll = function () {
                        $scope.selectedItems = ctrl.getAllSelectedRows();
                        $scope.$apply();
                    };
                };

                $scope.ndflReportJournalGrid = {
                    ctrl: {},
                    init: init,
                    options: {
                        datatype: "angularResource",
                        angularResource: DeclarationDataResource,
                        requestParameters: function () {
                            return {
                                projection: 'declarations',
                                filter: JSON.stringify({
                                    docStateIds: $filter('idExtractor')($scope.searchFilter.params.docState),
                                    departmentIds: $filter('idExtractor')($scope.searchFilter.params.departments),
                                    formKindIds: [APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id],
                                    declarationDataId: $scope.searchFilter.params.declarationNumber,
                                    declarationTypeIds: $filter('idExtractor')($scope.searchFilter.params.declarationTypes),
                                    formState: $scope.searchFilter.params.state ? $scope.searchFilter.params.state.id : undefined,
                                    fileName: $scope.searchFilter.params.file,
                                    note: $scope.searchFilter.params.note,
                                    oktmo: $scope.searchFilter.params.oktmo,
                                    taxOrganKpp: $scope.searchFilter.params.kpp,
                                    taxOrganCode: $scope.searchFilter.params.codeNo,
                                    correctionTag: $filter('correctionTagFormatter')($scope.searchFilter.params.correctionTag),
                                    reportPeriodIds: $filter('idExtractor')($scope.searchFilter.params.periods)
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
                            {
                                name: 'declarationType',
                                index: 'declarationType',
                                width: 170,
                                formatter: $filter('linkformatter')
                            },
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
                        ondblClickRow: function (rowId) {
                            $state.go("ndfl", {
                                declarationId: rowId
                            });
                        }
                    }
                };
                // Запоминаем самый поздний период для создания отчётности
                $scope.$watch('searchFilter.params.periods', function (selectedPeriods) {
                    if (selectedPeriods && selectedPeriods.length > 0) {
                        $scope.latestSelectedPeriod = selectedPeriods[selectedPeriods.length - 1];
                    } else {
                        $scope.latestSelectedPeriod = null;
                    }
                });

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/accept",
                        params: {
                            declarationDataIds: $filter('idExtractor')($scope.selectedItems, 'declarationDataId')
                        }
                    }).then(function (response) {
                        //Обновить страницу и, если есть сообщения, показать их
                        var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                        $state.go($state.current, params, {reload: true});
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.check = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/check",
                        params: {
                            declarationDataIds: $filter('idExtractor')($scope.selectedItems, 'declarationDataId')
                        }
                    }).then(function (response) {
                        //Обновить страницу и, если есть сообщения, показать их
                        var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                        $state.go($state.current, params, {reload: true});
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnToCreated = function () {
                    appModals.create('client/app/taxes/ndfl/returnToCreatedDialog.html', 'returnToCreatedCtrl', {
                        header: $filter('translate')('title.indicateReasonForReturn'),
                        msg: $filter('translate')('title.reasonForReturn')
                    }, {size: 'md'})
                        .result.then(
                        function (reason) {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/returnToCreated",
                                params: {
                                    declarationDataIds: $filter('idExtractor')($scope.selectedItems, 'declarationDataId'),
                                    reason: reason
                                }
                            }).then(function (response) {
                                //Обновить страницу и, если есть сообщения, показать их
                                var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                                $state.go($state.current, params, {reload: true});
                            });
                        });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Выгрузить отчетность"
                 */
                $scope.downloadReports = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/downloadReports",
                        params: {
                            declarationDataIds: $filter('idExtractor')($scope.selectedItems, 'declarationDataId')
                        }
                    }).then(function (response) {
                        //Обновить страницу и, если есть сообщения, показать их
                        var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                        $state.go($state.current, params, {reload: false});
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.delete = function () {
                    appModals.confirm($filter('translate')('title.confirm'), $filter('translate')('title.deleteDeclarations'))
                        .result.then(
                        function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/delete",
                                params: {
                                    declarationDataIds: $filter('idExtractor')($scope.selectedItems, 'declarationDataId')
                                }
                            }).then(function (response) {
                                //Обновить страницу и, если есть сообщения, показать их
                                var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                                $state.go($state.current, params, {reload: true});
                            });
                        });
                };

            }])
        /**
         * @description Форматтер для поля 'Вид налоговой формы' для перехода на конкретную НФ
         * @param cellValue Значение ячейки
         * @param options Данные таблицы
         */
        .filter('linkformatter', function () {
            return function (cellValue, options) {
                return "<a href='index.html#/taxes/ndfl/" + options.rowId + "'>" + cellValue + "</a>";
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
                return "<a target='_blank' href='controller/rest/declarationData/" + options.rowId + "/xml'>" + cellValue + "</a>";
            };
        })

        .filter('nameFormatter', function () {
            return function (entity) {
                return entity ? entity.name : "";
            };
        })

        .filter('periodFormatter', function () {
            return function (entity) {
                return entity ? entity.taxPeriod.year + ": " + entity.name : "";
            };
        })
        /**
         * @description Форматтер для преобразования тега корректировки из enum в boolean
         * @param correctionTag
         */
        .filter('correctionTagFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (correctionTag) {
                switch (correctionTag) {
                    case APP_CONSTANTS.CORRETION_TAG.ALL:
                        return undefined;
                    case APP_CONSTANTS.CORRETION_TAG.ONLY_PRIMARY:
                        return false;
                    case APP_CONSTANTS.CORRETION_TAG.ONLY_CORRECTIVE:
                        return true;
                }
                return undefined;
            };
        }]);
}());