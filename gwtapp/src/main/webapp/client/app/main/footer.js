(function () {
    'use strict';

    /**
     * @description Footer
     */
    angular.module('app.footer', [])
        .directive('appFooter', function () {
            return {
                templateUrl: 'client/app/main/footer.html?v=${buildUuid}'
            };
        });
}());