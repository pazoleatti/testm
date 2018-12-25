(function () {
    'use strict';

    /**
     * Fix for IE clear button
     */
    angular.module("app.FixIEClearButton", [])
        .directive('input', ['$timeout', function ($timeout) {
            return {
                restrict: 'E',
                scope: {},
                link: function (scope, elem, attrs) {

                    var validTypes = /^(search|email|url|tel|number|text)$/i;
                    if (!validTypes.test(attrs.type)) {
                        return;
                    }

                    elem.bind('mouseup', function () {
                        var oldValue = elem.val();
                        if (oldValue === '') {
                            return;
                        }

                        $timeout(function () {
                            var newValue = elem.val();
                            if (newValue !== oldValue) {
                                elem.val(oldValue);
                                elem.triggerHandler('keydown');
                                elem.val(newValue);
                                elem.triggerHandler('focus');
                            }
                        }, 0, false);
                    });
                }
            };
        }]);
}());

