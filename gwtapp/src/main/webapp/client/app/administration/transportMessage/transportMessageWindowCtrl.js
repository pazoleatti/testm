(function () {
    'use strict';

    /**
     * Контроллер диалогового окна "Просмотр записи журнала сообщений".
     */
    angular.module('app.transportMessage')

        .controller('transportMessageWindowCtrl', ['$scope', '$shareData', '$modalInstance',
            function ($scope, $shareData, $modalInstance) {

                $scope.message = $shareData.message;

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.close();
                };
            }
        ])
}());
