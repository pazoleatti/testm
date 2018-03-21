(function () {
    'use strict';
    /**
     * @description Модуль для работы со вкладкой "Основная информация" на форме макета
     */
    angular.module('app.templateInfoTab', [])
        .controller('TemplateInfoTabCtrl', ['$scope', '$window', function ($scope, $window) {
            $scope.tab = $scope.checksTab;

            $scope.downloadXsdClick = function () {
                if ($scope.declarationTemplate) {
                    $window.location = "controller/rest/blobData/" + $scope.declarationTemplate.xsdId + "/conf";
                }
            }
        }]);
}());