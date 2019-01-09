(function () {
    'use strict';

    /**
     * @description Модуль для работы со формами ндфл
     */
    angular.module('app.ndflJournal', ['ui.router', 'app.constants', 'app.rest', 'app.createDeclaration', 'app.logPanel', 'app.formatters', 'app.select.common'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflJournal', {
                url: '/taxes/ndflJournal',
                templateUrl: 'client/app/taxes/ndfl/taxForm/ndflJournal.html',
                controller: 'ndflJournalCtrl',
                params: {uuid: null},
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        /**
         * @description Контроллер списка форм
         */
        .controller('ndflJournalCtrl', [
            '$scope', '$state', '$stateParams', '$filter', '$rootScope', 'DeclarationDataResource', 'APP_CONSTANTS',
            '$aplanaModal', '$dialogs', '$logPanel', 'PermissionChecker', '$http', '$webStorage',
            function ($scope, $state, $stateParams, $filter, $rootScope, DeclarationDataResource, APP_CONSTANTS,
                      $aplanaModal, $dialogs, $logPanel, PermissionChecker, $http, $webStorage) {
                $rootScope.declarationPrimaryCreateAllowed = PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.CREATE_DECLARATION_PRIMARY);
                $rootScope.declarationConsolidatedCreateAllowed = PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.CREATE_DECLARATION_CONSOLIDATED);
                $rootScope.declarationCreateAllowed = $rootScope.declarationPrimaryCreateAllowed || $rootScope.declarationConsolidatedCreateAllowed;

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');

                var defaultCorrectionTag = APP_CONSTANTS.CORRECTION_TAG.ALL;

                $scope.searchFilter = {
                    params: {
                        correctionTag: defaultCorrectionTag
                    },
                    ajaxFilter: [],
                    isClear: false,
                    filterName: 'ndflJournalFilter',
                    onCreateComplete: function () {
                        $scope.refreshGrid();
                    }
                };

                // Флаг отображения кнопки "Сбросить"
                $scope.searchFilter.isClearByFilterParams = function () {
                    var needToClear = false;
                    angular.forEach($scope.searchFilter.params, function (value, key) {
                        //Если значение поля корретировки отличается от значения по умолчанию, то фильтр можно сбросить
                        if (key === 'correctionTag') {
                            needToClear = needToClear || value.id !== defaultCorrectionTag.id;
                        } else if (value != null) {
                            //Если у поля значение отлично от null, то фильтр можно сбросить
                            //Дополнительная проверка нужно только полей, значениями которых являются массивы и строки
                            //Длина таких значений должна быть больше 0
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

                // Запоминаем самый поздний период для создания налоговой формы
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

                $scope.ndflJournalGrid = {
                    ctrl: {},
                    options: {
                        gridName: 'ndflJournal',
                        datatype: "angularResource",
                        angularResource: DeclarationDataResource,
                        requestParameters: function () {
                            return {
                                projection: 'declarations',
                                filter: JSON.stringify({
                                    asnuIds: $filter('idExtractor')($scope.searchFilter.params.asnuList),
                                    departmentIds: $filter('idExtractor')($scope.searchFilter.params.departments),
                                    formKindIds: $filter('idExtractor')($scope.searchFilter.params.declarationKinds),
                                    declarationDataId: $scope.searchFilter.params.declarationNumber,
                                    declarationTypeIds: $filter('idExtractor')($scope.searchFilter.params.declarationTypes),
                                    formStates: $scope.searchFilter.params.state ? [$scope.searchFilter.params.state.id] : undefined,
                                    fileName: $scope.searchFilter.params.file,
                                    correctionTag: $filter('correctionTagFormatter')($scope.searchFilter.params.correctionTag),
                                    reportPeriodIds: $filter('idExtractor')($scope.searchFilter.params.periods),
                                    knfTypeIds: $filter('idExtractor')($scope.searchFilter.params.knfTypes)
                                })
                            };
                        },
                        value: [],
                        colNames: [
                            $filter('translate')('ndflJournal.grid.columnName.declarationNumber'),
                            $filter('translate')('ndflJournal.grid.columnName.declarationKind'),
                            $filter('translate')('ndflJournal.grid.columnName.declarationType'),
                            $filter('translate')('ndflJournal.grid.columnName.department'),
                            $filter('translate')('ndflJournal.grid.columnName.asnu'),
                            $filter('translate')('ndflJournal.grid.columnName.knfType'),
                            $filter('translate')('ndflJournal.grid.columnName.period'),
                            $filter('translate')('ndflJournal.grid.columnName.state'),
                            $filter('translate')('ndflJournal.grid.columnName.tfFile'),
                            $filter('translate')('ndflJournal.grid.columnName.creationDateTime'),
                            $filter('translate')('ndflJournal.grid.columnName.creator')],
                        colModel: [
                            {name: 'declarationDataId', index: 'declarationDataId', width: 135, key: true},
                            {name: 'declarationKind', index: 'declarationKind', width: 175},
                            {
                                name: 'declarationType',
                                index: 'declarationType',
                                width: 175,
                                formatter: $filter('linkformatter')
                            },
                            {name: 'department', index: 'department', width: 150},
                            {name: 'asnuName', index: 'asnuName', width: 176},
                            {name: 'knfTypeName', index: 'knfTypeName', width: 176},
                            {name: 'reportPeriod', index: 'reportPeriod', width: 110},
                            {name: 'state', index: 'state', width: 100},
                            {
                                name: 'fileName',
                                index: 'fileName',
                                width: 400,
                                formatter: $filter('linkFileNdflFormatter')
                            },
                            {
                                name: 'creationDate',
                                index: 'creationDate',
                                width: 230,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {name: 'creationUserName', index: 'creationUserName', width: 175}
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
                            $state.go("ndfl", {
                                declarationDataId: rowId
                            });
                        }
                    }
                };

                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.ndflJournalGrid.ctrl.refreshGrid(page);
                };

                /**
                 * Показ МО "Создание налоговой формы"
                 */
                $scope.showCreateDeclarationModal = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('createDeclaration.title'),
                        templateUrl: 'client/app/taxes/ndfl/taxForm/createDeclaration.html',
                        controller: 'createDeclarationFormCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    latestSelectedPeriod: $webStorage.get(APP_CONSTANTS.USER_STORAGE.NAME, APP_CONSTANTS.USER_STORAGE.KEYS.LAST_SELECTED_PERIOD, true)
                                };
                            }
                        }
                    }).result.then(
                        function (response) {
                            if (response) {
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
                            }
                        });
                };

                /**
                 * Проверка, может ли текущий пользоватеть выполнить операцию над выделенными налоговыми формами
                 * @param permission
                 */
                $scope.checkPermissionForSelectedItems = function (permission) {
                    var selectedItems = $scope.ndflJournalGrid.value;
                    if (selectedItems && selectedItems.length > 0) {
                        return selectedItems.every(function (item) {
                            return PermissionChecker.check(item, permission);
                        });
                    } else {
                        return false;
                    }
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Идентифицировать ФЛ"
                 */
                $scope.identify = function () {
                    var selectedItems = $scope.ndflJournalGrid.value;
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/identify",
                        data: $filter('idExtractor')(selectedItems, 'declarationDataId')
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Консолидировать"
                 */
                $scope.consolidate = function () {
                    var selectedItems = $scope.ndflJournalGrid.value;
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/consolidate",
                        data: $filter('idExtractor')(selectedItems, 'declarationDataId')
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function () {
                    var selectedItems = $scope.ndflJournalGrid.value;
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
                    var selectedItems = $scope.ndflJournalGrid.value;
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
                    }).result.then(
                        function (reason) {
                            var selectedItems = $scope.ndflJournalGrid.value;
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
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.delete = function () {
                    var selectedItems = $scope.ndflJournalGrid.value;
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

                /**
                 * @description Проверяется существование налоговой формы перед выгрузкой ТФ
                 */
                $(document).undelegate('#ndflJournalTable .tfDownloadLink', 'click');
                $(document).delegate('#ndflJournalTable .tfDownloadLink', 'click', function () {
                    var declarationDataId = $(this).attr('declarationDataId');
                    DeclarationDataResource.query({
                        declarationDataId: declarationDataId,
                        projection: "existenceAndKind"
                    }, function (response) {
                        if (response.exists) {
                            window.open('controller/rest/declarationData/' + declarationDataId + '/xml', '_self');
                        } else {
                            $dialogs.errorDialog({
                                content: $filter('translate')('ndflJournal.downloadTf.error.notExist.text', {declarationDataId: declarationDataId}),
                                closeBtnClick: function () {
                                    $state.go("/");
                                }
                            });
                        }
                    });
                });
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
        .filter('linkFileNdflFormatter', function () {
            return function (cellValue, options) {
                if (!cellValue) {
                    cellValue = '';
                }
                return '<a class="tfDownloadLink" declarationDataId="' + options.rowId + '">' + cellValue + '</a>';
            };
        })
    ;
}());