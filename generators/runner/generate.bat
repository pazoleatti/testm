 rem путь к java
SET jre="C:\Program Files\Java\jdk1.7.0_71\bin"
 rem jar-файл
SET jar=generators-1.1-jar-with-dependencies.jar
 rem тип генерируемого файла, возможные значения:
 rem xlsx - для РНУ Excel
 rem xml (имя файла не начинается с FL) - для генерации XML РНУ
 rem xml (имя файла начинается с FL) - для генерации XML справочника ФЛ
SET type=xml
 rem входной файл, на основе которого будет выполнена генерация
SET in=_______38_0000_00100031203200000000000000000kor9check1464v3.xml
 rem второй входной файл, у которого будут сгенерированны одинаковые данные для ФЛ как для первого файла
SET in2=FL_2018-02-28_sprav_628EAFC4B86F2FE8E05400144BDREKS.xml
 rem кол-во генерируемых физиков
SET flCount=1000
 rem кол-во файлов (только для xml)
SET filesCount=1

%jre%\java -jar %jar% %type% %in% %in2% %FLcount% %filesCount%

pause