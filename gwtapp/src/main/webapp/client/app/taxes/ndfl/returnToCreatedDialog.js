(function () {
    'use strict';

    /**
     * @description Модуль МО возврата декларации в статус "Создана"
     */

    angular.module('app.returnToCreatedDialog', [])

    /**
     * @description Контроллер МО возврата декларации в статус "Создана"
     */
        .controller('returnToCreatedCtrl', ['$scope', '$translate', '$modalInstance', '$shareData',
            function ($scope, $translate, $modalInstance, $shareData) {
            $scope.msg = (angular.isDefined($shareData.msg)) ? $shareData.msg : $translate.instant('DIALOGS_CONFIRMATION_MSG');
            $scope.returnToCreated = {
                reason :""
            };
            $scope.labelYes = $translate.instant('DIALOGS_CONTINUE');
            $scope.labelNo = $translate.instant('DIALOGS_CANCELLATION');
            //-- Methods -----//

            $scope.no = function(){
                $modalInstance.dismiss('no');
            }; // end close

            $scope.yes = function(){
                $modalInstance.close($scope.returnToCreated.reason);
            }; // end yes
        }]);
}());