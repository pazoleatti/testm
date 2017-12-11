 rem путь к java
SET jre="C:\Program Files\Java\jdk1.7.0_71\bin"
 rem jar-файл
SET jar=generatorTF-1.1-jar-with-dependencies.jar
 rem входной файл
SET in=in.xlsx
 rem тип генерируемого файла (xlsx или xml)
SET type=xlsx
 rem кол-во генерируемых физиков
SET flCount=1000
 rem кол-во файлов (только для xml)
SET filesCount=1

%jre%\java -jar %jar% %in% %type% %FLcount% %filesCount%

pause