(function () {
    'use strict';

    /**
     * @description Модуль для редактирования общих параметров'
     */

    angular.module('app.uploadParams', ['ui.router', 'app.rest', 'app.logPanel'])

    /**
     * @description Модуль для редактирования общих параметров'
     */
        .controller('uploadParamsCtrl', ['$scope', '$filter', '$http', '$modalInstance', 'commonParamsGrid',
            function ($scope, $filter, $http, $modalInstance, commonParamsGrid) {

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
                    $modalInstance.dismiss('Canceled');
                    commonParamsGrid.ctrl.refreshGrid();
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

