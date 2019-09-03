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

                $scope.reportData = {dataType: APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.ALL_DATA};
                //todo
                 if (!$shareData.filterIsClear) {
                    // $scope.declarationData.dataType = APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.ALL_DATA;
                } else {
                    // $scope.declarationData.dataType = APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.BY_FILTER_SELECTED;
                }


                /**
                 * @description Создание РНУ НДФЛ для ФЛ
                 */
                $scope.formationRNU = function () {

                    console.log($scope.reportData.dataType.id + ' = ' + $scope.reportData.dataType.name);

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
                                // todo $shareData.selectedRow.getActiveTab().getSelectedRows() ...
                                data: {}
                            }).success(function (response) {
                                if (response) {
                                    $logPanel.open('log-panel-container', response);
                                }
                            });
                            break;
                    }

                    $modalInstance.dismiss();
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

