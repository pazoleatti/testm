(function () {
    'use strict';

    /**
     * Модуль "Транспортные сообщения".
     */
    angular.module('app.transportMessage', [])

        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('transportMessageJournal', {
                url: '/administration/transportMessages',
                templateUrl: 'client/app/administration/transportMessage/transportMessageJournal.html',
                controller: 'transportMessageJournalCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_BLOCK)) {
                            $state.go("/");
                        }
                    }
                ]
            });
        }])
}());
