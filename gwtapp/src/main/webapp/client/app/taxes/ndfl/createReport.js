(function () {
    'use strict';

    /**
     * @description Модуль для создания отчетности
     */
    angular.module('app.createReport', [])

    /**
     * @description Контроллер окна "Создание отчетности"
     */
        .controller('createReportCtrl', ['$scope', '$uibModalInstance', function ($scope, $uibModalInstance) {
            $scope.save = function () {
                $uibModalInstance.close()
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('Canceled')
            };
        }])

}());