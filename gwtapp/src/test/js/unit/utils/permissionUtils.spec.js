describe('permissionUtils', function () {
    'use strict';

    beforeEach(module('app.permissionUtils'));

    /**
     * Дата пустая - выводим ''
     * В другом случае преобразуем дату в формат 'dd.MM.yyyy'
     */
    describe('$compareMasks', function () {

        var PermissionChecker;

        beforeEach(function () {
            inject(function (_PermissionChecker_) {
                PermissionChecker = _PermissionChecker_;
            })
        });

        it('1 permissive with mask 1', function () {
            expect(PermissionChecker.$compareMasks(1, 1)).toBe(true);
        });

        it('2 not permissive with mask 1', function () {
            expect(PermissionChecker.$compareMasks(2, 1)).toBe(false);
        });

        it('(2^20 + 4) permissive with mask 4', function () {
            expect(PermissionChecker.$compareMasks(Math.pow(2, 20) + 4, 4)).toBe(true);
        });

        it('(2^20 + 8) not permissive with mask 4', function () {
            expect(PermissionChecker.$compareMasks(Math.pow(2, 20) + 8, 4)).toBe(false);
        });

        it('(2^50 + 4) permissive with mask 4', function () {
            expect(PermissionChecker.$compareMasks(Math.pow(2, 50) + 4, 4)).toBe(true);
        });

        it('(2^50 + 8) not permissive with mask 4', function () {
            expect(PermissionChecker.$compareMasks(Math.pow(2, 50) + 8, 4)).toBe(false);
        });

        it('(2^50 + 2^40) permissive with mask 2^40', function () {
            expect(PermissionChecker.$compareMasks(Math.pow(2, 50) + Math.pow(2, 40), Math.pow(2, 40))).toBe(true);
        });

        it('(2^50 + 2^41) not permissive with mask 2^40', function () {
            expect(PermissionChecker.$compareMasks(Math.pow(2, 50) + Math.pow(2, 40), Math.pow(2, 41))).toBe(false);
        });

        it('8573157375 not permissive with mask 2^31', function () {
            expect(PermissionChecker.$compareMasks(8573157375, Math.pow(2, 31))).toBe(true);
        });
    });
});