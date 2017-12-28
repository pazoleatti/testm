(function () {
    'use strict';

    /* Директива для пейджера */
    angular.module("app.pager", [])
        .directive('appPager', [function () {
            return {
                restrict: 'EA',
                templateUrl: 'client/app/common/directives/pager/pager.html?v=${buildUuid}',
                transclude: true,
                replace: true,
                scope: {
                    onPageChange: '&',  // вызывается при изменении страницы
                    currPage: '=',      // текущая страница (начиная с 1)
                    pagesTotal: '='     // кол-во страниц
                },
                link: function (scope) {

                    function setPage(page) {
                        scope.onPageChange({page: page});
                    }

                    scope.$watch('currPage', function (newValue) {
                        scope.inputValue = newValue;
                    });

                    scope.onInputChange = function () {
                        if (scope.inputValue >= 1 && scope.inputValue <= scope.pagesTotal && scope.inputValue !== scope.currPage) {
                            setPage(scope.inputValue);
                        } else {
                            scope.inputValue = scope.currPage;
                        }
                    };

                    scope.firstPage = function () {
                        if (scope.currPage !== 1) {
                            setPage(1);
                        }
                    };

                    scope.nextPage = function () {
                        if (scope.currPage !== scope.pagesTotal) {
                            setPage(scope.currPage + 1)
                        }
                    };

                    scope.prevPage = function () {
                        if (scope.currPage > 1) {
                            setPage(scope.currPage - 1);
                        }
                    };

                    scope.lastPage = function () {
                        if (scope.currPage !== scope.pagesTotal) {
                            setPage(scope.pagesTotal);
                        }
                    };
                }
            };
        }]);
}());

