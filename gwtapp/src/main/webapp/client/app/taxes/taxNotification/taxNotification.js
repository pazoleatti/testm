(function () {
    'use strict';

    /**
     * @description Модуль модального окна формирования Уведомления о неудержанном налоге
     */
    angular.module('app.taxNotification', [])
    /**
     * @description Контроллер модального окна формирования Уведомления о неудержанном налоге
     */
        .controller('taxNotificationCtrl', ['$scope', '$http', '$filter', '$modalInstance', '$dialogs', '$logPanel', 'APP_CONSTANTS',
            function ($scope, $http, $filter, $modalInstance, $dialogs, $logPanel, APP_CONSTANTS) {

                $scope.params = {};

                // При выборе подразделения кидаем событие, чтобы перезаполнился список периодов
                $scope.$watch('params.department', function (newValue) {
                    if (typeof (newValue) !== 'undefined' && newValue != null) {
                        $scope.$broadcast(APP_CONSTANTS.EVENTS.DEPARTMENT_SELECTED, $scope.params.department.id);
                    }
                });

                // Запрос на создание Уведомления
                $scope.create = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/createTaxNotification",
                        params: {
                            departmentId: $scope.params.department.id,
                            periodId: $scope.params.period.id,
                            asnuIds: $filter('idExtractor')($scope.params.asnuList)
                        }
                    }).success(function (response) {
                        $logPanel.open('log-panel-container', response);
                        $modalInstance.close(response);
                    })
                };

                // Перегружаем метод, висящий на кнопке закрытия "x"
                $scope.modalCloseCallback = function () {
                    $scope.close();
                };

                // Закрытие окна, выводим диалог подтверждения
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('taxNotification.confirm.cancel.title'),
                        content: $filter('translate')('taxNotification.confirm.cancel.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.close();
                        }
                    });
                };
            }
        ])
}());