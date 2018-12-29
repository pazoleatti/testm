(function () {
    'use strict';

    angular.module('app.idDocRecordModal', ['ui.router'])

        .controller('idDocRecordModalCtrl', ['$scope', '$modalInstance', '$shareData', '$rootScope', 'APP_CONSTANTS', '$http',
            function ($scope, $modalInstance, $shareData, $rootScope, APP_CONSTANTS, $http) {
                $scope.idDoc = {};

                if ($shareData.idDoc) {
                    angular.copy($shareData.idDoc, $scope.idDoc);
                }
                // сообщение для валидатора формата серии и номера документа. Формируется в зависимости от типа документа
                $scope.$validationMessages = {
                    invalidFormat: {
                        message: '' // сообщение валидатора
                    }
                };
                // при изменений данных сбрасываем валидатор серии и номера
                $scope.$watchCollection('[idDoc.docType, idDoc.documentNumber]', function (newValues, oldValues) {
                    if (newValues && oldValues && (newValues[0] && oldValues[0] && newValues[0].id !== oldValues[0].id || !newValues[0] ||
                        newValues[1] && oldValues[1] && newValues[1] !== oldValues[1] || !newValues[1])) {
                        $scope.idDocRefBookRecordForm['iddoc_documentnumber'].$setValidity('invalidFormat', true);
                    }
                });
                // проверяет формат серии и номера документа по типу и если всё ок возвращяет созданный документ
                $scope.checkAndAddIdDoc = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/checkDul",
                        params: {
                            docCode: $scope.idDoc.docType.code,
                            docNumber: $scope.idDoc.documentNumber
                        }
                    }).success(function (response) {
                        if (response.errorMessage && response.errorMessage !== '') {
                            $scope.$validationMessages.invalidFormat.message = response.errorMessage;
                            $scope.idDocRefBookRecordForm['iddoc_documentnumber'].$setValidity('invalidFormat', false);
                        } else {
                            $scope.idDoc.documentNumber = response.formattedNumber;
                            $scope.$validationMessages.invalidFormat.message = '';
                            $scope.idDocRefBookRecordForm['iddoc_documentnumber'].$setValidity('invalidFormat', true);
                            if ($shareData.mode === APP_CONSTANTS.MODE.CREATE) {
                                $rootScope.$broadcast("createIdDoc", $scope.idDoc);
                            }
                            if ($shareData.mode === APP_CONSTANTS.MODE.EDIT) {
                                $shareData.idDoc = $scope.idDoc;
                                $rootScope.$broadcast("updateIdDoc", $scope.idDoc);
                            }
                            $modalInstance.close();
                        }
                    });
                };

                $scope.close = function () {
                    $modalInstance.close();
                };
            }]);
}());