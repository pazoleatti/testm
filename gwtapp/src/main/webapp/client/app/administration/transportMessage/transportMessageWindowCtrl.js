(function () {
    'use strict';

    /**
     * Контроллер диалогового окна "Просмотр записи журнала сообщений".
     */
    angular.module('app.transportMessage')

        .controller('transportMessageWindowCtrl', ['$scope', '$http', '$shareData', '$modalInstance',
            function ($scope, $http, $shareData, $modalInstance) {

                $scope.message = $shareData.message;

                // Запрос тела сообщения
                $http.get('controller/rest/transportMessages/' + $shareData.message.id + '/body')
                    .then(function (response) {
                        $scope.message.body = response.data;
                    });

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.close();
                };
            }
        ])
}());
