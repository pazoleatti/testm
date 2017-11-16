(function () {
    'use strict';

    /**
     * @description Модуль для подтверждения измений'
     */

    angular.module('app.confirmationAction', ['ui.router', 'app.rest', 'app.logPanel'])

    /**
     * @description Контроллер для подтверждения измения общих параметров по умолчанию'
     */
        .controller('confirmationActionCtrl', ['$scope', '$filter', '$http', '$modalInstance', 'commonParamsGrid',
            function ($scope, $filter, $http, $modalInstance, commonParamsGrid) {

                /**
                 * @description Редактирование параметра
                 */
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "/controller/actions/changeToDefaultCommonParams"
                    });

                    commonParamsGrid.ctrl.refreshGrid();
                    $modalInstance.dismiss('Canceled');
                };

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss('Canceled');
                };

            }]);
}());
