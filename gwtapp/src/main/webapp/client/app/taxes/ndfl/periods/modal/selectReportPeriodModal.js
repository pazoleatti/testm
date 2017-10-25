(function () {
    'use strict';

    /**
     * @description конфигурация модального окна выбора типа отчетного периода
     */

    angular.module('app.reportPeriodTypeModal', ['ui.router', 'app.rest', 'app.modals'])

        .controller('reportPeriodTypeCtrlModal', ['$scope', '$filter', 'RefBookValuesResource', '$uibModalInstance',
            function ($scope, $filter, RefBookValuesResource, $uibModalInstance) {

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {
                        name: "",
                        equal: false
                    },
                    isClear: false,
                    filterName: 'filter'
                };

                $scope.searchFilter.ajaxFilter.push({
                    property: "name",
                    value: ""
                });
                $scope.searchFilter.ajaxFilter.push({
                    property: "equal",
                    value: false
                });


                $scope.reportPeriodTypeGrid = {
                    ctrl: {},
                    value: [],
                    gridName: 'reportPeriodTypeGrid',
                    options: {
                        datatype: "angularResource",
                        angularResource: RefBookValuesResource,
                        requestParameters: function () {
                            return {
                                refBookId: 'reportPeriodType',
                                name: $scope.searchFilter.params.name,
                                equal: $scope.searchFilter.params.equal
                            };
                        },
                        height: 200,
                        colNames: [
                            $filter('translate')('reportPeriod.modal.grid.code'),
                            $filter('translate')('reportPeriod.modal.grid.name'),
                            $filter('translate')('reportPeriod.modal.grid.startDate'),
                            $filter('translate')('reportPeriod.modal.grid.endDate'),
                            $filter('translate')('reportPeriod.modal.grid.calendarDate')
                        ],
                        colModel: [
                            {name: 'code', index: 'code', width: 50},
                            {name: 'name', index: 'name', width: 140},
                            {
                                name: 'startDate',
                                index: 'startDate',
                                width: 250,
                                sortable: false,
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'endDate',
                                index: 'endDate',
                                width: 250,
                                sortable: false,
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'calendarStartDate',
                                index: 'calendarStartDate',
                                width: 250,
                                sortable: false,
                                formatter: $filter('dateFormatter')
                            }
                        ],
                        rowNum: 10,
                        rowList: [10, 20, 30],
                        viewrecords: true,
                        sortname: 'id',
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: false
                    }
                };


                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.reportPeriodTypeGrid.ctrl.refreshGrid(page);
                };


                /**
                 * @description Поиск по фильтру
                 */
                $scope.submitSearch = function () {
                    $scope.searchFilter.ajaxFilter = [];
                    $scope.searchFilter.fillFilterParams();
                    $scope.refreshGrid(1);
                    $scope.searchFilter.isClear = !_.isEmpty($scope.searchFilter.params.name) || !_.isEmpty($scope.searchFilter.params.equal);
                };

                /**
                 * @description сброс фильтра
                 */
                $scope.resetFilterModal = function () {
                    /* очистка всех инпутов на форме */
                    $scope.searchFilter.params.name = "";
                    $scope.searchFilter.params.equal = false;

                    /* убираем надпись "Сброс" */
                    $scope.isClear = false;

                    $scope.submitSearch();
                };

                /**
                 * @description Заполнение ajaxFilter
                 */
                $scope.searchFilter.fillFilterParams = function () {
                    if ($scope.searchFilter.params.name) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "name",
                            value: $scope.searchFilter.params.name
                        });
                    }
                    if ($scope.searchFilter.params.equal) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "equal",
                            value: $scope.searchFilter.params.equal
                        });
                    }
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    $uibModalInstance.dismiss('Canceled');
                };

                /**
                 * @description Обработчик кнопки "Добавить"
                 */
                $scope.confirmPeriod = function () {
                    $uibModalInstance.close($scope.reportPeriodTypeGrid.value);
                };

            }]);


}());