/**
 * aplana-popover (Всплывающие блоки)
 * Директива предназначена для создания всплывающего блока с произвольным контентом
 * http://localhost:8080/#/aplana_popover
 */
(function () {
    'use strict';

    angular.module('aplana.popover', ['aplana.utils'])
        .directive('aplanaPopover', [
            '$parse',
            '$compile',
            '$http',
            '$timeout',
            '$q',
            '$templateCache',
            'AplanaUtils', '$document',
            function ($parse, $compile, $http, $timeout, $q, $templateCache, AplanaUtils, $document) {
                return {
                    restrict: 'A',
                    link: function postLink(scope, element, attr, ctrl) {

                        var aplanaPopoverTemplate = attr.aplanaPopoverTemplate;
                        var getter = $parse(attr.aplanaPopover), setter = getter.assign, value = getter(scope), options = {};
                        if (angular.isObject(value)) {
                            options = value;
                        }
                        $q.when(options.content || $templateCache.get(value) || $http.get(value, { cache: true })).then(function onSuccess(template) {
                            if (angular.isObject(template)) {
                                template = template.data;
                            }
                            if (!!attr.unique) {
                                element.on('show', function (ev) {
                                    $('.popover.in').each(function () {
                                        var $this = $(this), popover = $this.data('popover');
                                        if (popover && !popover.$element.is(element)) {
                                            $this.popover('hide');
                                        }
                                    });
                                });
                            }
                            if (!!attr.hide) {
                                scope.$watch(attr.hide, function (newValue, oldValue) {
                                    if (!!newValue) {
                                        popover.hide();
                                    } else if (newValue !== oldValue) {
                                        popover.show();
                                    }
                                });
                            }
                            if (!!attr.show) {
                                scope.$watch(attr.show, function (newValue, oldValue) {
                                    if (!!newValue) {
                                        $timeout(function () {
                                            popover.show();
                                        });
                                    } else if (newValue !== oldValue) {
                                        popover.hide();
                                    }
                                });
                            }
                            element.popover(angular.extend({}, options, {
                                template: aplanaPopoverTemplate,
                                content: template,
                                html: true
                            }));
                            var popover = element.data('popover');
                            popover.hasContent = function () {
                                return this.getTitle() || template;
                            };
                            popover.getPosition = function () {
                                var r = $.fn.popover.Constructor.prototype.getPosition.apply(this, arguments);
                                // Удалим старый Scope
                                var oldScope = this.$tip.data().$scope;
                                if (angular.isDefined(oldScope)) {
                                    oldScope.$destroy();
                                }
                                // Создаем новый Scope для каждого открытия popover. Это исправляет утечку объектов,
                                // которая происходит из-за пересоздания контента внутри tooltip и последующей его компиляции Ангуляром.
                                var childScope = scope.$new();
                                $compile(this.$tip)(childScope);
                                childScope.$digest();
                                this.$tip.data('popover', this);
                                return r;
                            };
                            popover.getPositionOnly = function () {
                                return $.fn.popover.Constructor.prototype.getPosition.apply(this, arguments);
                            };
                            scope.$popover = function (name) {
                                popover(name);
                            };
                            angular.forEach([
                                'show', 'hide', 'destroy', 'toggle'
                            ], function (name) {
                                scope[name] = function () {
                                    popover[name]();
                                };
                            });
                            scope.dismiss = scope.hide;
                            angular.forEach([
                                'show',
                                'shown',
                                'hide',
                                'hidden'
                            ], function (name) {
                                element.on(name + '.bs.popover', function (ev) {
                                    scope.$emit('popover-' + name, ev);

                                    var funcName = attr['on' + AplanaUtils.capitaliseFirstLetter(name)];
                                    if (angular.isDefined(funcName)) {
                                        scope.$apply(funcName);
                                    }
                                });
                            });

                            var isInModal = ($(element).parents('.modal').size() !== 0);
                            var modalFooter; //Подвал модального окна
                            var modalHeader; //Шапка модального окна
                            if (isInModal) {
                                modalFooter = $(element).parents('.modal').find('.modal-footer');
                                modalHeader = $(element).parents('.modal').find('.modal-header');
                            }

                            //ф-ия проверяет не уехал ли поповер за границу модального окна, если уехал - закрывает
                            var checkPosition = function () {
                                if (isInModal) {
                                    var buttonPos = popover.getPositionOnly();

                                    if (modalFooter.offset().top <= buttonPos.top) {
                                        popover.hide();
                                    } else if (modalHeader.offset().top + modalHeader[0].getBoundingClientRect().height >= buttonPos.top) {
                                        popover.hide();
                                    }
                                }
                            };

                            scope.$on('WINDOW_RESIZED_MSG', function () {
                                //Если поповер открыт
                                if (popover.tip().hasClass('in')) {
                                    popover.updatePosition();
                                    checkPosition();
                                }
                            });

                            scope.$on('WINDOW_SCROLLED_MSG', function () {
                                //Если поповер открыт
                                if (popover.tip().hasClass('in')) {
                                    popover.updatePosition();
                                    checkPosition();
                                }
                            });

                            //Для скрола контейнера модального окна
                            angular.element("div.modal-body").bind("scroll", function () {
                                //Если поповер открыт
                                if (popover.tip().hasClass('in')) {
                                    popover.updatePosition();
                                    checkPosition();
                                }
                            });

                            scope.$on('POPOVER_UPDATE_POSITION', function () {
                                popover.updatePosition();
                                checkPosition();
                            });


                            var documentClickBind = function (event) {
                                var $popover = $('.popover');
                                if (event.target !== element[0] && !$.contains(element[0], event.target) &&
                                    angular.isDefined($popover[0]) && !$.contains($popover[0], event.target)) {
                                    popover.hide();
                                    $document.unbind('click', documentClickBind);
                                }
                            };

                            element.on('show', function () {
                                $document.bind('click', documentClickBind);
                            });

                            element.on('hide', function () {
                                $document.unbind('click', documentClickBind);
                            });

                            scope.$on('$destroy', function () {
                                scope.dismiss();
                            });
                        });
                    }
                };
            }
        ]);
}());
