package com.aplana.taxaccounting

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
    def static DB_USER = 'TAX_0_3_9'
    def static DB_PASSWORD = 'TAX'

    // Схема для сравнения макетов, null если сравнение не требуется
    def static DB_USER_COMPARE = 'TAX_0_3_8'

    // Путь к папке с шаблонами
    def static SRC_FOLDER_PATH = '../src/main/resources/form_template'
    def static TAX_FOLDERS = ['deal': 'МУКС',
            'income': 'Налог на прибыль',
            'vat': 'НДС',
            'transport': 'Транспортный налог']

    // Названия файлов отчетов
    def static REPORT_GIT_NAME = 'report_git_db_compare.html'
    def static REPORT_DB_NAME = 'report_db_compare.html'

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
    def static TEMPLATE_NAME_TO_TYPE_ID = [
            'deal': [
                    'auctions_property': 380,
                    'bank_service': 382,
                    'bank_service_income': 398,
                    'bank_service_outcome': 399,
                    'bonds_trade': 384,
                    'corporate_credit': 387,
                    'credit_contract': 385,
                    'foreign_currency': 390,
                    'forward_contracts': 391,
                    'guarantees': 388,
                    'guarantees_involvement': 401,
                    'interbank_credits': 389,
                    'letter_of_credit': 386,
                    'matrix': 400,
                    'nondeliverable': 392,
                    'notification': -1,
                    'organization_matching': 410,
                    'precious_metals_deliver': 393,
                    'precious_metals_trade': 394,
                    'rent_provision': 376,
                    'repo': 383,
                    'rights_acquisition': 404,
                    'securities': 381,
                    'software_development': 375,
                    'summary': 409,
                    'take_corporate_credit': 397,
                    'take_interbank_credit': 402,
                    'take_itf': 403,
                    'tech_service': 377,
                    'trademark': 379
            ],
            'income': [
                    'advanceDistribution': 500,
                    'app5': 372,
                    'declaration_bank': -1,
                    'declaration_op': -1,
                    'f7_8': 362,
                    'income_complex': 302,
                    'income_simple': 301,
                    'outcome_complex': 303,
                    'outcome_simple': 304,
                    'output1': 306,
                    'output2': 307,
                    'output3': 308,
                    'rnu107': -1,
                    'rnu108': -1,
                    'rnu110': -1,
                    'rnu111': -1,
                    'rnu112': -1,
                    'rnu115': -1,
                    'rnu116': -1,
                    'rnu117': -1,
                    'rnu118': -1,
                    'rnu119': -1,
                    'rnu12': 364,
                    'rnu120': -1,
                    'rnu14': 321,
                    'rnu16': 499,
                    'rnu17': 501,
                    'rnu22': 322,
                    'rnu23': 323,
                    'rnu25': 324,
                    'rnu26': 325,
                    'rnu27': 326,
                    'rnu30': 329,
                    'rnu31': 328,
                    'rnu32_1': 330,
                    'rnu32_2': 331,
                    'rnu33': 332,
                    'rnu36_1': 333,
                    'rnu36_2': 315,
                    'rnu38_1': 334,
                    'rnu38_2': 335,
                    'rnu39_1': 336,
                    'rnu39_2': 337,
                    'rnu4': 316,
                    'rnu40_1': 338,
                    'rnu40_2': 339,
                    'rnu44': 340,
                    'rnu45': 341,
                    'rnu46': 342,
                    'rnu47': 344,
                    'rnu48_1': 343,
                    'rnu48_2': 313,
                    'rnu49': 312,
                    'rnu5': 317,
                    'rnu50': 365,
                    'rnu51': 345,
                    'rnu53': 346,
                    'rnu54': 347,
                    'rnu55': 348,
                    'rnu56': 349,
                    'rnu57': 353,
                    'rnu59': 350,
                    'rnu6': 318,
                    'rnu60': 351,
                    'rnu61': 352,
                    'rnu62': 354,
                    'rnu64': 355,
                    'rnu7': 311,
                    'rnu70_1': 504,
                    'rnu70_2': 357,
                    'rnu71_1': 356,
                    'rnu71_2': 503,
                    'rnu72': 358,
                    'rnu75': 366,
                    'rnu8': 320
            ],
            'vat': [
                    'declaration_audit': -1,
                    'declaration_fns': -1,
                    'vat_724_1': 600,
                    'vat_724_2_1': 601,
                    'vat_724_2_2': 602,
                    'vat_724_4': 603,
                    'vat_724_6': 604,
                    'vat_724_7': 605,
                    'vat_937_1': 606,
                    'vat_937_1_13': 607,
                    'vat_937_2_13': 609,
                    'vat_937_2': 608
            ],
            'transport': [
                    'benefit_vehicles': 202,
                    'declaration': -1,
                    'summary': 200,
                    'vehicles': 201
            ]
    ]

    static void main(String[] args) {
        println("RDBMS url=$DB_URL")
        println("Compare local and $DB_USER scripts...")

        // Удаление старых отчетов, если такие есть
        def reportGit = new File(REPORT_GIT_NAME)
        if (reportGit.exists()) {
            reportGit.delete()
        }
        def reportDb = new File(REPORT_DB_NAME)
        if (reportDb.exists()) {
            reportDb.delete()
        }

        // Построение отчета сравнения Git и БД
        GitReport.updateScripts(GitReport.getDBVersions(), true)
        println("See $REPORT_GIT_NAME for details")

        // Сравнение схем в БД
        if (DB_USER_COMPARE != null) {
            println("Compare $DB_USER and $DB_USER_COMPARE form templates...")
            DBReport.compareDBFormTemplate(DB_USER, DB_USER_COMPARE)
        }
    }
}
