/**
 * aplana-dropdown (Выпадающее меню)
 * Директива aplana-dropdown служит для отображения кнопки с выпадающим меню
 * http://localhost:8080/#/aplana_dropdown
 */
(function () {
    'use strict';
    angular.module('aplana.dropdown', ['aplana.utils'])
        .directive('aplanaDropdown', [
            'AplanaUtils',
            function (AplanaUtils) {
                return {
                    restrict: 'A',
                    scope: true,
                    transclude: true,
                    replace: false,
                    templateUrl: AplanaUtils.templatePath + 'dropdown/dropdown.html',
                    compile: function compile(tElement, tAttrs, transclude) {
                        // выбор режима отображения
                        if (tAttrs.split !== undefined) {
                            // двухкнопочный режим
//                            tElement.find('button').text(tAttrs.text);
                            tElement.find('button').attr('data-ng-transclude', '');
                            tElement.find('div.btn-group').append('<button class="btn btn-primary dropdown-toggle" data-bs-dropdown="' + tAttrs.aplanaDropdown + '"><span class=\"caret\"></span></button>');
                        } else {
                            // однокнопочный режим

                            tElement.find('button').attr('data-bs-dropdown', tAttrs.aplanaDropdown);
                            tElement.find('button>span:first').attr('data-ng-transclude', '');
                            tElement.find('button').append('<span class=\"caret\"></span>');

                            AplanaUtils.moveAttributes(tElement, tElement.find('button'), ['data-aplana-dropdown']);

                            if (!tAttrs.aplanaDropdown) {
                                var nextSibling = tElement[0].nextSibling;
                                while (nextSibling && nextSibling.nodeType !== 1) {
                                    nextSibling = nextSibling.nextSibling;
                                }
                                if (nextSibling && nextSibling.classList.contains('dropdown-menu')) {
                                    tElement.find('button').after(nextSibling);
                                }
                            }
                        }

                        // направление выпадашки
                        if (tAttrs.dropup !== undefined) {
                            // выпадает вверх
                            tElement.find('div.btn-group').addClass('dropup');
                        }

                        if (tAttrs.small !== undefined) {
                            tElement.find('button').addClass('btn-small');
                        }
                    }
                };
            }
        ]);
}());