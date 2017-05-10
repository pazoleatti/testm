describe('Тестирование mtsUsim.productionLogging\n', function () {
    var scope, controller;

    beforeEach(module('mtsUsim.productionLogging'));

    beforeEach(inject(function (_$rootScope_, $controller) {
        scope = _$rootScope_.$new();
        controller = $controller('loggingCtrl', {$scope: scope})
    }));

    describe('Тестирование контроллера loggingCtrl\n', function () {
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
            describe('Тестирование кнопки "Поиск"', function () {
                it('Кнопка "Поиск" активна всегда', function () {
                    expect(scope.searchButtonEnabled).toBeTruthy();
                });
                it('Обработчик кнопки "Поиск" не существует', function () {
                    expect(scope.searchButtonClick).toBeDefined();
                });
            });

            describe('Тестирование кнопки "Сбросить"', function () {
                it('Кнопка "Сбросить" активна всегда', function () {
                    expect(scope.clearButtonEnabled).toBeTruthy();
                });
                it('Обработчик кнопки "Сбросить" не существует', function () {
                    expect(scope.clearButtonClick).toBeDefined();
                });
                it('Кнопка "Сбросить" не очищает фильтры', function() {
                    // todo - доработать с учетом фильтров дат
                    scope.dataOptions.filter.macroRegion = "Москва";
                    scope.clearButtonClick();
                    expect(scope.dataOptions.filter).toEqual({});
                })
            });
        });
        describe('Тестирование панель кнопок\n', function () {
            it('Кнопка "Выгрузить" не доступна если не выбрана ни одна строка', function () {
                selection = {
                    getSelectedCount: function () {
                        return 0;
                    }
                };
                controller.setUnloadButtonState(selection);
                expect(scope.unloadButtonEnabled).toBeFalsy();
            });
            it('Кнопка "Выгрузить" доступна при выборе одной строки', function () {
                selection = {
                    getSelectedCount: function () {
                        return 1;
                    }
                };
                controller.setUnloadButtonState(selection);
                expect(scope.unloadButtonEnabled).toBeTruthy();
            });
            it('Кнопка "Выгрузить" не доступна при выборе более одной', function () {
                selection = {
                    getSelectedCount: function () {
                        return 2;
                    }
                };
                controller.setUnloadButtonState(selection);
                expect(scope.unloadButtonEnabled).toBeFalsy();
            });
            it('Обработчик кнопки "Выгрузить" не существует', function () {
                expect(scope.unloadButtonClick).toBeDefined();
            });
        });
        describe('Тестирование получения данных с сервера\n', function () {
            it('Метод получения данных не существует', function () {
                expect(controller.fetchData).toBeDefined();
            });
        });
    });
});