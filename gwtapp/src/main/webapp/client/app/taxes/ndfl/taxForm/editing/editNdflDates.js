(function () {
    'use strict';
    /**
     * @description Модуль массового редактирования дат в строках Раздела 2
     */
    angular.module('app.editNdflDates', ['ui.router', 'app.rest', 'app.formatters'])
        .controller('editNdflDatesFormCtrl', ['$scope', '$http', '$filter', '$shareData', '$modalInstance', '$dialogs', '$logPanel', 'APP_CONSTANTS',
            function ($scope, $http, $filter, $shareData, $modalInstance, $dialogs, $logPanel, APP_CONSTANTS) {

                // Установка блокировки на форму
                $http({
                    method: "POST",
                    url: "controller//actions/declarationData/" + $shareData.declarationId + "/lockEdit"
                }).success(function (lock) {
                    if (lock.uuid) {
                        $logPanel.open('log-panel-container', lock.uuid);
                    }
                    if (!lock.success) {
                        $modalInstance.dismiss('Не можем установить блокировку');
                    }
                });

                $scope.byFilter = $shareData.byFilter;

                $scope.params = {
                    accrued: null,
                    payout: null,
                    tax: null,
                    transfer: null,
                    zeroTransferChecked: false
                };

                // Обработчик нажатия на кнопку "Сохранить"
                $scope.submit = function () {
                    if ($scope.formNotEmpty()) {
                        $scope.confirmAndSaveChanges();
                    } else {
                        $scope.showEmptyDataMessage();
                    }
                };

                /**
                 * @description Проверка заполнения формы.
                 * @returns boolean
                 */
                $scope.formNotEmpty = function () {
                    return $scope.params.accrued || $scope.params.payout || $scope.params.tax || $scope.params.transfer || $scope.params.zeroTransferChecked;
                };

                // Сохранение данных с подтверждением
                $scope.confirmAndSaveChanges = function () {
                    $dialogs.confirmDialog({
                        content: $filter('translate')('incomesAndTax.editDates.submit.confirm.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $scope.postData()
                                .success(function (response) {
                                    if (response && response.uuid) {
                                        $logPanel.open('log-panel-container', response.uuid);
                                    }
                                    $modalInstance.close(true);
                                });
                        }
                    });
                };

                /**
                 * @description Отправка данных формы на сервер.
                 * @returns HttpPromise
                 */
                $scope.postData = function () {
                    var transferDate = $scope.params.zeroTransferChecked ? APP_CONSTANTS.DATE_ZERO.AS_DATE : $scope.params.transfer;
                    var url = $shareData.byFilter ?
                        'controller/rest/declarationData/' + $shareData.declarationId + '/editNdflIncomeDates' :
                        'controller/rest/declarationData/' + $shareData.declarationId + '/editNdflIncomeDatesByFilter';
                    var requestParams = $shareData.byFilter ? {filter: $shareData.filter} : null;
                    return $http({
                        method: 'POST',
                        url: url,
                        data: {
                            incomeIds: $shareData.rowIds,
                            accruedDate: $scope.params.accrued,
                            payoutDate: $scope.params.payout,
                            taxDate: $scope.params.tax,
                            transferDate: transferDate
                        },
                        params: requestParams
                    });
                };

                // Сообщение при попытке сохранения пустой формы
                $scope.showEmptyDataMessage = function () {
                    $dialogs.messageDialog({
                        content: $filter('translate')('incomesAndTax.editDates.emptyData.message.text')
                    });
                };

                // Обработчик закрытия окна без сохранения
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        content: $filter('translate')('incomesAndTax.editDates.confirm.cancel.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.close();
                        }
                    });
                };
            }
        ]);
}());
