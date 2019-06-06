(function () {
    'use strict';

    /**
     * @description Модуль для отображения линейного справочника
     */
    angular.module('app.linearRefBook', ['app.refBookRecordModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('linearRefBook', {
                url: '/refBooks/linearRefBook/{refBookId}?recordId',
                templateUrl: 'client/app/refbooks/linearRefBook.html',
                controller: 'linearRefBookCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_NSI)) {
                            $state.go("/");
                        }
                    }]
            });
        }])
        .controller('linearRefBookCtrl', ['$scope', "$stateParams", "$injector", "$compile", "APP_CONSTANTS",
            "RefBookResource", "RefBookRecordResource", "$aplanaModal", '$filter', '$window', "$http", "$logPanel", "$dialogs",
            function ($scope, $stateParams, $injector, $compile, APP_CONSTANTS, RefBookResource, RefBookRecordResource,
                      $aplanaModal, $filter, $window, $http, $logPanel, $dialogs) {
                $scope.columnNames = [];
                $scope.columnModel = [];
                $scope.data = {};
                $scope.data.recordVersion = new Date();
                $scope.versionMode = $stateParams.recordId && $stateParams.recordId !== 'undefined';
                $scope.search = {
                    text: "",
                    precise: false
                };

                // Получаем данные справочника
                RefBookResource.query({
                    id: $stateParams.refBookId
                }, function (data) {
                    $scope.refBook = data;
                    // Определяем атрибут для сортировки по умолчанию в справочнике
                    if ($scope.refBook.sortAttribute) {
                        // Если в настройках справочника указан атрибут для сортировки - берем его
                        $scope.sortAttribute = $scope.refBook.sortAttribute.alias;
                    }

                    if ($scope.refBook.versioned) {
                        // Если справочник версионируемый - добавляем информацию о периоде действия записей
                        $scope.refBook.attributes.unshift({
                            name: $filter('translate')('refBook.versionTo'),
                            alias: APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_TO_ALIAS,
                            attributeType: 'DATE',
                            required: true,
                            visible: true,
                            width: 5
                        });
                        $scope.refBook.attributes.unshift({
                            name: $filter('translate')('refBook.versionFrom'),
                            alias: APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_FROM_ALIAS,
                            attributeType: 'DATE',
                            required: true,
                            visible: true,
                            width: 5
                        });
                    } else {
                        $scope.data.recordVersion = null;
                    }
                    $scope.constructGridColumns();
                    $scope.constructGrid();
                });

                /**
                 * Динамически создает колонки для грида в соответствии с аттрибутами справочника
                 */
                $scope.constructGridColumns = function () {
                    // Добавляем колонку с id записи для выбора строк
                    $scope.columnNames.push('');
                    $scope.columnModel.push(
                        {
                            name: 'id.value',
                            index: 'id',
                            width: 100,
                            key: true,
                            type: "NUMBER",
                            formatter: refBookValueFormatter,
                            hidden: true
                        }
                    );

                    // Добавляем колонку для каждого аттрибута
                    $scope.refBook.attributes.forEach(function (attribute) {
                        if (attribute.visible) {
                            $scope.columnNames.push(attribute.name);
                            $scope.columnModel.push(
                                {
                                    name: attribute.alias,
                                    index: attribute.alias,
                                    width: attribute.width * APP_CONSTANTS.REFBOOK_EM_TO_PX_CONVERSION_INDEX, //TODO: пока так, потому что в БД ширина задана в em, а оно не поддерживается в jqgrid
                                    type: attribute.attributeType,
                                    attribute: attribute,
                                    formatter: refBookValueFormatter,
                                    sortable: !$scope.versionMode && attribute.alias !== APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_FROM_ALIAS && attribute.alias !== APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_TO_ALIAS
                                }
                            );
                        }
                    });
                };

                /**
                 * Получает значение атрибута в зависимости от его типа
                 */
                function refBookValueFormatter(cellValue, options, row) {
                    var colModel = options.index ? options : options.colModel;
                    var refBookValue = row[colModel.index];
                    var value = "";
                    switch (colModel.type) {
                        case 'STRING':
                        case 'NUMBER':
                            value = refBookValue && typeof refBookValue.value !== 'undefined' ? refBookValue.value : "";
                            break;
                        case 'DATE':
                            // Особенность для справочника "Коды, определяющие налоговый (отчётный) период" - год отображать не надо
                            var formatter;
                            if ($scope.refBook.id === APP_CONSTANTS.REFBOOK.PERIOD_CODE ||
                                $scope.refBook.id === APP_CONSTANTS.REFBOOK.REPORT_PERIOD_IMPORT && (colModel.index === 'PERIOD_START_DATE' || colModel.index === 'PERIOD_END_DATE')) {
                                formatter = $filter('dateWithoutYearFormatter');
                            } else {
                                formatter = $filter('dateFormatter');
                            }
                            value = refBookValue && typeof refBookValue.value !== 'undefined' ? formatter(refBookValue.value) : "";
                            break;
                        case 'REFERENCE':
                            value = refBookValue && typeof refBookValue.referenceObject !== 'undefined' ? refCellFormatter(colModel, refBookValue) : "";
                            break;
                    }
                    return value;
                }

                function refCellFormatter(colModel, refBookValue) {
                    if (colModel.attribute.refBookId === APP_CONSTANTS.REFBOOK.DEDUCTION_MARK ||
                        colModel.attribute.refBookId === APP_CONSTANTS.REFBOOK.ASNU ||
                        colModel.attribute.refBookId === APP_CONSTANTS.REFBOOK.PERIOD_CODE) {
                        return $filter("codeNameFormatter")({
                            'code': refBookValue.referenceObject["CODE"].value,
                            'name': refBookValue.referenceObject["NAME"].value
                        });
                    } else {
                        return refBookValue.referenceObject[colModel.attribute.refBookAttribute.alias].value;
                    }
                }

                /**
                 * Динамически добавляет грид на страницу и заполняет его данными
                 */
                $scope.constructGrid = function () {
                    // Добавляем грид на страницу
                    $injector.invoke(function ($compile) {
                        var parent = angular.element(document.querySelector("#refBookGridContainer"));
                        var refBookGrid = $compile("<div class=\"flex-grid flex-fill\"\n" +
                            "             data-aplana-grid\n" +
                            "             data-grid-fill-space=\"true\"\n" +
                            "             data-grid-fill-space-container-selector=\"#refBookGridContainer\"\n" +
                            "             data-grid-fill-space-container-selector-top=\"#refBookRecordsGridTop\"\n" +
                            "             data-grid-options=\"refBookGrid.options\"\n" +
                            "             data-grid-ctrl=\"refBookGrid.ctrl\"\n" +
                            "             data-ng-model=\"refBookGrid.value\"\n" +
                            "             id=\"refBookGrid\"></div>")(parent.scope());
                        parent.append(refBookGrid);
                    });

                    // Заполняем грид записями
                    $scope.refBookGrid = {
                        ctrl: {},
                        value: [],
                        options: {
                            datatype: "angularResource",
                            angularResource: RefBookRecordResource,
                            requestParameters: function () {
                                return {
                                    refBookId: $stateParams.refBookId,
                                    recordId: $stateParams.recordId,
                                    version: $scope.data.recordVersion,
                                    searchPattern: $scope.search.text,
                                    exactSearch: $scope.search.precise
                                };
                            },
                            colNames: $scope.columnNames,
                            colModel: $scope.columnModel,
                            rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                            rowList: APP_CONSTANTS.COMMON.PAGINATION,
                            viewrecords: true,
                            hidegrid: false,
                            multiselect: true,
                            sortname: $scope.sortAttribute,
                            sortorder: "asc",
                            ondblClickRow: function (rowId) {
                                $scope.showRecord($scope.refBookGrid.ctrl.getRawData(rowId))
                            }
                        }
                    };
                };

                /**
                 * Обработка события изменения даты актуальности для отбора записей справочника
                 */
                $scope.onChangeVersion = function () {
                    if (!$scope.data.recordVersion) {
                        // Запрет на установку пустой версии
                        $scope.data.recordVersion = $scope.data.prevVersion
                    } else {
                        $scope.data.prevVersion = $scope.data.recordVersion;
                        if ($scope.refBookGrid) {
                            $scope.refBookGrid.ctrl.refreshGrid(1);
                        }
                    }
                };

                /**
                 * Отображает диалог для создания новой записи справочника
                 */
                $scope.createRecord = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('refBooks.createRecord'),
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal800',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "CREATE",
                                    refBook: $scope.refBook,
                                    recordId: $stateParams.recordId,
                                    record: $scope.refBookGrid.value.length === 1 ? $scope.refBookGrid.value[0] : null
                                };
                            }
                        }
                    }).result.then(function (needToRefresh) {
                        if (needToRefresh) {
                            $scope.refBookGrid.ctrl.refreshGrid(1);
                        }
                    });
                };

                /**
                 * Отображает диалог для просмотра записи справочника
                 */
                $scope.showRecord = function (record) {
                    $aplanaModal.open({
                        title: $filter('translate')('refBooks.showRecord'),
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal800',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "VIEW",
                                    refBook: $scope.refBook,
                                    record: record ? record : $scope.refBookGrid.value[0],
                                    gridData: $scope.refBookGrid.ctrl.getRawData()
                                };
                            }
                        }
                    });
                };

                /**
                 * Отображает диалог для редактирования записи справочника
                 */
                $scope.editRecord = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('refBooks.editRecord'),
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal800',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "EDIT",
                                    refBook: $scope.refBook,
                                    record: $scope.refBookGrid.value[0]
                                };
                            }
                        }
                    }).result.then(function (needToRefresh) {
                        if (needToRefresh) {
                            $scope.refBookGrid.ctrl.refreshGrid(1);
                        }
                    });
                };

                /**
                 * Удаляет записи справочника, выбранные в таблице
                 * Если форма в режиме версий - удаляются выбранные версии, иначе удаляются все версии выбранных записей
                 */
                $scope.deleteRecords = function () {
                    $dialogs.confirmDialog({
                        content: $scope.versionMode ?
                            $filter('translate')('refBook.confirm.versions.delete') :
                            $filter('translate')('refBook.confirm.delete'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            var ids = [];
                            $scope.refBookGrid.value.forEach(function (record) {
                                ids.push(record.id.value)
                            });

                            $http({
                                method: "POST",
                                url: "controller/actions/refBook/" + $scope.refBook.id + ($scope.versionMode ? "/deleteVersions" : "/deleteRecords"),
                                data: ids
                            }).then(function (response) {
                                if (response.data && response.uuid && response.uuid !== null) {
                                    $logPanel.open('log-panel-container', response.uuid);
                                } else {
                                    $scope.refBookGrid.ctrl.refreshGrid(1);
                                }
                            });
                        }
                    });
                };

                /**
                 * Отображает список версий записи справочника
                 */
                $scope.showVersions = function () {
                    $window.location = "index.html#/refBooks/linearRefBook/" + $scope.refBook.id + "?recordId=" + $scope.refBookGrid.value[0].record_id.value;
                };

                /**
                 * Формирование XLSX выгрузки записей справочника
                 */
                $scope.createReportXlsx = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/refBook/" + $stateParams.refBookId + "/reportXlsx",
                        params: {
                            version: $scope.refBook.versioned ? $scope.data.recordVersion : null,
                            pagingParams: JSON.stringify({
                                property: $scope.refBookGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortname'),
                                direction: $scope.refBookGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortorder')
                            }),
                            searchPattern: $scope.search.text,
                            exactSearch: $scope.search.precise
                        }
                    }).success(function (response) {
                        $logPanel.open('log-panel-container', response.uuid);
                    });
                };

                /**
                 * Формирование CSV выгрузки записей справочника
                 */
                $scope.createReportCsv = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/refBook/" + $stateParams.refBookId + "/reportCsv",
                        params: {
                            version: $scope.refBook.versioned ? $scope.data.recordVersion : null,
                            pagingParams: JSON.stringify({
                                property: $scope.refBookGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortname'),
                                direction: $scope.refBookGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortorder')
                            }),
                            searchPattern: $scope.search.text,
                            exactSearch: $scope.search.precise
                        }
                    }).success(function (response) {
                        $logPanel.open('log-panel-container', response.uuid);
                    });
                };

                /**
                 * Поиск по справочнику
                 */
                $scope.searchRecords = function () {
                    $scope.refBookGrid.ctrl.refreshGrid(1);
                }
            }]);
}());