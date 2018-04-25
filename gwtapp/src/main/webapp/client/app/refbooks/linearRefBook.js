(function () {
    'use strict';

    /**
     * @description Модуль для отображения линейного справочника
     */
    angular.module('app.linearRefBook', ['app.refBookRecordModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('linearRefBook', {
                url: '/refBooks/linearRefBook/{refBookId}?uuid',
                templateUrl: 'client/app/refbooks/linearRefBook.html?v=${buildUuid}',
                controller: 'linearRefBookCtrl'
            });
        }])
        .controller('linearRefBookCtrl', ['$scope', "$stateParams", "$injector", "$compile", "APP_CONSTANTS",
            "RefBookResource", "RefBookRecordResource", "$aplanaModal", '$filter',
            function ($scope, $stateParams, $injector, $compile, APP_CONSTANTS, RefBookResource, RefBookRecordResource, $aplanaModal, $filter) {
                $scope.columnNames = [];
                $scope.columnModel = [];
                $scope.data = {};
                $scope.data.recordVersion = new Date();

                // Получаем данные справочника
                RefBookResource.query({
                    id: $stateParams.refBookId
                }, function (data) {
                    $scope.refBook = data;
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
                                    formatter: refBookValueFormatter
                                }
                            )
                        }
                    });

                    // Если справочник версионируемый - добавляем информацию о периоде действия записей
                    if ($scope.refBook.versioned) {
                        $scope.columnNames.push($filter('translate')('refBook.versionFrom'));
                        $scope.columnNames.push($filter('translate')('refBook.versionTo'));
                        $scope.columnModel.push(
                            {
                                name: APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_FROM_ALIAS,
                                index: APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_FROM_ALIAS,
                                width: 150,
                                type: "DATE",
                                formatter: refBookValueFormatter
                            }
                        );
                        $scope.columnModel.push(
                            {
                                name: APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_TO_ALIAS,
                                index: APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_TO_ALIAS,
                                width: 150,
                                type: "DATE",
                                formatter: refBookValueFormatter
                            }
                        );
                    }
                };

                function refBookValueFormatter(cellValue, options, row) {
                    var colModel = options.index ? options : options.colModel;
                    var record = row[colModel.index];
                    var value = "";
                    switch (colModel.type) {
                        case 'STRING':
                            value = record && record.stringValue ? record.stringValue : "";
                            break;
                        case 'NUMBER':
                            value = record && record.numberValue ? record.numberValue : "";
                            break;
                        case 'DATE':
                            //TODO: проверить отображение дат на стенде, локально идут со смещением в 1 день
                            value = record && record.dateValue ? $filter('dateFormatter')(record.dateValue) : "";
                            break;
                        case 'REFERENCE':
                            value = record && record.referenceValue ? record.referenceValue : "";
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
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html?v=${buildUuid}',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal1200',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "CREATE",
                                    refBook: $scope.refBook
                                };
                            }
                        }
                    }).result.then(function () {
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
                    }).result.then(function () {
                        $scope.refBookGrid.ctrl.refreshGrid(1);
                    });
                };

                /**
                 * Удаляет записи справочника, выбранные в таблице
                 */
                $scope.deleteRecords = function () {
                }
            }]);
}());