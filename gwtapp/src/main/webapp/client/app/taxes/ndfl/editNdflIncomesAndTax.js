(function () {
    'use strict';

    /**
     * @description Модуль для редактирования строки КНФ в разделе 2 (Сведения о доходах и НДФЛ)
     */

    angular.module('app.editNdflIncomesAndTax', ['ui.router', 'app.rest', 'app.formatters'])

    /**
     * @description Контроллер диалогового окна
     */
        .controller('editNdflIncomesAndTaxFormCtrl', ["$scope", "$rootScope", "$http", '$state', '$stateParams',
            "$modalInstance", "$filter", "APP_CONSTANTS", '$shareData', '$dialogs', 'ndflIncomesAndTax',
            function ($scope, $rootScope, $http, $state, $stateParams, $modalInstance, $filter, APP_CONSTANTS, $shareData, $dialogs, ndflIncomesAndTax) {

                $scope.row = {};
                $scope.temp = {};
                $scope.disableTaxTransferDate = false;

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
                            if ($scope.row.taxTransferDate === $filter('translate')('title.taxTransferDateZeroDate')) {
                                $scope.disableTaxTransferDate = true;
                            }
                            $http({
                                method: "GET",
                                url: "controller//rest/getPersonDocTypeName/" + person.idDocType
                            }).success(function (docTypeName) {
                                $scope.temp.docTypeName = docTypeName;
                            });
                            // Получение данных ОКТМО для установки значения в выпадашку
                            $http({
                                method: "GET",
                                url: "controller//rest/refBookValues/oktmoByCode",
                                params: {
                                    code: $scope.row.oktmo
                                }
                            }).success(function (oktmo) {
                                if (oktmo) {
                                    $scope.temp.oktmo = oktmo;
                                } else {
                                    // Если запись не найдена - подставляем текст для кода, чтобы он отобразился в выпадашке
                                    $scope.temp.oktmo = {code: $scope.row.oktmo, name: "-"};
                                }
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
                 * Проверка, может ли заполнено ли любое поле разделов
                 * Раздел информации об доходе
                 * Раздел информации о налоговой базе
                 * Раздел информации о НДФЛ
                 */
                $scope.isIncomeAttributesRequired = function () {
                    return $scope.row.incomeAccruedDate || $scope.row.incomeAccruedSumm || $scope.row.incomePayoutDate ||
                        $scope.row.incomePayoutSumm || $scope.row.taxBase || $scope.row.totalDeductionsSumm ||
                        $scope.row.taxRate || $scope.row.calculatedTax || $scope.row.withholdingTax || $scope.row.taxDate ||
                        $scope.row.notHoldingTax || $scope.row.overholdingTax || $scope.row.refoundTax
                };

                /**
                 * Сохранение
                 */
                $scope.save = function () {
                    $scope.row.oktmo = $scope.temp.oktmo.code;
                    if ($scope.disableTaxTransferDate) {
                        $scope.row.taxTransferDate = $filter('translate')('title.taxTransferDateZeroDate');
                    }
                    ndflIncomesAndTax.update({declarationDataId: $shareData.declarationId}, $scope.row,
                        function (response) {
                            $modalInstance.close(true);
                        });
                };

                /**
                 * Определяет состояние ввода даты перечсисления в бюджет
                 */
                $scope.zeroDateClick = function() {
                    $scope.disableTaxTransferDate = !$scope.disableTaxTransferDate;
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