(function () {
    'use strict';

    /**
     * @description Модуль для работы со формами ндфл
     */
    angular.module('app.ndflJournal', ['ui.router', 'app.constants', 'app.modals', 'app.rest', 'app.createDeclaration', 'app.logPanel', 'app.formatters', 'app.select.common'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflJournal', {
                url: '/taxes/ndflJournal',
                templateUrl: 'client/app/taxes/ndfl/ndflJournal.html',
                controller: 'ndflJournalCtrl',
                params: {uuid: null}
            });
        }])

        /**
         * @description Контроллер списка форм
         */
        .controller('ndflJournalCtrl', [
            '$scope', '$state', '$stateParams', '$filter', '$rootScope', 'DeclarationDataResource', 'APP_CONSTANTS', 'appModals', '$logPanel', 'PermissionChecker', '$http',
            function ($scope, $state, $stateParams, $filter, $rootScope, DeclarationDataResource, APP_CONSTANTS, appModals, $logPanel, PermissionChecker, $http) {
                $scope.declarationCreateAllowed = PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.CREATE_DECLARATION_CONSOLIDATED);

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');

                var defaultCorrectionTag = APP_CONSTANTS.CORRETION_TAG.ALL;

                $scope.searchFilter = {
                    params: {
                        correctionTag: defaultCorrectionTag
                    },
                    ajaxFilter: [],
                    isClear: false,
                    filterName: 'ndflJournalFilter'
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

                $scope.$watch('searchFilter.params.periods', function (selectedPeriods) {
                    if (selectedPeriods && selectedPeriods.length > 0) {
                        $scope.latestSelectedPeriod = selectedPeriods[selectedPeriods.length - 1];
                    } else {
                        $scope.latestSelectedPeriod = null;
                    }
                });

                /**
                 * @description Инициализация грида
                 * @param ctrl Контроллер грида
                 */
                var init = function (ctrl) {
                    //Установить обработчик выбора строки
                    ctrl.onSelectRow = function (data) {
                        $scope.selectedItems = ctrl.getAllSelectedRows();
                        $scope.$apply();
                    };

                    //Установить обрабочик выбора всех строк
                    ctrl.onSelectAll = function (data) {
                        $scope.selectedItems = ctrl.getAllSelectedRows();
                        $scope.$apply();
                    };
                };

                $scope.ndflJournalGrid = {
                    ctrl: {},
                    init: init,
                    options: {
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
                                    formState: $scope.searchFilter.params.state ? $scope.searchFilter.params.state.id : undefined,
                                    fileName: $scope.searchFilter.params.file,
                                    correctionTag: $filter('correctionTagFormatter')($scope.searchFilter.params.correctionTag),
                                    reportPeriodIds: $filter('idExtractor')($scope.searchFilter.params.periods)
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
                            {name: 'reportPeriod', index: 'reportPeriod', width: 110},
                            {name: 'state', index: 'state', width: 100},
                            {name: 'fileName', index: 'fileName', width: 400, formatter: $filter('linkFileFormatter')},
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
                        ondblClickRow: function (rowId) {
                            $state.go("ndfl", {
                                declarationId: rowId
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
                    var modal = appModals.create('client/app/taxes/ndfl/createDeclaration.html', 'createDeclarationFormCtrl',
                        {latestSelectedPeriod: $scope.latestSelectedPeriod}, {size: 'md'});
                    modal.result.then(function (response) {
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

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Рассчитать"
                 */
                $scope.calculate = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/recalculate",
                        params: {
                            declarationDataIds: $filter('idExtractor')($scope.selectedItems, 'declarationDataId')
                        }
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