(function () {
    'use strict';

    /**
     * Директива для пейджера с кнопками на первую, пердыдущую, следующую и последнюю страницы и полем ввода номера страницы
     */
    angular.module("app.pager", [])
        .directive('appPager', [function () {
            return {
                restrict: 'EA',
                templateUrl: 'client/app/common/directives/pager/pager.html',
                transclude: true,
                replace: true,
                scope: {
                    onPageChange: '&',  // вызывается при изменении страницы
                    pager: '=ngModel',      // текущая страница (начиная с 1)
                    pagesTotal: '='     // кол-во страниц
                },
                link: function (scope) {

                    // По-умолчания текущая страница первая
                    if (!scope.pager.currPage) {
                        scope.pager.currPage = 1;
                    }
                    // Модель для поля ввода номера страницы. Отделена от currPage чтобы можно было восстановить значение при некорректном вводе
                    scope.inputValue = scope.pager.currPage;

                    /**
                     * Если страница изменилась, то вызваем внешнюю функцию для отображения страницы
                     */
                    scope.$watch('pager.currPage', function (newValue) {
                        scope.inputValue = newValue;
                        scope.onPageChange();
                    });

                    /**
                     * Вызывается при нажатии на enter в элементе ввода номера страницы.
                     * Выполняет проверку корректности ввода номера страницы
                     */
                    scope.onInputEnter = function () {
                        if (scope.inputValue >= 1 && scope.inputValue <= scope.pagesTotal && scope.inputValue !== scope.pager.currPage) {
                            // если введена корректная страница, то показываем её
                            scope.pager.currPage = scope.inputValue;
                        } else {
                            // если введена некоректная страница, то восстанавливаем значение на текущее
                            scope.inputValue = scope.pager.currPage;
                        }
                    };
                }
            };
        }]);
}());

