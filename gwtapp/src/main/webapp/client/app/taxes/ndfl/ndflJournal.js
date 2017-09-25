(function () {
    'use strict';

    /**
     * @description Модуль для работы со формами ндфл
     */
    angular.module('app.ndflJournal', ['ui.router', 'app.constants', 'app.modals', 'app.rest', 'app.createDeclaration', 'app.logPanel', 'RefBookValuesResource'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflJournal', {
                url: '/taxes/ndflJournal',
                templateUrl: 'client/app/taxes/ndfl/ndflJournal.html',
                controller: 'ndflJournalCtrl',
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
                    },
                    asnu: function (APP_CONSTANTS, RefBookValuesResource) {
                        return RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.ASNU}, function (data) {
                            return data;
                        }).$promise;
                    }
                }
            });
        }])

        /**
         * @description Контроллер списка форм
         */
        .controller('ndflJournalCtrl', [
            '$scope', '$state', '$filter', '$rootScope', 'DeclarationDataResource', 'APP_CONSTANTS', 'appModals', 'periods', 'openPeriods', 'declarationTypes',
            'asnu', '$logPanel', 'PermissionChecker',
            function ($scope, $state, $filter, $rootScope, DeclarationDataResource, APP_CONSTANTS, appModals, periods, openPeriods, declarationTypes,
                      asnu, $logPanel, PermissionChecker) {
                $scope.security = {
                    user: $rootScope.user
                };

                $scope.declarationCreateAllowed = PermissionChecker.check($scope.security.user, APP_CONSTANTS.USER_PERMISSION.CREATE_DECLARATION_CONSOLIDATED);

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
                    $scope.ctrlMyGrid.refreshGrid(page);
                };

                var correctionTags = [APP_CONSTANTS.CORRETION_TAG.ONLY_CORRECTIVE, APP_CONSTANTS.CORRETION_TAG.ONLY_PRIMARY, APP_CONSTANTS.CORRETION_TAG.ALL];
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

                var declarationKinds = [];
                if ($scope.security.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_CONTROL_NS) || $scope.security.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_CONTROL_UNP)) {
                    declarationKinds = [APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY, APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED];
                } else if ($scope.security.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_OPER)) {
                    declarationKinds = [APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY];
                }

                $scope.searchFilter = {
                    params: {
                        correctionTag: defaultCorrectionTag
                    },
                    ajaxFilter: [],
                    isClear: false,
                    filterName: 'ndflDeclarationFilter'
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

                $scope.declarationKindSelect = {
                    options: {
                        data: {
                            results: declarationKinds,
                            text: $filter('nameFormatter')
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

                $scope.asnuSelect = {
                    options: {
                        data: {
                            results: asnu,
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

                $scope.ndflJournalGridOptions = {
                    datatype: "angularResource",
                    angularResource: DeclarationDataResource,
                    requestParameters: function () {
                        return {
                            projection: 'declarations',
                            filter: JSON.stringify({
                                asnuIds: getIds($scope.searchFilter.params.asnuList),
                                departmentIds: getIds($scope.searchFilter.params.departments),
                                formKindIds: getIds($scope.searchFilter.params.declarationKinds),
                                declarationDataId: $scope.searchFilter.params.declarationNumber,
                                declarationTypeIds: getIds($scope.searchFilter.params.declarationTypes),
                                formState: $scope.searchFilter.params.state ? $scope.searchFilter.params.state.id : undefined,
                                fileName: $scope.searchFilter.params.file,
                                correctionTag: getCorrectionTag(),
                                reportPeriodIds: getIds($scope.searchFilter.params.periods)
                            })
                        };
                    },
                    value: [],
                    colNames: [
                        'Номер формы',
                        'Тип налоговой формы',
                        'Вид налоговой формы',
                        'Подразделение',
                        'Наименование АСНУ',
                        'Период',
                        'Состояние',
                        'Файл ТФ',
                        'Дата и время создания формы',
                        'Создал'],
                    colModel: [
                        {name: 'declarationDataId', index: 'declarationDataId', width: 135, key: true},
                        {name: 'declarationKind', index: 'declarationKind', width: 175},
                        {name: 'declarationType', index: 'declarationType', width: 175, formatter: linkformatter},
                        {name: 'department', index: 'department', width: 150},
                        {name: 'asnuName', index: 'asnuName', width: 176},
                        {name: 'reportPeriod', index: 'reportPeriod', width: 110},
                        {name: 'state', index: 'state', width: 100},
                        {name: 'fileName', index: 'fileName', width: 400, formatter: linkFileFormatter},
                        {
                            name: 'creationDate',
                            index: 'creationDate',
                            width: 230,
                            formatter: $filter('dateTimeFormatter')
                        },
                        {name: 'creationUserName', index: 'creationUserName', width: 175}
                    ],
                    rowNum: 100,
                    rowList: [10, 50, 100, 200],
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
                };

                /**
                 * @description инициализирует грид
                 * @param ctrl контроллер грида
                 */
                $scope.initOurGrid = function (ctrl) {
                    $scope.ctrlMyGrid = ctrl;
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
                 * Показ МО "Создание налоговой формы"
                 */
                $scope.showCreateDeclarationModal = function () {
                    var modal = appModals.create('client/app/taxes/ndfl/createDeclaration.html', 'createDeclarationFormCtrl', {
                        periods: openPeriods,
                        latestSelectedPeriod: $scope.latestSelectedPeriod
                    }, {size: 'md'});
                    modal.result.then(function (response) {
                        if (response.data && response.data.declarationId && response.data.declarationId !== null) {
                            $state.go('ndfl', {
                                declarationDataId: response.data.declarationId,
                                uuid: response.data.uuid
                            });
                        } else {
                            if (response.data && response.data.uuid && response.data.uuid !== null) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                        }
                    });
                };
            }])

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

        .filter('nameFormatter', function () {
            return function (entity) {
                return entity ? entity.name : "";
            };
        })

        .filter('periodFormatter', function () {
            return function (entity) {
                return entity ? entity.taxPeriod.year + ": " + entity.name : "";
            };
        });
}());