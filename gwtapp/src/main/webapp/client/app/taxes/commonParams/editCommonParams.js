(function () {
    'use strict';

    /**
     * @description Модуль для редактирования общих параметров'
     */

    angular.module('app.editParams', ['ui.router', 'app.rest'])

    /**
     * @description Модуль для редактирования общих параметров'
     */
        .controller('editParamsCtrl', ['$scope', '$filter', '$http', '$modalInstance', 'parameter', '$dialogs', '$logPanel',
            function ($scope, $filter, $http, $modalInstance, parameter, $dialogs,  $logPanel) {

                /**
                 * @description Редактирование параметра
                 */
                $scope.save = function () {

                    $http({
                        method: "POST",
                        url: "controller/actions/commonParams/editCommonParams",
                        params: {
                            config: $scope.parameter
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        $modalInstance.close();
                    });
                };

                /**
                 * @description Переменная содержащая редактируемое значение
                 */
                $scope.parameter = angular.copy(parameter);

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    if (angular.equals($scope.parameter, parameter)) {
                        $modalInstance.dismiss();
                    } else {
                        //Если внесены изменения предупреждаем пользователя о потери данных при закрытии
                        $dialogs.confirmDialog({
                            title: $filter('translate')('title.cancelChanges'),
                            content: $filter('translate')('commonParams.notSavedData'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('DIALOGS_CANCELLATION'),
                            okBtnClick: function () {
                                $modalInstance.dismiss();
                            }
                        });
                    }
                };

            }]);

}());

