/**
 * aplana-panes (Статичные закладки)
 * Директива предназначена для отображения статичных закладок с фиксированным содержимым
 * http://localhost:8080/#/aplana_panes
 */
(function () {
    'use strict';

    // Сделано из angular-strap bsTabs

    angular.module('aplana.panes', ['aplana.utils'])
        .directive('aplanaPanes', [
            '$parse', '$gridStack',
            'AplanaUtils',
            function ($parse, $gridStack, AplanaUtils) {
                return {
                    restrict: 'A',
                    require: '?ngModel',
                    priority: 0,
                    scope: true,
                    templateUrl: AplanaUtils.templatePath + 'panes/panes.html',
                    replace: true,
                    transclude: true,
                    compile: function compile(tElement, tAttrs, transclude) {
                        return function postLink(scope, iElement, iAttrs, controller) {
                            scope.$on('$destroy', function () {
                                iElement.remove();
                                scope = undefined;
                            });

                            var getter = $parse(iAttrs.aplanaPanes), setter = getter.assign, value = getter(scope);
                            scope.panes = [];
                            var $tabs = iElement.find('ul.nav-tabs');
                            var $panes = iElement.find('div.tab-content');
                            var activeTab = 0, id, title, active;
//                    $timeout(function () {            KSS непонятно зачем здесь был вставлен timeout. Все отлично работает без него. Убран из-за проблем с unit тестами
                            $panes.find('[data-title], [data-tab]').each(function (index) {
                                var $this = angular.element(this);
                                id = 'tab-' + scope.$id + '-' + index;
                                title = $this.data('title') || $this.data('tab');
                                active = !active && $this.hasClass('active');
                                $this.attr('id', id).addClass('tab-pane');
                                if (iAttrs.fade) {
                                    $this.addClass('fade');
                                }
                                scope.panes.push({
                                    id: id,
                                    title: title,
                                    content: this.innerHTML,
                                    active: active
                                });
                            });
                            if (scope.panes.length && !active) {
                                $panes.find('.tab-pane:first-child').addClass('active' + (iAttrs.fade ? ' in' : ''));
                                scope.panes[0].active = true;
                            }
//                    });
                            if (controller) {
                                iElement.on('show', function (ev) {
                                    var $target = $(ev.target);
                                    scope.$apply(function () {
                                        controller.$setViewValue($target.data('index'));
                                    });
                                });
                                scope.$watch(iAttrs.ngModel, function (newValue, oldValue) {
                                    if (angular.isUndefined(newValue)) {
                                        return;
                                    }
                                    activeTab = newValue;
                                    setTimeout(function () {
                                        var $next = $($tabs[0].querySelectorAll('li')[newValue * 1]);
                                        if (!$next.hasClass('active')) {
                                            $next.children('a').tab('show');
                                        }
                                    });
                                });
                            }

                            scope.onPaneClick = function () {
                                $gridStack.adjustAllGridsWidth();
                            };

                        };
                    }
                };
            }
        ]);
}());