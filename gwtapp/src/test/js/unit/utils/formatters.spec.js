describe('formatters', function () {
    'use strict';

    beforeEach(module('app.formatters'));

    // Функция аналог data provider
    function using(value, result, func) {
        for (var i = 0, count = value.length; i < count; i++) {
            var argumentsArray = Array.prototype.slice.call([value[i]]);
            argumentsArray.push(result[i]);
            func.apply(this, argumentsArray);
        }
    }

    /**
     * Дата пустая - выводим ''
     * В другом случае преобразуем дату в формат 'dd.MM.yyyy'
     */
    describe("dateFormatter", function () {
        var value = [null, new Date(2017, 4, 30), new Date(2017, 4, 30, 1, 1, 1, 1)];
        var result = ['', '30.05.2017', '30.05.2017'];

        using(value, result, function (value, result) {
            it("Formatters. Test1. Проверка dateFormatter", function () {
                inject(function (dateFormatterFilter) {
                    expect(dateFormatterFilter(value)).toBe(result);
                });
            });
        });
    });

    /**
     * Дата пустая - выводим ''
     * В другом случае преобразуем дату в формат 'dd.MM.yyyy HH:mm:ss'
     */
    describe("dateTimeFormatter", function () {
        var value = [null, new Date(2017, 4, 30), new Date(2017, 4, 30, 1, 2, 3)];
        var result = ['', '30.05.2017 00:00:00', '30.05.2017 01:02:03'];

        using(value, result, function (value, result) {
            it("Formatters. Test2. Проверка dateTimeFormatter", function () {
                inject(function (dateTimeFormatterFilter) {
                    expect(dateTimeFormatterFilter(value)).toBe(result);
                });
            });
        });
    });
});