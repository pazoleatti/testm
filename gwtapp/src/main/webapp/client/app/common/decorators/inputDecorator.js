(function () {
    'use strict';

    /**
     * Fix for IE clear button
     */
    angular.module("app.inputDecorator", [])
        .directive('input', function () {
            return {
                restrict: 'E',
                scope: {},
                link: function (scope, elem, attrs) {

                    // Only care about textboxes, not radio, checkbox, etc.
                    var validTypes = /^(search|email|url|tel|number|text)$/i;
                    if (!validTypes.test(attrs.type)) return;

                    // Bind to the mouseup event of the input textbox.
                    elem.bind('mouseup', function () {

                        // Get the old value (before click) and return if it's already empty
                        // as there's nothing to do.
                        var $input = $(this), oldValue = $input.val();
                        if (oldValue === '') return;

                        // Check new value after click, and if it's now empty it means the
                        // clear button was clicked. Manually trigger element's change() event.
                        setTimeout(function () {
                            var newValue = $input.val();
                            if (newValue === '') {
                                angular.element($input).change();
                            }
                        }, 1);
                    });
                }
            }
        });
}());

