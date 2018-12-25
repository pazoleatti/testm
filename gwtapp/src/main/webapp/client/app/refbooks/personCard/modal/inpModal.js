(function () {
    'use strict';

    angular.module('app.inpModal', ['ui.router'])

        .controller('inpModalCtrl', ['$scope', '$modalInstance', '$shareData', '$rootScope', 'APP_CONSTANTS',
            function ($scope, $modalInstance, $shareData, $rootScope, APP_CONSTANTS) {
                $scope.inp = {};
                $scope.mode = $shareData.mode;
                if ($shareData.inp) {
                    angular.copy($shareData.inp, $scope.inp);
                }
                $scope.save = function () {
                    if ($scope.inp.asnu && $scope.inp.inp) {
                        if ($scope.mode === APP_CONSTANTS.MODE.CREATE) {
                            $rootScope.$broadcast("createInp", $scope.inp)
                        }
                        if ($scope.mode === APP_CONSTANTS.MODE.EDIT) {
                            $rootScope.$broadcast("updateInp", $scope.inp)
                        }
                        $modalInstance.close()
                    }
                };

                $scope.close = function () {
                    $modalInstance.close()
                }
            }]);
}());