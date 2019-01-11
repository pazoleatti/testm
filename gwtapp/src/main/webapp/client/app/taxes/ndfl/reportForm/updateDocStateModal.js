(function () {
    'use strict';

    /**
     * @description Модальное окно изменения состояния ЭД формы
     */
    angular.module('app.updateDocStateModal', ['app.constants', 'app.rest', 'app.formatters'])
        .controller('UpdateDocStateCtrl', ['$http', '$scope', '$modalInstance',
            function ($http, $scope, $modalInstance) {

                $scope.form = {
                    docState: null
                };

                $scope.confirm = function () {
                    $modalInstance.close($scope.form.docState);
                };

                $scope.cancel = function () {
                    $modalInstance.dismiss();
                };
            }]
        );
}());