(function () {
    'use strict';

    /**
     * @description Модуль для страницы "Макеты налоговых форм"
     */
    angular.module('app.declarationTypeJournal', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('declarationTypeJournal', {
                url: '/administration/declarationTypeJournal',
                templateUrl: 'client/app/administration/declarationTemplate/declarationTypeJournal.html?v=${buildUuid}',
                controller: 'DeclarationTypeJournalCtrl'
            });
        }])

        .controller('DeclarationTypeJournalCtrl', ['$scope', '$filter', 'DeclarationTypeResource', '$http', 'APP_CONSTANTS',
            function ($scope, $filter, DeclarationTypeResource, $http, APP_CONSTANTS) {

                DeclarationTypeResource.querySource({
                        projection: "declarationTypeJournal"
                    },
                    function (data) {
                        if (data) {
                            $scope.declarationTypeJournalGrid.ctrl.refreshGridData(data);
                        }
                    }
                );

                $scope.declarationTypeJournalGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "local",
                        data: [],
                        colNames: [
                            '',
                            $filter('translate')('declarationTypeJournal.grid.name'),
                            $filter('translate')('declarationTypeJournal.grid.versionCount')],
                        colModel: [
                            {name: 'id', index: 'id', key: true, hidden: true},
                            {name: 'name', index: 'name', width: 1000, formatter: $filter('templatesByTypeLinkFormatter')},
                            {name: 'versionsCount', index: 'versionsCount'}
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
         * @description Формирует ссылку на версии макетов
         */
        .filter('templatesByTypeLinkFormatter', function () {
            return function (cellValue, options) {
                return "<a href='index.html#/administration/declarationTemplateJournal/" + options.rowId + "'>" + cellValue + "</a>";
            };
        })

}());