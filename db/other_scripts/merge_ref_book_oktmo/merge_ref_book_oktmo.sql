set serveroutput on;
-- https://jira.aplana.com/browse/SBRFNDFL-6985 - Реализовать отдельный скрипт добавления в БД новых ОКТМО для стенда ПРОМ
begin

merge into ref_book_oktmo a using
(
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000' as code, 'г Алексеевка' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000' as code, 'г Валуйки' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000' as code, 'г Грайворон' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000' as code, 'г Новый Оскол' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000' as code, 'г Шебекино' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000' as code, 'г Строитель' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000000' as code, 'Населенные пункты, входящие в состав городского округа Алексеевский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000001' as code, 'г Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000106' as code, 'п Геращенково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000111' as code, 'п Голубинский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000116' as code, 'п Лесиковка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000121' as code, 'п Николаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000126' as code, 'п Хмызовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000131' as code, 'с Алейниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000136' as code, 'с Алексеенково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000141' as code, 'с Афанасьевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000146' as code, 'с Белозорово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000151' as code, 'с Ближнее Чесночное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000156' as code, 'с Божково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000161' as code, 'с Бубликово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000166' as code, 'с Варваровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000171' as code, 'с Воробьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000176' as code, 'с Гарбузово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000181' as code, 'с Глуховка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000186' as code, 'с Дальнее Чесночное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000191' as code, 'с Жуково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000196' as code, 'с Запольное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000201' as code, 'с Зварыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000206' as code, 'с Иващенково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000211' as code, 'с Иловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000216' as code, 'с Ильинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000221' as code, 'с Калитва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000226' as code, 'с Камышеватое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000231' as code, 'с Ковалево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000236' as code, 'с Колтуновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000241' as code, 'с Красное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000246' as code, 'с Кущино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000251' as code, 'с Луценково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000256' as code, 'с Матрено-Гезово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000261' as code, 'с Меняйлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000266' as code, 'с Мухоудеровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000271' as code, 'с Надеждовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000276' as code, 'с Николаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000281' as code, 'с Осадчее' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000286' as code, 'с Пирогово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000291' as code, 'с Подсереднее' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000296' as code, 'с Репенка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000301' as code, 'с Славгородское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000306' as code, 'с Советское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000311' as code, 'с Станичное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000316' as code, 'с Студеный Колодец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000321' as code, 'с Теплинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000326' as code, 'с Тютюниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000331' as code, 'с Хлевище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000336' as code, 'с Чупринино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000341' as code, 'с Шапорево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000346' as code, 'с Щербаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000351' as code, 'х Бабичев' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000356' as code, 'х Батлуков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000361' as code, 'х Бережной' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000366' as code, 'х Березки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000371' as code, 'х Березняги-Вторые' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000376' as code, 'х Васильченков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000381' as code, 'х Власов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000386' as code, 'х Волков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000391' as code, 'х Гезов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000396' as code, 'х Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000401' as code, 'х Гречаников' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000406' as code, 'х Дудчин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000411' as code, 'х Игнатов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000416' as code, 'х Кириченков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000421' as code, 'х Климов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000426' as code, 'х Копанец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000431' as code, 'х Кукаречин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000436' as code, 'х Кулешов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000441' as code, 'х Куприянов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000446' as code, 'х Неменущий' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000451' as code, 'х Новоселовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000456' as code, 'х Орлов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000461' as code, 'х Осьмаков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000466' as code, 'х Папушин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000471' as code, 'х Покладов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000476' as code, 'х Попов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000481' as code, 'х Пышнограев' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000486' as code, 'х Редкодуб' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000491' as code, 'х Резников' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000496' as code, 'х Рыбалкин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000501' as code, 'х Сероштанов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000506' as code, 'х Сидоркин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000511' as code, 'х Соломахин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000516' as code, 'х Сыроватский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000521' as code, 'х Тараканов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000526' as code, 'х Хрещатый' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000531' as code, 'х Черепов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000536' as code, 'х Шапошников' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000541' as code, 'х Шелушин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14710000546' as code, 'х Шкуропатов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000000' as code, 'Населенные пункты, входящие в состав городского округа Валуйский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000001' as code, 'г Валуйки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000056' as code, 'п Уразово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000106' as code, 'п Дальний' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000111' as code, 'п Дружба' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000116' as code, 'п Ровное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000121' as code, 'п Рощино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000126' as code, 'с Агошевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000131' as code, 'с Аркатово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000136' as code, 'с Басово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000141' as code, 'с Безгодовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000146' as code, 'с Бирюч' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000151' as code, 'с Борисово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000156' as code, 'с Борки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000161' as code, 'с Бутырки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000166' as code, 'с Ватутино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000171' as code, 'с Вериговка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000176' as code, 'с Верхний Моисей' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000181' as code, 'с Вороновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000186' as code, 'с Герасимовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000191' as code, 'с Гладково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000196' as code, 'с Двулучное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000201' as code, 'с Долгое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000206' as code, 'с Дроново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000211' as code, 'с Знаменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000216' as code, 'с Ивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000221' as code, 'с Казинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000226' as code, 'с Казначеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000231' as code, 'с Карабаново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000236' as code, 'с Касеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000241' as code, 'с Колосково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000246' as code, 'с Колыхалино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000251' as code, 'с Конопляновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000256' as code, 'с Кукуевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000261' as code, 'с Лавы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000266' as code, 'с Логачевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000271' as code, 'с Лучка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000276' as code, 'с Майское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000281' as code, 'с Мандрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000286' as code, 'с Масловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000291' as code, 'с Насоново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000296' as code, 'с Новая Симоновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000301' as code, 'с Новоказацкое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000306' as code, 'с Новопетровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000311' as code, 'с Новый Изрог' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000316' as code, 'с Овчинниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000321' as code, 'с Орехово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000326' as code, 'с Подгорное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000331' as code, 'с Поминово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000336' as code, 'с Посохово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000341' as code, 'с Принцевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000346' as code, 'с Пристень' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000351' as code, 'с Рождествено' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000356' as code, 'с Селиваново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000361' as code, 'с Ситнянка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000366' as code, 'с Соболевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000371' as code, 'с Солоти' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000376' as code, 'с Старая Симоновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000381' as code, 'с Старцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000386' as code, 'с Старый Хутор' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000391' as code, 'с Сухарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000396' as code, 'с Терехово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000401' as code, 'с Тимоново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000406' as code, 'с Тогобиевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000411' as code, 'с Тулянка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000416' as code, 'с Углово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000421' as code, 'с Ураево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000426' as code, 'с Филиппово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000431' as code, 'с Хмелевец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000436' as code, 'с Хохлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000441' as code, 'с Храпово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000446' as code, 'с Шведуновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000451' as code, 'с Шелаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000456' as code, 'с Шушпаново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000461' as code, 'с Яблоново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000466' as code, 'х Бабка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000471' as code, 'х Барвинок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000476' as code, 'х Бережанка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000481' as code, 'х Богомолово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000486' as code, 'х Долгаловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000491' as code, 'х Дубровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000496' as code, 'х Дубровки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000501' as code, 'х Жердевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000506' as code, 'х Конотоповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000511' as code, 'х Кузнецовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000516' as code, 'х Кургашки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000521' as code, 'х Леоновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000526' as code, 'х Лобковка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000531' as code, 'х Миронов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000536' as code, 'х Михайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000541' as code, 'х Мокрый Лог' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000546' as code, 'х Нижние Мельницы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000551' as code, 'х Павловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000556' as code, 'х Песчанка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000561' as code, 'х Пригородные Тополи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000566' as code, 'х Пролесок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000571' as code, 'х Ромашовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14720000576' as code, 'х Рябики' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000000' as code, 'Населенные пункты, входящие в состав городского округа Грайворонский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000001' as code, 'г Грайворон' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000106' as code, 'п Горьковский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000111' as code, 'п Доброполье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000116' as code, 'п Казачок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000121' as code, 'п Совхозный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000126' as code, 'п Хотмыжск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000131' as code, 'п Чапаевский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000136' as code, 'с Антоновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000141' as code, 'с Безымено' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000146' as code, 'с Глотово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000151' as code, 'с Головчино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000156' as code, 'с Гора-Подол' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000161' as code, 'с Доброивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000166' as code, 'с Доброе' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000171' as code, 'с Дорогощь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000176' as code, 'с Дроновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000181' as code, 'с Дунайка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000186' as code, 'с Замостье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000191' as code, 'с Заречье-Первое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000196' as code, 'с Заречье-Второе' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000201' as code, 'с Ивановская Лисица' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000206' as code, 'с Казачья Лисица' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000211' as code, 'с Козинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000216' as code, 'с Косилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000221' as code, 'с Ломное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000226' as code, 'с Луговка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000231' as code, 'с Мокрая Орловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000236' as code, 'с Мощеное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000241' as code, 'с Новостроевка-Первая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000246' as code, 'с Новостроевка-Вторая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000251' as code, 'с Пороз' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000256' as code, 'с Почаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000261' as code, 'с Рождественка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000266' as code, 'с Санково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000271' as code, 'с Смородино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000276' as code, 'с Сподарюшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000281' as code, 'х Байрак' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000286' as code, 'х Масычево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000291' as code, 'х Понуры' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14725000296' as code, 'х Тополи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000000' as code, 'Населенные пункты, входящие в состав городского округа Новооскольский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000001' as code, 'г Новый Оскол' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000106' as code, 'п Грушное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000111' as code, 'п Козловский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000116' as code, 'п Нечаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000121' as code, 'п Полевой' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000126' as code, 'п Прибрежный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000131' as code, 'п Рудный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000136' as code, 'с Барсук' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000141' as code, 'с Беломестное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000146' as code, 'с Богдановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000151' as code, 'с Богородское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000156' as code, 'с Большая Ивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000161' as code, 'с Боровки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000166' as code, 'с Боровое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000171' as code, 'с Васильдол' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000176' as code, 'с Великомихайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000181' as code, 'с Глинное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000186' as code, 'с Голубино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000191' as code, 'с Грачевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000196' as code, 'с Гринево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000201' as code, 'с Гущенка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000206' as code, 'с Елецкое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000211' as code, 'с Ивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000216' as code, 'с Киселевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000221' as code, 'с Косицыно' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000226' as code, 'с Крюк' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000231' as code, 'с Кулевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000236' as code, 'с Леоновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000241' as code, 'с Львовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000246' as code, 'с Майорщина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000251' as code, 'с Макешкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000256' as code, 'с Малое Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000261' as code, 'с Можайское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000266' as code, 'с Мозолевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000271' as code, 'с Немцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000276' as code, 'с Николаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000281' as code, 'с Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000286' as code, 'с Ниновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000291' as code, 'с Новая Безгинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000296' as code, 'с Ольховатка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000301' as code, 'с Оскольское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000306' as code, 'с Остаповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000311' as code, 'с Песчанка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000316' as code, 'с Подвислое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000321' as code, 'с Покрово-Михайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000326' as code, 'с Семеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000331' as code, 'с Серебрянка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000336' as code, 'с Слоновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000341' as code, 'с Солонец-Поляна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000346' as code, 'с Старая Безгинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000351' as code, 'с Таволжанка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000356' as code, 'с Тростенец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000361' as code, 'с Шараповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000366' as code, 'с Яковлевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000371' as code, 'с Ярское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000376' as code, 'х Аринкин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000381' as code, 'х Белый Колодезь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000386' as code, 'х Березки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000391' as code, 'х Березов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000396' as code, 'х Богатый' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000401' as code, 'х Большая Яруга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000406' as code, 'х Большая Яруга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000411' as code, 'х Бондарев' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000416' as code, 'х Васильевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000421' as code, 'х Васильполье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000426' as code, 'х Веселый' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000431' as code, 'х Гайдашовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000436' as code, 'х Гнилица' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000441' as code, 'х Елец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000446' as code, 'х Ендовино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000451' as code, 'х Жилин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000456' as code, 'х Калиновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000461' as code, 'х Ключи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000466' as code, 'х Колодезный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000471' as code, 'х Костевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000476' as code, 'х Костин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000481' as code, 'х Красная Каменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000486' as code, 'х Криничный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000491' as code, 'х Кульма' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000496' as code, 'х Мазепин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000501' as code, 'х Махотынка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000506' as code, 'х Мирошники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000511' as code, 'х Мосьпанов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000516' as code, 'х Муренцев' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000521' as code, 'х Надежный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000526' as code, 'х Новоселовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000531' as code, 'х Погромец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000536' as code, 'х Подольхи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000541' as code, 'х Попасный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000546' as code, 'х Проточный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000551' as code, 'х Прудки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000556' as code, 'х Пустынка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000561' as code, 'х Развильный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000566' as code, 'х Редкодуб' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000571' as code, 'х Сабельный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000576' as code, 'х Севальный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000581' as code, 'х Симоновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000586' as code, 'х Скрынников' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000591' as code, 'х Соколовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000596' as code, 'х Тереховка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000601' as code, 'х Фироновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000606' as code, 'х Холки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000611' as code, 'х Чаусовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000616' as code, 'х Шевцов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000621' as code, 'х Шуваевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14735000626' as code, 'х Ямки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000000' as code, 'Населенные пункты, входящие в состав городского округа Старооскольский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000001' as code, 'г Старый Оскол' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000106' as code, 'п Логвиновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000111' as code, 'п Малый Присынок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000116' as code, 'п Набокино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000121' as code, 'п Пасечный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000126' as code, 'п Первомайский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000131' as code, 'п Петровский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000136' as code, 'с Анпиловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000141' as code, 'с Архангельское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000146' as code, 'с Бабанинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000151' as code, 'с Боровая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000156' as code, 'с Бочаровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000161' as code, 'с Великий Перевоз' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000166' as code, 'с Верхне-Атаманское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000171' as code, 'с Верхне-Чуфичево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000176' as code, 'с Владимировка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000181' as code, 'с Воротниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000186' as code, 'с Выползово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000191' as code, 'с Голофеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000196' as code, 'с Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000201' as code, 'с Готовье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000206' as code, 'с Дмитриевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000211' as code, 'с Долгая Поляна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000216' as code, 'с Знаменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000221' as code, 'с Ивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000226' as code, 'с Казачок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000231' as code, 'с Каплино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000236' as code, 'с Котеневка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000241' as code, 'с Котово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000246' as code, 'с Крутое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000251' as code, 'с Курское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000256' as code, 'с Лапыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000261' as code, 'с Луганка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000266' as code, 'с Монаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000271' as code, 'с Нагольное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000276' as code, 'с Незнамово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000281' as code, 'с Нижне-Чуфичево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000286' as code, 'с Нижнеатаманское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000291' as code, 'с Николаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000296' as code, 'с Николаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000301' as code, 'с Новиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000306' as code, 'с Новоалександровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000311' as code, 'с Новокладовое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000316' as code, 'с Новониколаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000321' as code, 'с Новоселовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000326' as code, 'с Обуховка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000331' as code, 'с Озерки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000336' as code, 'с Окольное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000341' as code, 'с Песчанка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000346' as code, 'с Потудань' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000351' as code, 'с Преображенка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000356' as code, 'с Приосколье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000361' as code, 'с Прокудино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000366' as code, 'с Роговатое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000371' as code, 'с Сергеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000376' as code, 'с Солдатское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000381' as code, 'с Сорокино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000386' as code, 'с Терехово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000391' as code, 'с Терновое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000396' as code, 'с Федосеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000401' as code, 'с Хорошилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000406' as code, 'с Черниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000411' as code, 'с Чужиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000416' as code, 'с Шаталовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000421' as code, 'с Шмарное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000426' as code, 'х Высокий' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000431' as code, 'х Глушковка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000436' as code, 'х Гриневка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000441' as code, 'х Змеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000446' as code, 'х Игнатовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000451' as code, 'х Ильины' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000456' as code, 'х Липяги' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000461' as code, 'х Менжулюк' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000466' as code, 'х Новая Деревня' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000471' as code, 'х Песочный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000476' as code, 'х Плота' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000481' as code, 'х Рекуновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000486' as code, 'х Сумароков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14740000491' as code, 'х Чумаки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000000' as code, 'Населенные пункты, входящие в состав городского округа Шебекинский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000001' as code, 'г Шебекино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000056' as code, 'рп Маслова Пристань' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000106' as code, 'п Батрацкая Дача' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000111' as code, 'п Красное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000116' as code, 'п Ленинский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000121' as code, 'п Первомайский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000126' as code, 'п Поляна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000131' as code, 'п Шамино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000136' as code, 'п Шебекинский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000141' as code, 'с Авиловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000146' as code, 'с Александровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000151' as code, 'с Артельное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000156' as code, 'с Архангельское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000161' as code, 'с Безлюдовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000166' as code, 'с Белый Колодезь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000171' as code, 'с Белянка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000176' as code, 'с Бершаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000181' as code, 'с Большетроицкое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000186' as code, 'с Большое Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000191' as code, 'с Борисовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000196' as code, 'с Боровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000201' as code, 'с Булановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000206' as code, 'с Верхнеберезово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000211' as code, 'с Вознесеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000216' as code, 'с Графовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000221' as code, 'с Дмитриевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000226' as code, 'с Доброе' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000231' as code, 'с Заводцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000236' as code, 'с Зиборовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000241' as code, 'с Зимовенька' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000246' as code, 'с Зимовное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000251' as code, 'с Ивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000256' as code, 'с Караичное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000261' as code, 'с Козьмодемьяновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000266' as code, 'с Коровино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000271' as code, 'с Кошлаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000276' as code, 'с Крапивное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000281' as code, 'с Красная Поляна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000286' as code, 'с Купино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000291' as code, 'с Максимовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000296' as code, 'с Маломихайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000301' as code, 'с Мешковое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000306' as code, 'с Муром' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000311' as code, 'с Нежеголь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000316' as code, 'с Неклюдово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000321' as code, 'с Нехотеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000326' as code, 'с Нижнее Березово-Второе' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000331' as code, 'с Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000336' as code, 'с Новая Таволжанка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000341' as code, 'с Огнищево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000346' as code, 'с Осиновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000351' as code, 'с Пенцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000356' as code, 'с Первое Цепляево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000361' as code, 'с Поповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000366' as code, 'с Пристень' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000371' as code, 'с Протопоповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000376' as code, 'с Репное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000381' as code, 'с Ржевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000386' as code, 'с Селишко' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000391' as code, 'с Середа' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000396' as code, 'с Стариково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000401' as code, 'с Старовщина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000406' as code, 'с Стрелица-Первая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000411' as code, 'с Стрелица-Вторая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000416' as code, 'с Сурково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000421' as code, 'с Терезовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000426' as code, 'с Терновое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000431' as code, 'с Титовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000436' as code, 'с Тюрино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000441' as code, 'с Цепляево-Второе' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000446' as code, 'с Червона Дибровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000451' as code, 'с Чураево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000456' as code, 'с Щигоревка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000461' as code, 'с Яблочково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000466' as code, 'х Александровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000471' as code, 'х Александровский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000476' as code, 'х Бабенков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000481' as code, 'х Балки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000486' as code, 'х Белокриничный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000491' as code, 'х Бессараб' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000496' as code, 'х Бондаренков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000501' as code, 'х Гордюшкин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000506' as code, 'х Гремячий' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000511' as code, 'х Дубовенька' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000516' as code, 'х Желобок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000521' as code, 'х Заречье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000526' as code, 'х Знаменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000531' as code, 'х Ивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000536' as code, 'х Красный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000541' as code, 'х Крепацкий' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000546' as code, 'х Марьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000551' as code, 'х Мухин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000556' as code, 'х Новая Заря' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000561' as code, 'х Новый Путь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000566' as code, 'х Панков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000571' as code, 'х Петровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000576' as code, 'х Пристень' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000581' as code, 'х Ржавец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000586' as code, 'х Саввин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000591' as code, 'х Стадников' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000596' as code, 'х Факовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000601' as code, 'х Шемраевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14750000606' as code, 'х Широкий' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000000' as code, 'Населенные пункты, входящие в состав городского округа Яковлевский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000001' as code, 'г Строитель' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000056' as code, 'п Томаровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000061' as code, 'п Яковлево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000106' as code, 'п Сажное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000111' as code, 'с Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000116' as code, 'с Бутово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000121' as code, 'с Быковка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000126' as code, 'с Верхний Ольшанец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000131' as code, 'с Вислое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000136' as code, 'с Волобуевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000141' as code, 'с Ворскла' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000146' as code, 'с Высокое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000151' as code, 'с Гостищево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000156' as code, 'с Дмитриевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000161' as code, 'с Драгунское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000166' as code, 'с Завидовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000171' as code, 'с Задельное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000176' as code, 'с Казацкое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000181' as code, 'с Калинино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000186' as code, 'с Клейменово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000191' as code, 'с Козычево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000196' as code, 'с Крапивное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000201' as code, 'с Красное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000206' as code, 'с Красный Отрожек' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000211' as code, 'с Кривцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000216' as code, 'с Крюково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000221' as code, 'с Кустовое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000226' as code, 'с Локня' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000231' as code, 'с Луханино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000236' as code, 'с Мариновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000241' as code, 'с Мощеное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000246' as code, 'с Неведомый Колодезь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000251' as code, 'с Непхаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000256' as code, 'с Новая Глинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000261' as code, 'с Новооскочное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000266' as code, 'с Новые Лозы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000271' as code, 'с Озерово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000276' as code, 'с Ольховка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000281' as code, 'с Подымовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000286' as code, 'с Пушкарное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000291' as code, 'с Раково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000296' as code, 'с Рождественка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000301' as code, 'с Сабынино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000306' as code, 'с Сажное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000311' as code, 'с Серетино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000316' as code, 'с Смородино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000321' as code, 'с Старая Глинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000326' as code, 'с Стрелецкое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000331' as code, 'с Терновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000336' as code, 'с Триречное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000341' as code, 'с Черкасское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000346' as code, 'с Чурсино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000351' as code, 'с Шопино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000356' as code, 'с Ямное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000361' as code, 'х Веселый' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000366' as code, 'х Вознесеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000371' as code, 'х Волохов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000376' as code, 'х Глушинский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000381' as code, 'х Домнино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000386' as code, 'х Дружный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000391' as code, 'х Дуброва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000396' as code, 'х Жданов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000401' as code, 'х Журавлиное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000406' as code, 'х Калинин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000411' as code, 'х Каменский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000416' as code, 'х Кисленко' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000421' as code, 'х Кондарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000426' as code, 'х Крапивенские Дворы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000431' as code, 'х Красное Подгороднее' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000436' as code, 'х Красный Восток' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000441' as code, 'х Крестов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000446' as code, 'х Махнов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000451' as code, 'х Мордовинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000456' as code, 'х Новоалександровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000461' as code, 'х Новоказацкий' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000466' as code, 'х Новочеркасский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000471' as code, 'х Редины Дворы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000476' as code, 'х Роговой' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000481' as code, 'х Семин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000486' as code, 'х Стрельников' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000491' as code, 'х Сырцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000496' as code, 'х Трубецкой' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000501' as code, 'х Фастов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000506' as code, 'х Федоренков' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000511' as code, 'х Цыхманов' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '14755000516' as code, 'х Шепелевка' as name, 2 as razd from dual
) b
on (a.code=b.code and a.status=0)
when not matched then
insert (id, record_id, version, status, code, name, razd)
values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval, b.version, 0, b.code, b.name, b.razd);

CASE SQL%ROWCOUNT 
WHEN 0 THEN dbms_output.put_line('Merge into ref_book_oktmo [WARNING]:'||' No changes was done');
ELSE dbms_output.put_line('Merge into ref_book_oktmo success: '||to_char(SQL%ROWCOUNT)||' rows');
END CASE; 

commit;

end;
/
exit;