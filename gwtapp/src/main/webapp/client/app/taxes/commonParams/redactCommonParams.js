(function () {
    'use strict';

    /**
     * @description Модуль для редактирования общих параметров'
     */

    angular.module('app.redactParams', ['ui.router', 'app.rest', 'app.logPanel'])


        .controller('controllerRedactParams', ['$scope', '$filter', '$http', '$uibModalInstance',
            function ($scope, $filter, $http, $uibModalInstance) {

                /**
                 * @description Редактирование параметра
                 */
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "/controller/actions/redactCommonParams/",
                        params: {
                            config: $scope.parameter
                        }

                    });
                    $uibModalInstance.dismiss('Canceled');
                    $scope.$resolve.data.commonParamsGrid.ctrl.refreshGrid();
                };
                $scope.parameter = $scope.$resolve.data.commonParamsGrid.value[0];

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $uibModalInstance.dismiss('Canceled');
                };


            }]);

}());

