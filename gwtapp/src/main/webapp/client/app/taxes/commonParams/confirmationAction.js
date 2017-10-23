(function () {
    'use strict';

    /**
     * @description Модуль для подтверждения'
     */

    angular.module('app.confirmationAction', ['ui.router', 'app.rest', 'app.logPanel'])

        .controller('controllerConfirmationAction', ['$scope', '$filter', '$http', '$uibModalInstance',
            function ($scope, $filter, $http, $uibModalInstance) {

                /**
                 * @description Редактирование параметра
                 */
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "/controller/actions/defaultCommonParams"
                    });

                    $scope.$resolve.data.commonParamsGrid.ctrl.refreshGrid();
                    $uibModalInstance.dismiss('Canceled');
                };


                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $uibModalInstance.dismiss('Canceled');
                };

            }]);

}());
