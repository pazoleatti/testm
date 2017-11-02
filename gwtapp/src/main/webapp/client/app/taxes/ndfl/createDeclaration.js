(function () {
    'use strict';

    /**
     * @description Модуль для создания налоговых форм
     */

    angular.module('app.createDeclaration', ['ui.router', 'app.rest', 'app.modals', 'app.formatters'])

    /**
     * @description Контроллер МО Создания налоговой формы
     */
        .controller('createDeclarationFormCtrl', ["$scope", "$rootScope", "$http", '$state', '$stateParams', "$uibModalInstance", "$filter",
            "RefBookValuesResource", 'DeclarationTypeForCreateResource', "APP_CONSTANTS",
            'data', 'appModals',
            function ($scope, $rootScope, $http, $state, $stateParams, $uibModalInstance, $filter,
                      RefBookValuesResource, DeclarationTypeForCreateResource, APP_CONSTANTS, data, appModals) {
                //По нажатию на кнопку пользователь может создать только консолидированную форму
                $scope.declarationKind = APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED;

                //Отчетный период из списка периодов в выпадающем списке, у которого самая поздняя дата окончания
                $scope.latestReportPeriod = {};

                $scope.declarationData = {
                    department: $rootScope.user.department
                };

                if (data.latestSelectedPeriod) {
                    $scope.declarationData.period = data.latestSelectedPeriod;
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
                    $rootScope.latestSelectedPeriod = $scope.declarationData.period;
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
                            $uibModalInstance.close(response);
                        });
                    } else {
                        appModals.error($filter('translate')('DIALOGS_ERROR'), $filter('translate')('createDeclaration.errorMessage'));
                    }
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    appModals.confirm($filter('translate')('createDeclaration.cancel.header'), $filter('translate')('createDeclaration.cancel.text'))
                        .result.then(function () {
                        $uibModalInstance.dismiss('Canceled');
                    });
                };
            }]);
}());