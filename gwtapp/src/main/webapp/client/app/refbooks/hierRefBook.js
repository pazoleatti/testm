(function () {
    'use strict';

    /**
     * @description Модуль для отображения линейного справочника
     */
    angular.module('app.hierRefBook', [])
        .config(['$stateProvider', function($stateProvider) {
            $stateProvider.state('hierRefBook', {
                url: '/refbooks/hierRefBook/{refBookId}?uuid',
                templateUrl: 'client/app/refbooks/hierRefBook.html?v=${buildUuid}',
                controller: 'hierRefBookCtrl'
            });
        }])

        .controller('hierRefBookCtrl', ['$scope', function($scope){

        }]);
}());