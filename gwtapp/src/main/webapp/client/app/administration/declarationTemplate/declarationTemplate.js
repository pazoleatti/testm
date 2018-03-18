(function () {
    'use strict';

    /**
     * @description Модуль для страницы "Макет налоговой формы"
     */
    angular.module('app.declarationTemplate', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('declarationTemplate', {
                url: '/administration/declarationTemplate/{declarationTemplateId}',
                templateUrl: 'client/app/administration/declarationTemplate/declarationTemplate.html?v=${buildUuid}',
                controller: 'DeclarationTemplateCtrl'
            });
        }])

        .controller('DeclarationTemplateCtrl', ['$scope', '$filter', '$stateParams', 'DeclarationTemplateResource', '$http', 'APP_CONSTANTS',
            function ($scope, $filter, $stateParams, DeclarationTemplateResource, $http, APP_CONSTANTS) {
                DeclarationTemplateResource.query({
                    projection: 'fetchOne',
                    id: $stateParams.declarationTemplateId
                }, function (declarationTemplate) {
                    $scope.declarationTemplate = declarationTemplate;
                });
            }
        ])
}());