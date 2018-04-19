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
                    $window.location = "controller/rest/blobData/" + $scope.declarationTemplate.xsdId + "/conf";
                }
            };

            $scope.uploadXsdClick = function (file) {
                if (file) {
                    $scope.deleteNewXsd();
                    Upload.upload({
                        url: 'controller/actions/declarationTemplate/uploadXsd',
                        data: {declarationTemplateId: $scope.declarationTemplate.id, uploader: file}
                    }).progress(function (e) {
                    }).then(function (response) {
                        var uuid = response.data;
                        if (uuid) {
                            $scope.declarationTemplate.isXsdNew = true;
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