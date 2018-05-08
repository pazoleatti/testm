(function () {
    'use strict';

    /**
     * @description Модуль для отображения линейного справочника
     */
    angular.module('app.linearRefBook', ['app.refBookRecordModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('linearRefBook', {
                url: '/refBooks/linearRefBook/{refBookId}?recordId',
                templateUrl: 'client/app/refbooks/linearRefBook.html?v=${buildUuid}',
                controller: 'linearRefBookCtrl'
            });
        }])
        .controller('linearRefBookCtrl', ['$scope', "$stateParams", "$injector", "$compile", "APP_CONSTANTS",
            "RefBookResource", "RefBookRecordResource", "$aplanaModal", '$filter', '$window', "$http",
            function ($scope, $stateParams, $injector, $compile, APP_CONSTANTS, RefBookResource, RefBookRecordResource, $aplanaModal, $filter, $window, $http) {
                $scope.columnNames = [];
                $scope.columnModel = [];
                $scope.data = {};
                $scope.data.recordVersion = new Date();
                $scope.versionMode = $stateParams.recordId && $stateParams.recordId !== 'undefined';

                // Получаем данные справочника
                RefBookResource.query({
                    id: $stateParams.refBookId
                }, function (data) {
                    $scope.refBook = data;
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
                            name: 'id.numberValue',
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
                                    width: attribute.width * 20, //TODO: пока так, потому что в БД ширина задана в em, а оно не поддерживается в jqgrid
                                    type: attribute.attributeType,
                                    referenceAttribute: attribute.refBookAttribute,
                                    formatter: refBookValueFormatter
                                }
                            )
                        }
                    });
                };

                function refBookValueFormatter(cellValue, options, row) {
                    var colModel = options.index ? options : options.colModel;
                    var record = row[colModel.index];
                    var value = "";
                    switch (colModel.type) {
                        case 'STRING':
                            value = record && record.stringValue && typeof record.stringValue !== 'undefined' ? record.stringValue : "";
                            break;
                        case 'NUMBER':
                            value = record && record.numberValue && typeof record.numberValue !== 'undefined' ? record.numberValue : "";
                            break;
                        case 'DATE':
                            //TODO: проверить отображение дат на стенде, локально идут со смещением в 1 день
                            value = record && record.dateValue && typeof record.dateValue !== 'undefined' ? $filter('dateFormatter')(record.dateValue) : "";
                            break;
                        case 'REFERENCE':
                            value = record && record.referenceObject && typeof record.referenceObject !== 'undefined' ? record.referenceObject[colModel.referenceAttribute.alias].value : "";
                            break;
                    }
                    return value;
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
                                    version: $scope.data.recordVersion
                                };
                            },
                            colNames: $scope.columnNames,
                            colModel: $scope.columnModel,
                            rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                            rowList: APP_CONSTANTS.COMMON.PAGINATION,
                            viewrecords: true,
                            hidegrid: false,
                            multiselect: true,
                            // TODO: задать сортировку по умолчанию
                            ondblClickRow: function (rowId) {
                                $scope.showRecord($scope.refBookGrid.ctrl.getRawData(rowId))
                            },
                            onSelectRow: function (rowId, status) {
                                if (($scope.refBookGrid.value.length === 0 && status) || $scope.refBookGrid.value.length === 2 && !status) {
                                    $http({
                                        method: "GET",
                                        url: "controller/actions/refBook/" + $stateParams.refBookId + "/recordVersionCount/" + rowId
                                    }).then(function (response) {
                                        $scope.versionsCount = response.data;
                                    });
                                } else {
                                    $scope.versionsCount = null;
                                }
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
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html?v=${buildUuid}',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal1200',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "CREATE",
                                    refBook: $scope.refBook,
                                    recordId: $stateParams.recordId
                                };
                            }
                        }
                    }).result.then(function () {
                        // TODO: лог панель отображается на заднем плане и ее нельзя пролистать
                        $scope.refBookGrid.ctrl.refreshGrid(1);
                    });
                };

                /**
                 * Отображает диалог для просмотра записи справочника
                 */
                $scope.showRecord = function (record) {
                    $aplanaModal.open({
                        title: $filter('translate')('refBooks.showRecord'),
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html?v=${buildUuid}',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal1200',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "VIEW",
                                    refBook: $scope.refBook,
                                    record: record ? record : $scope.refBookGrid.value[0]
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
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html?v=${buildUuid}',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal1200',
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
                        // TODO: лог панель отображается на заднем плане и ее нельзя пролистать
                        if (needToRefresh) {
                            $scope.refBookGrid.ctrl.refreshGrid(1);
                        }
                    });
                };

                /**
                 * Удаляет записи справочника, выбранные в таблице
                 */
                $scope.deleteRecords = function () {
                    var ids = [];
                    $scope.refBookGrid.value.forEach(function (record) {
                        ids.push(record.id.value)
                    });

                    $http({
                        method: "POST",
                        url: "controller/actions/refBook/" + $scope.refBook.id + "/deleteRecords",
                        data: ids
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        } else {
                            $scope.refBookGrid.ctrl.refreshGrid(1);
                        }
                    });
                };

                /**
                 * Отображает список версий записи справочника
                 */
                $scope.showVersions = function () {
                    $window.location = "index.html#/refBooks/linearRefBook/" + $scope.refBook.id + "?recordId=" + $scope.refBookGrid.value[0].record_id.numberValue;
                }
            }]);
}());