(function () {
    'use strict';

    angular.module('app.idDocRecordModal', ['ui.router'])

        .controller('idDocRecordModalCtrl', ['$scope', '$modalInstance', '$shareData', '$rootScope', 'APP_CONSTANTS', '$http',
            function ($scope, $modalInstance, $shareData, $rootScope, APP_CONSTANTS, $http) {
                $scope.idDoc = {};
                $scope.formatInvalid = false;
                $scope.formatInvalidMessage = '';

                if ($shareData.idDoc) {
                    angular.copy($shareData.idDoc, $scope.idDoc);
                }
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/checkDul",
                        params: {
                            docCode: $scope.idDoc.docType.code,
                            docNumber: $scope.idDoc.documentNumber
                        }
                    }).success(function (response) {
                        if (response.errorMessage && response.errorMessage !== '') {
                            $scope.formatInvalid = true;
                            $scope.formatInvalidMessage = response.errorMessage;
                        } else {
                            $scope.idDoc.documentNumber = response.formattedNumber;
                            $scope.formatInvalid = false;
                            $scope.formatInvalidMessage = '';
                            if ($shareData.mode === APP_CONSTANTS.MODE.CREATE) {
                                $rootScope.$broadcast("createIdDoc", $scope.idDoc)
                            }
                            if ($shareData.mode === APP_CONSTANTS.MODE.EDIT) {
                                $shareData.idDoc = $scope.idDoc;
                                $rootScope.$broadcast("updateIdDoc", $scope.idDoc);
                            }
                            $modalInstance.close()
                        }
                    });
                };

                $scope.close = function () {
                    $modalInstance.close()
                }
            }]);
}());