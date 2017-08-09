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

    /**
     * Возвращаем текст в зависимости от переданного числа
     */
    describe("amountCasesFormatter", function () {
        it("Formatters. Test3. Проверка amountCasesFormatter", function () {
            inject(function (amountCasesFormatterFilter) {
                expect(amountCasesFormatterFilter(1, "оповещение", "оповещения", "оповещений")).toBe("оповещение");
                expect(amountCasesFormatterFilter(2, "оповещение", "оповещения", "оповещений")).toBe("оповещения");
                expect(amountCasesFormatterFilter(5, "оповещение", "оповещения", "оповещений")).toBe("оповещений");
            });
        });
    });

    /**
     * Преобразуем коллекцию объектов в коллекцию их идентификаторов
     */
    describe("idExtractor", function () {
        var value = [[{id: 1, data: 10}], [{id: 1, data: 10}, {id: 2, data: 20}, {id: 3, data: 30}], []];
        var result = [[1], [1, 2, 3], []];

        using(value, result, function (value, result) {
            it("Formatters. Test3. Проверка idExtractor", function () {
                inject(function (idExtractorFilter) {
                    expect(idExtractorFilter(value)).toEqual(result);
                });
            });
        });
    });
});