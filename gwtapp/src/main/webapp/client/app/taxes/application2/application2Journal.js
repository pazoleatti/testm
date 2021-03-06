(function () {
    'use strict';

    /**
     * @description Модуль для работы с журналом "Приложение 2"
     */
    angular.module('app.application2Journal', ['ui.router', 'app.updateDocStateModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('application2Journal', {
                url: '/taxes/application2Journal',
                templateUrl: 'client/app/taxes/application2/application2Journal.html',
                controller: 'application2JournalCtrl',
                params: {uuid: null}
            });
        }])

        .controller('application2JournalCtrl', ['$scope', '$stateParams', '$filter', '$http', 'DeclarationDataResource',
            '$logPanel', '$aplanaModal', 'APP_CONSTANTS', 'PermissionChecker', 'NdflReportService',
            function ($scope, $stateParams, $filter, $http, DeclarationDataResource, $logPanel, $aplanaModal,
                        APP_CONSTANTS, PermissionChecker, NdflReportService) {

                var defaultCorrectionTag = APP_CONSTANTS.CORRECTION_TAG.ALL;

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

                function getDefaultFilterParams() {
                    return {};
                }

                $scope.searchFilter = {
                    params: getDefaultFilterParams(),
                    ajaxFilter: [],
                    filterName: 'app2Filter',
                    onCreateComplete: function () {
                        $scope.refreshGrid();
                    },
                    resetFilterParams: function () {
                        $scope.searchFilter.params = getDefaultFilterParams();
                    },
                    isClearByFilterParams: function () {
                        $scope.searchFilter.isClear = stringify($scope.searchFilter.params) !== stringify(getDefaultFilterParams());
                    }
                };

                // поля со значениями null, undefined или "" будут считаться эквивалентными
                function stringify(value) {
                    return JSON.stringify(value, function (key, value) {
                        return value ? value : undefined;
                    });
                }

                /**
                * @description Обновление грида
                * @param page
                */
                $scope.refreshGrid = function (page) {
                    $scope.application2JournalGrid.ctrl.refreshGrid(page);
                };

                $scope.application2JournalGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        gridName: 'application2Journal',
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
                            $filter('translate')('title.numberApplication2'),
                            $filter('translate')('title.period'),
                            $filter('translate')('title.formKind'),
                            $filter('translate')('title.state'),
                            $filter('translate')('title.docState'),
                            $filter('translate')('title.correctionNum'),
                            $filter('translate')('title.dateAndTimeCreate'),
                            $filter('translate')('title.creator'),
                            $filter('translate')('title.note')],
                        colModel: [
                            {name: 'declarationDataId', index: 'declarationDataId', width: 120, key: true},
                            {name: 'reportPeriod', index: 'reportPeriod', width: 175},
                            {
                                name: 'declarationType',
                                index: 'declarationType',
                                width: 170,
                                formatter: $filter('app2linkReportFormatter')
                            },
                            {name: 'state', index: 'state', width: 100},
                            {name: 'docState', index: 'docState', width: 130},
                            {name: 'correctionNum', index: 'correction_num', width: 110},
                            {
                                name: 'creationDate',
                                index: 'creationDate',
                                width: 215,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {name: 'creationUserName', index: 'creationUserName', width: 130},
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
                        fullScreen: true
                    }
                };

                function getFilter() {
                    return {
                        reportPeriodIds: $filter('idExtractor')($scope.searchFilter.params.periods),
                        correctionTag: $filter('correctionTagFormatter')($scope.searchFilter.params.correctionTag),
                        declarationDataId: $scope.searchFilter.params.application2Number,
                        correctionNum: $scope.searchFilter.params.correctionNum,
                        formStates: $filter('idExtractor')($scope.searchFilter.params.states),
                        note: $scope.searchFilter.params.note,
                        creationUserName: $scope.searchFilter.params.creationUserName,
                        creationDateFrom: $filter('dateTimeSerializer')($scope.searchFilter.params.creationDateFrom),
                        creationDateTo: $filter('dateTimeSerializer')($scope.searchFilter.params.creationDateTo),
                        formKindIds: [APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id],
                        declarationTypeIds: [APP_CONSTANTS.DECLARATION_TYPE.APP_2.id]
                    };
                }

                /**
                 * Показ МО "Создание Приложения 2"
                 */
                $scope.createApp2 = function () {
                    NdflReportService.createReport(true /* Создание Приложение 2 */);
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.check = function () {
                    var selectedItems = $scope.application2JournalGrid.value;

                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function () {
                    var selectedItems = $scope.application2JournalGrid.value;

                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnToCreated = function () {

                }

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.delete = function () {
                    var selectedItems = $scope.application2JournalGrid.value;
                    $dialogs.confirmDialog({
                        content: $filter('translate')('title.deleteDeclarations'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {

                        }
                    });
                };

                /**
                 * @description Выгрузить отчетность по фильтру по выбранным формам
                 */
                $scope.downloadReportsBySelected = function () {
                    var selectedRows = $scope.application2JournalGrid.value;

                };

                /**
                 * @description Выгрузить отчетность по фильтру
                 */
                $scope.downloadReportsByFilter = function () {

                };


            }
        ])
        /**
         * @description Форматтер для поля 'Вид налоговой формы' для перехода на конкретное "Приложение 2"
         * @param cellValue Значение ячейки
         * @param options Данные таблицы
         */
        .filter('app2linkReportFormatter', function () {
            return function (cellValue, options) {
                if (!cellValue) {
                    cellValue = '';
                }
                return "<a href='index.html#/taxes/app2/app2Report/" + options.rowId + "'>" + cellValue + "</a>";
            };
        });
}());