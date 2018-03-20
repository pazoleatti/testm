(function () {
    'use strict';

    /**
     * @description Модуль для страницы "Макет налоговой формы"
     */
    angular.module('app.declarationTemplate', ['app.templateChecksTab', 'app.templateInfoTab'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('declarationTemplate', {
                url: '/administration/declarationTemplate/{declarationTemplateId}',
                templateUrl: 'client/app/administration/declarationTemplate/declarationTemplate.html?v=${buildUuid}',
                controller: 'DeclarationTemplateCtrl'
            });
        }])

        .controller('DeclarationTemplateCtrl', ['$scope', '$filter', '$stateParams', 'DeclarationTemplateResource', '$window',
            function ($scope, $filter, $stateParams, DeclarationTemplateResource, $window) {
                $scope.declarationTemplate = {formType: {}};

                // Загружаем данные по макету
                DeclarationTemplateResource.query({
                    projection: 'fetchOne',
                    id: $stateParams.declarationTemplateId
                }, function (declarationTemplate) {
                    $scope.declarationTemplate = declarationTemplate;
                    $scope.declarationTemplate.yearFrom = new Date(declarationTemplate.version).getUTCFullYear();
                    if (declarationTemplate.versionEnd) {
                        $scope.declarationTemplate.yearTo = new Date(declarationTemplate.versionEnd).getUTCFullYear();
                    }
                });

                $scope.templateTabsCtrl = {};
                $scope.infoTab = {
                    title: $filter('translate')('declarationTemplate.tabs.info'),
                    contentUrl: 'client/app/administration/declarationTemplate/tabs/infoTab.html?v=${buildUuid}',
                    fetchTab: true,
                    active: true
                };
                $scope.checksTab = {
                    title: $filter('translate')('declarationTemplate.tabs.checks'),
                    contentUrl: 'client/app/administration/declarationTemplate/tabs/checksTab.html?v=${buildUuid}',
                    fetchTab: true
                };
                $scope.templateTabs = [$scope.infoTab, $scope.checksTab];
            }
        ])
}());