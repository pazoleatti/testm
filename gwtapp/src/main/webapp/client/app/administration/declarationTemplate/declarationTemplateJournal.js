(function () {
    'use strict';

    /**
     * @description Модуль для страницы "Макеты налоговых форм"
     */
    angular.module('app.declarationTemplateJournal', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('declarationTemplateJournal', {
                url: '/administration/declarationTemplateJournal/{declarationTypeId}',
                templateUrl: 'client/app/administration/declarationTemplate/declarationTemplateJournal.html?v=${buildUuid}',
                controller: 'DeclarationTemplateJournalCtrl'
            });
        }])

        .controller('DeclarationTemplateJournalCtrl', ['$scope', '$filter', '$stateParams', 'DeclarationTemplateByTypeResource', '$http', 'APP_CONSTANTS',
            function ($scope, $filter, $stateParams, DeclarationTemplateByTypeResource, $http, APP_CONSTANTS) {
                // todo
            }
        ])

}());