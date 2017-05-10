describe('Тестирование sbrfNdfl.productionPlanning\n', function () {
    var scope, controller;

    beforeEach(module('sbrfNdfl.productionPlanning'));

    beforeEach(inject(function (_$rootScope_, $controller) {
        scope = _$rootScope_.$new();
        controller = $controller('planningCtrl', {$scope: scope})
    }));

    describe('Тестирование контроллера planningCtrl\n', function () {
        describe('Тестирование инициализации\n', function () {
            describe('Тестирование dataOptions\n', function () {
                it('dataOptions должeн быть инициализирован', function () {
                    expect(scope.dataOptions).toBeDefined();
                });
                it('dataOptions.paging должен быть инициализирован', function () {
                    expect(scope.dataOptions.paging).toBeDefined();
                    expect(scope.dataOptions.paging.pageNumber).toEqual(1);
                    expect(scope.dataOptions.paging.pageSize).toEqual(20);
                });
                it('dataOptions.sort должен быть null', function () {
                    expect(scope.dataOptions.sort).toBeNull();
                });
                it('dataOptions.metaData должен быть null', function () {
                    expect(scope.dataOptions.metaData).toBeNull();
                });
                it('dataOptions.filter должен быть инициализирован', function () {
                    expect(scope.dataOptions.filter).toBeDefined();
                });
                it('dataOptions.filterList должен быть инициализирован', function () {
                    expect(scope.dataOptions.filterList).toBeDefined();
                });
            });
            describe('Тестирование gridOptions\n', function () {
                it('gridOptions должен быть инициализирован', function () {
                    expect(scope.gridOptions).toBeDefined();
                })
            })
        });
        describe('Тестирование панели фильтрации\n', function () {
            it('Кнопка "Поиск" активна всегда', function () {
                expect(scope.searchButtonEnabled).toBeTruthy();
            });
            it('Кнопка "Сбросить" активна всегда', function () {
                expect(scope.clearButtonEnabled).toBeTruthy();
            });
        });
    });
});