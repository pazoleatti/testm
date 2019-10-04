(function () {
    'use strict';

    /**
     * @description Модуль для для работы с приемниками и источниками формы
     */

    angular.module('app.formSources', ['ui.router', 'app.rest', 'app.ndfl'])

    /**
     * @description Контроллер модального окна, в котором выводятся источники и приемники формы
     */
        .controller('sourcesFormCtrl', ["$scope", "$http", '$logPanel', '$state', '$stateParams', "$modalInstance", "$filter", 'DeclarationDataResource', 'APP_CONSTANTS',
            function ($scope, $http, $logPanel, $state, $stateParams, $modalInstance, $filter, DeclarationDataResource, APP_CONSTANTS) {
                $scope.sourcesGridData = [];

                //Чекбоксы
                $scope.gridFilter = {
                    showSources: true,
                    showDestinations: true
                };

                //Получение списка приемников и источников
                DeclarationDataResource.querySource({
                    declarationDataId: $stateParams.declarationDataId,
                    projection: "sources"
                }, function (response) {
                    $scope.sourcesGridData = response;
                    $scope.updateGridData();
                });

                //Таблица
                $scope.sourcesGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "local",
                        data: [],
                        height: 237,
                        colNames: [
                            $filter('translate')('sources.tableColumn.index'),
                            $filter('translate')('sources.tableColumn.tax'),
                            $filter('translate')('sources.tableColumn.srcOrDest'),
                            $filter('translate')('sources.tableColumn.declarationId'),
                            $filter('translate')('sources.tableColumn.department'),
                            $filter('translate')('sources.tableColumn.correctionDate'),
                            $filter('translate')('sources.tableColumn.declarationKind'),
                            $filter('translate')('sources.tableColumn.declarationType'),
                            $filter('translate')('sources.tableColumn.year'),
                            $filter('translate')('sources.tableColumn.period'),
                            $filter('translate')('sources.tableColumn.formType'),
                            $filter('translate')('sources.tableColumn.declarationState')],
                        colModel: [
                            {
                                name: 'index',
                                index: 'index',
                                width: 30,
                                key: true,
                                sortable: false,
                                formatter: $filter('indexFormatter')
                            },
                            {
                                name: 'taxType',
                                index: 'taxType',
                                width: 50,
                                sortable: false,
                                formatter: $filter('taxFormatter')
                            },
                            {
                                name: 'srcOrDest',
                                index: 'srcOrDest',
                                width: 90,
                                sortable: false,
                                formatter: $filter('sourceFormatter')
                            },
                            {
                                name: 'declarationDataId',
                                index: 'declarationDataId',
                                width: 80,
                                sortable: false,
                                formatter: $filter('declarationDataIdFormatter')
                            },
                            {name: 'fullDepartmentName', index: 'fullDepartmentName', width: 113, sortable: false, general: true},
                            {
                                name: 'correctionDate',
                                index: 'correctionDate',
                                width: 106,
                                sortable: false,
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'declarationTemplate.declarationFormKind',
                                index: 'declarationTemplate.declarationFormKind',
                                width: 131,
                                sortable: false,
                                formatter: $filter('declarationFormKindFormatter')
                            },
                            {name: 'declarationTypeName', index: 'declarationTypeName', width: 203, sortable: false},
                            {name: 'year', index: 'year', width: 40, sortable: false},
                            {name: 'periodName', index: 'periodName', width: 110, sortable: false},
                            {name: 'formType', index: 'formType', width: 110, sortable: false},
                            {
                                name: 'declarationState',
                                index: 'declarationState',
                                width: 110,
                                sortable: false,
                                formatter: $filter('declarationStateFormatter')
                            }

                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        sortname: 'index',
                        sortorder: "asc"
                    }
                };

                /**
                 * Обновление данных в таблице с учетом чекбоксов, вызывается при нажатии на чекбоксы
                 */
                $scope.updateGridData = function () {
                    var sources = $scope.gridFilter.showSources;
                    var destinations = $scope.gridFilter.showDestinations;
                    var data = [];
                    angular.forEach($scope.sourcesGridData, function (source) {
                        if (sources && source.source || destinations && !source.source) {
                            data.push(source);
                        }
                    });
                    $scope.sourcesGrid.ctrl.refreshGridData(data);
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss();
                };

                /**
                 * Выгрузить в Excel
                 */
                $scope.unloadList = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/unloadListInXlsx",
                        params: {
                            sources: $scope.gridFilter.showSources,
                            destinations: $scope.gridFilter.showDestinations
                        }
                    }).then(function (response) {
                        if (response.data) {
                            $logPanel.open('log-panel-container', response.data);
                        }
                    });
                    $modalInstance.dismiss();
                };

                $scope.getRawDatalength = function () {
                    return $scope.$$childHead.$rawData.length;
                };

            }])

        .filter('indexFormatter', function () {
            return function (value, row) {
                return row.rowId;
            };
        })

        .filter('taxFormatter', function () {
            return function (value) {
                return value === "NDFL" ? "НДФЛ" : "";
            };
        })

        .filter('sourceFormatter', function () {
            return function (value, row, sourceObject) {
                return sourceObject.source ? "Источник" : "Приемник";
            };
        })

        .filter('declarationFormKindFormatter', function () {
            return function (value) {
                return value.name;
            };
        })

        .filter('declarationStateFormatter', function () {
            return function (value) {
                var result = "";
                switch (value) {
                    case 'CREATED':
                        result = 'Создана';
                        break;
                    case 'PREPARED':
                        result = 'Подготовлена';
                        break;
                    case 'ACCEPTED':
                        result = 'Принята';
                        break;
                    case 'NOT_EXIST':
                        result = 'Не создана';
                        break;
                }
                return result;
            };
        })

        .filter('declarationDataIdFormatter', ['$state', 'APP_CONSTANTS', function ($state, APP_CONSTANTS) {
            return function (declarationDataId, row, declarationData) {
                var url;

                if(declarationData.declarationTemplate.declarationFormKind.id === APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id) {
                    url = $state.href('ndflReport', {declarationDataId: declarationDataId});
                } else {
                    url = $state.href('ndfl', {declarationDataId: declarationDataId});
                }

                return '<a href="' + url + '">' + declarationDataId + '</a>';
            };
        }]);
}());