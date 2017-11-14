(function () {
    'use strict';

    /**
     * @description Модуль для создания налоговых форм
     */

    angular.module('app.createDeclaration', ['ui.router', 'app.rest', 'app.formatters'])

    /**
     * @description Контроллер МО Создания налоговой формы
     */
        .controller('createDeclarationFormCtrl', ["$scope", "$rootScope", "$http", '$state', '$stateParams', "$modalInstance", "$filter",
            "RefBookValuesResource", 'DeclarationTypeForCreateResource', "APP_CONSTANTS",
            '$shareData', '$dialogs', '$webStorage',
            function ($scope, $rootScope, $http, $state, $stateParams, $modalInstance, $filter,
                      RefBookValuesResource, DeclarationTypeForCreateResource, APP_CONSTANTS, $shareData, $dialogs, $webStorage) {
                //По нажатию на кнопку пользователь может создать только консолидированную форму
                $scope.declarationKind = APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED;

                //Отчетный период из списка периодов в выпадающем списке, у которого самая поздняя дата окончания
                $scope.latestReportPeriod = {};

                $scope.declarationData = {
                    department: $rootScope.user.department
                };

                if ($shareData.latestSelectedPeriod) {
                    $scope.declarationData.period = $shareData.latestSelectedPeriod;
                } else {
                    $scope.$watch("latestReportPeriod.period", function (value) {
                        $scope.declarationData.period = value;
                    });
                }

                /**
                 * Сохранение
                 */
                $scope.save = function () {
                    // Запоминаем период выбранный пользователем
                    $webStorage.set(APP_CONSTANTS.USER_STORAGE.NAME,
                        APP_CONSTANTS.USER_STORAGE.KEYS.LAST_SELECTED_PERIOD,
                        $scope.declarationData.period,
                        true);
                    if ($scope.declarationData.period && $scope.declarationData.department && $scope.declarationData.declarationType) {
                        $http({
                            method: "POST",
                            url: "controller/actions/declarationData/create",
                            params: {
                                declarationTypeId: $scope.declarationData.declarationType.id,
                                departmentId: $scope.declarationData.department.id,
                                periodId: $scope.declarationData.period.id
                            }
                        }).then(function (response) {
                            $modalInstance.close(response);
                        });
                    } else {
                        $dialogs.errorDialog({
                            content: $filter('translate')('createDeclaration.errorMessage')
                        });
                    }
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('createDeclaration.cancel.header'),
                        content: $filter('translate')('createDeclaration.cancel.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.dismiss('Canceled');
                        }
                    });
                };
            }]);
}());