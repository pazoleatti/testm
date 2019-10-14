describe('Поверка соответствия периодов связанных ' +
    ' элементов справчоников "Кода вида дохода" и "Вида дохода" (SBRFNDFL-8604)', function () {
    'use strict';


    angular.module('aplana.modal.dialogs', []);

    beforeEach(module('app.refBookInterceptors'));

    describe('Проверка логики формирования ошибок', function () {

        var refBookInterceptorsIncomeKindConditionUtils;

        beforeEach(function () {
            inject(function (_refBookInterceptorsIncomeKindConditionUtils_) {
                refBookInterceptorsIncomeKindConditionUtils = _refBookInterceptorsIncomeKindConditionUtils_;
            });
        });

        it('positive case 1: incomeKindStartDate < incomeTypeStartDate', function () {
            var incomeKindStartDate = '2019-01-09', incomeTypeStartDate = '2019-01-11';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError1(incomeKindStartDate, incomeTypeStartDate)).toBe(true);
        });

        it('positive case 2-1: incomeKindStartDate = incomeTypeEndDate', function () {
            var incomeKindStartDate = '2019-01-11', incomeTypeEndDate = '2019-01-11';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError2(incomeKindStartDate, incomeTypeEndDate)).toBe(true);
        });

        it('positive case 2-2: incomeKindStartDate = incomeTypeEndDate when incomeTypeEndDate is infinity', function () {
            var incomeKindStartDate = '2019-01-11', incomeTypeEndDate;
            expect(refBookInterceptorsIncomeKindConditionUtils.isError2(incomeKindStartDate, incomeTypeEndDate)).toBe(false);
        });

        it('positive case 2-3: incomeKindStartDate > incomeTypeEndDate', function () {
            var incomeKindStartDate = '2019-01-12', incomeTypeEndDate = '2019-01-11';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError2(incomeKindStartDate, incomeTypeEndDate)).toBe(true);
        });

        it('positive case 2-4: incomeKindStartDate > incomeTypeEndDate when incomeKindStartDate is infinity', function () {
            var incomeKindStartDate = '2019-01-12', incomeTypeEndDate;
            expect(refBookInterceptorsIncomeKindConditionUtils.isError2(incomeKindStartDate, incomeTypeEndDate)).toBe(false);
        });

        it('positive case 3: incomeKindEndDate > incomeTypeEndDate', function () {
            var incomeKindEndDate = '2019-01-10', incomeTypeEndDate = '2019-01-02';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(true);
        });

        it('positive case 3-1: incomeKindEndDate > incomeTypeEndDate when incomeKindEndDate is infinity', function () {
            var incomeKindEndDate, incomeTypeEndDate = '2019-01-02';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(false);
        });


        it('negative case 1-1: incomeKindStartDate = incomeTypeStartDate', function () {
            var incomeKindStartDate = '2019-01-09', incomeTypeStartDate = '2019-01-09';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError1(incomeKindStartDate, incomeTypeStartDate)).toBe(false);
        });

        it('negative case 1-2: incomeKindStartDate > incomeTypeStartDate', function () {
            var incomeKindStartDate = '2019-01-10', incomeTypeStartDate = '2019-01-09';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError1(incomeKindStartDate, incomeTypeStartDate)).toBe(false);
        });

        it('negative case 2-1: incomeKindStartDate < incomeTypeEndDate', function () {
            var incomeKindStartDate = '2019-01-01', incomeTypeEndDate = '2019-01-11';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError2(incomeKindStartDate, incomeTypeEndDate)).toBe(false);
        });

        it('negative case 2-2: incomeKindStartDate < incomeTypeEndDate when incomeTypeEndDate is infinity', function () {
            var incomeKindStartDate = '2019-01-01', incomeTypeEndDate;
            expect(refBookInterceptorsIncomeKindConditionUtils.isError2(incomeKindStartDate, incomeTypeEndDate)).toBe(false);
        });


        it('negative case 3-1: incomeKindEndDate = incomeTypeEndDate', function () {
            var incomeKindEndDate = '2019-01-10', incomeTypeEndDate = '2019-01-10';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(false);
        });

        it('negative case 3-2: incomeKindEndDate = incomeTypeEndDate when incomeKindEndDate is infinity', function () {
            var incomeKindEndDate, incomeTypeEndDate = '2019-01-10';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(false);
        });

        it('negative case 3-3: incomeKindEndDate = incomeTypeEndDate when incomeTypeEndDate is infinity', function () {
            var incomeKindEndDate = '2019-01-10', incomeTypeEndDate;
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(false);
        });

        it('negative case 3-4: incomeKindEndDate < incomeTypeEndDate', function () {
            var incomeKindEndDate = '2019-01-01', incomeTypeEndDate = '2019-01-10';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(false);
        });

        it('negative case 3-5: incomeKindEndDate < incomeTypeEndDate when incomeKindEndDate is infinity', function () {
            var incomeKindEndDate, incomeTypeEndDate = '2019-01-10';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(false);
        });

        it('negative case 3-6: incomeKindEndDate < incomeTypeEndDate when incomeTypeEndDate is infinity', function () {
            var incomeKindEndDate = '2019-01-01', incomeTypeEndDate;
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(false);
        });

    });

});
