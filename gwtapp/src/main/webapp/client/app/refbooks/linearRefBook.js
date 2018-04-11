(function () {
    'use strict';

    /**
     * @description Модуль для отображения линейного справочника
     */
    angular.module('app.linearRefBook', [])
        .config(['$stateProvider', function($stateProvider) {
            $stateProvider.state('linearRefBook', {
                url: '/refbooks/linearRefBook/{refBookId}?uuid',
                templateUrl: 'client/app/refbooks/linearRefBook.html?v=${buildUuid}',
                controller: 'linearRefBookCtrl'
            });
        }])

        .controller('linearRefBookCtrl', ['$scope', function($scope){

        }]);
}());