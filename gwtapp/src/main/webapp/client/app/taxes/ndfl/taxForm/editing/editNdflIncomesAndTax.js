(function () {
    'use strict';

    /**
     * @description Модуль для редактирования строки КНФ в разделе 2 (Сведения о доходах и НДФЛ)
     */
    angular.module('app.editNdflIncomesAndTax', ['ui.router', 'app.rest', 'app.formatters'])

    /**
     * @description Контроллер диалогового окна
     */
        .controller('editNdflIncomesAndTaxFormCtrl', ["$scope", "$rootScope", "$http", '$state', '$stateParams',
            "$modalInstance", '$logPanel', "$filter", "APP_CONSTANTS", '$shareData', '$dialogs', 'ndflIncomesAndTax',
            function ($scope, $rootScope, $http, $state, $stateParams, $modalInstance, $logPanel, $filter, APP_CONSTANTS, $shareData, $dialogs, ndflIncomesAndTax) {

                // Формат числа /20.2/
                $scope.patternNumber20_2 = /^[+-]?([0-9]{1,20})([.][0-9]{1,2})?$/;
                // Форматы целых чисел с ограничением по числу цифр
                $scope.patternNumber20 = /^[+-]?[0-9]{1,20}$/;
                $scope.patternNumber15 = /^[+-]?[0-9]{1,15}$/;

                // Инициализация чисто для удобства работы с полями в коде
                $scope.row = {
                    kpp: null,
                    oktmo: null,
                    incomeCode: null,
                    incomeAccruedDate: null,
                    incomeAccruedSumm: null,
                    incomeType: null,
                    incomePayoutDate: null,
                    incomePayoutSumm: null,
                    taxBase: null,
                    totalDeductionsSumm: null,
                    taxRate: null,
                    calculatedTax: null,
                    withholdingTax: null,
                    taxDate: null,
                    notHoldingTax: null,
                    overholdingTax: null,
                    refoundTax: null,
                    taxTransferDate: null,
                    paymentDate: null,
                    taxSumm: null,
                    paymentNumber: null,
                    disableTaxTransferDate: false
                };
                $scope.temp = {};

                // Получение данных ФЛ из раздела 1
                $http({
                    method: "GET",
                    url: "controller/rest/ndflPerson/" + $shareData.row.ndflPersonId
                }).success(function (person) {
                    $scope.row = $shareData.row;
                    $scope.temp.person = person;
                    if ($scope.row.taxTransferDate === APP_CONSTANTS.DATE_ZERO.AS_DATE) {
                        $scope.row.disableTaxTransferDate = true;
                        $scope.row.taxTransferDate = null;
                    }
                    if (person.idDocType) {
                        $http({
                            method: "GET",
                            url: "controller/rest/getPersonDocTypeName/" + person.idDocType
                        }).success(function (docTypeName) {
                            $scope.temp.docTypeName = docTypeName;
                        });
                    }
                    // Получение данных ОКТМО для установки значения в выпадашку
                    $http({
                        method: "GET",
                        url: "controller/rest/refBookValues/oktmoByCode",
                        params: {
                            code: $scope.row.oktmo
                        }
                    }).success(function (oktmo) {
                        if (oktmo) {
                            $scope.temp.oktmo = oktmo;
                        } else {
                            // Если запись не найдена - подставляем текст для кода, чтобы он отобразился в выпадашке
                            $scope.temp.oktmo = {code: $scope.row.oktmo, name: "-"};
                        }
                    });
                });

                /**
                 * Сохранение
                 */
                $scope.save = function () {
                    // Подставляем некоторые значения из форм заполнения в модель строки
                    $scope.row.oktmo = $scope.temp.oktmo.code;
                    if ($scope.row.disableTaxTransferDate) {
                        $scope.row.taxTransferDate = $filter('translate')('title.taxTransferDateZeroDate');
                    }

                    // Если валидация не пройдена - окно об ошибке, завершение.
                    var validationErrorMsg = $scope.validateRow();
                    if (validationErrorMsg) {
                        $dialogs.warningDialog({
                            content: validationErrorMsg
                        });
                        return;
                    }

                    ndflIncomesAndTax.update({declarationDataId: $shareData.declarationId}, $scope.row,
                        function (result) {
                            if (result && result.uuid) {
                                $logPanel.open('log-panel-container', result.uuid);
                            }
                            $modalInstance.close(true);
                        }
                    );
                };

                /**
                 * Валидация заполнения строки.
                 * @return {string} сообщение об ошибке валидации
                 */
                $scope.validateRow = function () {
                    // Определение типа строки. Если его нет, валидация провалена
                    var isRowAccrual = $scope.isRowOfType('typeAccrual');
                    var isRowPayout = $scope.isRowOfType('typePayout');
                    var isRowTransfer = $scope.isRowOfType('typeTransfer');

                    var isRowOfAnyType = isRowAccrual || isRowPayout || isRowTransfer;
                    if (!isRowOfAnyType) {
                        var typeFieldsTitles = $scope.fields
                            .filter(function (field) {
                                return field['typeAccrual'] || field['typePayout'] || field['typeTransfer'];
                            })
                            .map(function (field) {
                                return field.title;
                            })
                            .join(', ');

                        return 'ВНИМАНИЕ! Хотя бы одно из полей ' + typeFieldsTitles + ' должно быть заполнено';
                    }

                    // Перебираем поля строки, формируем список незаполненных обязательных. Если в него что-то попадет - валидация не пройдена.
                    var notFilledRequiredFields = [];
                    // Список заполненных полей определения типа. Нужен для вывода сообщения о провале валидации.
                    var filledTypeFields = [];
                    $scope.fields.forEach(function (field) {
                        if ($scope.isFieldFilled(field.name)) {
                            var isTypeField = field['typeAccrual'] || field['typePayout'] || field['typeTransfer'];
                            if (isTypeField) {
                                filledTypeFields.push(field.title);
                            }
                        } else {
                            var isFieldRequired = (isRowAccrual && field.requiredAccrual)
                                || (isRowPayout && field.requiredPayout)
                                || (isRowTransfer && field.requiredTransfer);
                            if (isFieldRequired) {
                                notFilledRequiredFields.push(field.title);
                            }
                        }
                    });
                    if (notFilledRequiredFields.length > 0) {
                        return 'Поскольку заполнены поля (' + filledTypeFields.join(', ')
                            + '), также необходимо заполнить поля: ' + notFilledRequiredFields.join(', ');
                    }
                };

                /**
                 * Массив описания полей строки, используется для валидации их заполнения
                 * @type {Object[]}
                 */
                $scope.fields = [{
                    name: 'kpp', title: 'КПП', requiredAccrual: true, requiredPayout: true, requiredTransfer: true
                }, {
                    name: 'oktmo', title: 'ОКТМО', requiredAccrual: true, requiredPayout: true, requiredTransfer: true
                }, {
                    name: 'incomeCode', title: 'Код дохода', requiredAccrual: true, requiredPayout: true
                }, {
                    name: 'incomeType', title: 'Признак дохода', requiredAccrual: true, requiredPayout: true
                }, {
                    name: 'incomeAccruedDate', title: 'Дата начисления', typeAccrual: true, requiredAccrual: true
                }, {
                    name: 'incomePayoutDate', title: 'Дата выплаты', typePayout: true, requiredPayout: true
                }, {
                    name: 'incomeAccruedSumm', title: 'Начислено', typeAccrual: true, requiredAccrual: true
                }, {
                    name: 'incomePayoutSumm', title: 'Выплачено', typePayout: true, requiredPayout: true
                }, {
                    name: 'totalDeductionsSumm', title: 'Сумма вычета'
                }, {
                    name: 'taxBase', title: 'Налоговая база', requiredAccrual: true
                }, {
                    name: 'taxRate', title: 'Процентная ставка, (%)', requiredAccrual: true, requiredPayout: true
                }, {
                    name: 'taxDate', title: 'Дата расчета', requiredAccrual: true, requiredPayout: true
                }, {
                    name: 'calculatedTax', title: 'Исчисленный налог', requiredAccrual: true
                }, {
                    name: 'withholdingTax', title: 'Удержанный налог', requiredPayout: true
                }, {
                    name: 'notHoldingTax', title: 'Не удержанный налог'
                }, {
                    name: 'overholdingTax', title: 'Излишне удержанный налог'
                }, {
                    name: 'refoundTax', title: 'Возвращенный налог'
                }, {
                    name: 'taxTransferDate', title: 'Срок перечисления', requiredPayout: true, requiredTransfer: true
                }, {
                    name: 'paymentDate', title: 'Дата ПП', typeTransfer: true, requiredTransfer: true
                }, {
                    name: 'taxSumm', title: 'Сумма ПП', typeTransfer: true, requiredTransfer: true
                }, {
                    name: 'paymentNumber', title: 'Номер ПП', typeTransfer: true, requiredTransfer: true
                }];

                /**
                 * Определение типа поля
                 * @param {string} type название параметра, определяющего тип поля, в $scope.fields
                 */
                $scope.isRowOfType = function (type) {
                    return $scope.fields
                        .filter(function (field) { // отбираем поля только выбранного типа
                            return field[type]
                        })
                        .reduce(function (anyFilled, field) { // проверяем их заполненность
                            return anyFilled || $scope.isFieldFilled(field.name);
                        }, false);
                };

                /**
                 * Определить, заполнено ли поле строки по его названию
                 * @param {string} fieldName название поля
                 * @return {boolean} поле с таким названием имеется и заполнено.
                 */
                $scope.isFieldFilled = function (fieldName) {
                    return ($scope.row.hasOwnProperty(fieldName) && $scope.row[fieldName]);
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('incomesAndTax.edit.cancel.header'),
                        content: $filter('translate')('incomesAndTax.edit.cancel.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.close(false);
                        }
                    });
                };
            }]);
}());
