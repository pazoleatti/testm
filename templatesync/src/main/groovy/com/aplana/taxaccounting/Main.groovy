package com.aplana.taxaccounting

import groovy.sql.Sql

/**
 * Утилита сравнения скриптов из git и БД с учетом версионирования.
 * Если скрипты в БД не актуальны, то они обновляются.
 *
 * Запуск командой gradle:
 * gradle run
 *
 * Сборка исполнимого приложения:
 * gradle installApp
 *
 * @author Dmitriy Levykin
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 */
class Main {
    // Параметры подключения к БД
    def static DB_URL = 'jdbc:oracle:thin:@//172.16.127.16:1521/ORCL.APLANA.LOCAL'
    def static DB_USER = 'TAX_0_8'
    def static DB_PASSWORD = 'TAX'
    // checkOnly, true — только сравнение, false — сравнение и обновление Git → БД
    def static checkOnlyFD = true // для налоговых форм
    def static checkOnlyDD = true // для деклараций

    // Схема для сравнения макетов, null если сравнение не требуется
    def static DB_USER_COMPARE = 'TAX_0_7'

    // Путь к папке с шаблонами
    def static SRC_FOLDER_PATH = '../src/main/resources/form_template'
    def static SRC_REFBOOK_PATH = '../src/main/resources/refbook'
    def static TAX_FOLDERS = ['deal'     : 'МУКС',
                              'income'   : 'Налог на прибыль',
                              'vat'      : 'НДС',
                              'transport': 'Транспортный налог',
                              'property' : 'Налог на имущество',
                              'etr'      : 'Эффективная налоговая ставка']

    // Названия файлов отчетов
    def static REPORT_GIT_NAME = 'report_git_db_compare.html'
    def static REPORT_DECL_GIT_NAME = 'report_decl_git_db_compare.html'
    def static REPORT_REFBOOK_GIT_NAME = 'report_refbook_git_db_compare.html'
    def static REPORT_DB_NAME = 'report_db_compare.html'
    def static REPORT_DECL_DB_NAME = 'report_decl_db_compare.html'
    def static REPORT_REFBOOK_DB_NAME = 'report_refbook_db_compare.html'
    def static REPORT_TYPE_DB_NAME = 'report_form_decl_type_db_compare.html'

    // Общие стили отчетов
    def static HTML_STYLE = '''
                        table.rt {
                            font-family: verdana,helvetica,arial,sans-serif;
                            font-size: 13px;
                            background-color: #FBFEFF;
                            width: 100%;
                            border-collapse: collapse;
                        }
                        .rt td, .rt th {
                            padding: 1px 3px 1px 3px;
                            border: 1px solid #E2E5E6;
                        }
                        .rt tr:hover {
                            background-color: #E2E5E6;
                        }
                        .rt th {
                            background-color: #0C183D;
                            color: white;
                        }
                        .td_ok {
                            color: #009900;
                            font-weight: bold;
                        }
                        .td_error {
                           color: #FF0000;
                           cursor: pointer;
                           background-color: #FFFF7F;
                        }
                        .td_gr {
                            cursor: pointer;
                        }
                        .er td, .td_gr {
                            color: #BEBEBE;
                        }
                        .hdr, .hdrh {
                            color: #0C183D;
                            font-weight: bold;
                            text-align: center;
                        }
                        .hdr {
                            background-color: white;
                            padding: 10px;
                        }
                        .hdrh {
                             font-size: 13px;
                        }
                        .dlg {
                             display: none;
                        }
                        '''

    // Имя папки → FORM_TYPE.ID
    // Нужно перечислить все папки, иначе будет ошибка
    // Для деклараций id указаны с минусом
    def static TEMPLATE_NAME_TO_TYPE_ID = [
            'deal'     : [
                    'app_4_1'                : 801, // Приложение 4.1. (6 месяцев) Отчет в отношении доходов и расходов Банка по сделкам с российскими ВЗЛ, применяющими Общий режим налогообложения
                    'app_4_1_9'              : 802, // Приложение 4.1. (9 месяцев) Отчет в отношении доходов и расходов Банка по сделкам с российскими ВЗЛ, применяющими Общий режим налогообложения
                    'app_4_2'                : 803, // Приложение 4.2. Отчет в отношении доходов и расходов Банка по сделкам с ВЗЛ, РОЗ, НЛ по итогам окончания Налогового периода
                    'app_6_2'                : 804, // 6.2. Размещение средств на межбанковском рынке
                    'app_6_6'                : 806, // 6.6. Заключение сделок РЕПО
                    'app_6_7'                : 805, // 6.7. Предоставление права пользования товарным знаком
                    'auctions_property'      : 380, // Приобретение услуг по организации и проведению торгов по реализации имущества
                    'bank_service'           : 382, // Оказание банковских услуг
                    'bank_service_income'    : 398, // Оказание услуг (доходы)
                    'bank_service_outcome'   : 399, // Оказание услуг (расходы)
                    'bonds_trade'            : 384, // Реализация и приобретение ценных бумаг
                    'corporate_credit'       : 387, // Предоставление корпоративного кредита
                    'credit_contract'        : 385, // Уступка прав требования по кредитным договорам
                    'foreign_currency'       : 390, // Купля-продажа иностранной валюты
                    'forward_contracts'      : 391, // Поставочные срочные сделки, базисным активом которых является иностранная валюта
                    'guarantees'             : 388, // Предоставление гарантий
                    'guarantees_involvement' : 401, // Привлечение гарантий
                    'interbank_credits'      : 389, // Предоставление межбанковских кредитов
                    'letter_of_credit'       : 386, // Предоставление инструментов торгового финансирования и непокрытых аккредитивов
                    'nondeliverable'         : 392, // Беспоставочные срочные сделки
                    'notification'           : -6,
                    'precious_metals_deliver': 393, // Поставочные срочные сделки с драгоценными металлами
                    'precious_metals_trade'  : 394, // Купля-продажа драгоценных металлов
                    'related_persons'        : 800, // Взаимозависимые лица
                    'rent_provision'         : 376, // Предоставление нежилых помещений в аренду
                    'repo'                   : 383, // Сделки РЕПО
                    'rights_acquisition'     : 404, // Приобретение прав требования
                    'securities'             : 381, // Приобретение и реализация ценных бумаг (долей в уставном капитале)
                    'software_development'   : 375, // Разработка, внедрение, поддержка и модификация программного обеспечения, приобретение лицензий
                    'summary'                : 409, // Сводный отчет
                    'take_corporate_credit'  : 397, // Привлечение ресурсов
                    'take_interbank_credit'  : 402, // Привлечение средств на межбанковском рынке
                    'take_itf'               : 403, // Привлечение ИТФ и аккредитивов
                    'tech_service'           : 377, // Техническое обслуживание нежилых помещений
                    'trademark'              : 379  // Предоставление права пользования товарным знаком
            ],
            'income'   : [
                    'advanceDistribution'   : 500, // Расчет распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации
                    'app2'                  : 415, // Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов
                    'app2_src_1'            : 418, // Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов (ЦФО НДФЛ)
                    'app5'                  : 372, // (Приложение 5) Сведения для расчета налога на прибыль
                    'declaration_bank'      : -2,
                    'declaration_bank_1'    : -9,
                    'declaration_bank_2'    : -11,
                    'declaration_op'        : -5,
                    'declaration_op_1'      : -10,
                    'declaration_op_2'      : -19,
                    'f7_8'                  : 362, // (Ф 7.8) Реестр совершенных операций с ценными бумагами по продаже и погашению, а также по открытию-закрытию короткой позиции
                    'f7_8_1'                : 363, // (Ф 7.8) Реестр совершенных операций с ценными бумагами по продаже и погашению, а также по открытию-закрытию короткой позиции (с 9 месяцев 2015)
                    'income_complex'        : 302, // Сводная форма начисленных доходов (доходы сложные)
                    'income_simple'         : 301, // Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)
                    'income_simple_1'       : 305, // Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые) (с полугодия 2015)
                    'incomeWithHoldingAgent': 10070, // Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов)
                    'income_agent_1'        : 314, // Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов) (с 9 месяцев 2015)
                    'outcome_complex'       : 303, // Сводная форма начисленных расходов (расходы сложные)
                    'outcome_simple'        : 304, // Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)
                    'outcome_simple_1'      : 310, // Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые) (с полугодия 2015)
                    'outcome_simple_gosb'   : 327, // Расходы, учитываемые в простых РНУ (расходы простые) (с полугодия 2015)
                    'output1'               : 306, // Сведения для расчета налога с доходов в виде дивидендов
                    'output1_1'             : 411, // Сведения для расчета налога с доходов в виде дивидендов (new)
                    'output1_2'             : 414, // Сведения для расчета налога с доходов в виде дивидендов (начиная с год 2014)
                    'output2'               : 307, // Расчет налога на прибыль с доходов, удерживаемого налоговым агентом
                    'output2_1'             : 413, // Сведения о дивидендах, выплаченных в отчетном квартале (new)
                    'output2_2'             : 416, // Сведения о дивидендах, выплаченных в отчетном квартале (начиная с год 2014)
                    'output3'               : 308, // Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика
                    'output3_1'             : 412, // Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика (new)
                    'output4'               : 417, // Сведения о суммах налога на прибыль, уплаченного Банком за рубежом
                    'output4_1'             : 421, // Сведения о суммах налога на прибыль, уплаченного Банком за рубежом (new)
                    'output5'               : 420, // Сведения о уплаченных суммах налога по операциям с ГЦБ
                    'remainsPrepayments'    : 309, // Остатки по начисленным авансовым платежам
                    'reserve'               : 614, // (РСД) Расчет резерва по сомнительным долгам в целях налогообложения
                    'reserve_debts'         : 618, // Сводный регистр налогового учета по формированию и использованию резерва по сомнительным долгам
                    'resident_taxpayers'    : 319, // Расчет налога и облагаемой суммы дивидендов по акциям налогоплательщиков-резидентов
                    'rnu4'                  : 316, // (РНУ-4) Простой регистр налогового учета "доходы"
                    'rnu5'                  : 317, // (РНУ-5) Простой регистр налогового учета "расходы"
                    'rnu6'                  : 318, // (РНУ-6) Справка бухгалтера для отражения доходов, учитываемых в РНУ-4, учет которых требует применения метода начисления
                    'rnu7'                  : 311, // (РНУ-7) Справка бухгалтера для отражения расходов, учитываемых в РНУ-5, учет которых требует применения метода начисления
                    'rnu8'                  : 320, // (РНУ-8) Простой регистр налогового учета "Требования"
                    'rnu12'                 : 364, // (РНУ-12) Регистр налогового учета расходов по хозяйственным операциям и оказанным Банку услугам
                    'rnu14'                 : 321, // (РНУ-14) Регистр налогового учета нормируемых расходов
                    'rnu16'                 : 499, // (РНУ-16) Регистр налогового учета доходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС, в соответствии с учетной политикой для целей налогообложения ОАО "Сбербанк России"
                    'rnu16_1'               : 505, // (РНУ-16) Регистр налогового учета доходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС, в соответствии с учетной политикой для целей налогообложения ПАО "Сбербанк России" (с 9 месяцев 2015)
                    'rnu17'                 : 501, // (РНУ-17) Регистр налогового учета расходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС, в соответствии с учетной политикой для целей налогообложения ОАО "Сбербанк России"
                    'rnu17_1'               : 506, // (РНУ-17) Регистр налогового учета расходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС, в соответствии с учетной политикой для целей налогообложения ПАО "Сбербанк России" (с 9 месяцев 2015)
                    'rnu22'                 : 322, // (РНУ-22) Регистр налогового учета периодически взимаемых комиссий по операциям кредитования
                    'rnu23'                 : 323, // (РНУ-23) Регистр налогового учета доходов по выданным гарантиям
                    'rnu25'                 : 324, // (РНУ-25) Регистр налогового учета расчета резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения
                    'rnu26'                 : 325, // (РНУ-26) Регистр налогового учета расчета резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения
                    'rnu27'                 : 326, // (РНУ-27) Регистр налогового учета расчета резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
                    'rnu30'                 : 329, // (РНУ-30) Расчет резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов.
                    'rnu31'                 : 328, // (РНУ-31) Регистр налогового учета процентного дохода по купонным облигациям
                    'rnu32_1'               : 330, // (РНУ-32.1) Регистр налогового учета начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчет 1
                    'rnu32_2'               : 331, // (РНУ-32.2) Регистр налогового учета начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчет 2
                    'rnu33'                 : 332, // (РНУ-33) Регистр налогового учета процентного дохода и финансового результата от реализации (выбытия) ГКО
                    'rnu36_1'               : 333, // (РНУ-36.1) Регистр налогового учета начисленного процентного дохода по ГКО. Отчет 1
                    'rnu36_2'               : 315, // (РНУ-36.2) Регистр налогового учета начисленного процентного дохода по ГКО. Отчет 2
                    'rnu38_1'               : 334, // (РНУ-38.1) Регистр налогового учета начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчет 1
                    'rnu38_2'               : 335, // (РНУ-38.2) Регистр налогового учета начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчет 2
                    'rnu39_1'               : 336, // (РНУ-39.1) Регистр налогового учета процентного дохода по коротким позициям. Отчет 1(месячный)
                    'rnu39_2'               : 337, // (РНУ-39.2) Регистр налогового учета процентного дохода по коротким позициям. Отчет 2(квартальный)
                    'rnu40_1'               : 338, // (РНУ-40.1) Регистр налогового учета начисленного процентного дохода по прочим дисконтным облигациям. Отчет 1
                    'rnu40_2'               : 339, // (РНУ-40.2) Регистр налогового учета начисленного процентного дохода по прочим дисконтным облигациям. Отчет 2
                    'rnu44'                 : 340, // (РНУ-44) Регистр налогового учета доходов, в виде восстановленной амортизационной премии при реализации ранее, чем по истечении 5 лет с даты ввода в эксплуатацию Взаимозависимым лицам и резидентам оффшорных зон основных средств введенных в эксплуатацию после 01.01.2013
                    'rnu45'                 : 341, // (РНУ-45) Регистр налогового учета "ведомость начисленной амортизации по нематериальным активам"
                    'rnu46'                 : 342, // (РНУ-46) Регистр налогового учета "карточка по учету основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества"
                    'rnu47'                 : 344, // (РНУ-47) Регистр налогового учета "ведомость начисленной амортизации по основным средствам, а также расходов в виде капитальных вложений"
                    'rnu48_1'               : 343, // (РНУ-48.1) Регистр налогового учета "ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб."
                    'rnu48_2'               : 313, // (РНУ-48.2) Регистр налогового учета "Сводная ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб."
                    'rnu49'                 : 312, // (РНУ-49) Регистр налогового учета "ведомость определения результатов от реализации (выбытия) имущества"
                    'rnu50'                 : 365, // (РНУ-50) Регистр налогового учета "ведомость понесенных убытков от реализации амортизируемого имущества"
                    'rnu51'                 : 345, // (РНУ-51) Регистр налогового учета финансового результата от реализации (выбытия) ОФЗ
                    'rnu53'                 : 346, // (РНУ-53) Регистр налогового учета открытых сделок РЕПО с обязательством продажи по 2-й части
                    'rnu54'                 : 347, // (РНУ-54) Регистр налогового учета открытых сделок РЕПО с обязательством покупки по 2-й части
                    'rnu55'                 : 348, // (РНУ-55) Регистр налогового учета процентного дохода по процентным векселям сторонних эмитентов
                    'rnu56'                 : 349, // (РНУ-56) Регистр налогового учета процентного дохода по дисконтным векселям сторонних эмитентов
                    'rnu57'                 : 353, // (РНУ-57) Регистр налогового учета финансового результата от реализации (погашения) векселей сторонних эмитентов
                    'rnu59'                 : 350, // (РНУ-59) Регистр налогового учета закрытых сделок РЕПО с обязательством продажи по 2-й части
                    'rnu60'                 : 351, // (РНУ-60) Регистр налогового учета закрытых сделок РЕПО с обязательством покупки по 2-й части
                    'rnu61'                 : 352, // (РНУ-61) Регистр налогового учета расходов по процентным векселям ОАО "Сбербанк России", учет которых требует применения метода начисления
                    'rnu61_1'               : 422, // (РНУ-61) Регистр налогового учета расходов по процентным векселям ПАО "Сбербанк России", учет которых требует применения метода начисления (с 9 месяцев 2015)
                    'rnu62'                 : 354, // (РНУ-62) Регистр налогового учета расходов по дисконтным векселям ОАО "Сбербанк России"
                    'rnu62_1'               : 423, // (РНУ-62) Регистр налогового учета расходов по дисконтным векселям ПАО "Сбербанк России" (с 9 месяцев 2015)
                    'rnu64'                 : 355, // (РНУ-64) Регистр налогового учета затрат, связанных с проведением сделок РЕПО
                    'rnu70_1'               : 504, // (РНУ-70.1) Регистр налогового учета уступки права требования до наступления, предусмотренного кредитным договором срока погашения основного долга
                    'rnu70_2'               : 357, // (РНУ-70.2) Регистр налогового учета уступки права требования до наступления предусмотренного кредитным договором срока погашения основного долга
                    'rnu71_1'               : 356, // (РНУ-71.1) Регистр налогового учета уступки права требования после предусмотренного кредитным договором срока погашения основного долга
                    'rnu71_2'               : 503, // (РНУ-71.2) Регистр налогового учета уступки права требования после предусмотренного кредитным договором срока погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон
                    'rnu72'                 : 358, // (РНУ-72) Регистр налогового учета уступки права требования как реализации финансовых услуг и операций с закладными
                    'rnu75'                 : 366  // (РНУ-75) Регистр налогового учета доходов по операциям депозитария
            ],
            'vat'      : [
                    'declaration_fns'      : -4,
                    'declaration_audit'    : -7,
                    'declaration_short'    : -20,
                    'declaration_8'        : -12,
                    'declaration_8_1'      : -13,
                    'declaration_8_sources': -18,
                    'declaration_9'        : -14,
                    'declaration_9_1'      : -15,
                    'declaration_9_sources': -21,
                    'declaration_10'       : -16,
                    'declaration_11'       : -17,
                    'vat_724_1'            : 600, // Отчет о суммах начисленного НДС по операциям Банка
                    'vat_724_2_1'          : 601, // Операции, не подлежащие налогообложению (освобождаемые от налогообложения), операции, не признаваемые объектом налогообложения, операции по реализации товаров (работ, услуг), местом реализации которых не признается территория Российской Федерации, а также суммы оплаты, частичной оплаты в счет предстоящих поставок (выполнения работ, оказания услуг), длительность производственного цикла изготовления которых составляет свыше шести месяцев
                    'vat_724_2_2'          : 602, // Расчет суммы налога по операциям по реализации товаров (работ, услуг), обоснованность применения налоговой ставки 0 процентов по которым документально подтверждена
                    'vat_724_4'            : 603, // Налоговые вычеты за прошедший налоговый период, связанные с изменением условий или расторжением договора, в случае возврата ранее реализованных товаров (отказа от услуг) или возврата соответствующих сумм авансовых платежей
                    'vat_724_6'            : 604, // Отчет о суммах НДС начисленных налоговым агентом с сумм дохода иностранных юридических лиц (балансовый счет 60309.02)
                    'vat_724_7'            : 605, // Отчет о суммах НДС начисленных налоговым агентом по договорам аренды имущества (балансовый счет 60309.03)
                    'vat_937_1'            : 606, // Итоговые данные книги покупок
                    'vat_937_1_1'          : 616, // Сведения из дополнительных листов книги покупок
                    'vat_937_1_13'         : 607, // Расшифровка графы 13 "Расхождение" формы 937.1
                    'vat_937_2'            : 608, // Итоговые данные книги продаж
                    'vat_937_2_1'          : 617, // Сведения из дополнительных листов книги продаж
                    'vat_937_2_13'         : 609, // Расшифровка графы 13 "Расхождение" формы 937.2
                    'vat_937_3'            : 619, // Итоговые данные из журнала полученных и выставленных счетов-фактур по посреднической деятельности
                    'vat_grant_basis'      : 622, // Расшифровка операций по реализации товаров (работ, услуг) на безвозмездной основе
                    'vat_operAgent'        : 621, // Разнарядка на безакцептное списание/зачисление по суммам НДС с территориальных банков, Московского банка и подразделений ЦА (по операциям налогового агента)
                    'vat_operBank'         : 620  // Разнарядка на безакцептное списание/зачисление по суммам НДС с территориальных банков, Московского банка и подразделений ЦА (по операциям банка)
            ],
            'transport': [
                    'benefit_vehicles': 202, // Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог
                    'declaration'     : -1,
                    'summary'         : 200, // Расчет суммы налога по каждому транспортному средству
                    'vehicles'        : 201  // Сведения о транспортных средствах, по которым уплачивается транспортный налог
            ],
            'property' : [
                    'declaration'   : -3,
                    'prepayment'    : -8,
                    'property_945_1': 610, // Данные бухгалтерского учета для расчета налога на имущество
                    'property_945_2': 611, // Данные о кадастровой стоимости объектов недвижимости для расчета налога на имущество
                    'property_945_3': 613, // Расчет налога на имущество по средней/среднегодовой стоимости
                    'property_945_4': 612, // Расчет налога на имущество по кадастровой стоимости
                    'property_945_5': 615  // Сводная форма данных бухгалтерского учета для расчета налога на имущество
            ],
            'etr' : [
                    'amount_tax'      : 700, // Величины налоговых платежей, вводимые вручную
                    'etr_4_1'         : 701, // Приложение 4-1. Абсолютная величина налоговых платежей
                    'etr_4_2'         : 702, // Приложение 4-2. Отношение налогов, уплаченных из прибыли к балансовой прибыли
                    'etr_4_3'         : 703, // Приложение 4-3. Отношение налоговых платежей к чистой прибыли Банка
                    'etr_4_4'         : 704, // Приложение 4-4. Отношение налоговых платежей к операционным доходам
                    'etr_4_5'         : 705, // Приложение 4-5. Отношение налогов, относимых на расходы к операционным расходам
                    'etr_4_6'         : 706, // Приложение 4-6. Отношение страховых взносов, уплачиваемых во внебюджетные фонды к расходам на оплату труда
                    'etr_4_7'         : 707, // Приложение 4-7. Отношение налога на имущество к остаточной стоимости
                    'etr_4_8'         : 708, // Приложение 4-8. Налоговая эффективность по уступке прав требования по проблемным активам
                    'etr_4_9_summary' : 7090,// Приложение 4-9. Налоговая эффективность по уступке прав требования по проблемным активам в разрезе ТБ (сводная)
                    'etr_4_10'        : 710, // Приложение 4-10. Трансфертное ценообразование – сделки с взаимозависимыми лицами (ВЗЛ) и резидентами оффшорных зон (РОЗ)
                    'etr_4_10_summary': 7100,// Приложение 4-10. Трансфертное ценообразование – сделки с взаимозависимыми лицами (ВЗЛ) и резидентами оффшорных зон (РОЗ) (сводная)
                    'etr_4_11'        : 711, // Приложение 4-11. Статистика доначислений
                    'etr_4_13'        : 713, // Приложение 4-13. Анализ структуры доходов и расходов, не учитываемых для целей налогообложения
                    'etr_4_14'        : 714, // Приложение 4-14. Соотношение расходов, не учитываемых по законодательству и не признанных по другим причинам.
                    'etr_4_15'        : 715, // Приложение 4-15. Анализ структуры налога на добавленную стоимость (НДС)
                    'etr_4_15_summary': 7150,// Приложение 4-15. Анализ структуры налога на добавленную стоимость (НДС) (сводная)
                    'etr_4_16'        : 716,  // Приложение 4-16. Доходы и расходы, не учитываемые для целей налогообложения по налогу на прибыль, и их влияние на финансовый результат
                    'result_report'   : 730 // Величины налоговых платежей, вводимые вручную
            ]
    ]

    // Имя папки -> REF_BOOK.ID
    def static REFBOOK_FOLDER_NAME_TO_ID = [
            'account_plan' : 101,
            'bond' : 84,
            'classificator_code_724_2_1' : 102,
            'classificator_country' : 10,
            'classificator_eco_activities' : 34,
            'classificator_income' : 28,
            'classificator_outcome' : 27,
            'currency_rate' : 22,
            'declaration_params_property' : 200,
            'declaration_params_transport' : 210,
            'department' : 30,
            'emitent' : 100,
            'income101' : 50,
            'income102' : 52,
            'jur_persons' : 520,
            'jur_persons_terms' : 515,
            'metal_rate' : 90,
            'okato' : 3,
            'okei' : 12,
            'organization' : 9,
            'problem_zones' : 504,
            'region' : 4,
            'tax_benefits_property' : 203,
            'tax_benefits_transport' : 7,
            'vehicles_average_cost' : 208
    ]

    static void main(String[] args) {
        println("RDBMS url=$DB_URL")
        println("Compare local and $DB_USER scripts...")

        // Удаление старых отчетов, если такие есть
        def report = new File(REPORT_GIT_NAME)
        if (report.exists()) {
            report.delete()
        }
        report = new File(REPORT_REFBOOK_GIT_NAME)
        if (report.exists()) {
            report.delete()
        }
        report = new File(REPORT_DB_NAME)
        if (report.exists()) {
            report.delete()
        }
        report = new File(REPORT_DECL_GIT_NAME)
        if (report.exists()) {
            report.delete()
        }
        report = new File(REPORT_DECL_DB_NAME)
        if (report.exists()) {
            report.delete()
        }
        report = new File(REPORT_REFBOOK_DB_NAME)
        if (report.exists()) {
            report.delete()
        }
        report = new File(REPORT_TYPE_DB_NAME)
        if (report.exists()) {
            report.delete()
        }

        println("DBMS connect: ${DB_USER}")
        def sql = Sql.newInstance(DB_URL, DB_USER, DB_PASSWORD, "oracle.jdbc.OracleDriver")

        // Построение отчета сравнения Git и БД
        try {
            if ((DB_USER.contains("NEXT") || DB_USER.contains("PSI")) && (!checkOnlyFD || !checkOnlyDD)) {
                println("На стендах NEXT и PSI ручное/автоматическое обновление скриптов запрещено! Будет произведено только сравнение.")
                println("Manual/automatic scripts update forbidden on NEXT and PSI stands! Only comparison will be done.")
            }
            GitReport.updateScripts(GitReport.getDBVersions(sql), sql, DB_USER.contains("NEXT") || DB_USER.contains("PSI") || checkOnlyFD)
            GitReport.updateDeclarationScripts(GitReport.getDeclarationDBVersions(sql), sql, DB_USER.contains("NEXT") || DB_USER.contains("PSI") || checkOnlyDD)
            GitReport.checkRefBooks(GitReport.getRefBookScripts(sql))
        } finally {
            sql.close()
        }
        println("See $REPORT_GIT_NAME, $REPORT_DECL_GIT_NAME and $REPORT_REFBOOK_GIT_NAME for details")

        // Сравнение схем в БД
        if (DB_USER_COMPARE != null) {
            println("Compare $DB_USER and $DB_USER_COMPARE form/declaration types...")
            DBReport.compareDBTypes(DB_USER, DB_USER_COMPARE)
            println("Compare $DB_USER and $DB_USER_COMPARE form templates...")
            DBReport.compareDBFormTemplate(DB_USER, DB_USER_COMPARE)
            println("Compare $DB_USER and $DB_USER_COMPARE declaration templates...")
            DBReport.compareDBDeclarationTemplate(DB_USER, DB_USER_COMPARE)
            println("Compare $DB_USER and $DB_USER_COMPARE refbook scripts...")
            DBReport.compareDBRefbookScript(DB_USER, DB_USER_COMPARE)
        }
    }
}
