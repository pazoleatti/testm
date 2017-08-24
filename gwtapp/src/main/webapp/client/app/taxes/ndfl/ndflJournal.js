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
            '$scope', '$state', '$filter', '$rootScope', 'DeclarationDataResource',
            function ($scope, $state, $filter, $rootScope, DeclarationDataResource) {
                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');
                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.ctrlMyGrid.refreshGrid(page);
                };

                $scope.ndflJournalGridOptions =
                    {
                        datatype: "angularResource",
                        angularResource: DeclarationDataResource,
                        requestParameters: function () {
                            return {
                                projection: 'declarations',
                                isReport: false
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
                            {name: 'creationDate', index: 'creationDate', width: 230, formatter: $filter('dateTimeFormatter')},
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
                    if(!cellValue) {
                        cellValue = '';
                    }
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