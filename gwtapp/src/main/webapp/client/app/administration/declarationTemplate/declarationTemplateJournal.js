(function () {
    'use strict';

    /**
     * @description Модуль для страницы "Версии макетов налоговых форм"
     */
    angular.module('app.declarationTemplateJournal', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('declarationTemplateJournal', {
                url: '/administration/declarationTemplateJournal/{declarationTypeId}',
                templateUrl: 'client/app/administration/declarationTemplate/declarationTemplateJournal.html?v=${buildUuid}',
                controller: 'DeclarationTemplateJournalCtrl'
            });
        }])

        .controller('DeclarationTemplateJournalCtrl', ['$scope', '$filter', '$stateParams', 'DeclarationTypeResource', 'DeclarationTemplateResource', '$http', 'APP_CONSTANTS',
            function ($scope, $filter, $stateParams, DeclarationTypeResource, DeclarationTemplateResource, $http, APP_CONSTANTS) {

                DeclarationTypeResource.query({
                    declarationTypeId: $stateParams.declarationTypeId
                }, function (declarationType) {
                    $scope.declarationType = declarationType;
                });

                $scope.declarationTemplateJournalGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: DeclarationTemplateResource,
                        requestParameters: function () {
                            return {
                                projection: 'allByTypeId',
                                id: $stateParams.declarationTypeId
                            };
                        },
                        colNames: [
                            '',
                            $filter('translate')('declarationTemplateJournal.grid.name'),
                            $filter('translate')('declarationTemplateJournal.grid.versionFrom'),
                            $filter('translate')('declarationTemplateJournal.grid.versionEnd')],
                        colModel: [
                            {name: 'id', index: 'id', key: true, hidden: true},
                            {name: 'name', index: 'name', width: 700, formatter: $filter('templateLinkFormatter')},
                            {name: 'version', index: 'version', width: 300, formatter: $filter('dateFormatter')},
                            {name: 'versionEnd', index: 'versionEnd', width: 300, formatter: $filter('dateFormatter')}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'name',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false
                    }
                };
            }
        ])

        /**
         * @description Формирует ссылку на версию макетов
         */
        .filter('templateLinkFormatter', function () {
            return function (cellValue, options) {
                return "<a href='index.html#/administration/declarationTemplate/" + options.rowId + "'>" + cellValue + "</a>";
            };
        })
}());