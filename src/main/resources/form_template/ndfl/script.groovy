package form_template.ndfl

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import groovy.sql.Sql

//---- test ----
def db = [url: 'jdbc:hsqldb:mem:testDB', user: 'sa', password: '', driver: 'org.hsqldb.jdbc.JDBCDriver']
def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)

sql.execute '''
    set database sql syntax ora true;
    SET DATABASE EVENT LOG SQL LEVEL 3;
 '''

String createMainSql = new File("../src/main/resources/ddl/create_main.sql").text
sql.execute(createMainSql)

String createConstraintSql = new File("../src/main/resources/ddl/create_constraint.sql").text
sql.execute(createConstraintSql)
//--------------

//Now you can invoke sql, e.g. to create a table:
/*sql.execute '''
     create table PROJECT (
         id integer not null,
         name varchar(50),
         url varchar(100),
     )
 '''*/

//Or insert a row using JDBC PreparedStatement inspired syntax:

String insStr = "INSERT INTO NDFL_PERSON (id, declaration_data_id, inp, FIRST_NAME, LAST_NAME, BIRTH_DAY, CITIZENSHIP, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS) VALUES (SEQ_NDFL_PERSON.nextVal, -1, 1234567890, 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'),'Российская Федерация', '112233', '0000', '1515')"
sql.execute(insStr);
String insStr2 = "INSERT INTO NDFL_PERSON (id, declaration_data_id, inp, FIRST_NAME, LAST_NAME, BIRTH_DAY, CITIZENSHIP, ID_DOC_TYPE, ID_DOC_NUMBER, STATUS) VALUES (SEQ_NDFL_PERSON.nextVal, -1, 1234567890, 'Ivan', 'Ivanov', TO_DATE('01-01-1980', 'DD-MM-YYYY'),'Российская Федерация', '112233', '0000', '1515')"
sql.execute(insStr2);

//def params = [10, 'Groovy', 'http://groovy.codehaus.org']
//sql.execute 'insert into PROJECT (id, name, url) values (?, ?, ?)', params

//Or insert a row using GString syntax:
//def map = [id:20, name:'Grails', url:'http://grails.codehaus.org']
//sql.execute "insert into PROJECT (id, name, url) values ($map.id, $map.name, $map.url)"

//Or a row update:
//def newUrl = 'http://grails.org'
//def project = 'Grails'
//sql.executeUpdate "update PROJECT set url=$newUrl where name=$project"

//Now try a query using eachRow:
//        println 'Some GR8 projects:'
sql.eachRow('select * from NDFL_PERSON') { row ->
    println "$row.id | $row.birth_day | $row.citizenship"
}

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        println "call create"
        //formDataService.addRow(formData, null, [], [])
        break
    case FormDataEvent.CALCULATE:
        println "call calculate"
        //checkSourceForm()
        calc()
        //logicCheck()
        //updateHistoryOnCalcOrSave()
        //formDataService.saveCachedDataRows(formData, logger)
        break
}


void calc() {

    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    def root = new XmlParser().parse(xmlInputStream);

    if (root == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }

    def servicePart = root.get('СлЧасть')

    def infoParts = root.get('ИнфЧасть')

    infoParts.each {
        processInfoPart(it);
    }
}

void processInfoPart(infoPart) {

    def ndflPerson = infoPart.get('ПолучДох')
    processNdflPerson(ndflPerson);

    def ndflPersonOperations = infoPart.get('СведОпер')
    ndflPersonOperations.each {
        processNdflPersonOperation(ndflPerson, it);
    }

}

void processNdflPerson(ndflPerson) {
    println "processNdflPerson " + ndflPerson
}


void processNdflPersonOperation(ndflPerson, ndflPersonOperation) {


    def ndflPersonIncomes = ndflPersonOperation.get('СведДохНал'); //1..n

    def ndflPersonDeductions = ndflPersonOperation.get('СведВыч'); //0..n


    def ndflPersonPrepayments = ndflPersonOperation.get('СведАванс'); //0..n



    println "processNdflPersonOperation " + ndflPersonOperation

}

void processNdflPersonIncome(ndflPerson, ndflPersonOperation, ndflPersonIncome) {


}

void ndflPersonDeduction(ndflPersonOperation) {


}

void ndflPersonPrepayment(ndflPersonOperation) {


}





