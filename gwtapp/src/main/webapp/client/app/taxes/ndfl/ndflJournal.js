(function () {
    'use strict';

    /**
     * @description Модуль для работы со формами ндфл
     */
    angular.module('app.ndflJournal', ['ui.router', 'app.constants'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflJournal', {
                url: '/taxes/ndflJournal',
                templateUrl: 'client/app/taxes/ndfl/ndflJournal.html',
                controller: 'ndflJournalCtrl'
            });
        }])

        /**
         * @description Контроллер списка форм
         */
        .controller('ndflJournalCtrl', [
            '$scope', '$state', '$filter', '$rootScope', 'DeclarationDataResource', 'APP_CONSTANTS',
            function ($scope, $state, $filter, $rootScope, DeclarationDataResource, APP_CONSTANTS) {
                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');
                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.ctrlMyGrid.refreshGrid(page);
                };

                // TODO: https://jira.aplana.com/browse/SBRFNDFL-1756 получать данные справоника АСНУ с сервера.
                var asnu = {
                    1: 'АС "SAP"',
                    2: 'АИС "Дивиденд"',
                    3: 'АС "Diasoft Custody 5NT"',
                    4: 'АС "Инфобанк"',
                    5: 'АИС "Депозитарий"',
                    6: 'Материальная выгода. Кредиты_АС "ЕКП"',
                    7: 'Экономическая выгода. Кредиты_АС "ЕКП"',
                    8: 'Экономическая выгода. Карты_ АС "ИПС БК"',
                    9: 'Экономическая выгода. Комиссии_АС "ЕКП"',
                    10: 'Реструктуризация валютных кредитов_АС "ЕКП"',
                    11: 'Прощение долга (амнистия). Кредиты_АС "ЕКП"',
                    12: 'Выплаты клиентам по решениям суда_АС "ЕКП"',
                    13: 'Призы, подарки клиентам_АС "SAP"',
                    14: 'АС "Back Office"',
                    15: 'АС "ЕКС"',
                    '-1': ''
                };

                $scope.ndflJournalGridOptions =
                    {
                        datatype: "angularResource",
                        angularResource: DeclarationDataResource,
                        requestParameters: function () {
                            return {
                                projection: 'declarations'
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
                            {
                                name: 'declarationFormKind',
                                index: 'declarationFormKind',
                                width: 175,
                                formatter: 'select',
                                editoptions: {value: APP_CONSTANTS.NDFL_FORMKIND}

                            },
                            {
                                name: 'declarationType',
                                index: 'declarationType',
                                width: 175,
                                sortable: false,
                                formatter: linkformatter
                            },
                            {name: 'departmentName', index: 'departmentName', width: 150, sortable: false},
                            {
                                name: 'asnuId', index: 'asnuId', width: 176, sortable: false,
                                formatter: 'select',
                                editoptions: {value: asnu}
                            },
                            {name: 'reportPeriodName', index: 'reportPeriodName', width: 110, sortable: false},
                            {
                                name: 'state', index: 'state', width: 100, sortable: false,
                                formatter: 'select',
                                editoptions: {value: APP_CONSTANTS.NDFL_STATS}
                            },
                            {
                                name: 'fileName', index: 'fileName', width: 400, sortable: false,
                                formatter: linkFileFormatter
                            },
                            {
                                name: 'declarationDataCreationDate',
                                index: 'declarationDataCreationDate',
                                width: 230, sortable: false,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {
                                name: 'declarationDataCreationUserName',
                                index: 'declarationDataCreationUserName',
                                width: 175, sortable: false
                            }
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
                    return "<a target='_blank' href='controller/rest/declarationData/" + options.rowId + "/xml'>" + cellValue + "</a>";
                }

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
            }]);
}());