(function () {
    'use strict';

    /**
     * @description Модуль для работы с модальным окном 'Формирование рну ндфл для всех ФЛ на основе Меню выбора'
     */
    angular.module('app.createExcelTemplateModal', ['ui.router', 'app.rest', 'app.formatters', 'ui.select2'])
    /**
     * @description Контроллер формы создания/ Меню выбора
     */
        .controller('createExcelTemplateModalCtrl', ["$scope", "$http", '$state', '$stateParams', "$modalInstance",
            "RefBookValuesResource", "APP_CONSTANTS",
            '$shareData', '$dialogs', '$webStorage', '$logPanel',
            function ($scope, $http, $state, $stateParams, $modalInstance, RefBookValuesResource,
                    APP_CONSTANTS, $shareData, $dialogs, $webStorage, $logPanel) {

                $scope.excelTemplateType = {type: APP_CONSTANTS.EXCEL_TEMPLATE_GENERATION_TYPE.ALL_DATA };

                $scope.selectedRows = $shareData.selectedRow.getActiveTab().getSelectedRows();
                $scope.activeTab = $shareData.selectedRow.getActiveTab();

                /**
                 * @description Создание Шаблона ТФ (Excel)
                 */
                $scope.create = function () {

                    var templateTypeId = $scope.excelTemplateType.type.id;
                    switch (templateTypeId) {
                        case APP_CONSTANTS.EXCEL_TEMPLATE_GENERATION_TYPE.ALL_DATA.id:
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/excelTemplate"
                            }).success(function (response) {
                                if (response) {
                                    $logPanel.open('log-panel-container', response);
                                }
                            });
                            break;
                        case APP_CONSTANTS.EXCEL_TEMPLATE_GENERATION_TYPE.SELECTED_ON_PAGE.id:
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/selectedExcelTemplate",
                                data: getSelectedRowsData($shareData.selectedRow.getActiveTab())
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

