(function () {
    'use strict';

    /**
     * @description Модуль для редактирования общих параметров'
     */

    angular.module('app.uploadParams', ['ui.router', 'app.rest', 'app.logPanel'])

    /**
     * @description Модуль для редактирования общих параметров'
     */
        .controller('uploadParamsCtrl', ['$scope', '$filter', '$http', '$uibModalInstance',
            function ($scope, $filter, $http, $uibModalInstance) {

                /**
                 * @description Редактирование параметра
                 */
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "/controller/actions/uploadCommonParams/",
                        params: {
                            config: $scope.parameter
                        }

                    });
                    $uibModalInstance.dismiss('Canceled');
                    $scope.$resolve.data.commonParamsGrid.ctrl.refreshGrid();
                };

                /**
                 * @description Переменная содержащая значения редактируемое значение
                 */
                $scope.parameter = $scope.$resolve.data.commonParamsGrid.value[0];

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $uibModalInstance.dismiss('Canceled');
                };


            }]);

}());

