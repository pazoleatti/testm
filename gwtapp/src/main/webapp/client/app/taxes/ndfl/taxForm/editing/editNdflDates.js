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
                    url: "controller/actions/declarationData/" + $shareData.declarationId + "/lockEdit"
                }).then(function (response) {
                    if (response.data.uuid) {
                        $logPanel.open('log-panel-container', response.data.uuid);
                    }
                    if (!response.data.success) {
                        $modalInstance.dismiss();
                    }
                }).catch(function (reason) {
                    $modalInstance.dismiss('Не можем установить блокировку на форму. Причина: ' + reason);
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
                    var dialogContent = $shareData.byFilter ?
                        $filter('translate')('incomesAndTax.editDates.byFilter.submit.confirm.text') :
                        $filter('translate')('incomesAndTax.editDates.selected.submit.confirm.text');

                    $dialogs.confirmDialog({
                        content: dialogContent,
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $scope.postData()
                                .then(function (response) {
                                    if (response.data && response.data.uuid) {
                                        $logPanel.open('log-panel-container', response.data.uuid);
                                    }
                                    $modalInstance.close(true);
                                })
                                .catch(function () {
                                    $modalInstance.close();
                                });
                        }
                    });
                };

                /**
                 * @description Отправка данных формы на сервер.
                 * @returns HttpPromise сервиса $http
                 */
                $scope.postData = function () {
                    var transferDate = $scope.params.zeroTransferChecked ? APP_CONSTANTS.DATE_ZERO.AS_DATE : $scope.params.transfer;
                    var url = $shareData.byFilter ?
                        'controller/rest/declarationData/' + $shareData.declarationId + '/editNdflIncomeDatesByFilter' :
                        'controller/rest/declarationData/' + $shareData.declarationId + '/editNdflIncomeDates';

                    var requestParams = {};
                    if ($shareData.byFilter) {
                        requestParams = {filter: $shareData.filter};
                        requestParams.filter.declarationDataId = $shareData.declarationId;
                    }

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
