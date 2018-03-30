(function () {
    'use strict';

    /**
     * @description Модуль для редактирования строки КНФ в разделе 3 (Сведения о вычетах)
     */

    angular.module('app.editNdflDeduction', ['ui.router', 'app.rest', 'app.formatters'])

    /**
     * @description Контроллер диалогового окна
     */
        .controller('editNdflDeductionFormCtrl', ["$scope", "$rootScope", "$http", '$state', '$stateParams',
            "$modalInstance", "$filter", "APP_CONSTANTS", '$shareData', '$dialogs', 'ndflDeduction',
            function ($scope, $rootScope, $http, $state, $stateParams, $modalInstance, $filter, APP_CONSTANTS, $shareData, $dialogs, ndflDeduction) {

                $scope.row = {};
                $scope.temp = {};

                // Установка блокировки на форму
                $http({
                    method: "POST",
                    url: "controller//actions/declarationData/" + $shareData.declarationId + "/lock"
                }).success(function (lock) {
                    if (lock && lock.declarationDataLocked) {
                        // Получение данных ФЛ из раздела 1
                        $http({
                            method: "GET",
                            url: "controller//rest/ndflPerson/" + $shareData.row.ndflPersonId
                        }).success(function (person) {
                            $scope.row = $shareData.row;
                            $scope.temp.person = person;
                            $http({
                                method: "GET",
                                url: "controller//rest/getPersonDocTypeName/" + person.idDocType
                            }).success(function (docTypeName) {
                                $scope.temp.docTypeName = docTypeName;
                            });
                        });
                    } else {
                        $dialogs.errorDialog({
                            content: $filter('translate')('incomesAndTax.edit.locked', {
                                declaration: $shareData.declarationId,
                                department: $shareData.department
                            })
                        });
                    }
                });

                /**
                 * Сохранение
                 */
                $scope.save = function () {
                    ndflDeduction.update({declarationDataId: $shareData.declarationId}, $scope.row,
                        function () {
                            $modalInstance.close(true);
                        });
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('incomesAndTax.edit.cancel.header'),
                        content: $filter('translate')('incomesAndTax.edit.cancel.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.close(false);
                        }
                    });
                };
            }]);
}());