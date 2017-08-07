(function () {
    'use strict';

    /**
     * @description Модуль для для работы с приемниками и источниками формы
     */

    angular.module('app.formSources', ['ui.router', 'app.rest', 'app.ndfl'])

    /**
     * @description Контроллер модального окна, в котором выводятся источники и приемники формы
     */
        .controller('sourcesFormCtrl', ["$scope", "$http", '$state', '$stateParams', "$uibModalInstance", "$filter", 'SourcesResource',
            function ($scope, $http, $state, $stateParams, $uibModalInstance, $filter, SourcesResource) {
                $scope.sourcesGridData = [];

                //Чекбоксы
                $scope.gridFilter = {
                    showSources: true,
                    showDestinations: true,
                    showUncreated: false
                };

                //Получение списка приемников и источников
                SourcesResource.query({
                    declarationId: $stateParams.declarationId
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
                        height: "auto",
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
                                width: 150,
                                sortable: false,
                                formatter: $filter('sourceFormatter')
                            },
                            {
                                name: 'declarationDataId',
                                index: 'declarationDataId',
                                width: 100,
                                sortable: false,
                                formatter: $filter('declarationIdFormatter')
                            },
                            {name: 'fullDepartmentName', index: 'fullDepartmentName', width: 150, sortable: false},
                            {
                                name: 'correctionDate',
                                index: 'correctionDate',
                                width: 180,
                                sortable: false,
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'declarationTemplate.declarationFormKind',
                                index: 'declarationTemplate.declarationFormKind',
                                width: 100,
                                sortable: false,
                                formatter: $filter('declarationFormKindFormatter')
                            },
                            {name: 'declarationTypeName', index: 'declarationTypeName', width: 150, sortable: false},
                            {name: 'year', index: 'year', width: 40, sortable: false},
                            {name: 'periodName', index: 'periodName', width: 70, sortable: false},
                            {
                                name: 'declarationState',
                                index: 'declarationState',
                                width: 130,
                                sortable: false,
                                formatter: $filter('declarationStateFormatter')
                            }

                        ],
                        rowNum: 10,
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
                    var uncreated = $scope.gridFilter.showUncreated;
                    var data = [];
                    angular.forEach($scope.sourcesGridData, function (source) {
                        if ((sources && source.source || destinations && !source.source) && (uncreated || source.created)) {
                            data.push(source);
                        }
                    });
                    $scope.sourcesGrid.ctrl.refreshGridData(data);
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $uibModalInstance.dismiss('Canceled');
                };

                //Переход по ссылке на другую форму
                $(document).undelegate('#sourcesTable .sources-link', 'click');
                $(document).delegate('#sourcesTable .sources-link', 'click', function () {
                    var declarationId = $(this).attr('declaration-id');
                    $scope.close();
                    $state.go('ndfl', {declarationId: declarationId});
                });
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
                var result = "";
                switch (value) {
                    case 'ADDITIONAL':
                        result = 'Выходная';
                        break;
                    case 'CONSOLIDATED':
                        result = 'Консолидированная';
                        break;
                    case 'PRIMARY':
                        result = 'Первичная';
                        break;
                    case 'SUMMARY':
                        result = 'Сводная';
                        break;
                    case 'UNP':
                        result = 'Форма УНП';
                        break;
                    case 'CALCULATED':
                        result = 'Расчетная';
                        break;
                    case 'REPORTS':
                        result = 'Отчетная';
                        break;
                }
                return result;
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

        .filter('declarationIdFormatter', function () {
            return function (value) {
                return '<a class="sources-link" declaration-id="' + value + '">' + value + '</a>';
            };
        });
}());