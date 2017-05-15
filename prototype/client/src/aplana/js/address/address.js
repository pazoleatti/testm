/**
 * aplana-address
 * Компонент выбора даты
 * http://localhost:8080/#/aplana_address
 */
(function () {
    "use strict";

    angular.module('aplana.address', ['aplana.utils', 'aplana.modal', 'pascalprecht.translate', 'aplana.tooltip'])
    /**
     * Форматтер для даты, склеивает непустые поля через ","
     */
        .filter('addressFormatter', ['$filter', 'AplanaUtils', function ($filter, AplanaUtils) {
            return function (value) {
                var result = value ? $.grep([value.postcode, value.regionCode, value.area, value.city, value.locality,
                    value.street, value.house, value.building, value.flat], Boolean).join(", ") : '';

                // Вариант защита от XSS http://jira.aplana.com/browse/CBRSIPSGA-94
                /*result = AplanaUtils.sanitizeRecursively(result);

                // Этот блок кода ломает тесты - альтернатива библиотека https://github.com/mathiasbynens/he
                var elem = document.createElement('div');
                elem.innerHTML = result;
                result = elem.childNodes[0].nodeValue;*/

                return result;
            };
        }])
    /**
     * Ограничения полей адреса
     */
        .factory('AddressFieldConstraints', function () {
            var constraints = {
                postcode: {pattern: /^\d{6}$/},
                regionCode: {pattern: /^\d{2}$/},
                area: {maxlength: 150},
                city: {maxlength: 150},
                locality: {maxlength: 150},
                street: {maxlength: 150},
                house: {pattern: /^[-a-zA-Zа-яА-Я0-9/\s.]{1,20}$/},
                building: {pattern: /^[-a-zA-Zа-яА-Я0-9/\s.]{1,20}$/},
                flat: {pattern: /^[-a-zA-Zа-яА-Я0-9\s.]{1,20}$/}
            };

            return constraints;
        })
    /**
     * Дирректива выбора адреса
     */
        .directive('aplanaAddress', ['$filter', 'AplanaUtils', '$aplanaModal', 'AddressFieldConstraints',
            function ($filter, AplanaUtils, $aplanaModal, addressFieldConstraints) {
                return {
                    restrict: 'A',
                    scope: {
                        modelValue: '=ngModel',
                        addressRequired: '='
                    },
                    require: ['ngModel', '?^form'],
                    replace: true,
                    templateUrl: AplanaUtils.templatePath + 'address/address.html',
                    link: function (scope, element, attrs, controllers) {
                        var inputElement = element.find('input');

                        //вычленяем контроллеры модели и формы
                        scope.ngModelCtrl = controllers[0];
                        scope.ngFormCtrl = controllers[1];

                        //Если лежим на форме - удалим из ее контроллера вложенный инпут
                        // angular 1.4.7 приходит null вместо undefined
                        if (angular.isDefined(scope.ngFormCtrl) && scope.ngFormCtrl != null) {
                            scope.ngFormCtrl.$removeControl(inputElement.controller('ngModel'));
                        }

                        //ф-ия отрисовки компонента
                        scope.ngModelCtrl.$render = function () {
                            formatAddress();
                        };

                        //обработчки кнопки редактирования
                        scope.editValue = function () {
                            $aplanaModal.open(angular.extend({
                                    templateUrl: AplanaUtils.templatePath + 'address/addressModal.html',
                                    controller: 'AddressFormCtrl',
                                    title: 'Выбрать адрес',
                                    titleIcon: 'icon-cbr-windows-new_doc',
                                    windowClass: 'modal600',
                                    resolve: {
                                        $address: function () {
                                            return angular.copy(scope.modelValue);
                                        }
                                    }
                                }, scope.$eval(attrs.addressModalConfig) || {})
                                ).result.then(function (value) {
                                    scope.ngModelCtrl.$setViewValue(value);
                                    formatAddress();
                                });
                        };

                        //обработчик кнопки очистки
                        scope.clearValue = function () {
                            scope.ngModelCtrl.$setViewValue(null);
                            formatAddress();
                        };

                        //модель для инпута со строковым адресом
                        scope.formattedAddress = '';
                        var formatAddress = function () {
                            if (scope.ngModelCtrl.$viewValue) {
                                scope.formattedAddress = $filter('addressFormatter')(scope.ngModelCtrl.$viewValue);
                            } else {
                                scope.formattedAddress = "Адрес не введен";
                            }
                        };

                        //парсер. в данном случае viewValue - это то, что нам отдает модальное окно - объект с адресом
                        //его не нужно парсить, только отвалидировать
                        var addressParser = function (value) {
                            var valid = true;
                            valid &= validateRequired(value);
                            valid &= validateAddressFields(value);

                            return valid ? value : null;
                        };

                        //форматтер. форматировать значение нам не нужно, только обновить валидацию
                        var addressFormatter = function (value) {
                            validateRequired(value);
                            validateAddressFields(value);

                            return value;
                        };

                        //валидатор обязательности ввода
                        var validateRequired = function (value) {
                            var valid = !scope.addressRequired || !!value;
                            scope.ngModelCtrl.$setValidity('required', valid);

                            return valid;
                        };

                        //валидатор полей адреса
                        var validateAddressFields = function (value) {
                            var valid = !value || !!value.regionCode;

                            if (value) {
                                for (var key in addressFieldConstraints) {
                                    var constraints = addressFieldConstraints[key];
                                    var fieldValue = value[key];

                                    if (fieldValue) {
                                        if (constraints.pattern) {
                                            valid &= constraints.pattern.test(fieldValue);
                                        }
                                        if (constraints.maxlength) {
                                            valid &= fieldValue.length <= constraints.maxlength;
                                        }
                                    }
                                }
                            }

                            scope.ngModelCtrl.$setValidity('address', !!valid);

                            return valid;
                        };

                        scope.ngModelCtrl.$parsers.push(addressParser);
                        scope.ngModelCtrl.$formatters.unshift(addressFormatter);

                        scope.$watch('addressRequired', function () {
                            validateRequired(scope.ngModelCtrl.$viewValue);
                        });
                    }
                };
            }])
    /**
     * Контроллер модального окна ввода адреса
     */
        .controller('AddressFormCtrl', ['$scope', '$filter', '$modalInstance' , '$address', 'AddressFieldConstraints',
            function ($scope, $filter, $modalInstance, $address, addressFieldConstraints) {
                $scope.address = $address || {};

                //опубликуем ограничения полей
                $scope.constraints = addressFieldConstraints;

                $scope.formattedAddress = '';

                $scope.$watch('address', function (newValue, oldValue) {
                    $scope.formattedAddress = $filter('addressFormatter')(newValue);
                }, true);

                $scope.saveAddress = function () {
                    $modalInstance.close($scope.address);
                };

                $scope.cancelForm = function () {
                    $modalInstance.dismiss();
                };

                $scope.closeCallback = function () {
                    $scope.cancelForm();
                };
            }]);
}());

