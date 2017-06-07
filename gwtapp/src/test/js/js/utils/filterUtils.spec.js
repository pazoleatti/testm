'use strict';

describe('Test filterUtils.', function () {

    var service;

    beforeEach(function () {
        module('sbrfNdfl.filterUtils');

        inject(function ($injector) {
            service = $injector.get('filterUtils');
        });
    });

    describe('Test 1, filterUtils. Проверка инициализации.', function () {
        it('Сервис filterUtils должен быть инициализирован', function () {
            expect(service).toBeDefined();
        });
    });

    describe('Test 2, filterUtils. Проверка методов.', function () {
        it('Проверка метода getSurnameWithInitials', function () {
            var filter = [];
            service.getSurnameWithInitials("Иванов Иван Иванович", filter);
            expect(filter).toEqual([
                {property: "surnameWithInitials", value: "Иванов И.И."}
            ]);
            filter = [];
            service.getSurnameWithInitials("", filter);
            expect(filter).toEqual([]);
            filter = [];
            service.getSurnameWithInitials(null, filter);
            expect(filter).toEqual([]);
        });
    });

});