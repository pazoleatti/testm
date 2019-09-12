(function () {
    'use strict';

    /**
     * @description Модуль для работы с модальным окном 'Формирование рну ндфл для всех ФЛ на основе Меню выбора'
     */
    angular.module('app.rnuNdflPersonFaceMenu', ['ui.router', 'app.rest', 'app.formatters', 'ui.select2'])
    /**
     * @description Контроллер формы создания/ Меню выбора
     */
        .controller('rnuNdflPersonFaceMenuFormCtrl', ["$scope", "$http", '$state', '$stateParams', "$modalInstance", "$filter",
            "RefBookValuesResource", "APP_CONSTANTS",
            '$shareData', '$dialogs', '$webStorage', '$logPanel',
            function ($scope, $http, $state, $stateParams, $modalInstance, $filter,
                      RefBookValuesResource, APP_CONSTANTS, $shareData, $dialogs, $webStorage, $logPanel) {

                $scope.reportData = {dataType: APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.ALL_DATA, filterIsClear: $shareData.filter.filterIsClear};

                $scope.filterIsClear = $shareData.filterIsClear;
                $scope.selectedRows = $shareData.selectedRow.getActiveTab().getSelectedRows();

                /**
                 * @description Создание РНУ НДФЛ для ФЛ
                 */
                $scope.formationRNU = function () {

                    var typeReportId = $scope.reportData.dataType.id;
                    switch (typeReportId) {
                        case APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.ALL_DATA.id:
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/report/rnuNdflAllPersons"
                            }).success(function (response) {
                                if (response) {
                                    $logPanel.open('log-panel-container', response);
                                }
                            });
                            break;
                        case APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.BY_FILTER_SELECTED.id:
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/report/rnuNdflAllPersons/byFilter",
                                data: JSON.stringify($shareData.filter)
                            }).success(function (response) {
                                if (response) {
                                    $logPanel.open('log-panel-container', response);
                                }
                            });
                            break;
                        case APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.SELECTED_ON_PAGE.id:
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/report/rnuNdflAllPersons/bySelected",
                                data: getSelectedRowsData($shareData.selectedRow.getActiveTab())
                            }).success(function (response) {
                                if (response) {
                                    $logPanel.open('log-panel-container', response);
                                }
                            });
                            break;
                    }

                    $modalInstance.dismiss();

                    function getSelectedRowsData(activeTab) {
                        var result = {"persons": [], "incomes": [], "deductions": [], "prepayments": []};
                        switch (activeTab.getSection()) {
                            case APP_CONSTANTS.NDFL_PERSON_REPORT_ACTIVE_TAB.PERSONS.id:
                                result.persons = activeTab.getSelectedRows();
                                break;
                            case APP_CONSTANTS.NDFL_PERSON_REPORT_ACTIVE_TAB.INCOMES.id:
                                result.incomes = activeTab.getSelectedRows();
                                break;
                            case APP_CONSTANTS.NDFL_PERSON_REPORT_ACTIVE_TAB.DEDUCTIONS.id:
                                result.deductions = activeTab.getSelectedRows();
                                break;
                            case APP_CONSTANTS.NDFL_PERSON_REPORT_ACTIVE_TAB.PREPAYMENTS.id:
                                result.prepayments = activeTab.getSelectedRows();
                                break;
                        }
                        return result;
                    }
                };

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss();
                };
            }
        ]);
}());

