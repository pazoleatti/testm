(function () {
    'use strict'

    angular
        .module('sbrfNdfl.Constants', [])

        .constant('APP_CONSTANTS', {
            COMMON: {
                PAGING_SIZES:[10, 20, 50, 100, 200, 500]
            },
            CONSTRAINTS: {
                MAX_STRING_LENGTH: 4000,
                MAX_NUMBER: 1000000000000000000,
                MIN_STRING_LENGTH: 0,
                MIN_NUMBER: 0
            }
        })
}());