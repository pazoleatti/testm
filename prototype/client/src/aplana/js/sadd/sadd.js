/**
 * $sadd (сервис для интеграции с ActiveX API САДД (Дело))
 * http://localhost:8080/#/aplana_sad
 */
(function () {
    'use strict';

    angular.module("aplana.sadd", ['aplana.utils'])
        .factory('$sadd', ['$alertService', '$filter', function ($alertService, $filter) {
            var service = {};
            var head = null;
            try {
                head = new ActiveXObject("eapi.head");
            } catch (e) {
                $alertService.error('ActiveX компонент САДД не найден!');
            }

            /**
             * Преобразует js массив в массив пригодный для передачи в activeX API (VBArray)
             */
            var getSafeArray = function (jsArr) {
                var dict = new ActiveXObject("Scripting.Dictionary");
                for (var i = 0; i < jsArr.length; i++) {
                    dict.add(i, jsArr[i]);
                }
                return dict.Items();
            };

            /**
             * Подключение к САДД
             */
            var connect = function() {
                head.Open();
                //head.OpenWithParams("FAD", "FAD");
                if (head.ErrCode !== 0) {
                    $alertService.error('Ошибка соединения с САДД. Код ошибки: ' + head.ErrCode + '. Причина: ' + head.ErrText);
                }
            };

            /**
             * Инициализирует подключение к САДД, если оно еще не установлено
             */
            service.init = function() {
                if (head.active !== true) {
                    //Если соединение не установлено, то подключаемся
                    connect();
                }
            };

            /**
             * Отключение от САДД
             */
            service.disconnect = function() {
                head.Close();
                if (head.Active) {
                    $alertService.error('Не удалось отключиться от САДД. Код ошибки: ' + head.ErrCode + '. Причина: ' + head.ErrText);
                }
            };

            /**
             * Возвращает значения указанных справочников в виде списка объектов {id: значение idAlias, name: значение nameAlias} для каждого справочника
             * @param references набор справочников, значения которых надо получить. Представляет из себя массив объектов вида:
             *       reference = {
             *          name: "",
             *          alias: "",
             *          itemType: undefined,
             *          filter: "",
             *          fields: {
             *              id: "ISN",
             *              name: "NAME"
             *          }
             *      }
             *      где,
             *      name - имя справочника. Например DocGroup
             *      alias - (опционально) уникальное имя справочника для идентификации в группе. Например employees (берутся из Department). По-умолчанию = name
             *      itemType - (опционально) ограничение по типу возвращаемых записей. По умолчанию - пусто
             *      filter - (опционально) функция-фильтр для отбора записей
             *      fields - (опционально) набор полей у выходного объекта. По умолчанию - {id: "ISN", name: "NAME"}
             */
            service.getReferenceValues = function(references){
                if (head.active !== true) {
                    //Если соединение отпало, то снова подключаемся
                    connect();
                }
                var referenceValues = {};
                references.forEach(function(reference) {
                    if (typeof reference.name === 'undefined') {
                        throw new Error('Не указано обязательное поле name в конфигурации справочника');
                    }
                    if (typeof reference.alias === 'undefined') {
                        reference.alias = reference.name;
                    }
                    if (typeof reference.fields === 'undefined') {
                        //Если поля возвращаемого объекта не указаны, то берем по-умолчанию
                        reference.fields = {
                            id: "ISN",
                            name: "NAME"
                        };
                    } else {
                        //Если указаны, то проверяем есть ли кастомные значения id и name. Если нет - берем дефолтные
                        if (!reference.fields.hasOwnProperty("id")) {
                            reference.fields["id"] = "ISN";
                        } if (!reference.fields.hasOwnProperty("name")) {
                            reference.fields["name"] = "NAME";
                        }
                    }
                    var values = [];

                    var rs = head.GetResultSet();
                    //Ищем в справочниках
                    rs.Source = head.GetCriterion("Vocabulary");
                    //Выбраем конкретный справочник
                    rs.Source.Vocabulary = reference.name;
                    if (typeof reference.itemType !== 'undefined') {
                        //Добавляем ограничение по типу возвращаемых записей
                        rs.Source.ItemType = reference.itemType;
                    }

                    //Получаем данные
                    rs.Fill();
                    if (rs.ErrCode !== 0) {
                        $alertService.error('Ошибка чтения данных из САДД. Код ошибки: ' + rs.ErrCode + '. Причина: ' + rs.ErrText);
                    }
                    for (var i = 0; i < rs.ItemCnt; i++) {
                        var item = rs.Item(i);
                        var row = {};
                        //Применяем фильтр для отбора записей по условию
                        if (typeof reference.filter === 'undefined' || reference.filter(item)) {
                            for (var prop in reference.fields) {
                                //Перебираем поля, которые надо получить и заполняем их значениями
                                if (reference.fields.hasOwnProperty(prop)) {
                                    row[prop] = item[reference.fields[prop]];
                                }
                            }
                            values.push(row);
                        }
                    }
                    referenceValues[reference.alias] = values;
                    rs.Clear();
                });
                return referenceValues;
            };

            /**
             * Создает РКПД в САДД (ДЕЛО)
             * @param rkpd объект РКПД из СИП СГА
             * @returns регистрационный номер РКПД из САДД
             */
            service.createRKPD = function(rkpd){
                if (head.active !== true) {
                    //Если соединение отпало, то снова подключаемся
                    connect();
                }
                if (typeof head.UserInfo.DepDL.DCode === 'undefined') {
                    $alertService.error('Текущий пользователь не ассоциирован с каким либо подразделением и не может быть указан как исполнитель для РКПД!');
                    return;
                }
                //РКПД
                var rcpd = new ActiveXObject("Scripting.Dictionary");
                rcpd.add("object", "PRJ_RC");
                //Код группы документов, в соответствие со справочником "Группы документов" (САДД)
                rcpd.add("aDueDocgroup", rkpd.docGroupCode);
                //Регистрационный номер
                rcpd.add("aFreeNum", null);
                //Дата регистрации проекта документа в формате YYYYMMDD
                rcpd.add("aPrjDate", $filter('date')(rkpd.creationDate,'yyyyMMdd'));
                //Идентификатор грифа доступа (САДД)
                rcpd.add("aSecurlevel", rkpd.securityLevelId);
                //Состав
                rcpd.add("aConsists", rkpd.consists);
                //Плановая  дата исполнения проекта в формате YYYYMMDD
                rcpd.add("aPlanDate", $filter('date')(rkpd.planDate,'yyyyMMdd'));
                //Содержание
                rcpd.add("aAnnotat", rkpd.annotation);
                //Примечание
                rcpd.add("aNote", rkpd.note);
                //Код due первого исполнителя  проекта, в соответствие со справочником "Подразделения" (САДД)
                //Фактически сейчас выбираем пользователя инициирующего создание РКПД
                rcpd.add("aDuePersonExe", head.UserInfo.DepDL.DCode);
                //Идентификатор связанной РК
                rcpd.add("aIsnLinkingDoc", null);
                //Идентификатор текущего поручения связанной РК
                rcpd.add("aIsnLinkingRes", null);
                //Идентификатор типа связки, в соответствие со справочником типов связок (САДД)
                rcpd.add("aIsnClLink", null);
                //Маска для копирования реквизитов из aIsnLinkingDoc
                rcpd.add("acopy_shablon", null);
                //Флаг "Оригинал в электронном виде"
                rcpd.add("aEDocument", null);

                //Группа параметров задания реквизитов адресатов РКПД
                var refSend = new ActiveXObject("Scripting.Dictionary");
                refSend.add("object", "REF_SEND");

                /**
                 Название справочника, из которого добавляются адресаты
                 'organiz' - Список организаций,
                 'citizen' - Граждане,
                 'department' - Подразделения
                 */
                //TODO: (dloshkarev) надо лепить отдельную структуру для хранения и отображения адресатов + форму. Пока пустые
                refSend.add("aClassif", "department");
                //Список кодов адресатов. Разделитель "|".
                refSend.add("Codes", null);
                //refSend.add("Codes", formatAddresses());
                //Список идентификаторов контактов к добавляемым адресатам. Разделитель "|".
                refSend.add("aIsnsContact", null);

                //Группа параметров задания виз и подписей РКПД
                var prjVisaSign = new ActiveXObject("Scripting.Dictionary");
                //TODO: (dloshkarev) надо лепить отдельную структуру для хранения и отображения визирующих/подписантов. Пока статика
                prjVisaSign.add("object", "PRJ_VISA_SIGN");
                //Вид добавляемых записей (1-подпись, 2-виза, null)
                prjVisaSign.add("aKind", 2);
                //Список  идентификаторов должностных лиц, в соответствие со справочником "Подразделения"
                prjVisaSign.add("as_rep_isns", "3636");

                //Группа параметров задания рубрик РКПД
                var refRubric = new ActiveXObject("Scripting.Dictionary");
                refRubric.add("object", "REF_RUBRIC");
                //Список кодов due, в соответствие со справочником "Рубрикатор"
                refRubric.add("codes", "DEFAULT");

                //Группа параметров задания рубрик РКПД
                var ar = new ActiveXObject("Scripting.Dictionary");
                ar.add("object", "AR_RC_VALUE");

                //Выполняем процедуру создания РКПД в САДД
                var args = getSafeArray([rcpd, refSend, prjVisaSign, refRubric, ar]);
                head.ExecuteProcEx("add_prj_ex", args);
                if (head.ErrCode === 0) {
                    return rcpd.Item("aIsn");
                } else {
                    $alertService.error('Ошибка записи данных в САДД. Код ошибки: ' + head.ErrCode + '. Причина: ' + head.ErrText);
                }
            };

            /**
             * Удаляет РКПД из САДД по ее регистрационному номеру
             * @param isn идентификатор РКПД в САДД
             */
            service.deleteRKPD = function(isn){
                if (head.active !== true) {
                    //Если соединение отпало, то снова подключаемся
                    connect();
                }
                var proc = head.GETPROC("del_prj");
                proc.Parameters.Append(proc.CreateParameter("aIsnBatch", 3, 1, 0, isn));
                proc.Parameters.Append(proc.CreateParameter("aRetNumber", 3, 1, 0, 1));
                proc.Parameters.Append(proc.CreateParameter("aIsnPrj", 3, 1, 0, isn));
                head.ExecuteProc(proc);
                if (head.ErrCode !== 0) {
                    $alertService.error('Ошибка удаления РКПД из САДД: ' + head.ErrCode + '. Причина: ' + head.ErrText);
                }
            };

            /**
             * Возвращает РКПД из САДД по ее идентификатору
             * @param isn идентификатор РКПД в САДД
             * @returns объект РКПД
             */
            service.getRkpd = function(isn) {
                if (head.active !== true) {
                    //Если соединение отпало, то снова подключаемся
                    connect();
                }
                var rkpd = head.GetRow("RcPrj", isn);
                if (head.ErrCode !== 0) {
                    $alertService.error('Ошибка получения РКПД из САДД: ' + head.ErrCode + '. Причина: ' + head.ErrText);
                    return;
                }
                return rkpd;
            };

            return service;
        }]);
}());

