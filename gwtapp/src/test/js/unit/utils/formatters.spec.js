describe('formatters', function () {
    'use strict';

    beforeEach(module('app.formatters'));

    /**
     * Дата пустая - выводим ''
     * В другом случае преобразуем дату в формат 'dd.MM.yyyy'
     */
    describe('dateFormatter', function () {

        var dateFormatter;

        beforeEach(function () {
            inject(function (dateFormatterFilter) {
                dateFormatter = dateFormatterFilter;
            })
        });

        it('returns an empty string for null', function () {
            expect(dateFormatter(null)).toBe('');
        });

        it('formats a date', function () {
            expect(dateFormatter(new Date(2017, 4, 30))).toBe('30.05.2017')
        });

        it('formats a datetime as a date', function () {
            expect(dateFormatter(new Date(2017, 4, 30, 1, 1, 1, 1))).toBe('30.05.2017')
        });
    });

    /**
     * Дата пустая - выводим ''
     * В другом случае преобразуем дату в формат 'dd.MM.yyyy HH:mm:ss'
     */
    describe('dateTimeFormatter', function () {

        var dateTimeFormatter;

        beforeEach(function () {
            inject(function (dateTimeFormatterFilter) {
                dateTimeFormatter = dateTimeFormatterFilter;
            })
        });

        it('returns an empty string for null', function () {
            expect(dateTimeFormatter(null)).toBe('');
        });

        it('formats a date as a datetime', function () {
            expect(dateTimeFormatter(new Date(2017, 4, 30))).toBe('30.05.2017 00:00:00')
        });

        it('formats a datetime', function () {
            expect(dateTimeFormatter(new Date(2017, 4, 30, 1, 2, 3))).toBe('30.05.2017 01:02:03')
        });
    });

    /**
     * Возвращаем текст в зависимости от переданного числа
     */
    describe('amountCasesFormatter', function () {

        var amountCasesFormatter;

        function resultFor(number) {
            return amountCasesFormatter(number, 'оповещение', 'оповещения', 'оповещений');
        }

        beforeEach(function () {
            inject(function (amountCasesFormatterFilter) {
                amountCasesFormatter = amountCasesFormatterFilter;
            })
        });

        it('returns the 1st for 1, 21, 101, 121', function () {
            expect(resultFor(1)).toBe('оповещение');
            expect(resultFor(21)).toBe('оповещение');
            expect(resultFor(101)).toBe('оповещение');
            expect(resultFor(121)).toBe('оповещение');
        });

        it('returns the 2nd for 2-4, 22-24, 102-104, 122-124', function () {
            expect(resultFor(2)).toBe('оповещения');
            expect(resultFor(4)).toBe('оповещения');
            expect(resultFor(22)).toBe('оповещения');
            expect(resultFor(24)).toBe('оповещения');
            expect(resultFor(102)).toBe('оповещения');
            expect(resultFor(104)).toBe('оповещения');
            expect(resultFor(122)).toBe('оповещения');
            expect(resultFor(124)).toBe('оповещения');
        });

        it('returns the 3rd for 0, 5-20, etc.', function () {
            expect(resultFor(0)).toBe('оповещений');
            expect(resultFor(5)).toBe('оповещений');
            expect(resultFor(10)).toBe('оповещений');
            expect(resultFor(11)).toBe('оповещений');
            expect(resultFor(12)).toBe('оповещений');
            expect(resultFor(14)).toBe('оповещений');
            expect(resultFor(15)).toBe('оповещений');
            expect(resultFor(20)).toBe('оповещений');
            expect(resultFor(25)).toBe('оповещений');
            expect(resultFor(30)).toBe('оповещений');
            expect(resultFor(100)).toBe('оповещений');
            expect(resultFor(105)).toBe('оповещений');
            expect(resultFor(110)).toBe('оповещений');
            expect(resultFor(111)).toBe('оповещений');
            expect(resultFor(114)).toBe('оповещений');
            expect(resultFor(120)).toBe('оповещений');
        });
    });

    /**
     * Преобразуем коллекцию объектов в коллекцию их идентификаторов
     */
    describe('idExtractor', function () {

        var idExtractor;

        beforeEach(function () {
            inject(function (idExtractorFilter) {
                idExtractor = idExtractorFilter;
            })
        });

        it('returns an empty array for undefined', function () {
            expect(idExtractor(undefined)).toEqual([]);
        });

        it('returns an empty array for null', function () {
            expect(idExtractor(null)).toEqual([]);
        });

        it('returns an empty array for empty array', function () {
            expect(idExtractor([])).toEqual([]);
        });

        it('extracts for one element', function () {
            expect(idExtractor([{id: 1, data: 10}])).toEqual([1]);
        });

        it('extracts for multiple elements', function () {
            var array = [
                {id: 1, data: 10},
                {id: 2, data: 20},
                {id: 3, data: 30}
            ];
            expect(idExtractor(array)).toEqual([1, 2, 3]);
        });
    });

    /**
     * Тесты на joinObjectsPropFormatter
     */
    describe('joinObjectsPropFormatter', function () {

        var joinObjectsPropFormatter;

        beforeEach(function () {
            inject(function (joinObjectsPropFormatterFilter) {
                joinObjectsPropFormatter = joinObjectsPropFormatterFilter;
            })
        });

        it('returns an empty string for null', function () {
            expect(joinObjectsPropFormatter(null)).toBe('');
        });

        it('returns an empty string for empty array', function () {
            expect(joinObjectsPropFormatter([])).toBe('');
        });

        it('joins correctly with default params', function () {
            var array = [
                {name: 1},
                {name: 2},
                {name: 3}
            ];
            expect(joinObjectsPropFormatter(array)).toBe('1, 2, 3');
        });

        it('joins correctly with custom separator and property', function () {
            var array = [
                {a: 1},
                {a: 2},
                {a: 3}
            ];
            expect(joinObjectsPropFormatter(array, '; ', 'a')).toBe('1; 2; 3');
        });

        it('joins correctly with custom separator and inner property', function () {
            var array = [
                {a: {b: 1}},
                {a: {b: 2}},
                {a: {b: 3}}
            ];
            expect(joinObjectsPropFormatter(array, '; ', 'a.b')).toBe('1; 2; 3');
        });
    });


    describe('codeNameFormatter', function () {

        var codeNameFormatter;

        beforeEach(function () {
            inject(function (codeNameFormatterFilter) {
                codeNameFormatter = codeNameFormatterFilter;
            });
        });

        it('works for common object value', function () {
            expect(codeNameFormatter({code: '1', name: 'Паспорт'})).toBe('(1) Паспорт');
        });

        it('returns an empty string for null', function () {
            expect(codeNameFormatter(null)).toBe('');
        });

        it('returns an empty string for empty object', function () {
            expect(codeNameFormatter({})).toBe('');
        });

        it('formats only code if no name present', function () {
            expect(codeNameFormatter({code: '1'})).toBe('(1)');
        });

        it('returns only name if no code present', function () {
            expect(codeNameFormatter({name: 'Паспорт'})).toBe('Паспорт');
        });
    });

    /**
     * docTypeFormatter
     */
    describe('docTypeFormatter', function () {

        var docTypeFormatter;

        // Шпионим за вызовами других фильтров
        var translate = jasmine.createSpy().and.callFake(function (value) {
            if (value === 'refBook.fl.table.label.permissionDenied') {
                return 'Permission denied';
            }
        });
        var codeNameFormatter = jasmine.createSpy().and.callFake(function () {
        });

        beforeEach(function () {
            // Прокидываем mock в контекст ангуляра
            module(function ($provide) {
                $provide.value('translateFilter', translate);
                $provide.value('codeNameFormatterFilter', codeNameFormatter);
            });

            inject(function (docTypeFormatterFilter) {
                docTypeFormatter = docTypeFormatterFilter;
            })
        });

        it('returns an empty string for null', function () {
            expect(docTypeFormatter(null)).toBe('');
        });

        it('returns permission denied message for forbidden value', function () {
            var forbidden = {
                permission: false,
                value: null
            };
            var result = docTypeFormatter(forbidden);
            expect(translate).toHaveBeenCalledWith('refBook.fl.table.label.permissionDenied');
            expect(result).toBe('Permission denied');
        });

        it('delegates formatting to codeNameFormatter', function () {
            var data = {
                permission: true,
                value: {
                    docType: 'anyValue'
                }
            };
            docTypeFormatter(data);
            expect(codeNameFormatter).toHaveBeenCalledWith('anyValue');
        });
    });
});
