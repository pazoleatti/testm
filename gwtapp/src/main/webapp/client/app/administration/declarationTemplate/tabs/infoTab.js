(function () {
    'use strict';
    /**
     * @description Модуль для работы со вкладкой "Основная информация" на форме макета
     */
    angular.module('app.templateInfoTab', [])
        .controller('TemplateInfoTabCtrl', ['$scope', '$window', '$dialogs', '$filter', 'Upload', function ($scope, $window, $dialogs, $filter, Upload) {
            $scope.tab = $scope.checksTab;

            $scope.downloadXsdClick = function () {
                if ($scope.declarationTemplate && $scope.declarationTemplate.xsdId) {
                    $window.location = "controller/actions/declarationTemplate/" + $scope.declarationTemplate.id + "?projection=downloadXsd";
                }
            };

            $scope.uploadXsdClick = function (file) {
                if (file) {
                    Upload.upload({
                        url: 'controller/actions/declarationTemplate/uploadXsd',
                        data: {declarationTemplateId: $scope.declarationTemplate.id, uploader: file}
                    }).progress(function (e) {
                    }).then(function (response) {
                        var uuid = response.data;
                        if (uuid) {
                            $scope.declarationTemplate.xsdId = uuid;
                            $dialogs.messageDialog({
                                content: $filter('translate')('declarationTemplate.message.fileUploaded')
                            });
                        }
                    });
                }
            };
        }]);
}());