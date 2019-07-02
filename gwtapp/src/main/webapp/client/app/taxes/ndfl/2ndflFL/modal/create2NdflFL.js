(function () {
    'use strict';

    /**
     * @description Модуль на модальное окно создания 2-НДФЛ (ФЛ)
     */
    angular.module('app.create2NdflFL', [])

        .controller('Create2NdflFLCtrl', ["$scope", "$http", "$modalInstance", "$filter", "APP_CONSTANTS", '$shareData', '$dialogs',
            function ($scope, $http, $modalInstance, $filter, APP_CONSTANTS, $shareData, $dialogs) {

                $scope.create2NdflFLForm = {
                    declarationType: APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_FL
                };

                $scope.create = function () {
                    var createParams = {
                        personId: $shareData.person.id,
                        declarationTypeId: $scope.create2NdflFLForm.declarationType.id,
                        departmentId: $scope.user.terBank.id,
                        reportPeriodId: $scope.create2NdflFLForm.reportPeriod.id,
                        kppOktmoPairs: $scope.create2NdflFLForm.kppOktmoPairs,
                        signatory: $scope.create2NdflFLForm.signatory
                    };
                    create(createParams);
                };

                function create(createParams) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/create2NdflFL",
                        data: createParams
                    }).then(function (response) {
                        $modalInstance.close(response);
                    });
                }

                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('createDeclaration.cancel.header'),
                        content: $filter('translate')('createDeclaration.cancel.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.dismiss();
                        }
                    });
                };
            }]);
}());