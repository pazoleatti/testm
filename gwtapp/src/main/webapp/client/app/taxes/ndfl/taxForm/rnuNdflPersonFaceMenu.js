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


                 if (!$shareData.filterIsClear) {
                    // $scope.declarationData.dataType = APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.ALL_DATA;
                } else {
                    // $scope.declarationData.dataType = APP_CONSTANTS.NDFL_PERSON_REPORT_GENERATION_TYPE.BY_FILTER_SELECTED;
                }




                /**
                 * @description Создание рну ндфл для физ лица
                 */
                $scope.formationRNU = function () {

                    console.log($scope.reportData.dataType.id + ' = ' + $scope.reportData.dataType.name);

                    var typeReportId = $scope.reportData.dataType.id;

                    switch (typeReportId) {
                        case 1:
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/specific/" +
                                    APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_PERSON_ALL_DB + "/" + typeReportId
                            }).success(function (response) {
                                if (response) {
                                    $logPanel.open('log-panel-container', response);
                                }
                            });
                            break;
                        case 2:
                            console.log($shareData.filterIsClear);
                            if (!$shareData.filterIsClear) {
                            }
                            break;
                        case 3:
                            console.log($shareData.selectedRow.getActiveTab().getSelectedRows().length);
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

            }]);

}());

