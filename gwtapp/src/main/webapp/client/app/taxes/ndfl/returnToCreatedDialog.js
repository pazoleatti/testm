(function () {
    'use strict';

    /**
     * @description Модуль МО возврата декларации в статус "Создана"
     */

    angular.module('app.returnToCreatedDialog', [])

    /**
     * @description Контроллер МО возврата декларации в статус "Создана"
     */
        .controller('returnToCreatedCtrl', ['$scope', '$translate', '$uibModalInstance', 'data', function ($scope, $translate, $uibModalInstance, data) {
            $scope.header = (angular.isDefined(data.header)) ? data.header : $translate.instant('DIALOGS_CONFIRMATION');
            $scope.msg = (angular.isDefined(data.msg)) ? data.msg : $translate.instant('DIALOGS_CONFIRMATION_MSG');
            $scope.reason = "";
            $scope.labelYes = $translate.instant('DIALOGS_CONTINUE');
            $scope.labelNo = $translate.instant('DIALOGS_CANCEL');
            //-- Methods -----//

            $scope.no = function(){
                $uibModalInstance.dismiss('no');
            }; // end close

            $scope.yes = function(){
                $uibModalInstance.close($scope.reason);
            }; // end yes
        }]);
}());