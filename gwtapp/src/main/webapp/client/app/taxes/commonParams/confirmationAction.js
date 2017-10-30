(function () {
    'use strict';

    /**
     * @description Модуль для подтверждения измений'
     */

    angular.module('app.confirmationAction', ['ui.router', 'app.rest', 'app.logPanel'])

    /**
     * @description Контроллер для подтверждения измения общих параметров по умолчанию'
     */
        .controller('confirmationActionCtrl', ['$scope', '$filter', '$http', '$uibModalInstance',
            function ($scope, $filter, $http, $uibModalInstance) {

                /**
                 * @description Редактирование параметра
                 */
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "/controller/actions/changeToDefaultCommonParams"
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
