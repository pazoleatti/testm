/**
 * aplana-tooltip (Отображение подсказок)
 * Компонент отображения подсказок (tooltip)
 * http://localhost:8080/#/aplana_tooltip
 */
(function () {
    'use strict';

    angular.module('aplana.tooltip', ['aplana.utils'])
        .directive('aplanaTooltip', ['AplanaUtils', '$compile',
            function (AplanaUtils, $compile) {
                return {
                    restrict: 'A',
                    replace: true,
                    link: function (scope, element, attrs) {
                        var tooltip = $compile('<div data-container="body" class="tooltip-bottom-popup" style="margin: 10px; position:fixed;">' +
                            '<div class="tooltip-text">' +
                            '</div>' +
                            '</div>')(scope);
                        element.after(tooltip);
                        $(element).hover(function (event) {
                            var toolTipText = AplanaUtils.sanitizeParams(attrs.aplanaTooltip);
                            if (angular.isString(toolTipText) && !!toolTipText.trim()) {
                                tooltip.find('div.tooltip-text').html(AplanaUtils.sanitizeParams(toolTipText));
                                tooltip.css('left', event.clientX);
                                tooltip.css('top', event.clientY);
                                tooltip.addClass('showpopup');
                            }
                        }, function () {
                            tooltip.removeClass('showpopup');
                        });
                    }
                };
            }
        ]);
}());