(function () {
    'use strict';

    angular.module('app.personTbModal', ['ui.router'])

        .controller('personTbModalCtrl', ['$scope', '$modalInstance', '$shareData', '$rootScope', 'APP_CONSTANTS',
            function ($scope, $modalInstance, $shareData, $rootScope, APP_CONSTANTS) {
                $scope.tb = {};
                $scope.mode = $shareData.mode;
                $scope.presentedTb = $shareData.presentedTb;
                if ($shareData.tb) {
                    angular.copy($shareData.tb, $scope.tb);
                }
                $scope.save = function () {
                    if ($scope.tb.importDate && $scope.tb.tbDepartment) {
                        if ($scope.mode === APP_CONSTANTS.MODE.CREATE) {
                            $rootScope.$broadcast("createTb", $scope.tb)
                        }
                        if ($scope.mode === APP_CONSTANTS.MODE.EDIT) {
                            $rootScope.$broadcast("updateTb", $scope.tb)
                        }
                        $modalInstance.close()
                    }
                };

                $scope.close = function () {
                    $modalInstance.close()
                }
            }]);
}());