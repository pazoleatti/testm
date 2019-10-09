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

        it('positive case 2-2: incomeKindStartDate > incomeTypeEndDate', function () {
            var incomeKindStartDate = '2019-01-12', incomeTypeEndDate = '2019-01-11';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError2(incomeKindStartDate, incomeTypeEndDate)).toBe(true);
        });

        it('positive case 3: incomeKindEndDate > incomeTypeEndDate', function () {
            var incomeKindEndDate = '2019-01-10', incomeTypeEndDate = '2019-01-02';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(true);
        });


        it('negative case 1-1: incomeKindStartDate = incomeTypeStartDate', function () {
            var incomeKindStartDate = '2019-01-09', incomeTypeStartDate = '2019-01-09';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError1(incomeKindStartDate, incomeTypeStartDate)).toBe(false);
        });

        it('negative case 1-2: incomeKindStartDate > incomeTypeStartDate', function () {
            var incomeKindStartDate = '2019-01-10', incomeTypeStartDate = '2019-01-09';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError1(incomeKindStartDate, incomeTypeStartDate)).toBe(false);
        });

        it('negative case 2: incomeKindStartDate < incomeTypeEndDate', function () {
            var incomeKindStartDate = '2019-01-01', incomeTypeEndDate = '2019-01-11';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError2(incomeKindStartDate, incomeTypeEndDate)).toBe(false);
        });

        it('negative case 3-1: incomeKindEndDate = incomeTypeEndDate', function () {
            var incomeKindEndDate = '2019-01-10', incomeTypeEndDate = '2019-01-10';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(false);
        });

        it('negative case 3-2: incomeKindEndDate < incomeTypeEndDate', function () {
            var incomeKindEndDate = '2019-01-01', incomeTypeEndDate = '2019-01-10';
            expect(refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)).toBe(false);
        });

    });

});
