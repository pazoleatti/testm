(function () {
    'use strict';

    angular.module('aplana.utils.directives', []).

    /**
     * В отличие от стандартной ngTransclude размещает HTML контент не внутрь тэга, а после тэга.
     */
        directive('aplanaTransclude', function () {
            return {
                controller: ['$transclude', function ($transclude) {
                    // remember the transclusion fn but call it during linking so that we don't process transclusion before directives on
                    // the parent element even when the transclusion replaces the current element. (we can't use priority here because
                    // that applies only to compile fns and not controllers
                    this.$transclude = $transclude;
                }],

                link: function ($scope, $element, $attrs, controller) {
                    controller.$transclude(function (clone) {
                        $element.html('');
                        // Отличие от ngTransclude в следующей строке
                        $element.after(clone);
                    });
                }
            };
        });
}());