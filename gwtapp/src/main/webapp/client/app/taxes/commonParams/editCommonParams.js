(function () {
    'use strict';

    /**
     * @description Модуль для редактирования общих параметров'
     */

    angular.module('app.editParams', ['ui.router', 'app.rest', 'app.logPanel'])

    /**
     * @description Модуль для редактирования общих параметров'
     */
        .controller('editParamsCtrl', ['$scope', '$filter', '$http', '$modalInstance', 'commonParamsGrid', '$logPanel',
            function ($scope, $filter, $http, $modalInstance, commonParamsGrid, $logPanel) {

                /**
                 * @description Редактирование параметра
                 */
                $scope.save = function () {

                    $http({
                        method: "POST",
                        url: "controller/actions/editCommonParams",
                        params: {
                            config: $scope.parameter
                        }
                    }).then(function(response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        commonParamsGrid.ctrl.refreshGrid();
                    });

                    $modalInstance.dismiss('Canceled');

                };
                /**
                 * @description Переменная содержащая значения редактируемое значение
                 */
                $scope.parameter = commonParamsGrid.value[0];

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss('Canceled');

                };

            }]);

}());

