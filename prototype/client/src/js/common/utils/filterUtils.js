(function () {
        "use strict";
        // Вспомогательные утилиты для фильтров в журналах
        angular.module('ndfl.filterUtils', [])
            .factory('filterUtils', function () {
                return {
                    /**
                     * Заполняет filter множеством условий вхождения каждого отдельного слова из параметра value в объект property
                     *
                     * Пример применения:
                     *   filterUtils.getSurnameWithInitials("user.surnameWithInitials", $scope.filter.fullName, searchFilter);
                     *   Заполнит searchFilter условиями вхождения в user.fullName каждого отдельного слова из строки $scope.filter.fullName
                     *
                     */
                    getSurnameWithInitials: function (value, filter) {
                        if (!_.isUndefined(value) && !_.isNull(value) && value !== "") {
                            var fullNameAsArray = value.split(" ");
                            var surnameWithInitials = fullNameAsArray[0] + " " + fullNameAsArray[1].charAt(0) + "." + fullNameAsArray[2].charAt(0) + ".";
                            filter.push({property: "surnameWithInitials", operation: "STRING_CONTAINS_IC", value: surnameWithInitials});
                        }
                    }
                };
            });
    }()
)
;

