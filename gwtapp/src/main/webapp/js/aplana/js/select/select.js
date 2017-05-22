/**
 * ui-select2 (Селект 2)
 * Директива ui-select2 служит для отображения выпадающего списка
 * http://localhost:8080/#/ui_select2
 */
(function () {
    'use strict';
    /* Директива для AplanaSelect */
    angular.module("aplana.select", ['aplana.utils'])
        .directive('aplanaSelect', [
            'AplanaUtils',
            '$timeout',
            '$window',
            function (AplanaUtils, $timeout, $window) {
                var selectEl = function (el) {
                    return angular.element(el.find('select')[0]);
                };
                return {
                    replace: false,
                    templateUrl: AplanaUtils.templatePath + 'select/select.html',
                    restrict: 'A',
                    require: '?ngModel',
                    compile: function compile(element, attributes, transclude) {
                        var select = selectEl(element);
                        // ===================== обязательные ==========================
                        if (attributes.csOptions !== undefined) {
                            // варианты выбора
                            select.attr('data-ng-options', attributes.csOptions);
                        }
                        if (attributes.ngModel !== undefined) {
                            // поле модели
                            select.attr('data-ng-model', attributes.ngModel);
                        }

                        // ===================== опциональные ==========================
                        if (attributes.required !== undefined) {
                            // заполнение обязательно
                            element.addClass('required');
                            select.attr('required', attributes.required);
                        }
                        if (attributes.error !== undefined) {
                            // маркер инвалидности
                            element.addClass('error');
                        }
                        if (attributes.disabled !== undefined) {
                            // маркер выключенного компонента
                            select.attr('disabled', 'true');
                        }
                        if (attributes.editable !== undefined) {
                            // редактируемый
                            select.attr('data-live-search', 'true');
                        }
                        if (attributes.name !== undefined) {
                            // для валидации
                            select.attr('name', attributes.name);
                        }

                        return function link(scope, element, attrs, controller) {

                            var options = scope.$eval(attrs.bsSelect) || {};
                            //$timeout(function () {
                                element.selectpicker(options);
                                element.next().removeClass('ng-scope');

                                // меняем стили выпадашки
                                var btn = element.find('div.btn-group');
                                var dd = element.find('div.dropdown-menu');
                                dd.css('position', 'fixed');


                                var updateDropodwnPostition = function () {
                                    var bounds = btn[0].getBoundingClientRect();

                                    var top = bounds.top;
                                    var height = btn.css('height');

                                    // парсим
                                    height = parseInt(height.substr(0, height.length - 2), 0);

                                    dd.css('left', bounds.left + 'px');
                                    // фикс растягивания на 100% экрана
                                    dd.css('min-width', '100px');
                                    // установка ширины, раной кнопке
                                    dd.css('width', btn.width() + 'px');
                                    // фикс не появляющегося скроллбара
                                    dd.css('overflow-y', 'auto');

                                    if (angular.element($window).height() - bounds.top < 200) {
                                        dd.addClass('dropup');
                                        // костыль, чтобы растягивалсь dropup выпадашка
                                        dd.css('max-height', dd.height() + 'px');
                                        dd.css('height', dd.height() + 'px');
                                        top -= dd.height() + 16; // высота отличается на 12px, по неизвестным причинам + пара пикселей на бордеры
                                    } else {
                                        top += height;
                                    }
                                    dd.css('top', top + 'px');

                                };

                                scope.$on('WINDOW_RESIZED_MSG', function () {
                                    if (btn.hasClass('open')) {
                                        // если скроллим и выпадашка открыта - обновить координаты
                                        updateDropodwnPostition();
                                    }
                                });

                                scope.$on('WINDOW_SCROLLED_MSG', function () {
                                    if (btn.hasClass('open')) {
                                        // если скроллим и выпадашка открыта - обновить координаты
                                        updateDropodwnPostition();
                                    }
                                });

                                btn.click(function () {
                                    updateDropodwnPostition();
                                });
                            //});
                            if (controller) {
                                scope.$watch(attrs.ngModel, function (newValue, oldValue) {
                                    if (!angular.equals(newValue, oldValue)) {
                                        // адский костыль, обновляет данные, когда все загружено
                                        element.selectpicker('refresh');
                                        $timeout(function () {
                                            scope[attrs.ngModel] = newValue;
                                        });

                                    }
                                });
                            }
                        };
                    }
                };
            }]);
}());