(function () {
    'use strict';

    angular.module('app.refBookInterceptors')
        .factory('refBookInterceptorsIncomeKindConditionUtils', function () {
            /**
             * Сравнивает две даты
             * возвращает 0, если равны
             * возвращает 1, если date1 > date2
             * возвращает -1, если date1 < date2
             * @param date1
             * @param date2
             */
            function compareDate(date1, date2) {
                if (_.isNull(date1) || _.isUndefined(date1) || _.isNull(date2) || _.isUndefined(date2)) {
                    return -1;
                }
                var dateFmt = "YYYY-MM-DD";
                var moment1 = moment(date1, dateFmt);
                var moment2 = moment(date2, dateFmt);

                if (moment1 > moment2) {
                    return 1;
                } else if (moment1 < moment2) {
                    return -1;
                } else {
                    return 0;
                }
            }

            return {
                isError1: function (incomeKindStartDate, incomeTypeStartDate) {
                    //incomeKindStartDate < incomeTypeStartDate
                    return compareDate(incomeKindStartDate, incomeTypeStartDate) < 0;
                },
                isError2: function (incomeKindStartDate, incomeTypeEndDate) {
                    //incomeKindStartDate >= incomeTypeEndDate
                    return incomeTypeEndDate && compareDate(incomeKindStartDate, incomeTypeEndDate) >= 0;
                },
                isError3: function (incomeKindEndDate, incomeTypeEndDate) {
                    //incomeKindEndDate > incomeTypeEndDate
                    return incomeTypeEndDate && compareDate(incomeKindEndDate, incomeTypeEndDate) > 0;
                }
            };
        })
        .config(function ($provide) {

            $provide.decorator('$refBookInterceptors',
                function refBookInterceptorsDecorator($delegate, $q, $dialogs, APP_CONSTANTS,
                                                      IncomeTypeRecordResource,
                                                      refBookInterceptorsIncomeKindConditionUtils) {

                    var ERROR_1 = _.template('Дата начала актуальности Вида Дохода: <%= incomeKindStartDate %> должна быть больше или равна ' +
                        'Дате начала актуальности Кода Вида Дохода: <%= incomeTypeStartDate %>');

                    var ERROR_2 = _.template('Дата начала актуальности Вида Дохода: <%= incomeKindStartDate %> должна быть меньше ' +
                        'Даты окончания актуальности Кода Вида Дохода: <%= incomeTypeEndDate %>');

                    var ERROR_3 = _.template('Дата окончания актуальности Вида Дохода: <%= incomeKindEndDate %> должна быть меньше  ' +
                        'Даты окончания актуальности Кода Вида Дохода: <%= incomeTypeEndDate %>');

                    var ERROR_SEPARATOR = '<br>';


                    function condition(eventData) {

                        function formatDate(dt) {
                            return moment(dt, "YYYY-MM-DD").format('DD.MM.YYYY');
                        }

                        var incomeKind = eventData.record;
                        return IncomeTypeRecordResource.query({
                            id: incomeKind.INCOME_TYPE_ID.referenceObject.record_id.value
                        }).$promise.then(function (value) {

                            var incomeType = value.records > 0 ? value.rows[0] : null;

                            if (incomeType) {
                                var incomeKindStartDate = incomeKind.record_version_from && incomeKind.record_version_from.value;
                                var incomeKindEndDate = incomeKind.record_version_to && incomeKind.record_version_to.value;

                                var incomeTypeStartDate = incomeType.record_version_from && incomeType.record_version_from.value;
                                var incomeTypeEndDate = incomeType.record_version_to && incomeType.record_version_to.value;

                                var error;

                                if (refBookInterceptorsIncomeKindConditionUtils.isError1(incomeKindStartDate, incomeTypeStartDate)) {
                                    error = ERROR_1({
                                        incomeKindStartDate: formatDate(incomeKindStartDate),
                                        incomeTypeStartDate: formatDate(incomeTypeStartDate)
                                    });
                                }

                                if (refBookInterceptorsIncomeKindConditionUtils.isError2(incomeKindEndDate, incomeTypeEndDate)) {
                                    error = error + error ? ERROR_SEPARATOR : '';
                                    error = error + ERROR_2({
                                        incomeKindStartDate: formatDate(incomeKindStartDate),
                                        incomeTypeEndDate: formatDate(incomeTypeEndDate)
                                    });
                                }

                                if (refBookInterceptorsIncomeKindConditionUtils.isError3(incomeKindEndDate, incomeTypeEndDate)) {
                                    error = error + error ? ERROR_SEPARATOR : '';
                                    error = error || '' + ERROR_3({
                                        incomeKindEndDate: formatDate(incomeKindEndDate),
                                        incomeTypeEndDate: formatDate(incomeTypeEndDate)
                                    });
                                }

                                if (error) {
                                    return $q.reject(error);
                                }
                                return $q.resolve();
                            }

                            return $q.reject();
                        });
                    }

                    $delegate.subscribe('beforeSaveRecord', function (eventData) {
                        if (eventData.$shareData.refBook.tableName !== "REF_BOOK_INCOME_KIND") {
                            return $q.resolve();
                        }

                        return condition(eventData)
                            .then(function () {
                                return $q.resolve();
                            }, function (error) {
                                var reason = 'Невозможно создать Вид дохода: ' + ERROR_SEPARATOR + error;
                                return $dialogs.messageDialog({
                                    content: reason
                                }).result
                                    .then(function () {
                                        return $q.reject(reason);
                                    });
                            });

                    });
                    return $delegate;
                }
            );
        });

})();
