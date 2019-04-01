set serveroutput on;
-- https://jira.aplana.com/browse/SBRFNDFL-7143 - Скрипт загрузки отсутствующих записей ОКТМО из УН (по коду)
begin
merge into ref_book_oktmo a using
(
select to_date('01.06.2017','dd.mm.yyyy') as version, '01641425103' as code, 'п им Владимира Ильича' as name, 2 as razd from dual union all 
select to_date('01.09.2018','dd.mm.yyyy') as version, '01645428104' as code, 'с Новосёловка' as name, 2 as razd from dual union all 
select to_date('01.09.2018','dd.mm.yyyy') as version, '01645428105' as code, 'с Омутское' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '01653488103' as code, 'с Бор-Кособулат' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '01653488104' as code, 'с Ляпуново' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '01653488105' as code, 'п Мирный' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '01653488111' as code, 'с Шадруха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000001' as code, 'г Благодарный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000106' as code, 'с Александрия' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000111' as code, 'с Алексеевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000116' as code, 'х Алтухов' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000121' as code, 'х Большевик' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000126' as code, 'с Бурлацкое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000131' as code, 'п Видный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000136' as code, 'п Госплодопитомник' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000141' as code, 'х Гремучий' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000146' as code, 'х Дейнекин' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000151' as code, 'с Елизаветинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000156' as code, 'п Каменка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000161' as code, 'с Каменная Балка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000166' as code, 'х Красный Ключ' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000171' as code, 'х Кучурин' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000176' as code, 'с Мирное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000181' as code, 'п Мокрая Буйвола' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000186' as code, 'п Молочный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000191' as code, 'х Новоалександровский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000196' as code, 'с Сотниковское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000201' as code, 'с Спасское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000206' as code, 'п Ставропольский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000211' as code, 'с Шишкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07705000216' as code, 'аул Эдельбай' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000106' as code, 'ст-ца Александрийская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000111' as code, 'п Балковский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000116' as code, 'ст-ца Георгиевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000121' as code, 'х им. Кирова' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000126' as code, 'с Краснокумское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000131' as code, 'п Крутоярский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000136' as code, 'ст-ца Лысогорская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000141' as code, 'ст-ца Незлобная' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000146' as code, 'п Нижнезольский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000151' as code, 'с Новозаведенное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000156' as code, 'х Новомихайловский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000161' as code, 'п Новоульяновский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000166' as code, 'п Новый' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000171' as code, 'с Обильное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000176' as code, 'п Ореховая Роща' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000181' as code, 'п Падинский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000186' as code, 'ст-ца Подгорная' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000191' as code, 'п Приэтокский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000196' as code, 'п Роговой' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000201' as code, 'п Семеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000206' as code, 'п Терский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000211' as code, 'п Ульяновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000216' as code, 'ст-ца Урухская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07707000221' as code, 'п Шаумянский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000001' as code, 'г Изобильный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000056' as code, 'п Рыздвяный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000061' as code, 'п Солнечнодольск' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000106' as code, 'ст-ца Баклановская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000111' as code, 'х Беляев' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000116' as code, 'ст-ца Гаевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000121' as code, 'ст-ца Каменнобродская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000126' as code, 'х Козлов' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000131' as code, 'х Красная Балка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000136' as code, 'п Левоегорлыкский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000141' as code, 'с Московское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000146' as code, 'с Найденовка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000151' as code, 'п Новоизобильный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000156' as code, 'ст-ца Новотроицкая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000161' as code, 'п Передовой' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000166' as code, 'с Подлужное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000171' as code, 'с Птичье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000176' as code, 'ст-ца Рождественская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000181' as code, 'х Смыков' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000186' as code, 'х Спорный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000191' as code, 'ст-ца Староизобильная' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000196' as code, 'х Сухой' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000201' as code, 'с Тищенское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000206' as code, 'ст-ца Филимоновская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07713000211' as code, 'х Широбоков' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000001' as code, 'г Ипатово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000106' as code, 'с Большая Джалга' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000111' as code, 'п Большевик' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000116' as code, 'х Бондаревский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000121' as code, 'с Бурукшун' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000126' as code, 'х Вавилон' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000131' as code, 'х Васильев' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000136' as code, 'п Верхнетахтинский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000141' as code, 'аул Верхний Барханчак' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000146' as code, 'х Верхний Кундуль' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000151' as code, 'х Веселый' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000156' as code, 'х Веселый' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000161' as code, 'п Винодельненский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000166' as code, 'х Водный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000171' as code, 'х Восточный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000176' as code, 'п Горлинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000181' as code, 'п Двуречный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000186' as code, 'с Добровольное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000191' as code, 'п Донцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000196' as code, 'п Дружный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000201' as code, 'п Залесный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000206' as code, 'с Золотаревка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000211' as code, 'п Калаусский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000216' as code, 'с Кевсала' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000221' as code, 'х Кочержинский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000226' as code, 'с Красная Поляна' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000231' as code, 'х Красный Кундуль' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000236' as code, 'п Красочный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000241' as code, 'с Крестьянское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000246' as code, 'с Лесная Дача' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000251' as code, 'с Лиман' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000256' as code, 'п Малоипатовский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000261' as code, 'п Малые Родники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000266' as code, 'аул Малый Барханчак' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000271' as code, 'х Мелиорация' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000276' as code, 'аул Нижний Барханчак' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000281' as code, 'с Новоандреевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000286' as code, 'п Новокрасочный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000291' as code, 'с Октябрьское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000296' as code, 'с Первомайское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000301' as code, 'п Правокугультинский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000306' as code, 'с Родники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000311' as code, 'п Советское Руно' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000316' as code, 'с Софиевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000321' as code, 'п Софиевский Городок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000326' as code, 'х Средний Кундуль' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000331' as code, 'с Тахта' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07714000336' as code, 'аул Юсуп-Кулакский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000001' as code, 'г Новопавловск' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000106' as code, 'с Горнозаводское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000111' as code, 'п Грибной' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000116' as code, 'х Веселый' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000121' as code, 'х Закавказский Партизан' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000126' as code, 'п Золка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000131' as code, 'ст-ца Зольская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000136' as code, 'п Зольский Карьер' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000141' as code, 'п Камышовый' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000146' as code, 'п Коммаяк' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000151' as code, 'п Комсомолец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000156' as code, 'х Крупско-Ульяновский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000161' as code, 'х Курганный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000166' as code, 'х Липчанский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000171' as code, 'ст-ца Марьинская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000176' as code, 'с Новосредненское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000181' as code, 'с Орловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000186' as code, 'х Пегушин' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000191' as code, 'п Прогресс' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000196' as code, 'ст-ца Советская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000201' as code, 'х Совпахарь' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000206' as code, 'ст-ца Старопавловская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07716000211' as code, 'п Фазанный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000001' as code, 'г Нефтекумск' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000056' as code, 'п Затеречный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000106' as code, 'аул Абдул-Газы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000111' as code, 'аул Абрам-Тюбе' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000116' as code, 'аул Артезиан-Мангит' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000121' as code, 'аул Бакрес' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000126' as code, 'аул Бейсей' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000131' as code, 'аул Бияш' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000136' as code, 'аул Кок-Бас' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000141' as code, 'аул Кунай' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000146' as code, 'аул Махач-Аул' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000151' as code, 'аул Махмуд-Мектеб' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000156' as code, 'аул Новкус-Артезиан' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000161' as code, 'аул Тукуй-Мектеб' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000166' as code, 'аул Уллуби-Юрт' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000171' as code, 'аул Уч-Тюбе' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000176' as code, 'аул Ямангой' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000181' as code, 'п Зимняя Ставка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000186' as code, 'п Зункарь' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000191' as code, 'п Левобалковский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000196' as code, 'с Ачикулак' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000201' as code, 'с Кара-Тюбе' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000206' as code, 'с Каясула' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000211' as code, 'с Озек-Суат' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07725000216' as code, 'х Андрей-Курган' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000001' as code, 'г Новоалександровск' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000106' as code, 'х Верный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000111' as code, 'п Виноградный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000116' as code, 'х Воровский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000121' as code, 'ст-ца Воскресенская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000126' as code, 'п Восточный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000131' as code, 'п Встречный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000136' as code, 'х Ганькин' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000141' as code, 'п Горьковский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000146' as code, 'ст-ца Григорополисская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000151' as code, 'п Дружба' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000156' as code, 'п Заречный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000161' as code, 'ст-ца Кармалиновская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000166' as code, 'п Кармалиновский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000171' as code, 'х Керамик' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000176' as code, 'х Краснодарский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000181' as code, 'п Краснозоринский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000186' as code, 'п Краснокубанский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000191' as code, 'х Красночервонный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000196' as code, 'п Крутобалковский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000201' as code, 'п Курганный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000206' as code, 'п Лиманный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000211' as code, 'х Мокрая Балка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000216' as code, 'п Озерный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000221' as code, 'х Первомайский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000226' as code, 'х Петровский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000231' as code, 'п Присадовый' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000236' as code, 'п Равнинный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000241' as code, 'п Радуга' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000246' as code, 'с Раздольное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000251' as code, 'п Рассвет' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000256' as code, 'ст-ца Расшеватская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000261' as code, 'х Родионов' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000266' as code, 'х Румяная Балка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000271' as code, 'п Светлый' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000276' as code, 'п Славенский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000281' as code, 'п Темижбекский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000286' as code, 'п Ударный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000291' as code, 'х Фельдмаршальский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000296' as code, 'х Чапцев' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07726000301' as code, 'п Южный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000001' as code, 'г Светлоград' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000106' as code, 'с Благодатное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000111' as code, 'х Вознесенский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000116' as code, 'с Высоцкое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000121' as code, 'п Горный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000126' as code, 'с Гофицкое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000131' as code, 'с Донская Балка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000136' as code, 'х Козинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000141' as code, 'с Константиновское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000146' as code, 'с Кугуты' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000151' as code, 'с Мартыновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000156' as code, 'п Маяк' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000161' as code, 'с Николина Балка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000166' as code, 'х Носачев' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000171' as code, 'с Ореховка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000176' as code, 'п Полевой' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000181' as code, 'п Прикалаусский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000186' as code, 'с Просянка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000191' as code, 'п Пшеничный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000196' as code, 'п Рогатая Балка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000201' as code, 'х Соленое Озеро' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000206' as code, 'с Сухая Буйвола' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000211' as code, 'х Сычевский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000216' as code, 'п Цветочный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000221' as code, 'с Шангала' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07731000226' as code, 'с Шведино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000001' as code, 'г Зеленокумск' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000106' as code, 'х Андреевский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000111' as code, 'п Брусиловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000116' as code, 'х Восточный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000121' as code, 'х Глубокий' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000126' as code, 'с Горькая Балка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000131' as code, 'п Железнодорожный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000136' as code, 'х Кавказский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000141' as code, 'х Ковганский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000146' as code, 'х Колесников' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000151' as code, 'п Колтуновский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000156' as code, 'х Кононов' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000161' as code, 'п Михайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000166' as code, 'с Нины' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000171' as code, 'с Отказное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000176' as code, 'х Петровский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000181' as code, 'с Правокумское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000186' as code, 'х Привольный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000191' as code, 'х Примерный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000196' as code, 'х Рог' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000201' as code, 'п Селивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000206' as code, 'х Средний Лес' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000211' as code, 'с Солдато-Александровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000216' as code, 'х Тихомировка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000221' as code, 'х Федоровский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '07735000226' as code, 'х Чарыков' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '08624407106' as code, 'п Среднехорский' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '08624425114' as code, 'с Дальневосточное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '08644432' as code, 'Горненское' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '08644432101' as code, 'п Горный' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '10770000001' as code, 'г Циолковский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '11605442103' as code, 'д Горночаровская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '11605442104' as code, 'д Завелье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '11605442105' as code, 'д Заподюжье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '11605442123' as code, 'п Рылковский Погост' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '11650426106' as code, 'п Авангард' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '11650426111' as code, 'п Лиственичный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '11650426116' as code, 'п Ломовое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '11650426121' as code, 'п Малька' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '11650426126' as code, 'п Холмогорская' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '12620425103' as code, 'с Зюзино' as name, 2 as razd from dual union all 
select to_date('01.10.2018','dd.mm.yyyy') as version, '12625436103' as code, 'с Лебяжье' as name, 2 as razd from dual union all 
select to_date('01.10.2018','dd.mm.yyyy') as version, '12625436104' as code, 'п Октябрьский' as name, 2 as razd from dual union all 
select to_date('01.10.2018','dd.mm.yyyy') as version, '12625436116' as code, 'с Полдневое' as name, 2 as razd from dual union all 
select to_date('01.10.2018','dd.mm.yyyy') as version, '12625436121' as code, 'п Дамчик' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '12630404108' as code, 'с Малый Арал' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151102' as code, 'с Бирючья Коса' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151103' as code, 'с Забурунное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151104' as code, 'с Бударино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151105' as code, 'с Дальнее' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151111' as code, 'с Воскресеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151116' as code, 'с Камышово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151121' as code, 'с Яр-Базар' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151126' as code, 'с Зорино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151131' as code, 'с Караванное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151136' as code, 'п железнодорожного разъезда № 4' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151141' as code, 'с Кряжевое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151146' as code, 'с Судачье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151151' as code, 'с Михайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151156' as code, 'с Заречное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151161' as code, 'с Новогеоргиевск' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151166' as code, 'с Проточное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151171' as code, 'с Рынок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '12635151176' as code, 'с Вышка' as name, 2 as razd from dual union all 
select to_date('01.10.2018','dd.mm.yyyy') as version, '12640448116' as code, 'п Наримановский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410' as code, 'Заречное' as name, 1 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410101' as code, 'д Аристово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410106' as code, 'д Агеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410111' as code, 'д Алексеевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410116' as code, 'д Анциферово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410121' as code, 'д Балагурово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410126' as code, 'д Бахарево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410131' as code, 'д Баюшевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410136' as code, 'д Беляшкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410141' as code, 'д Бернятино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410146' as code, 'д Биричево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410151' as code, 'д Большие Крутцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410156' as code, 'д Большие Слободы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410161' as code, 'д Большое Чебаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410166' as code, 'д Бор' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410171' as code, 'д Буково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410176' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410181' as code, 'д Вепрево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410186' as code, 'д Верхнее Бородкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410191' as code, 'д Верхнее Грибцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410196' as code, 'д Верхнее Панкратово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410201' as code, 'д Верхний Заемкуч' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410206' as code, 'д Викторово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410211' as code, 'д Высокая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410216' as code, 'д Гора-Семеновская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410221' as code, 'д Горлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410226' as code, 'д Горяево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410231' as code, 'д Грибино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410236' as code, 'д Гришино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410241' as code, 'д Демидово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410246' as code, 'д Деревенька' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410251' as code, 'д Едново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410256' as code, 'д Игнатьевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410261' as code, 'д Измарухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410266' as code, 'д Изонинская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410271' as code, 'с Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410276' as code, 'д Исток' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410281' as code, 'д Карасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410286' as code, 'д Климлево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410291' as code, 'д Козлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410296' as code, 'д Конково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410301' as code, 'д Копылово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410306' as code, 'д Копылово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410311' as code, 'д Королево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410316' as code, 'д Кривая Береза' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410321' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410326' as code, 'д Кузьминская Выставка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410331' as code, 'д Кузьминское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410336' as code, 'д Кулаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410341' as code, 'д Куракино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410346' as code, 'д Кушалово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410351' as code, 'д Лаврешово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410356' as code, 'д Логиновская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410361' as code, 'д Луза' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410366' as code, 'д Лучнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410371' as code, 'д Малое Петровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410376' as code, 'д Малое Чебаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410381' as code, 'д Мартищево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410386' as code, 'д Милославская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410391' as code, 'д Мителево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410396' as code, 'д Михайловская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410401' as code, 'д Насоново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410406' as code, 'д Нижнее Грибцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410411' as code, 'д Нижнее Панкратово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410416' as code, 'д Нижний Заемкуч' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410421' as code, 'д Новое Завражье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410426' as code, 'д Новое Рожково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410431' as code, 'д Новое Семенниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410436' as code, 'д Новоселово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410441' as code, 'д Павшино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410446' as code, 'с Палема' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410451' as code, 'д Парфеново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410456' as code, 'д Парфеновская Выставка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410461' as code, 'д Пенье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410466' as code, 'д Первомайское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410471' as code, 'д Погорелово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410476' as code, 'д Подберезье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410481' as code, 'д Подволочье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410486' as code, 'д Подворские' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410491' as code, 'д Поповское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410496' as code, 'д Пополутково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410501' as code, 'д Прислон' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410506' as code, 'д Пустая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410511' as code, 'д Родионовица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410516' as code, 'д Рожково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410521' as code, 'д Рукавишниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410526' as code, 'д Семенниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410531' as code, 'д Слинкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410536' as code, 'д Слободка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410541' as code, 'д Смолинская Выставка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410546' as code, 'д Сондас' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410551' as code, 'д Старое Завражье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410556' as code, 'д Тараканово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410561' as code, 'д Угол' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410566' as code, 'д Усов Починок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410571' as code, 'д Уткино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410576' as code, 'д Федоровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410581' as code, 'д Филатово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410586' as code, 'д Холмец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410591' as code, 'д Чернаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410596' as code, 'д Черная' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410601' as code, 'д Чучеры' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410606' as code, 'д Шастово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410611' as code, 'д Юрьевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614410616' as code, 'д Ярыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428102' as code, 'д Афанасовец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428103' as code, 'д Березово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428104' as code, 'д Большое Вострое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428105' as code, 'д Большое Каликино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428113' as code, 'д Выползово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428123' as code, 'д Григорьевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428128' as code, 'д Давыдовское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428137' as code, 'д Загорье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428138' as code, 'д Запань' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428139' as code, 'д Заручевье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428168' as code, 'д Ленивица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428169' as code, 'д Лодейка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428174' as code, 'д Малая Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428178' as code, 'д Мартыново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428188' as code, 'д Мякальская Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428190' as code, 'д Нижний Прилук' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428208' as code, 'д Печерза' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428214' as code, 'д Пупышево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428218' as code, 'д Ровдино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428220' as code, 'д Рупосово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428223' as code, 'д Скорятино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428225' as code, 'д Соловьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428238' as code, 'д Тишино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614428254' as code, 'д Царева Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476108' as code, 'д Антипово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476124' as code, 'д Бакшеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476148' as code, 'с Верхняя Шарденьга' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476154' as code, 'д Выставка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476163' as code, 'д Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476165' as code, 'д Горбачево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476173' as code, 'д Жуково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476175' as code, 'д Загорье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476183' as code, 'д Истопная' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476185' as code, 'д Касьянка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476188' as code, 'д Липовец 1-й' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476190' as code, 'д Липовка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476193' as code, 'д Москвин Починок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476195' as code, 'д Мурдинская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476202' as code, 'д Орлов Починок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476203' as code, 'д Осница' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476204' as code, 'д Подвалье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476205' as code, 'д Подволочье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476208' as code, 'д Ребцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476218' as code, 'д Слободчиково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476224' as code, 'д Старый Починок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476238' as code, 'д Упирево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19614476246' as code, 'д Якушино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420102' as code, 'д Аксеновская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420103' as code, 'д Анисимовская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420104' as code, 'д Анциферовская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420105' as code, 'д Бирючевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420107' as code, 'д Боярская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420108' as code, 'д Ворониха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420109' as code, 'д Гнилужская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420110' as code, 'д Елисеевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420112' as code, 'д Захаровская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420113' as code, 'д Ивановская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420114' as code, 'д Козевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420115' as code, 'д Кузнецовская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420117' as code, 'д Матвеевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420118' as code, 'д Мухинская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420119' as code, 'д Оринодоры' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420120' as code, 'д Осташевская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420122' as code, 'п Рогна' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420123' as code, 'д Ряполовская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420124' as code, 'д Савинская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420125' as code, 'д Сакулинская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420131' as code, 'д Сафроновская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420136' as code, 'д Секушинская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420141' as code, 'д Студенцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19616420146' as code, 'д Харитоновская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464132' as code, 'д Ватланово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464134' as code, 'д Водогино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464138' as code, 'д Высоково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464152' as code, 'д Горшково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464153' as code, 'д Дорково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464154' as code, 'д Еремеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464155' as code, 'п Ермаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464162' as code, 'д Есиково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464164' as code, 'д Закрышкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464173' as code, 'д Кедрово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464188' as code, 'д Колкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464198' as code, 'д Кубаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464218' as code, 'п Лесково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464220' as code, 'д Макарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464224' as code, 'д Марково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464248' as code, 'д Нагорье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464250' as code, 'д Назарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464254' as code, 'д Новое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464258' as code, 'д Отрадное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464268' as code, 'д Починок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464278' as code, 'д Прокунино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464283' as code, 'д Рогозкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464285' as code, 'п Рубцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464293' as code, 'д Скорбежево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464295' as code, 'д Смольево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464298' as code, 'д Спирино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464313' as code, 'д Тимофеевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464324' as code, 'д Шоломово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620464328' as code, 'д Юрьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468117' as code, 'д Болотово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468118' as code, 'д Борборино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468119' as code, 'д Бурдуково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468120' as code, 'рзд Бурдуково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468124' as code, 'п Васильевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468143' as code, 'д Глушица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468153' as code, 'д Долгово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468172' as code, 'д Закобяйкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468173' as code, 'д Захарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468174' as code, 'д Ивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468175' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468177' as code, 'д Калинкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468178' as code, 'д Карцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468207' as code, 'д Копцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468209' as code, 'д Косково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468234' as code, 'д Лобково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468238' as code, 'д Лукинцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468242' as code, 'д Марково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468244' as code, 'д Матвеевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468297' as code, 'д Низьма' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468298' as code, 'д Никулино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468299' as code, 'ж/д ст Паприха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468322' as code, 'д Поповское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468324' as code, 'д Порошино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468328' as code, 'д Редькино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468330' as code, 'д Рогачево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468352' as code, 'д Спасс' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468354' as code, 'д Тишиново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468358' as code, 'д Фроловское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468374' as code, 'д Чернецкое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19620468388' as code, 'д Яковлево' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408108' as code, 'д Анциферово' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408110' as code, 'д Берег' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408113' as code, 'д Вашуково' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408162' as code, 'д Загородская' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408168' as code, 'д Каньшино' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408188' as code, 'д Крюковская' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408208' as code, 'д Лахново' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408210' as code, 'д Лечино' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408233' as code, 'д Мишино' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408238' as code, 'д Никулино' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408258' as code, 'п Октябрьский' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408298' as code, 'д Пигарево' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408334' as code, 'д Саминский Погост' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408343' as code, 'д Силово' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408363' as code, 'д Тикачево' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408365' as code, 'д Титово' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408383' as code, 'д Усть-Пажье' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622408385' as code, 'д Чеково' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444102' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444103' as code, 'п Васюковские Острова' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444104' as code, 'д Верхнее Понизовье' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444107' as code, 'д Верховье' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444108' as code, 'д Голяши' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444109' as code, 'д Голяши' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444110' as code, 'д Гора' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444113' as code, 'д Ежины' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444123' as code, 'д Кондушский Погост' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444125' as code, 'с Коштуги' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444127' as code, 'д Кюршево' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444128' as code, 'д Ларшина' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444129' as code, 'д Лема' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444130' as code, 'п Межозерье' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444132' as code, 'д Мостовая' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444133' as code, 'д Нижнее Понизовье' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444134' as code, 'д Новинка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444135' as code, 'д Озерное Устье' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444137' as code, 'с Ошта' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444138' as code, 'д Палозеро' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444139' as code, 'д Палтога' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444140' as code, 'д Пустошь' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444146' as code, 'д Сяргозеро' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '19622444151' as code, 'д Трутнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424108' as code, 'д Акуловское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424114' as code, 'д Антропьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424188' as code, 'д Иванищево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424218' as code, 'д Коцыно' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424234' as code, 'д Макарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424244' as code, 'д Марковское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424254' as code, 'д Мотовилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424258' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424324' as code, 'д Сахарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424338' as code, 'д Становое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424340' as code, 'д Степановское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424343' as code, 'д Тупицыно' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424345' as code, 'д Турыбанино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424349' as code, 'с Шейбухта' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19632424371' as code, 'д Юсово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428112' as code, 'д Баданки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428114' as code, 'д Березово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428115' as code, 'д Блудново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428117' as code, 'д Большое Сверчково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428119' as code, 'д Бродовица' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428133' as code, 'починок Гороховский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428135' as code, 'д Дворище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428143' as code, 'починок Зырянский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428154' as code, 'д Калауз' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428177' as code, 'д Куданга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428178' as code, 'п Кудангский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428179' as code, 'п Лесная Роща' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428180' as code, 'д Липово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428185' as code, 'д Малое Сверчково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428202' as code, 'д Пахомово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428203' as code, 'д Пермас' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428204' as code, 'починок Пермасский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428207' as code, 'д Повечерная' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428232' as code, 'д Сторожевая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428234' as code, 'д Тарасовы Лога' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '19634428237' as code, 'д Шири' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404107' as code, 'д Анофринское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404108' as code, 'д Антуфьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404109' as code, 'д Афанасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404110' as code, 'д Бакулино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404113' as code, 'д Бессолово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404118' as code, 'д Большой Двор' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404120' as code, 'д Борщево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404123' as code, 'д Бурцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404127' as code, 'д Вахнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404128' as code, 'д Вачево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404129' as code, 'с Великий Двор' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404130' as code, 'д Герасимово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404137' as code, 'д Голодеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404138' as code, 'д Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404139' as code, 'с Грибцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404140' as code, 'д Деревенька' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404142' as code, 'д Дмитриково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404143' as code, 'д Заболотье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404144' as code, 'д Заледеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404153' as code, 'д Иваниха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404163' as code, 'д Исаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404168' as code, 'д Истоминское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404170' as code, 'д Карьер' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404177' as code, 'д Кокошилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404178' as code, 'д Конаниха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404179' as code, 'д Копосиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404180' as code, 'д Копытово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404183' as code, 'д Кромино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404188' as code, 'д Кузнечиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404197' as code, 'д Лагуново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404198' as code, 'д Лебечиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404199' as code, 'д Левково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404202' as code, 'д Марфинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404203' as code, 'д Медведево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404204' as code, 'д Меленка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404205' as code, 'д Михалево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404207' as code, 'д Морженга' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404209' as code, 'д Мялицыно' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404212' as code, 'д Нестерово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404213' as code, 'д Никулинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404214' as code, 'д Новое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404215' as code, 'д Острилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404217' as code, 'д Панютино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404218' as code, 'д Пахино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404227' as code, 'д Покровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404228' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404229' as code, 'д Починок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404237' as code, 'д Пустыня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404238' as code, 'д Решетниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404239' as code, 'д Рыкуля' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404240' as code, 'д Рылово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404248' as code, 'д Сверчково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404258' as code, 'д Середнее' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404262' as code, 'д Сониха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404263' as code, 'д Сосновец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404264' as code, 'д Спицыно' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404265' as code, 'д Трепарево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404267' as code, 'д Труфаново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404268' as code, 'д Туреево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404270' as code, 'д Угол' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404278' as code, 'д Чепурово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404280' as code, 'д Шилыково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404286' as code, 'д Шулепово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '19638404291' as code, 'д Щуриха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '20654456116' as code, 'с Николаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151102' as code, 'с Александровка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151103' as code, 'с Березовка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151104' as code, 'с Гари' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151105' as code, 'с Дубовка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151107' as code, 'с Журелейка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151108' as code, 'д Кавлей' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151109' as code, 'д Каркалей' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151110' as code, 'с Кармалейка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151112' as code, 'с Кудлей' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151134' as code, 'с Сиязьма' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22602151156' as code, 'с Чуварлей-Майдан' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000001' as code, 'г Перевоз' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000102' as code, 'п им Дзержинского' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000105' as code, 'с Мармыжи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000108' as code, 'п Новый Путь' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000111' as code, 'с Ревезень' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000114' as code, 'п Смородиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000117' as code, 'п Шильниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000120' as code, 'с Большие Кемары' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000123' as code, 'с Дубское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000126' as code, 'д Малые Кемары' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000129' as code, 'д Чепас' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000132' as code, 'д Балахна' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000135' as code, 'с Ичалки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000138' as code, 'д Козловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000141' as code, 'с Корсаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000144' as code, 'п Красная Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000147' as code, 'с Пилекшево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000150' as code, 'с Сунеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000153' as code, 'д Вязовка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000156' as code, 'п Гари' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000159' as code, 'д Дубки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000162' as code, 'п Зеленуха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000165' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000168' as code, 'д Крутое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000171' as code, 'д Павловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000174' as code, 'с Палец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000177' as code, 'п Широкий' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000180' as code, 'д Беляниха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000183' as code, 'п Борок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000186' as code, 'с Гридино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000189' as code, 'д Карташиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000192' as code, 'д Киселиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000195' as code, 'д Медведково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000198' as code, 'д Нерослиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000201' as code, 'д Орлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000204' as code, 'д Сквозново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000207' as code, 'с Танайково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000210' as code, 'д Фатьянково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000213' as code, 'с Шпилево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000216' as code, 'с Горышкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000219' as code, 'д Ковалево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000222' as code, 'с Тилинино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000225' as code, 'с Шершово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000228' as code, 'с Ягодное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000231' as code, 'с Вельдеманово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000234' as code, 'с Выжлеи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000237' as code, 'д Заключная' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000240' as code, 'п Зименки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000243' as code, 'д Коноплянка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000246' as code, 'с Поляна' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000249' as code, 'д Селищи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000252' as code, 'п Центральный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '22739000255' as code, 'д Чергать' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '24635420103' as code, 'с Мугреевский' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '24635420116' as code, 'д 56 Пикет' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '25614158106' as code, 'д Юхта' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '25614158111' as code, 'с Тарасово' as name, 2 as razd from dual union all 
select to_date('01.12.2018','dd.mm.yyyy') as version, '25620701912' as code, 'п Визирный' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '25640153106' as code, 'с Мальта' as name, 2 as razd from dual union all 
select to_date('01.07.2017','dd.mm.yyyy') as version, '26720000001' as code, 'г Сунжа' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000001' as code, 'г Багратионовск' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000106' as code, 'п Березовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000111' as code, 'п Большаковское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000116' as code, 'п Большое Озерное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000121' as code, 'п Боровое' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000126' as code, 'п Гвардейское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000131' as code, 'п Дубки' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000136' as code, 'п Загородное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000141' as code, 'п Знаменское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000146' as code, 'п Ильюшино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000151' as code, 'п Курское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000156' as code, 'п Малое Озерное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000161' as code, 'п Марийское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000166' as code, 'п Минино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000171' as code, 'п Московское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000176' as code, 'п Надеждино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000181' as code, 'п Невское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000186' as code, 'п Новоселки' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000191' as code, 'п Орехово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000196' as code, 'п Осокино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000201' as code, 'п Прудки' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000206' as code, 'п Песочное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000211' as code, 'п Рябиновка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000216' as code, 'п Славяновка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000221' as code, 'п Солдатское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000226' as code, 'п Солнечное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000231' as code, 'п Сергеево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000236' as code, 'п Стрельня' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000241' as code, 'п Староселье' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000246' as code, 'п Тамбовское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000251' as code, 'п Тишино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000256' as code, 'п Чехово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000261' as code, 'п Августовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000266' as code, 'п Богатово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000271' as code, 'п Вальки' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000276' as code, 'п Высокое' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000281' as code, 'п Долгоруково' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000286' as code, 'п Дубровка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000291' as code, 'п Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000296' as code, 'п Красноармейское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000301' as code, 'п Краснознаменское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000306' as code, 'п Лермонтово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000311' as code, 'п Нагорное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000316' as code, 'п Побережье' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000321' as code, 'п Пограничное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000326' as code, 'п Подгорное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000331' as code, 'п Пушкино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000336' as code, 'п Славское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000341' as code, 'п Чапаево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000346' as code, 'п Широкое' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000351' as code, 'п Владимирово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000356' as code, 'п Заречное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000361' as code, 'п Калмыково' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000366' as code, 'п Линейное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000371' as code, 'п Майское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000376' as code, 'п Нивенское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000381' as code, 'п Партизанское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000386' as code, 'п Победа' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000391' as code, 'п Садовое' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000396' as code, 'п Северный' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000401' as code, 'п Южный' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000406' as code, 'п Малое Отважное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000411' as code, 'п Отважное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000416' as code, 'п Большедорожное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000421' as code, 'п Ветрово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000426' as code, 'п Жуковка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000431' as code, 'п Знаменка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000436' as code, 'п Ильичевка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000441' as code, 'п Корнево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000446' as code, 'п Косатухино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000451' as code, 'п Медовое' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000456' as code, 'п Кунцево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000461' as code, 'п Московское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000466' as code, 'п Мушкино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000471' as code, 'п Ново-Московское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000476' as code, 'п Новоселово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000481' as code, 'п Октябрьское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000486' as code, 'п Первомайское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000491' as code, 'п Пограничный' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000496' as code, 'п Приморское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000501' as code, 'п Пролетарское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000506' as code, 'п Пятидорожное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000511' as code, 'п Раздольное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000516' as code, 'п Совхозное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000521' as code, 'п Сосновка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000526' as code, 'п Тимирязево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000531' as code, 'п Тропинино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27703000536' as code, 'п Яблочкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000' as code, 'Балтийский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000001' as code, 'г Балтийск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000006' as code, 'г Приморск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000106' as code, 'п Береговое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000111' as code, 'п Лунино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000116' as code, 'п Дивное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000121' as code, 'п Крыловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000126' as code, 'п Нивы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000131' as code, 'п Парусное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000136' as code, 'п Прозорово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000141' as code, 'п Тихореченское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000146' as code, 'п Цветное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27705000151' as code, 'п Черемухино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000001' as code, 'г Неман' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000106' as code, 'п Акулово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000111' as code, 'п Артемовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000116' as code, 'п Большое Село' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000121' as code, 'п Барсуковка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000126' as code, 'п Березовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000131' as code, 'п Бобры' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000136' as code, 'п Ватутино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000141' as code, 'п Ветрово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000146' as code, 'п Волочаево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000151' as code, 'п Гарино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000156' as code, 'п Ганновка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000161' as code, 'п Говорово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000166' as code, 'п Гривино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000171' as code, 'п Грушевка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000176' as code, 'п Гудково' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000181' as code, 'п Дубки' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000186' as code, 'п Думиничи' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000191' as code, 'п Дубравино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000196' as code, 'п Жданки' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000201' as code, 'п Жилино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000206' as code, 'п Загорское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000211' as code, 'п Забродино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000216' as code, 'п Зайцево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000221' as code, 'п Игнатово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000226' as code, 'п Искра' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000231' as code, 'п Канаш' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000236' as code, 'п Каштановка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000241' as code, 'п Котельниково' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000246' as code, 'п Красное Село' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000251' as code, 'п Кустово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000256' as code, 'п Лесное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000261' as code, 'п Лунино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000266' as code, 'п Лукьяново' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000271' as code, 'п Маломожайское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000276' as code, 'п Мичуринский' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000281' as code, 'п Новоколхозное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000286' as code, 'п Обручево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000291' as code, 'п Пелевино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000296' as code, 'п Подгорное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000301' as code, 'п Пушкино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000306' as code, 'п Ракитино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000311' as code, 'п Рудаково' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000316' as code, 'п Рядино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000321' as code, 'п Становое' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000326' as code, 'п Тушино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000331' as code, 'п Ульяново' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000336' as code, 'п Фадеево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000341' as code, 'п Шмелево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27714000346' as code, 'п Шепетовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000' as code, 'Нестеровский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000001' as code, 'г Нестеров' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000106' as code, 'п Ватутино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000111' as code, 'п Высокое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000116' as code, 'п Дивное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000121' as code, 'п Заводское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000126' as code, 'п Зеленое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000131' as code, 'п Илюшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000136' as code, 'п Калиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000141' as code, 'п Нежинское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000146' as code, 'п Садовое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000151' as code, 'п Совхозное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000156' as code, 'п Сосновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000161' as code, 'п Фурмановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000166' as code, 'п Хуторское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000171' as code, 'п Чкалово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000176' as code, 'п Шолохово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000181' as code, 'п Ясная Поляна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000186' as code, 'п Бабушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000191' as code, 'п Вознесенское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000196' as code, 'п Воскресенское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000201' as code, 'п Выселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000206' as code, 'п Детское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000211' as code, 'п Луговое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000216' as code, 'п Невское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000221' as code, 'п Некрасово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000226' as code, 'п Первомайское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000231' as code, 'п Петровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000236' as code, 'п Покрышкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000241' as code, 'п Пригородное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000246' as code, 'п Пушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000251' as code, 'п Раздольное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000256' as code, 'п Чапаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000261' as code, 'п Чернышевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000266' as code, 'п Черняхово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000271' as code, 'п Ягодное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000276' as code, 'п Боровиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000281' as code, 'п Ветряк' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000286' as code, 'п Дмитриевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000291' as code, 'п Докучаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000296' as code, 'п Дубовая Роща' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000301' as code, 'п Знаменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000306' as code, 'п Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000311' as code, 'п Калинино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000316' as code, 'п Карпинское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000321' as code, 'п Краснолесье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000326' as code, 'п Лесистое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000331' as code, 'п Мичуринское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000336' as code, 'п Озерки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000341' as code, 'п Пугачево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000346' as code, 'п Садовое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000351' as code, 'п Сосновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000356' as code, 'п Токаревка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000361' as code, 'п Уварово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '27715000366' as code, 'п Чистые Пруды' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000001' as code, 'г Полесск' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000106' as code, 'п Подсобный' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000111' as code, 'п Тюленино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000116' as code, 'п Беломорское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000121' as code, 'п Головкино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000126' as code, 'п Заливино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000131' as code, 'п Красное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000136' as code, 'п Малая Матросовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000141' as code, 'п Матросово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000146' as code, 'п Разино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000151' as code, 'п Александровка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000156' as code, 'п Ближнее' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000161' as code, 'п Виноградное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000166' as code, 'п Дальнее' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000171' as code, 'п Залесье' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000176' as code, 'п Заречье' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000181' as code, 'п Зеленово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000186' as code, 'п Зуевка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000191' as code, 'п Искрово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000196' as code, 'п Каштаново' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000201' as code, 'п Краснохолмское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000206' as code, 'п Новая Жизнь' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000211' as code, 'п Новосельское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000216' as code, 'п Октябрьское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000221' as code, 'п Пески' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000226' as code, 'п Полевой' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000231' as code, 'п Ягодное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000236' as code, 'п Березовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000241' as code, 'п Богатово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000246' as code, 'п Григорьевка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000251' as code, 'п Ельниково' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000256' as code, 'п Заповедники' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000261' as code, 'п Изобильное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000266' as code, 'п Ильичево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000271' as code, 'п Красный Бор' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000276' as code, 'п Ломоносовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000281' as code, 'п Марксово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000286' as code, 'п Междулесье' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000291' as code, 'п Новая Деревня' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000296' as code, 'п Петино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000301' as code, 'п Саранское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000306' as code, 'п Сосновка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000311' as code, 'п Февральское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000316' as code, 'п Шолохово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000321' as code, 'п Бригадное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000326' as code, 'п Дружное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000331' as code, 'п Журавлевка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000336' as code, 'п Зеленое' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000341' as code, 'п Ивановка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000346' as code, 'п Июльское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000351' as code, 'п Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000356' as code, 'п Липовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000361' as code, 'п Майское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000366' as code, 'п Нахимово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000371' as code, 'п Некрасово' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000376' as code, 'п Никитовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000381' as code, 'п Овражье' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000386' as code, 'п Придорожное' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000391' as code, 'п Речки' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000396' as code, 'п Рыбкино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000401' as code, 'п Свободный' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000406' as code, 'п Сибирское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000411' as code, 'п Славянское' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000416' as code, 'п Трудовой' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000421' as code, 'п Тургенево' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000426' as code, 'п Ушаковка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '27718000431' as code, 'п Фурмановка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '27734000' as code, 'Светлогорский' as name, 1 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '27734000001' as code, 'г Светлогорск' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '27734000106' as code, 'п Донское' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '27734000111' as code, 'п Лесное' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '27734000116' as code, 'п Марьинское' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '27734000121' as code, 'п Маяк' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '27734000126' as code, 'п Молодогвардейское' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '27734000131' as code, 'п Приморье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410122' as code, 'д Борщево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410127' as code, 'д Бронниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410147' as code, 'д Данилково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410162' as code, 'д Комлево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410182' as code, 'д Медянки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410187' as code, 'д Мотаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410188' as code, 'д Петровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410197' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410207' as code, 'д Пронино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410208' as code, 'д Романцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410209' as code, 'д Савелово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410212' as code, 'д Селиваново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410217' as code, 'д Столбищи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410222' as code, 'д Тарачево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410223' as code, 'д Тебеньки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410232' as code, 'д Ушаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410233' as code, 'д Хмельнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410234' as code, 'д Чернецкое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410237' as code, 'д Шенское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410247' as code, 'д Барское Александрово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410252' as code, 'д Высокое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410253' as code, 'д Вялье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410257' as code, 'д Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28610410262' as code, 'д Кузьминское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28628406102' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408102' as code, 'д Алайково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408103' as code, 'д Афанасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408107' as code, 'д Васиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408108' as code, 'д Виноколы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408109' as code, 'д Владенино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408122' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408137' as code, 'д Ивашиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408138' as code, 'с Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408147' as code, 'п Крючково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408148' as code, 'д Лазарево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408157' as code, 'д Никифорово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408177' as code, 'д Поршинец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408192' as code, 'д Рычково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408202' as code, 'д Старо-Потрасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638408207' as code, 'д Федово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420102' as code, 'д Большая Переходня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420107' as code, 'д Бочельниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420117' as code, 'д Дойбино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420118' as code, 'д Дроздово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420132' as code, 'д Зайково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420137' as code, 'д Иваново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420147' as code, 'д Кожухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420157' as code, 'д Лежнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420167' as code, 'д Малая Переходня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420187' as code, 'д Первитино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420197' as code, 'д Подрезово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420222' as code, 'д Степаново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420227' as code, 'д Холм' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638420236' as code, 'д Рогово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438112' as code, 'д Барановка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438113' as code, 'д Бархатиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438114' as code, 'д Белочеревица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438115' as code, 'с Большое Плоское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438142' as code, 'д Затулки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438157' as code, 'д Крапивка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438162' as code, 'д Лисьи Горы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438163' as code, 'д Локотцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438177' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438182' as code, 'д Мотошелиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438187' as code, 'д Некрасиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438207' as code, 'д Осипково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438217' as code, 'д Пруды' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438218' as code, 'д Пурышево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438227' as code, 'д Соколово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438232' as code, 'д Степная Нива' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438247' as code, 'д Тимошкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438248' as code, 'д Трещетино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28638438256' as code, 'д Чашково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410112' as code, 'д Борисово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410113' as code, 'д Бортники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410114' as code, 'д Васильевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410132' as code, 'д Коробово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410133' as code, 'д Крючье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410137' as code, 'д Ленино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410152' as code, 'д Рожково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410153' as code, 'д Рябово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410154' as code, 'д Сидорово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410157' as code, 'д Сысоево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410162' as code, 'д Фуники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410167' as code, 'д Щуряевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410168' as code, 'д Юркино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410182' as code, 'д Дор' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410183' as code, 'с Ельцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410192' as code, 'д Ильино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410212' as code, 'д Манухино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410213' as code, 'д Мелентьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410214' as code, 'д Митино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410215' as code, 'д Митьково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410217' as code, 'д Петренево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410218' as code, 'д Попково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650410222' as code, 'д Тверицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432122' as code, 'д Бор Волго' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432123' as code, 'д Бугор' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432132' as code, 'д Верхние Горицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432133' as code, 'д Волга' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432137' as code, 'д Заднее Заручевье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432138' as code, 'д Заручевье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432142' as code, 'д Казаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432143' as code, 'д Колобово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432152' as code, 'д Куниченково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432157' as code, 'д Лемно' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432167' as code, 'д Мишина Нива' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432168' as code, 'д Мишково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432192' as code, 'д Хорево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432202' as code, 'д Шуваево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432217' as code, 'д Боково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432227' as code, 'д Высокое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432228' as code, 'д Девичье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432232' as code, 'д Жилино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432247' as code, 'д Нивы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432248' as code, 'д Нижние Горицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432252' as code, 'д Пашутино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432267' as code, 'д Раменье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650432268' as code, 'д Ручьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449' as code, 'Селижаровское' as name, 1 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449106' as code, 'д Алешино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449111' as code, 'д Ананьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449116' as code, 'д Барагино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449121' as code, 'д Безумничино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449126' as code, 'д Безымянка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449131' as code, 'д Березуг' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449136' as code, 'д Берники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449141' as code, 'д Большаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449146' as code, 'д Большие Нивы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449151' as code, 'д Большое Гольтино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449156' as code, 'д Большое Ивахново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449161' as code, 'д Большое Ларионово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449166' as code, 'д Будаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449171' as code, 'д Бураково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449176' as code, 'д Бутырки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449181' as code, 'д Бучково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449186' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449191' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449196' as code, 'д Верхнее Голенково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449201' as code, 'д Верхние Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449206' as code, 'д Вилейка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449211' as code, 'д Ворожино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449216' as code, 'д Вороново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449221' as code, 'д Выставка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449226' as code, 'д Гогино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449231' as code, 'д Голенково-Погост' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449236' as code, 'д Голубево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449241' as code, 'д Горелуша' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449246' as code, 'д Горицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449251' as code, 'д Дергуново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449256' as code, 'д Дерюшино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449261' as code, 'д Дрыгомо' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449266' as code, 'д Дягилево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449271' as code, 'д Заречье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449276' as code, 'д Захарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449281' as code, 'д Зеленино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449286' as code, 'д Ивково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449291' as code, 'д Казицино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449296' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449301' as code, 'д Килешино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449306' as code, 'д Козловцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449311' as code, 'д Кононово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449316' as code, 'д Костенево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449321' as code, 'ж/д ст Красицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449326' as code, 'д Краски' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449331' as code, 'д Красная Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449336' as code, 'д Крестцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449341' as code, 'д Кубышкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449346' as code, 'д Кузнятино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449351' as code, 'д Кузьминское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449356' as code, 'д Кулаково Ближнее' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449361' as code, 'д Кулаково Дальнее' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449366' as code, 'д Кутолово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449371' as code, 'д Ладное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449376' as code, 'д Ларионово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449381' as code, 'д Лукино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449386' as code, 'д Макарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449391' as code, 'д Малое Гольтино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449396' as code, 'д Малое Ивахново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449401' as code, 'д Мамоново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449406' as code, 'д Манухино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449411' as code, 'д Медведево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449416' as code, 'д Мосягино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449421' as code, 'д Мошки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449426' as code, 'д Мясково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449431' as code, 'д Нивы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449436' as code, 'д Нижнее Голенково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449441' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449446' as code, 'д Острые' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449451' as code, 'д Палихово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449456' as code, 'д Перово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449461' as code, 'д Петелино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449466' as code, 'д Печеницыно' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449471' as code, 'д Пискачево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449476' as code, 'д Плаксино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449481' as code, 'д Поддубное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449486' as code, 'д Подмошье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449491' as code, 'д Подсосонье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449496' as code, 'д Покровка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449501' as code, 'д Пронино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449506' as code, 'д Пыхари' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449511' as code, 'д Рыжково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449516' as code, 'д Рытое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449521' as code, 'д Сапрыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449526' as code, 'д Селино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449531' as code, 'д Сижинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449536' as code, 'д Славотино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449541' as code, 'д Смольки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449546' as code, 'д Соловьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449551' as code, 'д Старая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449556' as code, 'д Ступино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449561' as code, 'д Сутоки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449566' as code, 'д Сушково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449571' as code, 'д Тальцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449576' as code, 'д Теглецы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449581' as code, 'д Терентьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449586' as code, 'д Трофимково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449591' as code, 'д Угольница' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449596' as code, 'д Филиппово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449601' as code, 'д Хомяково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449606' as code, 'д Чащевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449611' as code, 'д Черницино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449616' as code, 'д Черное Рытое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449621' as code, 'д Чижи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449626' as code, 'д Шалахино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28650449631' as code, 'д Языково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408102' as code, 'д Арпачево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408103' as code, 'д Бавыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408107' as code, 'д Бунятино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408108' as code, 'д Гари' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408109' as code, 'д Глебово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408110' as code, 'д Глухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408117' as code, 'д Детково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408118' as code, 'д Добрыни' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408119' as code, 'д Ерешкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408120' as code, 'д Ескино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408122' as code, 'д Ефремово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408127' as code, 'д Казицино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408128' as code, 'д Карпеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408129' as code, 'д Клин' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408130' as code, 'д Колодино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408132' as code, 'д Корытниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408142' as code, 'д Лесная' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408143' as code, 'д Малое Вишенье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408144' as code, 'д Машкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408145' as code, 'д Мишково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408147' as code, 'с Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408152' as code, 'д Осташково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408153' as code, 'д Островок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408154' as code, 'п Поведская Больница' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408155' as code, 'с Поведь' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408157' as code, 'д Пудышево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408162' as code, 'с Пятница Плот' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408163' as code, 'д Рылово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408167' as code, 'д Сельцо' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408168' as code, 'д Симонково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408169' as code, 'д Смердово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408170' as code, 'д Соколино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408172' as code, 'д Соколово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408173' as code, 'д Сосенка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408174' as code, 'д Старое Китово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408177' as code, 'д Трубино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408197' as code, 'д Шевелино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408206' as code, 'с Яконово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408211' as code, 'д Якшино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654408216' as code, 'д Ям' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414107' as code, 'х Бахани' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414108' as code, 'д Березай' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414109' as code, 'д Битьково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414112' as code, 'д Большая Песочня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414113' as code, 'д Большое Петрово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414117' as code, 'д Будово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414127' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414128' as code, 'д Горшково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414142' as code, 'д Климово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414147' as code, 'д Кочено' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414148' as code, 'д Круглое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414162' as code, 'д Малая Песочня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414163' as code, 'д Малое Петрово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414167' as code, 'д Нижнее' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414168' as code, 'д Осташково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414169' as code, 'д Парнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414172' as code, 'д Погорельцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414177' as code, 'д Рамушки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414178' as code, 'д Стояново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414179' as code, 'д Тимонцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414197' as code, 'д Чернево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654414198' as code, 'д Шубино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418107' as code, 'д Барыково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418112' as code, 'д Бибиково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418113' as code, 'д Богатьково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418117' as code, 'д Буконтово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418118' as code, 'д Ванеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418119' as code, 'д Васильево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418137' as code, 'д Жулево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418142' as code, 'д Зябриково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418143' as code, 'д Игнатьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418147' as code, 'д Колосово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418148' as code, 'д Копкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418149' as code, 'д Коробино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418150' as code, 'д Костромино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418152' as code, 'д Красные Зори' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418153' as code, 'д Лаврово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418154' as code, 'д Ладьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418155' as code, 'д Логуново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418157' as code, 'д Горицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418158' as code, 'д Дары' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418162' as code, 'д Воропуни' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418167' as code, 'д Дудорово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418168' as code, 'д Еменово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418169' as code, 'д Жеротино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418170' as code, 'д Жилкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418172' as code, 'д Лыково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418173' as code, 'д Михайлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418174' as code, 'д Нестерово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418182' as code, 'д Ново-Глинкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418183' as code, 'д Обухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418184' as code, 'д Патраково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418185' as code, 'д Пестово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418187' as code, 'д Подолы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418188' as code, 'д Понкратово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418206' as code, 'д Скрябино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418211' as code, 'д Старое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418216' as code, 'д Стукшино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418221' as code, 'д Филитово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654418226' as code, 'д Цапушево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424107' as code, 'д Богуново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424117' as code, 'с Дмитровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424118' as code, 'д Захожье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424119' as code, 'д Ильино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424120' as code, 'д Кашаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424122' as code, 'д Костино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424123' as code, 'д Леушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424127' as code, 'д Млевичи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424132' as code, 'д Пирогово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424133' as code, 'д Радилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424137' as code, 'д Селестрово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424147' as code, 'д Чайкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424148' as code, 'д Чернавы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654424149' as code, 'д Чуриково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430102' as code, 'д Андрианово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430137' as code, 'д Глядини' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430138' as code, 'д Домославль' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430142' as code, 'д Измайлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430143' as code, 'д Клоково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430144' as code, 'д Кляково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430145' as code, 'д Коноплище' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430147' as code, 'д Кресино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430148' as code, 'д Любини' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430157' as code, 'п Набережный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430158' as code, 'д Новое Беркаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430159' as code, 'д Орехово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430177' as code, 'д Селихово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430186' as code, 'д Старое Беркаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430191' as code, 'д Терешкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430196' as code, 'ж/д ст Терешкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654430201' as code, 'д Юрьицево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446102' as code, 'д Абакумово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446103' as code, 'д Анненское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446104' as code, 'д Бессменино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446117' as code, 'д Заречье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446127' as code, 'д Мельгубово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446147' as code, 'д Ряхово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446167' as code, 'д Теткино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446177' as code, 'д Хребтово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446178' as code, 'д Чеграево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446207' as code, 'д Липига' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446208' as code, 'д Лопатино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446222' as code, 'д Тредубье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28654446223' as code, 'д Троицкое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000001' as code, 'г Осташков' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000104' as code, 'д Александрово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000107' as code, 'д Алексеевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000110' as code, 'д Алкатово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000113' as code, 'д Ананьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000116' as code, 'д Анушино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000119' as code, 'д Бараново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000122' as code, 'д Барутино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000125' as code, 'д Белка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000128' as code, 'д Белкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000131' as code, 'д Березово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000134' as code, 'д Березовый Рядок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000137' as code, 'д Береснево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000140' as code, 'д Боголюбское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000143' as code, 'д Болошово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000146' as code, 'д Большое Веретье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000149' as code, 'д Большое Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000152' as code, 'д Большое Лохово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000155' as code, 'д Большое Ронское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000158' as code, 'д Большой Чащивец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000161' as code, 'д Бородино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000164' as code, 'д Ботово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000167' as code, 'д Буковичи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000170' as code, 'д Бухвостово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000173' as code, 'д Верхние Котицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000176' as code, 'д Волговерховье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000179' as code, 'д Волоховщина' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000182' as code, 'д Волчья Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000185' as code, 'д Воровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000188' as code, 'д Вороново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000191' as code, 'д Выворожье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000194' as code, 'д Выселок Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000197' as code, 'д Высокое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000200' as code, 'д Вязовня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000203' as code, 'д Гладкое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000206' as code, 'д Глебово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000209' as code, 'д Глубочица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000212' as code, 'д Голенек' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000215' as code, 'д Горбово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000218' as code, 'д Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000221' as code, 'д Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000224' as code, 'д Горовастица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000227' as code, 'ст Горовастица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000230' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000233' as code, 'д Городец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000236' as code, 'д Городок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000239' as code, 'д Гринино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000242' as code, 'д Гуща' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000245' as code, 'д Давыдово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000248' as code, 'д Данилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000251' as code, 'д Дмитровщина' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000254' as code, 'д Доброе' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000257' as code, 'д Дроздово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000260' as code, 'д Дубенка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000263' as code, 'д Дубово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000266' as code, 'д Дубок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000269' as code, 'д Дубровка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000272' as code, 'д Дубье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000275' as code, 'д Дулово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000278' as code, 'д Ель' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000281' as code, 'д Жалыбня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000284' as code, 'д Жар' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000287' as code, 'д Жданово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000290' as code, 'д Жданское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000293' as code, 'д Жегалово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000296' as code, 'д Жилино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000299' as code, 'д Жирома' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000302' as code, 'д Жук' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000305' as code, 'д Жуково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000308' as code, 'д Жулево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000311' as code, 'д Заболотье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000314' as code, 'д Заболотье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000317' as code, 'д Заболотье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000320' as code, 'д Заборки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000323' as code, 'д Заборье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000326' as code, 'д Задубье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000329' as code, 'д Залесье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000332' as code, 'д Залесье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000335' as code, 'д Залучье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000338' as code, 'д Залучье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000341' as code, 'д Зальцо' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000344' as code, 'д Замошье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000347' as code, 'д Занепречье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000350' as code, 'д Заозерье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000353' as code, 'д Заозерье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000356' as code, 'д Заплавье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000359' as code, 'д Заполек' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000362' as code, 'д Заселье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000365' as code, 'д Заречье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000368' as code, 'д Заречье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000371' as code, 'д Заузье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000374' as code, 'д Звягино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000377' as code, 'д Зехново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000380' as code, 'д Зорино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000383' as code, 'д Иванова Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000386' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000389' as code, 'д Ивановщина' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000392' as code, 'д Игнашовка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000395' as code, 'д Карповщина' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000398' as code, 'д Картунь' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000401' as code, 'д Клетино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000404' as code, 'д Климова Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000407' as code, 'д Княжое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000410' as code, 'д Кобенево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000413' as code, 'д Кожурица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000416' as code, 'д Коковкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000419' as code, 'д Колода' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000422' as code, 'д Конево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000425' as code, 'д Конец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000428' as code, 'д Кононово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000431' as code, 'д Корпово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000434' as code, 'нп Кордон Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000437' as code, 'д Косарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000440' as code, 'д Котчище' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000443' as code, 'д Кравотынь' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000446' as code, 'д Краклово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000449' as code, 'д Крапивня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000452' as code, 'д Красуха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000455' as code, 'д Кречетово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000458' as code, 'д Крутец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000461' as code, 'д Кукорево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000464' as code, 'д Кулатово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000467' as code, 'д Куряево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000470' as code, 'д Ласкоревщина' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000473' as code, 'д Лежнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000476' as code, 'д Лещины' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000479' as code, 'д Липовец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000482' as code, 'д Липуха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000485' as code, 'д Локотец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000488' as code, 'д Лом' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000491' as code, 'д Лукьяново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000494' as code, 'д Лучки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000497' as code, 'д Любимка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000500' as code, 'д Люшино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000503' as code, 'д Ляпино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000506' as code, 'д Малое Веретье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000509' as code, 'д Малое Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000512' as code, 'д Малое Лохово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000515' as code, 'д Малое Ронское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000518' as code, 'д Малый Чащивец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000521' as code, 'д Мартюшино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000524' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000527' as code, 'д Маслово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000530' as code, 'д Матерово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000533' as code, 'д Машугина Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000536' as code, 'д Междуречье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000539' as code, 'д Межник' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000542' as code, 'д Могилево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000545' as code, 'д Мосеевцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000548' as code, 'д Мошенка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000551' as code, 'д Нелегино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000554' as code, 'д Неприе' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000557' as code, 'д Нескучное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000560' as code, 'д Нехина Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000563' as code, 'д Нижние Котицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000566' as code, 'д Никишки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000569' as code, 'д Никола Рожок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000572' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000575' as code, 'д Новинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000578' as code, 'д Новые Ельцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000581' as code, 'д Носовица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000584' as code, 'д Овинец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000587' as code, 'д Озерки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000590' as code, 'д Ореховка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000593' as code, 'д Орлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000596' as code, 'д Осинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000599' as code, 'д Острицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000602' as code, 'п Осцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000605' as code, 'д Павлиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000608' as code, 'д Палиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000611' as code, 'д Панюки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000614' as code, 'д Пачково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000617' as code, 'д Перетерг' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000620' as code, 'д Первое Мая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000623' as code, 'д Пески' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000626' as code, 'д Петриково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000629' as code, 'д Петровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000632' as code, 'д Пихтень' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000635' as code, 'д Погорелое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000638' as code, 'д Подгорье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000641' as code, 'д Подолище' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000644' as code, 'д Покровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000647' as code, 'д Покровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000650' as code, 'д Польки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000653' as code, 'д Поребрица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000656' as code, 'д Поселье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000659' as code, 'д Пыжи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000662' as code, 'д Радухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000665' as code, 'д Раменье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000668' as code, 'д Рвеница' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000671' as code, 'д Ровень Мосты' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000674' as code, 'д Рогожа' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000677' as code, 'д Роги' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000680' as code, 'д Рязановщина' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000683' as code, 'д Савина Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000686' as code, 'д Сальниковщина' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000689' as code, 'д Самара' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000692' as code, 'д Свапуще' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000695' as code, 'д Светлица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000698' as code, 'д Святое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000701' as code, 'с Святое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000704' as code, 'д Семеновщина' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000707' as code, 'п Сиговка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000710' as code, 'д Сигово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000713' as code, 'ж/д ст Сигово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000716' as code, 'д Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000719' as code, 'д Смешово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000722' as code, 'д Собро' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000725' as code, 'д Сорога' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000728' as code, 'д Сосница' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000731' as code, 'д Сосново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000734' as code, 'д Спицино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000737' as code, 'д Старый Сиг' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000740' as code, 'д Старое Село' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000743' as code, 'д Старые Поля' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000746' as code, 'д Студенец' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000749' as code, 'д Сухая Нива' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000752' as code, 'д Сухлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000755' as code, 'д Тарасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000758' as code, 'д Твердякино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000761' as code, 'д Тереховщина' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000764' as code, 'д Толстик' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000767' as code, 'д Трестино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000770' as code, 'д Третники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000773' as code, 'д Троеручица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000776' as code, 'нп Турбаза Сокол""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000779' as code, 'нп Турбаза Хатинь Бор""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000782' as code, 'д Турская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000785' as code, 'д Узгово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000788' as code, 'д Уницы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000791' as code, 'д Уревы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000794' as code, 'д Урицкое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000797' as code, 'д Хитино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000800' as code, 'д Хретень' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000803' as code, 'нп Хутора Дубские' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000806' as code, 'д Черный Дор' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000809' as code, 'ж/д ст Черный Дор' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000812' as code, 'д Чигориха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000815' as code, 'д Шадыки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000818' as code, 'д Шалабаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000821' as code, 'д Шелехово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000824' as code, 'д Шиловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000827' as code, 'д Щебериха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000830' as code, 'х Щемелинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000833' as code, 'д Щучье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000836' as code, 'п Южный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000839' as code, 'д Язово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '28752000842' as code, 'д Ясенское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000' as code, 'Кашинский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000001' as code, 'г Кашин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000103' as code, 'д Акулинкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000105' as code, 'д Аладьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000107' as code, 'д Алексеевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000109' as code, 'д Алехино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000111' as code, 'д Алпатово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000113' as code, 'д Андрейково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000115' as code, 'д Андрейцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000117' as code, 'д Антюшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000119' as code, 'д Апарниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000121' as code, 'д Апраксино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000123' as code, 'д Артемово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000125' as code, 'д Архангельское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000127' as code, 'д Бабеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000129' as code, 'д Бакланово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000131' as code, 'д Бакшеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000133' as code, 'д Бараново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000135' as code, 'д Барыково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000137' as code, 'д Батурово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000139' as code, 'д Башвино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000141' as code, 'д Безгузово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000143' as code, 'д Белеутово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000145' as code, 'д Белино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000147' as code, 'д Бережки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000149' as code, 'д Берница' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000151' as code, 'д Бибиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000153' as code, 'д Болотово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000155' as code, 'нп Больница им Калинина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000157' as code, 'д Большие Крутцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000159' as code, 'д Большие Сетки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000161' as code, 'д Большое Макарово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000163' as code, 'д Большое Савино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000165' as code, 'д Большое Софроново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000167' as code, 'д Борихино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000169' as code, 'д Борки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000171' as code, 'д Бормосово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000173' as code, 'д Борщево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000175' as code, 'д Братково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000177' as code, 'д Бузыково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000179' as code, 'д Буйково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000181' as code, 'д Булатниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000183' as code, 'д Булатово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000185' as code, 'д Бурмакино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000187' as code, 'д Бурцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000189' as code, 'д Бухвостово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000191' as code, 'д Бяково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000193' as code, 'д Вантеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000195' as code, 'д Ванчугово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000197' as code, 'д Ваньково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000199' as code, 'д Васенево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000201' as code, 'д Василево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000203' as code, 'д Васильево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000205' as code, 'д Васильевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000207' as code, 'д Васнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000209' as code, 'д Вахромеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000211' as code, 'д Введенское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000213' as code, 'с Введенское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000215' as code, 'д Вениково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000217' as code, 'д Верезино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000219' as code, 'д Верхнее Устье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000221' as code, 'д Верхняя Троица' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000223' as code, 'д Витенево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000225' as code, 'д Власьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000227' as code, 'д Вознесенье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000229' as code, 'д Волжанка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000231' as code, 'д Волково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000233' as code, 'д Вороново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000235' as code, 'д Воронцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000237' as code, 'д Вотолино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000239' as code, 'д Вощилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000241' as code, 'д Высоково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000243' as code, 'д Вязовец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000245' as code, 'д Вячково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000247' as code, 'д Гаврильцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000249' as code, 'д Гапшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000251' as code, 'д Гладышево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000253' as code, 'д Глазатово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000255' as code, 'д Гоготово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000257' as code, 'д Головеньки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000259' as code, 'д Гольнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000261' as code, 'д Горбуново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000263' as code, 'д Горбуново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000265' as code, 'д Гордеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000267' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000269' as code, 'д Горлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000271' as code, 'д Городищи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000273' as code, 'д Гостинеж' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000275' as code, 'д Грибово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000277' as code, 'д Громилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000279' as code, 'д Губцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000281' as code, 'д Давыдово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000283' as code, 'д Данилково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000285' as code, 'д Данилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000287' as code, 'д Данильцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000289' as code, 'д Дементьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000291' as code, 'д Демидово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000293' as code, 'д Демино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000295' as code, 'д Деревенька' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000297' as code, 'д Деулино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000299' as code, 'д Дмитровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000301' as code, 'д Домажино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000303' as code, 'д Домажирово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000305' as code, 'д Доманово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000307' as code, 'д Дуботолки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000309' as code, 'д Дудино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000311' as code, 'д Дулепово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000313' as code, 'д Дьяково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000315' as code, 'д Дьяконово 1-е' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000317' as code, 'д Дьяконово 2-е' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000319' as code, 'д Егорьевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000321' as code, 'д Жидинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000323' as code, 'д Жилкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000325' as code, 'д Жуково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000327' as code, 'д Заводы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000329' as code, 'д Заволжье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000331' as code, 'д Заворино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000333' as code, 'д Задово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000335' as code, 'д Заречье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000337' as code, 'д Захарово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000339' as code, 'д Зеленцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000341' as code, 'д Зеленцыно' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000343' as code, 'д Злобино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000345' as code, 'д Зобнино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000347' as code, 'д Золотиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000349' as code, 'д Золотилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000351' as code, 'д Ивайково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000353' as code, 'д Иваньково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000355' as code, 'д Ивашнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000357' as code, 'д Игнатово 1-е' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000359' as code, 'д Игнатово 2-е' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000361' as code, 'д Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000363' as code, 'д Илькино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000365' as code, 'д Ильково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000367' as code, 'д Итьково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000369' as code, 'д Каданово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000371' as code, 'д Калицыно' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000373' as code, 'д Карабузино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000375' as code, 'д Келарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000377' as code, 'д Киряково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000379' as code, 'д Киселево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000381' as code, 'д Киселево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000383' as code, 'д Клепцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000385' as code, 'д Клестово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000387' as code, 'д Климатино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000389' as code, 'д Клитино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000391' as code, 'д Клюкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000393' as code, 'д Клясово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000395' as code, 'д Ковырино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000397' as code, 'д Кожино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000399' as code, 'д Козино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000401' as code, 'д Козлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000403' as code, 'с Козьмодемьяновское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000405' as code, 'д Колбасино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000407' as code, 'д Коленцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000409' as code, 'д Колобово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000411' as code, 'д Коляково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000413' as code, 'д Кондратово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000415' as code, 'д Кононово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000417' as code, 'д Конопелки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000419' as code, 'д Константиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000421' as code, 'д Коржавино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000423' as code, 'д Коробеньково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000425' as code, 'д Коробово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000427' as code, 'д Кортино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000429' as code, 'д Корюгино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000431' as code, 'д Костюшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000433' as code, 'д Кочеватово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000435' as code, 'д Кочемли' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000437' as code, 'д Крапивино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000439' as code, 'д Красный Бор' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000441' as code, 'п Красный Май' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000443' as code, 'д Кривцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000445' as code, 'д Кружково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000447' as code, 'д Кубасово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000449' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000451' as code, 'д Курово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000453' as code, 'д Курьяново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000455' as code, 'д Ладыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000457' as code, 'д Лапшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000459' as code, 'д Ластома' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000461' as code, 'д Леванидово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000463' as code, 'д Лежнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000465' as code, 'д Леушино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000467' as code, 'д Леушино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000469' as code, 'д Лисова Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000471' as code, 'д Литвиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000473' as code, 'д Лобково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000475' as code, 'д Логиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000477' as code, 'д Лубеньки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000479' as code, 'д Лужки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000481' as code, 'д Лучкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000483' as code, 'д Льгово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000485' as code, 'д Ляхово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000487' as code, 'д Македоново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000489' as code, 'д Маковницы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000491' as code, 'д Малафеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000493' as code, 'д Малахово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000495' as code, 'д Малечкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000497' as code, 'д Малое Макарово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000499' as code, 'д Малое Софроново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000501' as code, 'д Малые Сетки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000503' as code, 'д Малыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000505' as code, 'д Малыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000507' as code, 'д Маматово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000509' as code, 'д Маринино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000511' as code, 'д Марково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000513' as code, 'д Марково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000515' as code, 'д Мартынки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000517' as code, 'д Маслово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000519' as code, 'д Маслятка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000521' as code, 'д Матино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000523' as code, 'д Медведево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000525' as code, 'п Медведица' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000527' as code, 'д Медведково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000529' as code, 'д Мехтенево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000531' as code, 'д Мизгирево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000533' as code, 'д Миклюково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000535' as code, 'д Милославское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000537' as code, 'д Митино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000539' as code, 'д Митрохино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000541' as code, 'д Мокриха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000543' as code, 'д Молевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000545' as code, 'д Монино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000547' as code, 'д Морево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000549' as code, 'д Мостище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000551' as code, 'д Мошнино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000553' as code, 'д Мялицино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000555' as code, 'д Непотягово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000557' as code, 'д Нивищи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000559' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000561' as code, 'с Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000563' as code, 'д Никулино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000565' as code, 'д Никулкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000567' as code, 'д Нижняя Троица' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000569' as code, 'д Нижняя Троица' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000571' as code, 'д Новинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000573' as code, 'д Новинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000575' as code, 'д Новое Село' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000577' as code, 'д Новое Сташино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000579' as code, 'д Овсянниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000581' as code, 'д Овсянниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000583' as code, 'д Окороково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000585' as code, 'д Опухлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000587' as code, 'д Ордынка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000589' as code, 'д Осиновец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000591' as code, 'д Осипово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000593' as code, 'д Отрубнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000595' as code, 'д Павловское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000597' as code, 'д Панкратово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000599' as code, 'д Пенье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000601' as code, 'д Пенье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000603' as code, 'п Первомайский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000605' as code, 'д Перетрясово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000607' as code, 'д Пестриково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000609' as code, 'д Петраково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000611' as code, 'д Петровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000613' as code, 'д Письяковка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000615' as code, 'д Плюгино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000617' as code, 'д Подберезье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000619' as code, 'д Подберезье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000621' as code, 'д Подселье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000623' as code, 'д Покровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000625' as code, 'д Покровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000627' as code, 'д Полукьяново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000629' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000631' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000633' as code, 'д Поповка-2' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000635' as code, 'д Посады' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000637' as code, 'д Постельниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000639' as code, 'д Потупово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000641' as code, 'д Почапки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000643' as code, 'д Починки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000645' as code, 'д Прокофьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000647' as code, 'д Пузиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000649' as code, 'д Пустынька' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000651' as code, 'д Пустыри' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000653' as code, 'д Путилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000655' as code, 'д Пучихино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000657' as code, 'д Пушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000659' as code, 'д Рагузино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000661' as code, 'д Рагузино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000663' as code, 'д Раднево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000665' as code, 'д Раково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000667' as code, 'д Рассолово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000669' as code, 'д Ратчино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000671' as code, 'д Рахманово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000673' as code, 'д Ременница' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000675' as code, 'д Репрево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000677' as code, 'д Рождественно' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000679' as code, 'д Рождествено' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000681' as code, 'д Ройга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000683' as code, 'д Ромашино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000685' as code, 'д Рудлево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000687' as code, 'д Ручейки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000689' as code, 'д Рыкулино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000691' as code, 'д Савашкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000693' as code, 'д Савелково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000695' as code, 'с Савцыно' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000697' as code, 'д Саково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000699' as code, 'с Салтыково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000701' as code, 'д Сальково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000703' as code, 'д Сафонеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000705' as code, 'д Свезево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000707' as code, 'д Свинцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000709' as code, 'д Свитино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000711' as code, 'д Селихово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000713' as code, 'д Семеновское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000715' as code, 'д Семеновское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000717' as code, 'д Серговка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000719' as code, 'д Сипягино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000721' as code, 'д Сипягино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000723' as code, 'д Скатерка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000725' as code, 'д Скриплево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000727' as code, 'с Славково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000729' as code, 'д Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000731' as code, 'д Слободка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000733' as code, 'д Слободка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000735' as code, 'д Соколово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000737' as code, 'д Соколово-Кошкарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000739' as code, 'д Сологовское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000741' as code, 'д Спасс' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000743' as code, 'с Спасское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000745' as code, 'д Спицино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000747' as code, 'д Старово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000749' as code, 'д Старое Сташино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000751' as code, 'д Староселье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000753' as code, 'с Стельково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000755' as code, 'д Степаньково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000757' as code, 'с Стражково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000759' as code, 'д Студеное Поле' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000761' as code, 'п Стулово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000763' as code, 'д Судниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000765' as code, 'д Сумино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000767' as code, 'д Сусолиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000769' as code, 'д Сухолом' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000771' as code, 'д Тарбаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000773' as code, 'д Терботунь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000775' as code, 'д Терехино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000777' as code, 'д Тетьково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000779' as code, 'д Тиволино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000781' as code, 'д Токарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000783' as code, 'д Троицкое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000785' as code, 'д Тросухино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000787' as code, 'д Трубино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000789' as code, 'д Трубино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000791' as code, 'д Турлеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000793' as code, 'д Туровино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000795' as code, 'с Турово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000797' as code, 'д Тушнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000799' as code, 'д Уницкая Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000801' as code, 'с Уницы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000803' as code, 'д Усатиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000805' as code, 'д Устиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000807' as code, 'д Фаладьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000809' as code, 'д Фалево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000811' as code, 'д Фарафоновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000813' as code, 'д Федоровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000815' as code, 'д Федосьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000817' as code, 'д Филипищево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000819' as code, 'д Филитово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000821' as code, 'д Фролово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000823' as code, 'д Фролово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000825' as code, 'д Фроловское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000827' as code, 'д Харлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000829' as code, 'д Хлябово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000831' as code, 'д Холстово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000833' as code, 'д Хрипелево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000835' as code, 'с Чагино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000837' as code, 'д Чаплинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000839' as code, 'д Чеканово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000841' as code, 'д Чекмарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000843' as code, 'д Челагино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000845' as code, 'д Ченцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000847' as code, 'д Ченцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000849' as code, 'д Черемухино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000851' as code, 'д Чернышево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000853' as code, 'д Чернятино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000855' as code, 'д Чириково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000857' as code, 'д Чупрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000859' as code, 'с Шевелево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000861' as code, 'д Шевригино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000863' as code, 'д Шепели' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000865' as code, 'д Шестаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000867' as code, 'д Шилково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000869' as code, 'д Шихурово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000871' as code, 'д Шишелово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000873' as code, 'д Шишкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000875' as code, 'д Шубино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000877' as code, 'д Щапицы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000879' as code, 'д Щекотово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000881' as code, 'д Щелково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000883' as code, 'д Эндогорово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000885' as code, 'д Эскино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000887' as code, 'д Юрино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000889' as code, 'д Языково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000891' as code, 'д Яйцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28758000893' as code, 'д Ясная Поляна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000' as code, 'Нелидовский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000001' as code, 'г Нелидово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000106' as code, 'д Алексеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000111' as code, 'д Антипово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000116' as code, 'п Арбузово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000121' as code, 'д Арбузово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000126' as code, 'д Барули' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000131' as code, 'д Батурино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000136' as code, 'д Бахвалово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000141' as code, 'д Белая Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000146' as code, 'д Белейка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000151' as code, 'д Березники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000156' as code, 'д Березовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000161' as code, 'п Богданово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000166' as code, 'д Большая Каменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000171' as code, 'д Большие Ясновицы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000176' as code, 'д Большое Голаново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000181' as code, 'д Большое Реданово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000186' as code, 'д Большое Федоровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000191' as code, 'д Бор' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000196' as code, 'д Бор' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000201' as code, 'д Борисово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000206' as code, 'д Борки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000211' as code, 'п Борок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000216' as code, 'д Борщевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000221' as code, 'д Бурцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000226' as code, 'п Бутаки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000231' as code, 'д Бутаки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000236' as code, 'д Верхнее Заборье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000241' as code, 'д Власевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000246' as code, 'д Волчищево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000251' as code, 'д Высокое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000256' as code, 'д Высокое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000261' as code, 'д Вяземка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000266' as code, 'д Глазково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000271' as code, 'д Глужнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000276' as code, 'д Голосово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000281' as code, 'д Гомово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000286' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000291' as code, 'д Дегтево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000296' as code, 'д Дорохи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000301' as code, 'д Драньково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000306' as code, 'д Дрогачево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000311' as code, 'д Дулево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000316' as code, 'п Дятлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000321' as code, 'д Железница' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000326' as code, 'д Жердовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000331' as code, 'д Жеребцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000336' as code, 'д Жиглицы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000341' as code, 'д Загвоздье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000346' as code, 'п Загородный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000351' as code, 'п Заповедный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000356' as code, 'п Земцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000361' as code, 'д Земцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000366' as code, 'д Земцы-2' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000371' as code, 'п Ильюшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000376' as code, 'д Ильюшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000381' as code, 'д Иоткино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000386' as code, 'д Иоткино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000391' as code, 'д Казаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000396' as code, 'д Казарино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000401' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000406' as code, 'д Каменца' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000411' as code, 'д Канаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000416' as code, 'д Карпово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000421' as code, 'д Киркорово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000426' as code, 'д Клютиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000431' as code, 'д Ключевая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000436' as code, 'д Ковалево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000441' as code, 'д Козино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000446' as code, 'д Козино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000451' as code, 'д Козлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000456' as code, 'д Козлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000461' as code, 'д Колесня' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000466' as code, 'п Копейки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000471' as code, 'д Кортяшево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000476' as code, 'д Костино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000481' as code, 'д Кошелево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000486' as code, 'д Красные Нивы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000491' as code, 'д Кривцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000496' as code, 'д Кукуево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000501' as code, 'д Лемешиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000506' as code, 'д Липинское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000511' as code, 'д Малая Хмелевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000516' as code, 'д Макарьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000521' as code, 'д Макруша' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000526' as code, 'д Максимова Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000531' as code, 'д Малое Чернецово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000536' as code, 'д Малые Ущаны' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000541' as code, 'д Малюшкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000546' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000551' as code, 'д Машкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000556' as code, 'п Межа' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000561' as code, 'д Мешки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000566' as code, 'д Миндюри' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000571' as code, 'п Мирный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000576' as code, 'д Михайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000581' as code, 'д Михалево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000586' as code, 'д Можайка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000591' as code, 'д Монино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000596' as code, 'д Мохоярово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000601' as code, 'д Мякишево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000606' as code, 'д Нелидовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000611' as code, 'д Нива' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000616' as code, 'д Нивы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000621' as code, 'д Нижнее Заборье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000626' as code, 'д Никитино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000631' as code, 'д Никулинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000636' as code, 'д Ново-Богданово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000641' as code, 'д Новое Кутьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000646' as code, 'д Новоникольское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000651' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000656' as code, 'д Овсянкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000661' as code, 'д Орешенки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000666' as code, 'д Отвага' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000671' as code, 'п Откос' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000676' as code, 'д Паникля' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000681' as code, 'д Перевоз' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000686' as code, 'д Плоское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000691' as code, 'д Подберезье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000696' as code, 'ж/д рзд Подсосенка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000701' as code, 'д Половцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000706' as code, 'д Поповцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000711' as code, 'д Приволье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000716' as code, 'д Прохорово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000721' as code, 'д Прудня 2-я' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000726' as code, 'д Прудовая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000731' as code, 'д Прудянка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000736' as code, 'д Пустое Подлесье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000741' as code, 'д Ратькино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000746' as code, 'д Рожки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000751' as code, 'д Ростовая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000756' as code, 'д Рубцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000761' as code, 'д Рябиновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000766' as code, 'д Селезенкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000771' as code, 'д Семеново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000776' as code, 'д Семеново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000781' as code, 'д Семеновское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000786' as code, 'д Семики' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000791' as code, 'д Сёлы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000796' as code, 'д Сильники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000801' as code, 'д Смольники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000806' as code, 'д Соболевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000811' as code, 'д Соболи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000816' as code, 'д Соловьянка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000821' as code, 'д Сосноватка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000826' as code, 'д Средняя' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000831' as code, 'д Старое Кутьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000836' as code, 'д Староселье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000841' as code, 'д Староселье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000846' as code, 'д Стройново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000851' as code, 'д Сюльки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000856' as code, 'д Талухино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000861' as code, 'д Тесы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000866' as code, 'п Тросно' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000871' as code, 'д Туд' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000876' as code, 'д Ульянино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000881' as code, 'д Фильченки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000886' as code, 'д Хлюстовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000891' as code, 'д Хмелевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000896' as code, 'д Хрущево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000901' as code, 'д Чернецово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000906' as code, 'д Чернушка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000911' as code, 'д Шабаны' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000916' as code, 'д Шабровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000921' as code, 'д Шанино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000926' as code, 'д Шарыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000931' as code, 'д Шейкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000936' as code, 'д Шелопаново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000941' as code, 'д Шумилы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '28759000946' as code, 'п Южный' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '29615405123' as code, 'д Голенки' as name, 2 as razd from dual union all 
select to_date('01.10.2018','dd.mm.yyyy') as version, '29615430123' as code, 'д Новые Клины' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '29629456251' as code, 'д Азарово' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '29629456256' as code, 'д Бесово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '29632408107' as code, 'д Родники' as name, 2 as razd from dual union all 
select to_date('01.10.2018','dd.mm.yyyy') as version, '29650416151' as code, 'д Ресса' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '29701000471' as code, 'д Берёзовка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '29701000476' as code, 'д Калашников Хутор' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '29701000481' as code, 'д Переселенец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '30824701902' as code, 'с Кострома' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '34634428138' as code, 'д Дьяково' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '34634428163' as code, 'д Курьяново' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '34634428238' as code, 'с Потрусово' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '34634428303' as code, 'д Федюшино' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '34648457106' as code, 'д Безнег' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '34648457111' as code, 'п Варакинский' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '34648457116' as code, 'д Красный Холм' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '34648457121' as code, 'починок Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '34648457126' as code, 'м Лесничество' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37602451109' as code, 'с Рыбное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37602451116' as code, 'с Чистое' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37604417109' as code, 'с Бузан' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37604417113' as code, 'с Зюзино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37604417115' as code, 'д Лихачи' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37604417121' as code, 'д Новозаборка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37604417126' as code, 'д Слободчикова' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468102' as code, 'д Асямолова' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468103' as code, 'д Белоусова' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468104' as code, 'д Бралгина' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468105' as code, 'с Брылино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468107' as code, 'д Жикина' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468108' as code, 'с Житниковское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468112' as code, 'с Локти' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468113' as code, 'д Мамонова' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468114' as code, 'д Новая Никольская' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468115' as code, 'с Новоиковское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468117' as code, 'с Пустуево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468131' as code, 'д Савина' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468136' as code, 'д Северная' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468141' as code, 'д Тукманное' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37610468146' as code, 'д Чемякина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37614404111' as code, 'д Новая Затобольная' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37614404116' as code, 'с Темляково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37614424111' as code, 'п Илецкий' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37614424116' as code, 'п Чашинский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37614452111' as code, 'с Ровная' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37616424103' as code, 'с Березово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37616424109' as code, 'д Новая Калиновка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37616424113' as code, 'д Птичье' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37616448102' as code, 'д Белое' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37616448103' as code, 'д Грызаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37616448104' as code, 'с Закоулово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37616448105' as code, 'с Каминское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37616448109' as code, 'д Курмыши' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37616448113' as code, 'д Язево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37616451106' as code, 'с Маслово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37616451111' as code, 'д Таволжанка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37616464102' as code, 'д Борок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37616464117' as code, 'д Новоникольская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '37616464126' as code, 'с Угловое' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37618408106' as code, 'д Александровка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37618408111' as code, 'с Калашное' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37618416103' as code, 'с Балакуль' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37618416104' as code, 'д Бочаговка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37618416105' as code, 'с Дубровное' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37618416108' as code, 'д Урожайная' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37620412113' as code, 'д Покровка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37620412114' as code, 'с Трюхино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37620462103' as code, 'с Басковское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37620462111' as code, 'с Мартино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37620462116' as code, 'с Слевное' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37630408111' as code, 'с Притобольное' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37630408116' as code, 'д Ясная' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37638405103' as code, 'с Большое Кабанье' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37638405118' as code, 'д Моховое' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '37642412116' as code, 'с Прошкино' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610414116' as code, 'д Копенки' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610414121' as code, 'п Богатыревский' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610414126' as code, 'с Большебоброво' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610416146' as code, 'д Нижнее Жданово' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610416151' as code, 'д Верхнее Жданово' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610416156' as code, 'х Заречье' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610416161' as code, 'д Калиновка' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610416166' as code, 'д Клюшниково' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610416171' as code, 'х Ленина' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610416176' as code, 'д Овсянниково' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610416181' as code, 'х Ольшанец' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610428131' as code, 'д Снецкое' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610428136' as code, 'д Мокрыж' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440131' as code, 'д Басово' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440136' as code, 'д Басово-Заречье' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440141' as code, 'д Жилино' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440146' as code, 'д Козюлькина' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440151' as code, 'д Колесникова' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440156' as code, 'д Комаровка' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440161' as code, 'д Протасово' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440166' as code, 'д Сухарева' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440171' as code, 'с Шатохино' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610440176' as code, 'х Ясная Поляна' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610446111' as code, 'с Трояново' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610446116' as code, 'п Гавриловский' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38610446121' as code, 'п Ольховка' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38634432116' as code, 'д Свобода' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38634432121' as code, 'с Ломакино' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38634432126' as code, 'п Первомайский' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38634432131' as code, 'д Кулига' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38634432136' as code, 'с Барамыково' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38634432141' as code, 'с Боброво' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38634432146' as code, 'д Верхнее Лухтоново' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38634432151' as code, 'д Матохино' as name, 2 as razd from dual union all 
select to_date('01.06.2018','dd.mm.yyyy') as version, '38634432156' as code, 'д Перецелуево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '41612155005' as code, 'г Кудрово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152051' as code, 'гп Новоселье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152106' as code, 'д Алакюля' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152108' as code, 'п Аннино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152111' as code, 'д Большие Томики' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152116' as code, 'д Иннолово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152121' as code, 'д Капорское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152126' as code, 'д Кемпелево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152131' as code, 'д Куттузи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152136' as code, 'д Лесопитомник' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152146' as code, 'д Пески' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152151' as code, 'д Пигелево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152156' as code, 'д Рапполово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152161' as code, 'д Рюмки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630152166' as code, 'д Тиммолово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157' as code, 'Виллозское' as name, 1 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157051' as code, 'гп Виллози' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157106' as code, 'д Аропаккузи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157111' as code, 'д Вариксолово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157116' as code, 'д Кавелахта' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157121' as code, 'д Карвала' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157126' as code, 'д Малое Карлино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157131' as code, 'д Мурилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157136' as code, 'д Мюреля' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157141' as code, 'п Новогорелово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157146' as code, 'д Перекюля' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157151' as code, 'д Пикколово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157156' as code, 'д Рассколово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157161' as code, 'д Ретселя' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41630157166' as code, 'д Саксолово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41648165051' as code, 'гп Фёдоровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41648165106' as code, 'д Аннолово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41648165111' as code, 'д Глинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '41648165116' as code, 'д Ладога' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '42612444121' as code, 'с Павловка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '42612444126' as code, 'д Георгиевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '42612444131' as code, 'д Кочегуровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '42612444136' as code, 'д Кочетовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '42612444141' as code, 'д Евлановка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '42612444146' as code, 'д Смеловка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '42612444151' as code, 'п им. Ильича' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '42633432116' as code, 'с Павловское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '42633432121' as code, 'с Грязновка' as name, 2 as razd from dual union all 
select to_date('01.01.2012','dd.mm.yyyy') as version, '4610433' as code, 'Предивинский сельсовет' as name, 1 as razd from dual union all 
select to_date('01.01.2012','dd.mm.yyyy') as version, '4614454' as code, 'сельсовет Памяти 13 Борцов' as name, 1 as razd from dual union all 
select to_date('01.01.2012','dd.mm.yyyy') as version, '4635426' as code, 'Южно-Енисейский сельсовет' as name, 1 as razd from dual union all 
select to_date('01.01.2012','dd.mm.yyyy') as version, '4647454' as code, 'Уральский сельсовет' as name, 1 as razd from dual union all 
select to_date('01.01.2012','dd.mm.yyyy') as version, '4654439' as code, 'Светлогорский сельсовет' as name, 1 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000' as code, 'Дмитровский' as name, 1 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000001' as code, 'г Дмитров' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000006' as code, 'г Яхрома' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000056' as code, 'рп Деденево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000061' as code, 'рп Икша' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000066' as code, 'рп Некрасовский' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000103' as code, 'д Абрамцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000105' as code, 'с Абрамцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000107' as code, 'п Автополигон' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000109' as code, 'д Агафониха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000111' as code, 'д Акишево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000113' as code, 'д Акулово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000115' as code, 'д Алабуха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000117' as code, 'д Аладьино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000119' as code, 'д Александрово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000121' as code, 'д Алешино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000123' as code, 'д Андрейково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000125' as code, 'д Андрейково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000127' as code, 'д Андреянцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000129' as code, 'д Арбузово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000131' as code, 'д Арбузово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000133' as code, 'д Аревское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000135' as code, 'д Арханово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000137' as code, 'д Ассаурово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000139' as code, 'д Астрецово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000141' as code, 'д Афанасово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000143' as code, 'д Ащерино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000145' as code, 'д Бабаиха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000147' as code, 'д Бабкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000149' as code, 'д Базарово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000151' as code, 'д Банино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000153' as code, 'с Батюшково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000155' as code, 'д Безбородово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000157' as code, 'д Беклемишево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000159' as code, 'с Белый Раст' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000161' as code, 'д Бестужево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000163' as code, 'д Бешенково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000165' as code, 'д Бирлово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000167' as code, 'д Благовещенское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000169' as code, 'д Благовещенье' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000171' as code, 'д Благодать' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000173' as code, 'д Ближнево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000175' as code, 'д Боброво' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000177' as code, 'д Богданово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000179' as code, 'д Большое Прокошево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000181' as code, 'с Борисово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000183' as code, 'д Борносово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000185' as code, 'д Бородино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000187' as code, 'д Бородино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000189' as code, 'д Бортниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000191' as code, 'д Борцово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000193' as code, 'д Бунятино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000195' as code, 'д Буславль' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000197' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000199' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000201' as code, 'д Ваганово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000203' as code, 'д Ваньково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000205' as code, 'д Варварино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000207' as code, 'д Василево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000209' as code, 'д Василево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000211' as code, 'д Васнево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000213' as code, 'с Ведерницы' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000215' as code, 'д Векшино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000217' as code, 'д Власково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000219' as code, 'с Внуково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000221' as code, 'д Волдынское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000223' as code, 'с Вороново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000225' as code, 'д Высоково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000227' as code, 'д Гаврилково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000229' as code, 'д Глазачево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000231' as code, 'д Глазово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000233' as code, 'д Глебездово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000235' as code, 'д Глухово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000237' as code, 'д Глухово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000239' as code, 'д Говейново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000241' as code, 'д Голиково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000243' as code, 'д Головино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000245' as code, 'д Голявино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000247' as code, 'д Голяди' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000249' as code, 'д Гончарово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000251' as code, 'д Гора' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000253' as code, 'д Горбово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000255' as code, 'д Горицы' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000257' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000259' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000261' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000263' as code, 'д Горки Сухаревские' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000265' as code, 'д Горчаково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000267' as code, 'п Горшково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000269' as code, 'д Григорково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000271' as code, 'д Гришино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000273' as code, 'д Гульнево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000275' as code, 'д Давыдково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000277' as code, 'д Данилиха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000279' as code, 'д Дедлово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000281' as code, 'д Демьяново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000283' as code, 'д Дмитровка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000285' as code, 'п дома отдыха Горки""' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000287' as code, 'д Доронино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000289' as code, 'д Драчево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000291' as code, 'д Дрочево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000293' as code, 'д Дубровки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000295' as code, 'д Дуброво' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000297' as code, 'д Думино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000299' as code, 'д Дутшево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000301' as code, 'д Дьяково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000303' as code, 'д Дядьково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000305' as code, 'д Дятлино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000307' as code, 'д Елизаветино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000309' as code, 'д Ермолино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000311' as code, 'д Ерыково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000313' as code, 'с Жестылево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000315' as code, 'д Животино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000317' as code, 'д Жирково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000319' as code, 'д Жуковка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000321' as code, 'д Жуково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000323' as code, 'д Зараменье' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000325' as code, 'д Зверково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000327' as code, 'д Зуево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000329' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000331' as code, 'с Ивановское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000333' as code, 'д Иванцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000335' as code, 'д Ивашево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000337' as code, 'д Ивлево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000339' as code, 'д Ивлево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000341' as code, 'д Игнатовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000343' as code, 'с Игнатово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000345' as code, 'д Измайлово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000347' as code, 'с Ильино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000349' as code, 'с Ильинское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000351' as code, 'д Исаково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000353' as code, 'д Исаково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000355' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000357' as code, 'д Капорки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000359' as code, 'д Караваево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000361' as code, 'д Карамышево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000363' as code, 'д Карпово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000365' as code, 'д Карцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000367' as code, 'д Кекишево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000369' as code, 'д Кикино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000371' as code, 'д Киндяково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000373' as code, 'д Клусово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000375' as code, 'д Клюшниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000377' as code, 'д Княжево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000379' as code, 'д Коверьянки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000381' as code, 'д Ковригино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000383' as code, 'д Колотилово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000385' as code, 'д Комаровка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000387' as code, 'д Кончинино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000389' as code, 'д Копылово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000391' as code, 'д Копытово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000393' as code, 'д Коргашино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000395' as code, 'д Космынка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000397' as code, 'д Костино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000399' as code, 'д Костино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000401' as code, 'с Костино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000403' as code, 'д Костюнино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000405' as code, 'д Кочергино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000407' as code, 'д Кромино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000409' as code, 'д Круглино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000411' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000413' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000415' as code, 'д Кузяево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000417' as code, 'д Кузяево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000419' as code, 'с Куликово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000421' as code, 'д Кульпино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000423' as code, 'д Куминово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000425' as code, 'д Куминово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000427' as code, 'д Кунисниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000429' as code, 'д Курово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000431' as code, 'д Курьково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000433' as code, 'д Лавровки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000435' as code, 'п Лавровки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000437' as code, 'д Левково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000439' as code, 'п Лесной' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000441' as code, 'д Липино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000443' as code, 'д Лифаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000445' as code, 'д Лишенино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000447' as code, 'д Лотосово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000449' as code, 'п Луговой' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000451' as code, 'д Лукьяново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000453' as code, 'д Лупаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000455' as code, 'д Лутьково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000457' as code, 'д Лучинское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000459' as code, 'д Малая Черная' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000461' as code, 'д Малое Насоново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000463' as code, 'д Малое Рогачево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000465' as code, 'д Малое Телешово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000467' as code, 'д Малыгино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000469' as code, 'д Малые Дубровки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000471' as code, 'д Маншино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000473' as code, 'д Маринино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000475' as code, 'д Мартыново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000477' as code, 'д Матвеево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000479' as code, 'д Матвейково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000481' as code, 'д Медведково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000483' as code, 'д Меленки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000485' as code, 'д Мелихово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000487' as code, 'п Мельчевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000489' as code, 'д Микишкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000491' as code, 'д Микляево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000493' as code, 'д Минеево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000495' as code, 'д Мисиново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000497' as code, 'д Митькино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000499' as code, 'д Михайловское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000501' as code, 'д Михалево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000503' as code, 'д Михеево-Сухарево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000505' as code, 'д Мишуково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000507' as code, 'д Морозово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000509' as code, 'д Мотовилово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000511' as code, 'д Муравьево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000513' as code, 'д Муханки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000515' as code, 'д Муханки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000517' as code, 'д Мышенки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000519' as code, 'д Надеждино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000521' as code, 'д Надмошье' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000523' as code, 'д Назарово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000525' as code, 'д Назарово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000527' as code, 'д Насадкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000529' as code, 'д Насоново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000531' as code, 'д Настасьино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000533' as code, 'д Непейно' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000535' as code, 'д Нерощино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000537' as code, 'д Нестерово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000539' as code, 'д Нестерцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000541' as code, 'д Нефедиха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000543' as code, 'д Нечаево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000545' as code, 'д Нижнево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000547' as code, 'д Никитино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000549' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000551' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000553' as code, 'д Никулино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000555' as code, 'д Никульское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000557' as code, 'д Новинки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000559' as code, 'д Новлянки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000561' as code, 'п Новое Гришино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000563' as code, 'д Новое Село' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000565' as code, 'д Новое Сельцо' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000567' as code, 'д Новокарцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000569' as code, 'п Новонекрасовский' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000571' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000573' as code, 'п Новосиньково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000575' as code, 'д Носково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000577' as code, 'д Овсянниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000579' as code, 'д Овсянниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000581' as code, 'д Овчино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000583' as code, 'с Озерецкое' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000585' as code, 'с Ольгово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000587' as code, 'д Ольсово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000589' as code, 'д Ольявидово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000591' as code, 'п опытного хозяйства Ермолино""' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000593' as code, 'п опытного хозяйства центральной торфо-болотной опытной станции' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000595' as code, 'д Орево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000597' as code, 'п Орево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000599' as code, 'с Орудьево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000601' as code, 'п Орудьевского т/б предприятия' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000603' as code, 'д Очево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000605' as code, 'д Пантелеево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000607' as code, 'д Паньково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000609' as code, 'д Парамоново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000611' as code, 'с Пересветово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000613' as code, 'д Пески' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000615' as code, 'д Петраково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000617' as code, 'д Пешково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000619' as code, 'д Плетенево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000621' as code, 'д Подвязново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000623' as code, 'д Подгорное' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000625' as code, 'д Поддубки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000627' as code, 'д Подмошье' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000629' as code, 'д Подосинки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000631' as code, 'п Подосинки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000633' as code, 'д Подсосенье' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000635' as code, 'с Подчерково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000637' as code, 'с Подъячево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000639' as code, 'д Поздняково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000641' as code, 'с Покровское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000643' as code, 'д Попадьино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000645' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000647' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000649' as code, 'д Поповское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000651' as code, 'д Поповское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000653' as code, 'д Постниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000655' as code, 'д Походкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000657' as code, 'д Притыкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000659' as code, 'д Прудцы' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000661' as code, 'д Пруды' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000663' as code, 'д Пулиха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000665' as code, 'д Пуриха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000667' as code, 'д Пустынь' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000669' as code, 'д Пыхино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000671' as code, 'п Раменский' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000673' as code, 'д Раменье' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000675' as code, 'д Ревякино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000677' as code, 'д Редькино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000679' as code, 'д Редькино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000681' as code, 'с Рогачево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000683' as code, 'д Рождествено' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000685' as code, 'д Рыбаки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000687' as code, 'п Рыбное' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000689' as code, 'д Саввино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000691' as code, 'д Савелово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000693' as code, 'д Савельево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000695' as code, 'д Садниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000697' as code, 'д Садовая' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000699' as code, 'д Сазонки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000701' as code, 'д Сальково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000703' as code, 'д Саморядово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000705' as code, 'д Сафоново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000707' as code, 'д Сбоево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000709' as code, 'д Свистуха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000711' as code, 'д Свистуха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000713' as code, 'д Святогорово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000715' as code, 'д Селевкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000717' as code, 'д Селиваново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000719' as code, 'д Селявино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000721' as code, 'д Семенково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000723' as code, 'с Семеновское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000725' as code, 'д Сергейково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000727' as code, 'с Синьково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000729' as code, 'д Сихнево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000731' as code, 'д Скриплево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000733' as code, 'д Слободищево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000735' as code, 'п совхоза Буденновец""' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000737' as code, 'п совхоза Останкино""' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000739' as code, 'д Соколовский Починок' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000741' as code, 'д Сокольники' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000743' as code, 'д Софрыгино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000745' as code, 'д Спас-Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000747' as code, 'д Спиридово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000749' as code, 'п станции Костино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000751' as code, 'д Старо' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000753' as code, 'д Старово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000755' as code, 'д Степаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000757' as code, 'д Стреково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000759' as code, 'д Ступино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000761' as code, 'п СУ-847' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000763' as code, 'д Сурмино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000765' as code, 'д Сысоево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000767' as code, 'д Сычевки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000769' as code, 'д Татищево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000771' as code, 'п Татищево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000773' as code, 'д Телешово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000775' as code, 'д Тендиково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000777' as code, 'д Терехово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000779' as code, 'д Теряево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000781' as code, 'д Тефаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000783' as code, 'с Тимоново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000785' as code, 'д Тимофеево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000787' as code, 'д Тимошкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000789' as code, 'д Титово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000791' as code, 'д Тишино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000793' as code, 'д Торговцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000795' as code, 'д Трехденево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000797' as code, 'с Трехсвятское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000799' as code, 'д Трощейково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000801' as code, 'д Труневки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000803' as code, 'с Турбичево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000805' as code, 'д Тютьково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000807' as code, 'д Удино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000809' as code, 'д Ульянки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000811' as code, 'д Усть-Пристань' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000813' as code, 'п Участок № 7' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000815' as code, 'п фабрики Первое Мая' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000817' as code, 'д Федоровка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000819' as code, 'д Федоровка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000821' as code, 'д Федоровское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000823' as code, 'д Федотово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000825' as code, 'д Филимоново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000827' as code, 'д Филимоново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000829' as code, 'д Фофаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000831' as code, 'д Фофаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000833' as code, 'д Харламово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000835' as code, 'д Хвостово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000837' as code, 'д Хлыбы' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000839' as code, 'д Хорошилово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000841' as code, 'д Хорьяково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000843' as code, 'с Храброво' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000845' as code, 'д Целеево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000847' as code, 'д Чайниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000849' as code, 'д Чеприно' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000851' as code, 'с Чернеево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000853' as code, 'д Черны' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000855' as code, 'д Шабаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000857' as code, 'д Шадрино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000859' as code, 'д Шелепино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000861' as code, 'д Шихово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000863' as code, 'д Шуколово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000865' as code, 'д Шулепниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000867' as code, 'д Шульгино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000869' as code, 'д Шустино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000871' as code, 'д Щепино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000873' as code, 'д Щетнево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000875' as code, 'д Эскино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000877' as code, 'д Юркино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000879' as code, 'д Юрьево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000881' as code, 'д Языково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000883' as code, 'д Яковлево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000885' as code, 'с Якоть' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000887' as code, 'д Ярово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000889' as code, 'д Ярцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000891' as code, 'п 3-й Участок' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46715000893' as code, 'п 4-й Участок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46722000056' as code, 'рп Рязановский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000001' as code, 'г Зарайск' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000106' as code, 'д Авдеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000111' as code, 'д Авдеевские Выселки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000116' as code, 'д Алтухово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46729000116' as code, 'д Алтухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000121' as code, 'д Алтухово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46729000121' as code, 'с Алтухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000126' as code, 'д Алферьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000131' as code, 'д Апонитищи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000136' as code, 'д Аргуново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000141' as code, 'д Астрамьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000146' as code, 'д Бавыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000151' as code, 'д Баребино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000156' as code, 'д Березники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000161' as code, 'д Беспятово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000166' as code, 'д Болваньково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000171' as code, 'д Болотня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000176' as code, 'д Большие Белыничи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000181' as code, 'д Большое Еськино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000186' as code, 'д Борисово-Околицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000191' as code, 'д Бровкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000196' as code, 'д Великое Поле' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000201' as code, 'д Верхнее Вельяминово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000206' as code, 'д Верхнее Плуталово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000211' as code, 'д Верхнее Маслово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000216' as code, 'д Веселкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000221' as code, 'д Воронино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000226' as code, 'д Гололобово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000231' as code, 'д Гремячево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000236' as code, 'д Давыдово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000241' as code, 'д Даровое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000246' as code, 'д Добрая Слободка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000251' as code, 'д Дубакино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000256' as code, 'д Дятлово 1' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000261' as code, 'д Дятлово 2' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000266' as code, 'д Дятлово-3' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000271' as code, 'д Ерново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000276' as code, 'с Жемово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000281' as code, 'д Жилконцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000286' as code, 'д Журавна' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000291' as code, 'д Зайцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000296' as code, 'д Замятино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000301' as code, 'п Зарайский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000306' as code, 'д Зимёнки-1' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000311' as code, 'д Злыхино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000316' as code, 'д Иванчиково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000321' as code, 'д Иваньшево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000326' as code, 'д Ивашково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000331' as code, 'д Ильицино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000336' as code, 'д Истоминка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000341' as code, 'д Карино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000346' as code, 'д Карманово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000351' as code, 'д Клепальники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000356' as code, 'д Клин-Бельдин' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000361' as code, 'д Кобылье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000366' as code, 'д Козловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000371' as code, 'д Комово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000376' as code, 'д Косовая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000381' as code, 'д Крутой Верх' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000386' as code, 'д Кувшиново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000391' as code, 'д Кудиново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000396' as code, 'д Куково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000401' as code, 'д Латыгори' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000406' as code, 'д Летуново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000411' as code, 'д Логвёново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000416' as code, 'с Макеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000421' as code, 'д Малое Еськино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000426' as code, 'д Малые Белыничи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000431' as code, 'д Маркино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000436' as code, 'п Масловский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000441' as code, 'д Машоново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000446' as code, 'д Мендюкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000451' as code, 'д Михалево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000456' as code, 'д Мишино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000461' as code, 'д Моногарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000466' as code, 'д Назарьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000471' as code, 'д Нижнее Вельяминово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000476' as code, 'д Нижнее Плуталово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000481' as code, 'д Никитино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000486' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000491' as code, 'д Новая Деревня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000496' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000501' as code, 'д Овечкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000506' as code, 'д Озерки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000511' as code, 'п Отделения 2 совхоза Зарайский""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000516' as code, 'д Пенкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000521' as code, 'д Перепелкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000526' as code, 'д Пески' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000531' as code, 'д Печерники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000536' as code, 'д Потлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000541' as code, 'д Пронюхлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000546' as code, 'с Протекино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000551' as code, 'д Прудки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000556' as code, 'д Пыжево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000561' as code, 'д Радушино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000566' as code, 'д Рассохты' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000571' as code, 'д Ратькино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000576' as code, 'д Рожново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000581' as code, 'д Рябцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000586' as code, 'д Саблино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000591' as code, 'д Секирино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000596' as code, 'д Ситьково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000601' as code, 'д Слепцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000606' as code, 'д Солопово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000611' as code, 'д Сохино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000616' as code, 'с Спас-Дощатый' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000621' as code, 'д Староподастрамьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000626' as code, 'д Староподгороднее' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000631' as code, 'д Столпово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000636' as code, 'д Струпна' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000641' as code, 'д Татины' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000646' as code, 'д Титово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000651' as code, 'д Трасна' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000656' as code, 'д Требовое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000661' as code, 'д Трегубово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000666' as code, 'д Федоровка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000671' as code, 'д Филипповичи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000676' as code, 'д Хлопово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000681' as code, 'п центральной усадьбы совхоза 40 лет Октября""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000686' as code, 'д Черемошня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000691' as code, 'д Чернево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000696' as code, 'д Чирьяково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000701' as code, 'с Чулки-Соколово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000706' as code, 'д Шарапово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000711' as code, 'д Широбоково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000716' as code, 'д Шистово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46729000721' as code, 'д Якшино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000001' as code, 'г Истра' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000006' as code, 'г Дедовск' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000056' as code, 'дп Снегири' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000104' as code, 'п Агрогородок' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000107' as code, 'д Адуево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000110' as code, 'д Александрово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000113' as code, 'д Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000116' as code, 'д Алексино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000119' as code, 'д Алёхново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000122' as code, 'д Ананово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000125' as code, 'д Андреевское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000128' as code, 'д Аносино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000131' as code, 'д Антоновка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000134' as code, 'д Армягово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000137' as code, 'д Бабкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000140' as code, 'х Берёзовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000143' as code, 'д Бодрово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000146' as code, 'д Большое Ушаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000149' as code, 'д Борзые' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000152' as code, 'д Борисково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000155' as code, 'д Борки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000158' as code, 'д Бочкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000161' as code, 'д Брыково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000164' as code, 'д Будьково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000167' as code, 'д Бужарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000170' as code, 'д Букарёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000173' as code, 'д Буньково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000176' as code, 'д Бутырки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000179' as code, 'д Васильевское-Голохвастово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000182' as code, 'д Веледниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000185' as code, 'д Вельяминово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000188' as code, 'д Веретёнки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000191' as code, 'д Верхуртово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000194' as code, 'д Воронино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000197' as code, 'д Воскресёнки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000200' as code, 'д Высоково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000203' as code, 'п гидроузла им Куйбышева' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000206' as code, 'д Глебово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000209' as code, 'д Глебово-Избище' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000212' as code, 'п Глебовский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000215' as code, 'д Глинки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000218' as code, 'д Головино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000221' as code, 'д Гомово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000224' as code, 'д Гордово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000227' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000230' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000233' as code, 'д Горнево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000236' as code, 'д Горшково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000239' as code, 'д Граворново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000242' as code, 'д Гребеньки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000245' as code, 'д Давыдково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000248' as code, 'д Давыдовское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000251' as code, 'с Дарна' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000254' as code, 'д Дедёшино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000257' as code, 'д Дедово-Талызино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000260' as code, 'п Дедовской школы-интерната' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000263' as code, 'д Денежкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000266' as code, 'д Деньково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000269' as code, 'д Дергайково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000272' as code, 'д Долево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000275' as code, 'п дома отдыха Румянцево""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000278' as code, 'п дома отдыха им А.П. Чехова' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000281' as code, 'д Дубровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000284' as code, 'д Дуплёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000287' as code, 'д Духанино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000290' as code, 'д Дьяково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000293' as code, 'д Еремеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000296' as code, 'д Ермолино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000299' as code, 'д Ефимоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000302' as code, 'д Жевнево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000305' as code, 'д Жилкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000308' as code, 'д Житянино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000311' as code, 'д Загорье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000314' as code, 'д Захарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000317' as code, 'д Зеленково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000320' as code, 'п Зелёный Курган' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000323' as code, 'д Зенькино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000326' as code, 'д Зорино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000329' as code, 'д Зыково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000332' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000335' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000338' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000341' as code, 'д Ильино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000344' as code, 'д Исаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000347' as code, 'д Карасино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000350' as code, 'д Карцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000353' as code, 'д Качаброво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000356' as code, 'д Кашино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000359' as code, 'д Киселёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000362' as code, 'д Козенки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000365' as code, 'п Колшино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000368' as code, 'д Кореньки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000371' as code, 'д Корсаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000374' as code, 'д Кострово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000377' as code, 'д Котерево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000380' as code, 'д Котово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000383' as code, 'п Котово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000386' as code, 'п Красная Горка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000389' as code, 'д Красновидово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000392' as code, 'п Красный' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000395' as code, 'д Красный Посёлок' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000398' as code, 'д Крюково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000401' as code, 'д Крючково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000404' as code, 'д Курово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000407' as code, 'п Курсаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000410' as code, 'д Куртниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000413' as code, 'д Кучи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000416' as code, 'д Кучи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000419' as code, 'д Ламишино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000422' as code, 'х Ламишино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000425' as code, 'д Ламоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000428' as code, 'д Ленино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000431' as code, 'д Леоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000434' as code, 'д Леоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000437' as code, 'п Лесодолгоруково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000440' as code, 'д Лечищево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000443' as code, 'д Лешково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000446' as code, 'д Лисавино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000449' as code, 'д Лобаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000452' as code, 'д Лужки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000455' as code, 'д Лужки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000458' as code, 'д Лукино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000461' as code, 'с Лучинское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000464' as code, 'д Лыщёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000467' as code, 'д Львово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000470' as code, 'д Мазилово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000473' as code, 'д Максимовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000476' as code, 'д Малое Ушаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000479' as code, 'д Манихино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000482' as code, 'д Мансурово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000485' as code, 'д Марково-Курсаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000488' as code, 'д Мартюшино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000491' as code, 'д Матвейково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000494' as code, 'д Медведки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000497' as code, 'д Меры' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000500' as code, 'д Михайловка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000503' as code, 'д Мыканино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000506' as code, 'д Надеждино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000509' as code, 'д Надеждино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000512' as code, 'д Надовражино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000515' as code, 'д Назарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000518' as code, 'д Нижневасильевское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000521' as code, 'д Никитское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000524' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000527' as code, 'д Никулино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000530' as code, 'д Новинки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000533' as code, 'д Новодарьино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000536' as code, 'с Новопетровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000539' as code, 'д Новораково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000542' as code, 'д Новосёлово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000545' as code, 'д Обновлённый Труд' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000548' as code, 'д Обушково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000551' as code, 'д Огарково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000554' as code, 'п Огниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000557' as code, 'п Октябрьский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000560' as code, 'п Октябрьской фабрики' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000563' as code, 'с Онуфриево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000566' as code, 'п опытного производственного хозяйства Манихино""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000569' as code, 'с Павловская Слобода' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000572' as code, 'д Павловское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000575' as code, 'д Падиково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000578' as code, 'п пансионата Берёзка""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000581' as code, 'д Парфёнки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000584' as code, 'п Первомайский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000587' as code, 'д Первомайское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000590' as code, 'д Петрово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000593' as code, 'д Петровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000596' as code, 'д Петровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000599' as code, 'д Петушки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000602' as code, 'п Пионерский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000605' as code, 'д Пирогово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000608' as code, 'д Писково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000611' as code, 'д Подпорино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000614' as code, 'д Покоево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000617' as code, 'д Покровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000620' as code, 'п Полевшина' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000623' as code, 'д Пречистое' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000626' as code, 'д Раково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000629' as code, 'д Ремянники' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000632' as code, 'д Родионцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000635' as code, 'д Рождествено' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000638' as code, 'с Рождествено' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000641' as code, 'д Рожново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000644' as code, 'д Рубцово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000647' as code, 'п Румянцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000650' as code, 'д Рыбушки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000653' as code, 'д Рыжково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000656' as code, 'д Рычково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000659' as code, 'д Савельево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000662' as code, 'д Садки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000665' as code, 'д Санниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000668' as code, 'д Сафонтьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000671' as code, 'п Северный' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000674' as code, 'д Селиваниха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000677' as code, 'д Синево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000680' as code, 'д Скрябино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000683' as code, 'д Слабошеино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000686' as code, 'д Славково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000689' as code, 'д Сокольники' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000692' as code, 'д Сорокино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000695' as code, 'п станции Лукино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000698' as code, 'п станции Манихино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000701' as code, 'п станции Холщёвики' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000704' as code, 'п Станция Павловская Слобода' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000707' as code, 'д Сысоево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000710' as code, 'д Талицы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000713' as code, 'д Татищево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000716' as code, 'д Телепнево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000719' as code, 'д Троица' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000722' as code, 'п Троицкий' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000725' as code, 'д Трусово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000728' as code, 'д Турово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000731' as code, 'д Ульево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000734' as code, 'д Устиново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000737' as code, 'х Фёдоровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000740' as code, 'д Филатово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000743' as code, 'д Фроловское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000746' as code, 'д Хволово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000749' as code, 'д Хмолино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000752' as code, 'д Хованское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000755' as code, 'д Холмы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000758' as code, 'д Холщёвики' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000761' as code, 'п Хуторки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000764' as code, 'д Чаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000767' as code, 'д Часовня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000770' as code, 'д Чёрная' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000773' as code, 'д Чесноково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000776' as code, 'п Чеховский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000779' as code, 'д Чудцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000782' as code, 'д Шаблыкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000785' as code, 'д Шапково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000788' as code, 'д Шебаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000791' as code, 'д Шейно' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000794' as code, 'п Шёлковая Гора' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000797' as code, 'д Шишаиха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000800' as code, 'д Юркино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000803' as code, 'д Юрьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000806' as code, 'д Ябедино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000809' as code, 'д Ядромино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46733000812' as code, 'д Якунино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000' as code, 'Клин' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000001' as code, 'г Клин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000006' as code, 'г Высоковск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000056' as code, 'рп Решетниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000104' as code, 'д Акатово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000107' as code, 'д Акатьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000110' as code, 'д Аксениха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000113' as code, 'д Аксеново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000116' as code, 'д Акулово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000119' as code, 'д Александрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000122' as code, 'д Алексейково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000125' as code, 'д Алферьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000128' as code, 'д Ананьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000131' as code, 'д Андрианково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000134' as code, 'д Андрианково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000137' as code, 'д Анненка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000140' as code, 'д Атеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000143' as code, 'д Афанасово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000146' as code, 'д Бакланово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000149' as code, 'д Бекетово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000152' as code, 'д Белавино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000155' as code, 'д Белозерки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000158' as code, 'д Березино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000161' as code, 'д Бирево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000164' as code, 'д Боблово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000167' as code, 'д Богаиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000170' as code, 'д Болдыриха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000173' as code, 'д Большое Щапово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000176' as code, 'д Борис-Глеб' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000179' as code, 'д Борисово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000182' as code, 'д Борихино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000185' as code, 'д Борки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000188' as code, 'д Борозда' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000191' as code, 'д Бортниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000194' as code, 'д Бортницы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000197' as code, 'с Борщево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000200' as code, 'д Бутырки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000203' as code, 'д Василево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000206' as code, 'д Васильевское-Соймоново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000209' as code, 'д Васильково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000212' as code, 'д Ватолино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000215' as code, 'д Введенское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000218' as code, 'д Вельмогово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000221' as code, 'д Вертково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000224' as code, 'д Владимировка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000227' as code, 'д Владыкина Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000230' as code, 'д Владыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000233' as code, 'с Воздвиженское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000236' as code, 'д Воловниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000239' as code, 'д Волосово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000242' as code, 'д Воронино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000245' as code, 'п Выголь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000248' as code, 'д Высоково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000251' as code, 'д Вьюхово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000254' as code, 'д Гафидово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000257' as code, 'д Глухино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000260' as code, 'д Голенищево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000263' as code, 'д Голиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000266' as code, 'д Головково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000269' as code, 'д Гологузово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000272' as code, 'д Голышкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000275' as code, 'д Горбово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000278' as code, 'д Горицы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000281' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000284' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000287' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000290' as code, 'д Грешнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000293' as code, 'д Григорьевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000296' as code, 'д Губино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000299' as code, 'д Давыдково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000302' as code, 'п Демьяново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000305' as code, 'д Денисово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000308' as code, 'д Дмитроково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000311' as code, 'п дома отдыха Высокое""' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000314' as code, 'д Доршево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000317' as code, 'д Дурасово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000320' as code, 'д Дятлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000323' as code, 'д Егорьевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000326' as code, 'д Екатериновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000329' as code, 'д Елгозино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000332' as code, 'д Елино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000335' as code, 'д Ельцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000338' as code, 'д Еросимово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000341' as code, 'д Жестоки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000344' as code, 'д Жуково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000347' as code, 'д Заболотье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000350' as code, 'д Задний Двор' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000353' as code, 'д Залесье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000356' as code, 'д Заовражье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000359' as code, 'д Захарово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000362' as code, 'д Захарово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000365' as code, 'д Золино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000368' as code, 'п Зубово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000371' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000374' as code, 'с Ивановское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000377' as code, 'д Игумново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000380' as code, 'д Иевлево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000383' as code, 'д Ильино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000386' as code, 'д им Дмитриева' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000389' as code, 'д Исаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000392' as code, 'д Кадниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000395' as code, 'д Калинино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000398' as code, 'д Караваево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000401' as code, 'п кирпичного завода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000404' as code, 'д Китенево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000407' as code, 'д Кленково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000410' as code, 'д Климовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000413' as code, 'д Княгинино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000416' as code, 'д Ковылино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000419' as code, 'д Колосово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000422' as code, 'д Комлево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000425' as code, 'д Кондырино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000428' as code, 'д Кононово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000431' as code, 'д Коноплино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000434' as code, 'д Копылово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000437' as code, 'д Кореньки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000440' as code, 'д Корост' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000443' as code, 'д Косово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000446' as code, 'д Коськово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000449' as code, 'д Красный Холм' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000452' as code, 'д Крупенино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000455' as code, 'д Крутцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000458' as code, 'д Крюково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000461' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000464' as code, 'д Кузнечково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000467' as code, 'д Лаврово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000470' as code, 'д Лазарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000473' as code, 'п Лесной' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000476' as code, 'д Ловцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000479' as code, 'д Лукино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000482' as code, 'д Максимково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000485' as code, 'д Макшеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000488' as code, 'д Малая Борщёвка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000491' as code, 'д Малеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000494' as code, 'д Малое Щапово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000497' as code, 'д Марино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000500' as code, 'п Марков Лес' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000503' as code, 'д Марфино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000506' as code, 'д Масюгино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000509' as code, 'д Матвеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000512' as code, 'д Мащерово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000515' as code, 'д Медведково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000518' as code, 'д Меленки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000521' as code, 'д Микляево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000524' as code, 'д Милухино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000527' as code, 'д Минино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000530' as code, 'д Мисирёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000533' as code, 'д Михайловское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000536' as code, 'д Мишнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000539' as code, 'д Мужево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000542' as code, 'д Мякинино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000545' as code, 'д Нагорное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000548' as code, 'д Нагорное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000551' as code, 'д Надеждино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000554' as code, 'д Назарьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000557' as code, 'д Напругово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000560' as code, 'п Нарынка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000563' as code, 'д Некрасино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000566' as code, 'д Непейцино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000569' as code, 'д Никитское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000572' as code, 'д Николаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000575' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000578' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000581' as code, 'д Новиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000584' as code, 'д Новинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000587' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000590' as code, 'д Новощапово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000593' as code, 'д Ногово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000596' as code, 'п Нудоль' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000599' as code, 'д Овсянниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000602' as code, 'д Опалево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000605' as code, 'д Опритово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000608' as code, 'д Орлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000611' as code, 'д Отрада' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000614' as code, 'д Павельцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000617' as code, 'д Папивино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000620' as code, 'д Парфенькино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000623' as code, 'д Першутино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000626' as code, 'д Петровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000629' as code, 'с Петровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000632' as code, 'д Плюсково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000635' as code, 'п ПМК-8' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000638' as code, 'д Поджигородово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000641' as code, 'д Подоистрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000644' as code, 'д Подорки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000647' as code, 'д Подтеребово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000650' as code, 'д Покров' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000653' as code, 'д Покровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000656' as code, 'д Покровское-Жуково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000659' as code, 'д Полуханово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000662' as code, 'д Полушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000665' as code, 'д Попелково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000668' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000671' as code, 'д Праслово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000674' as code, 'д Пупцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000677' as code, 'д Пустые Меленки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000680' as code, 'д Радованье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000683' as code, 'п Раздолье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000686' as code, 'д Решоткино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000689' as code, 'д Рогатино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000692' as code, 'д Рубчиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000695' as code, 'д Румяново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000698' as code, 'д Русино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000701' as code, 'д Савино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000704' as code, 'д Свистуново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000707' as code, 'д Селевино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000710' as code, 'с Селинское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000713' as code, 'д Селифоново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000716' as code, 'д Семенково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000719' as code, 'д Семчино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000722' as code, 'д Сергеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000725' as code, 'д Сидорково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000728' as code, 'д Синьково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000731' as code, 'д Ситники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000734' as code, 'д Скрепящево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000737' as code, 'д Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000740' as code, 'д Слободка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000743' as code, 'д Сметанино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000746' as code, 'д Соголево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000749' as code, 'д Соково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000752' as code, 'д Соколово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000755' as code, 'д Сохино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000758' as code, 'с Спас-Заулок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000761' as code, 'д Спас-Коркодино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000764' as code, 'д Спасское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000767' as code, 'д Спецово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000770' as code, 'д Степанцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000773' as code, 'д Степаньково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000776' as code, 'д Стреглово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000779' as code, 'д Стрелково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000782' as code, 'д Струбково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000785' as code, 'д Таксино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000788' as code, 'д Тарасово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000791' as code, 'д Тархово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000794' as code, 'д Темново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000797' as code, 'д Теренино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000800' as code, 'д Терехова' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000803' as code, 'д Тетерино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000806' as code, 'д Тиликтино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000809' as code, 'д Тимонино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000812' as code, 'д Титково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000815' as code, 'д Тихомирово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000818' as code, 'д Третьяково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000821' as code, 'д Трехденево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000824' as code, 'д Троицино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000827' as code, 'д Троицкое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000830' as code, 'д Троицкое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000833' as code, 'п Туркмен' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000836' as code, 'д Украинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000839' as code, 'д Фроловское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000842' as code, 'д Хлыниха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000845' as code, 'д Хохлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000848' as code, 'п Чайковского' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000851' as code, 'д Чернятино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000854' as code, 'д Чумичево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000857' as code, 'д Шарино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000860' as code, 'д Шевелево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000863' as code, 'д Шевериха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000866' as code, 'п Шевляково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000869' as code, 'д Шипулино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000872' as code, 'д Ширяево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000875' as code, 'д Щекино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000878' as code, 'д Языково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000881' as code, 'д Ямуга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000884' as code, 'п Ямуга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46737000887' as code, 'д Ясенево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000056' as code, 'рп Пески' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46738000103' as code, 'п Пески' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000106' as code, 'с Акатьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000111' as code, 'с Амерево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000116' as code, 'д Андреевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000121' as code, 'с Андреевское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000126' as code, 'д Апраксино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000131' as code, 'д Афанасьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000136' as code, 'д Бакунино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000141' as code, 'д Барановка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000146' as code, 'д Берняково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000151' as code, 'п Биорки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000156' as code, 'д Богдановка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000161' as code, 'д Богородское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000166' as code, 'д Большое Карасёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000171' as code, 'с Большое Колычево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000176' as code, 'д Борисово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000181' as code, 'д Борисовское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000186' as code, 'д Бортниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000191' as code, 'д Бузуково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000196' as code, 'с Васильево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000201' as code, 'д Верхнее Хорошово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000206' as code, 'п Возрождение' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000211' as code, 'д Воловичи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000216' as code, 'д Ворыпаевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000221' as code, 'с Гололобово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000226' as code, 'с Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000231' as code, 'д Горностаево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000236' as code, 'с Городец' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000241' as code, 'д Городище-Юшково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000246' as code, 'д Городки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000251' as code, 'д Грайвороны' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000256' as code, 'д Гришино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000261' as code, 'д Губастово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000266' as code, 'с Дарищи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000271' as code, 'д Дворики' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000276' as code, 'с Дмитровцы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000281' as code, 'д Дубенки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000286' as code, 'д Дубна' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000291' as code, 'д Дуброво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000296' as code, 'д Елино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000301' as code, 'д Ерково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000306' as code, 'п Запрудный' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000311' as code, 'п Заречный' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000316' as code, 'д Зарудня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000321' as code, 'д Захаркино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000326' as code, 'д Зиновьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000331' as code, 'д Змеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000336' as code, 'д Игнатьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000341' as code, 'д Ильинское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000346' as code, 'п Индустрия' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000351' as code, 'д Исаиха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000356' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46738000356' as code, 'п Каменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46738000361' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000361' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000366' as code, 'д Колодкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000371' as code, 'д Комлево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000376' as code, 'д Конев-Бор' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000381' as code, 'с Коробчеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000386' as code, 'д Коростыли' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000391' as code, 'д Кочаброво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000396' as code, 'д Кудрявцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000401' as code, 'д Куземкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000406' as code, 'п Лесной' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000411' as code, 'с Лукерьино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000416' as code, 'д Лыково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000421' as code, 'с Лысцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000426' as code, 'с Макшеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000431' as code, 'с Маливо' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000436' as code, 'д Малое Карасёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000441' as code, 'д Малое Уварово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000446' as code, 'д Малышево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000451' as code, 'д Михеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46738000451' as code, 'п Михеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000456' as code, 'д Михеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46738000456' as code, 'д Михеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000461' as code, 'д Молитвино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000466' as code, 'д Молодинки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000471' as code, 'д Морозовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000476' as code, 'д Мостищи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000481' as code, 'д Мякинино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000486' as code, 'с Мячково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000491' as code, 'д Настасьино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000496' as code, 'д Негомож' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000501' as code, 'с Непецино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000506' as code, 'д Нестерово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000511' as code, 'с Нижнее Хорошово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000516' as code, 'с Никульское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000521' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000526' as code, 'д Новое' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000531' as code, 'с Новое Бобренево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000536' as code, 'д Новопокровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000541' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46738000541' as code, 'п Новоселки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000546' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46738000546' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000551' as code, 'с Октябрьское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000556' as code, 'п Осёнка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000561' as code, 'д Павлеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000566' as code, 'д Паново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000571' as code, 'д Паньшино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000576' as code, 'с Парфентьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000581' as code, 'п Первомайский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000586' as code, 'с Пестриково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000591' as code, 'д Петрово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000596' as code, 'д Печенцино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000601' as code, 'с Пирочи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000606' as code, 'с Подберезники' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000611' as code, 'д Подлужье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000616' as code, 'д Подмалинки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000621' as code, 'д Подосинки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000626' as code, 'д Полубояриново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000631' as code, 'п Проводник' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000636' as code, 'с Пруссы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000641' as code, 'п Радужный' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000646' as code, 'д Речки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000651' as code, 'д Рождественка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000656' as code, 'д Романовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000661' as code, 'д Санино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000666' as code, 'с Северское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000671' as code, 'д Сельниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000676' as code, 'д Сельцо-Петровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46738000681' as code, 'д Семёновское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000681' as code, 'д Семёновское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000686' as code, 'д Семёновское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46738000686' as code, 'п Семёновское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000691' as code, 'д Семибратское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000696' as code, 'п Сергиевский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000701' as code, 'с Сергиевское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000706' as code, 'д Солосцово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000711' as code, 'п станции Непецино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000716' as code, 'с Старое Бобренево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000721' as code, 'д Субботово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000726' as code, 'д Сурино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000731' as code, 'д Сычёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000736' as code, 'д Тимирёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000741' as code, 'с Троицкие Озёрки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000746' as code, 'д Туменское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000751' as code, 'д Угорная Слобода' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000756' as code, 'д Ульяновка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000761' as code, 'с Федосьино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000766' as code, 'д Хлопна' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000771' as code, 'с Чанки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000776' as code, 'с Черкизово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000781' as code, 'д Чуркино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000786' as code, 'д Шапкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000791' as code, 'д Шейно' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000796' as code, 'д Шелухино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000801' as code, 'с Шеметово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000806' as code, 'д Шереметьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000811' as code, 'с Шкинь' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000816' as code, 'п шлюза Северка""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000821' as code, 'д Щепотьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46738000826' as code, 'д Щурово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000056' as code, 'рп Свердловский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000106' as code, 'с Анискино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000111' as code, 'п Аничково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000116' as code, 'п Биокомбината' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000121' as code, 'д Кармолино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000126' as code, 'д Корпуса' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000131' as code, 'д Леониха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000136' as code, 'п Медное-Власово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000141' as code, 'д Мизиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000146' as code, 'д Митянино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000151' as code, 'д Орловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000156' as code, 'д Осеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000161' as code, 'д Райки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000166' as code, 'д Савинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000171' as code, 'д Топорково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000176' as code, 'д Улиткино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46742000181' as code, 'п Юность' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000001' as code, 'г Красногорск' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000056' as code, 'рп Нахабино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000106' as code, 'д Александровка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000111' as code, 'с Ангелово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000116' as code, 'д Аристово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000121' as code, 'п Архангельское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000126' as code, 'д Бузланово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000131' as code, 'д Воронки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000136' as code, 'д Гаврилково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000141' as code, 'д Глухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000146' as code, 'д Гольево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000151' as code, 'д Грибаново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000156' as code, 'п дачного хозяйства Архангельское""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000161' as code, 'с Дмитровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000166' as code, 'д Желябино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000171' as code, 'д Захарково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000176' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000181' as code, 'с Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000186' as code, 'п Ильинское-Усово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000191' as code, 'п Инженерный 1' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000196' as code, 'п Истра' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000201' as code, 'д Козино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000206' as code, 'д Коростово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000211' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000216' as code, 'п Мечниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000221' as code, 'д Михалково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000226' as code, 'д Нефедьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000231' as code, 'с Николо-Урюпино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000236' as code, 'п Новый' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000241' as code, 'п Отрадное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000246' as code, 'с Петрово-Дальнее' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000251' as code, 'д Поздняково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000256' as code, 'д Путилково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000261' as code, 'д Сабурово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000266' as code, 'п Светлые Горы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000271' as code, 'д Степановское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46744000276' as code, 'д Тимошкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000' as code, 'Можайский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000001' as code, 'г Можайск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000056' as code, 'рп Уваровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000103' as code, 'д Авдотьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000105' as code, 'д Акиньшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000108' as code, 'д Аксаново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000110' as code, 'д Аксентьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000113' as code, 'д Александровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000115' as code, 'д Александрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000118' as code, 'д Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000120' as code, 'д Алексеенки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000123' as code, 'д Алискино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000125' as code, 'д Андреевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000128' as code, 'д Аниканово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000130' as code, 'д Аникино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000133' as code, 'д Антоново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000135' as code, 'д Арбеково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000138' as code, 'д Артемки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000140' as code, 'д Астафьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000143' as code, 'д Бабаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000145' as code, 'д Бабынино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000148' as code, 'д Бакулино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000150' as code, 'д Балобново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000153' as code, 'д Бараново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000155' as code, 'д Бараново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000158' as code, 'д Бараново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000160' as code, 'д Барсуки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000163' as code, 'д Бартеньево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000165' as code, 'д Барцылово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000168' as code, 'д Барыши' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000170' as code, 'д Батынки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000173' as code, 'д Бедняково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000175' as code, 'д Беззубово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000178' as code, 'д Бели' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000180' as code, 'д Блазново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000183' as code, 'д Бобры' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000185' as code, 'д Болото Старое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000188' as code, 'д Большие Парфенки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000190' as code, 'д Большое Новосурино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000193' as code, 'д Большое Соколово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000195' as code, 'д Большое Тёсово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000198' as code, 'с Борисово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000200' as code, 'д Бородавкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000203' as code, 'д Бородино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000205' as code, 'п Бородино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000208' as code, 'п Бородинского лесничества' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000210' as code, 'п Бородинского музея' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000213' as code, 'п Бородинское Поле' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000215' as code, 'д Бражниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000218' as code, 'д Бугайлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000220' as code, 'д Бурково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000223' as code, 'д Бурмакино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000225' as code, 'д Бурцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000228' as code, 'д Бурцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000230' as code, 'д Бутырки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000233' as code, 'д Бухарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000235' as code, 'д Бычково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000238' as code, 'д Бычково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000240' as code, 'д Валуево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000243' as code, 'д Васюково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000245' as code, 'д Ваулино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000248' as code, 'д Вельяшево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000250' as code, 'д Вёшки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000253' as code, 'д Вишенки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000255' as code, 'д Власово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000258' as code, 'д Власово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000260' as code, 'д Волосково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000263' as code, 'д Воронино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000265' as code, 'д Вороново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000268' as code, 'д Воронцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000270' as code, 'д Воронцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000273' as code, 'п Ворошилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000275' as code, 'д Высокое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000278' as code, 'д Вышнее' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000280' as code, 'д Вяземское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000283' as code, 'д Гавшино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000285' as code, 'д Гальчино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000288' as code, 'п Гидроузел' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000290' as code, 'д Глазово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000293' as code, 'д Глуховка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000295' as code, 'д Глядково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000298' as code, 'д Головино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000300' as code, 'д Головино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000303' as code, 'д Голышкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000305' as code, 'д Горбуны' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000308' as code, 'д Горетово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000310' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000313' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000315' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000318' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000320' as code, 'д Горячкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000323' as code, 'д Грибово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000325' as code, 'д Гриднево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000328' as code, 'д Грязи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000330' as code, 'д Губино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000333' as code, 'д Дальнее' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000335' as code, 'д Дегтяри' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000338' as code, 'д Демихово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000340' as code, 'д Денежниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000343' as code, 'д Денисьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000345' as code, 'д Дёрново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000348' as code, 'д Долгинино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000350' as code, 'п дома отдыха Красный стан""' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000353' as code, 'п дорожно-эксплуатационного участка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000355' as code, 'д Доронино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000358' as code, 'д Дровнино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000360' as code, 'д Дурнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000363' as code, 'д Дурыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000365' as code, 'д Дьяково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000368' as code, 'д Елево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000370' as code, 'д Ельник' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000373' as code, 'д Ельня' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000375' as code, 'д Еремеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000378' as code, 'д Ерышово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000380' as code, 'д Желомеено' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000383' as code, 'д Жизлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000385' as code, 'д Замошье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000388' as code, 'д Заполье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000390' as code, 'д Заречная Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000393' as code, 'д Заречье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000395' as code, 'д Заслонино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000398' as code, 'д Захаровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000400' as code, 'д Захарьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000403' as code, 'д Зачатьё' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000405' as code, 'д Збышки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000408' as code, 'д Зенино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000410' as code, 'д Знаменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000413' as code, 'д Золотилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000415' as code, 'д Ивакино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000418' as code, 'д Игумново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000420' as code, 'д Ильинская Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000423' as code, 'д Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000425' as code, 'п им Дзержинского' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000428' as code, 'д Исавицы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000430' as code, 'д Калужское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000433' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000435' as code, 'д Камынинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000438' as code, 'д Каржень' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000440' as code, 'п карьероуправления' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000443' as code, 'д Кикино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000445' as code, 'д Киселёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000448' as code, 'д Клементьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000450' as code, 'д Клемятино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000453' as code, 'д Кобяково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000455' as code, 'д Ковалёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000458' as code, 'д Кожино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000460' as code, 'д Кожухово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000463' as code, 'д Колоцкое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000465' as code, 'п Колычёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000468' as code, 'д Копытово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000470' as code, 'д Коровино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000473' as code, 'д Корытово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000475' as code, 'д Корытцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000478' as code, 'д Косьмово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000480' as code, 'д Красновидово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000483' as code, 'д Красноиншино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000485' as code, 'д Красный Балтиец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000488' as code, 'д Красный Стан' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000490' as code, 'д Криушино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000493' as code, 'д Кромино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000495' as code, 'д Крылатки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000498' as code, 'д Крюково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000500' as code, 'д Кубаревка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000503' as code, 'д Кузяево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000505' as code, 'д Кукарино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000508' as code, 'д Купрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000510' as code, 'д Куровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000513' as code, 'д Кусково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000515' as code, 'д Кутлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000518' as code, 'д Ладыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000520' as code, 'д Левашово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000523' as code, 'п Лесное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000525' as code, 'п леспромхоза' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000528' as code, 'п лесхоза Юрлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000530' as code, 'д Липовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000533' as code, 'д Липуниха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000535' as code, 'д Лобково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000538' as code, 'д Логиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000540' as code, 'д Лопатино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000543' as code, 'д Лубёнки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000545' as code, 'д Лусось' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000548' as code, 'д Лыкшево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000550' as code, 'д Лысково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000553' as code, 'д Лыткино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000555' as code, 'д Люльки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000558' as code, 'д Макарово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000560' as code, 'д Маклаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000563' as code, 'д Маланьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000565' as code, 'д Малое Новосурино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000568' as code, 'д Малое Соколово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000570' as code, 'д Малое Тёсово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000573' as code, 'д Малые Парфенки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000575' as code, 'д Малые Решники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000578' as code, 'д Марфин-Брод' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000580' as code, 'д Махово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000583' as code, 'п медико-инструментального завода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000585' as code, 'д Межутино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000588' as code, 'д Милятино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000590' as code, 'п Мира' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000593' as code, 'д Митино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000595' as code, 'д Митьково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000598' as code, 'д Михайловское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000600' as code, 'д Михайловское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000603' as code, 'д Михалёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000605' as code, 'д Моденово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000608' as code, 'д Мокрое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000610' as code, 'д Мордвиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000613' as code, 'д Москворецкая Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000615' as code, 'д Мотягино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000618' as code, 'д Мышкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000620' as code, 'д Наричино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000623' as code, 'д Настасьино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000625' as code, 'д Небогатово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000628' as code, 'д Некрасово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000630' as code, 'д Неровново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000633' as code, 'д Никитино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000635' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000638' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000640' as code, 'д Новинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000643' as code, 'д Нововасильевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000645' as code, 'д Новое Село' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000648' as code, 'д Новомихайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000650' as code, 'д Новопокров' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000653' as code, 'д Новопоречье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000655' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000658' as code, 'д Новые Сычики' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000660' as code, 'д Новый Путь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000663' as code, 'д Облянищево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000665' as code, 'д Острицы-1' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000668' as code, 'д Острицы-2' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000670' as code, 'п отделения-4 совхоза Павлищево""' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000673' as code, 'д Отяково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000675' as code, 'д Павлищево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000678' as code, 'д Панино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000680' as code, 'д Пасильево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000683' as code, 'д Пеньгово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000685' as code, 'д Первое Мая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000688' as code, 'д Перещапово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000690' as code, 'д Петраково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000693' as code, 'д Петрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000695' as code, 'д Плешаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000698' as code, 'д Погорелое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000700' as code, 'д Подсосенье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000703' as code, 'д Поздняково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000705' as code, 'д Поминово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000708' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000710' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000713' as code, 'с Поречье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000715' as code, 'д Потапово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000718' as code, 'д Поченичено' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000720' as code, 'д Починки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000723' as code, 'д Праслово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000725' as code, 'д Преснецово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000728' as code, 'д Приданцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000730' as code, 'д Прокофьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000733' as code, 'д Прудня' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000735' as code, 'д Псарёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000738' as code, 'д Псарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000740' as code, 'д Пуршево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000743' as code, 'д Пушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000745' as code, 'д Пятково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000748' as code, 'д Рассолово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000750' as code, 'д Ратчино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000753' as code, 'д Рогачево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000755' as code, 'д Рогачёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000758' as code, 'д Романцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000760' as code, 'д Рыльково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000763' as code, 'д Рябинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000765' as code, 'д Сады' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000768' as code, 'д Сальницы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000770' as code, 'д Самынино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000773' as code, 'д Свинцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000775' as code, 'д Сельцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000778' as code, 'д Семейники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000780' as code, 'д Семёновское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000783' as code, 'с Семёновское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000785' as code, 'д Сергово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000788' as code, 'д Сивково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000790' as code, 'д Синичино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000793' as code, 'д Слащёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000795' as code, 'д Собольки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000798' as code, 'с Сокольниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000800' as code, 'д Соловьёвка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000803' as code, 'п Спутник' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000805' as code, 'п станции Колочь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000808' as code, 'д Старая Тяга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000810' as code, 'д Старое Село' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000813' as code, 'д Старое Село' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000815' as code, 'д Стеблево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000818' as code, 'д Стреево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000820' as code, 'п Строитель' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000823' as code, 'д Судаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000825' as code, 'д Суконниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000828' as code, 'д Сытино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000830' as code, 'д Сычи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000833' as code, 'д Сычики' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000835' as code, 'д Татариново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000838' as code, 'д Твердики' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000840' as code, 'д Телятьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000843' as code, 'д Тетерино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000845' as code, 'д Тимошино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000848' as code, 'д Тиунцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000850' as code, 'д Тихоново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000853' as code, 'д Топорово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000855' as code, 'д Троица' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000858' as code, 'с Тропарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000860' as code, 'д Тушков Городок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000863' as code, 'д Ульяново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000865' as code, 'д Утицы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000868' as code, 'п учхоза Александрово""' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000870' as code, 'д Фалилеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000873' as code, 'д Фёдоровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000875' as code, 'д Фомино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000878' as code, 'д Фомкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000880' as code, 'д Ханево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000883' as code, 'д Хващёвка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000885' as code, 'д Холдеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000888' as code, 'д Холм' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000890' as code, 'д Холмец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000893' as code, 'д Холмово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000895' as code, 'д Хорошилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000898' as code, 'д Хотилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000900' as code, 'д Храброво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000903' as code, 'д Цветки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000905' as code, 'п Цветковский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000908' as code, 'д Цезарево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000910' as code, 'п центральной усадьбы совхоза Синичино""' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000913' as code, 'п центральной усадьбы совхоза Уваровский-2' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000915' as code, 'п Цуканово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000918' as code, 'д Цыплино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000920' as code, 'д Чебуново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000923' as code, 'д Ченцово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000925' as code, 'д Чернево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000928' as code, 'д Черняки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000930' as code, 'д Шаликово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000933' as code, 'п Шаликово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000935' as code, 'д Шапкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000938' as code, 'д Шваново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000940' as code, 'д Швечково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000943' as code, 'д Шебаршино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000945' as code, 'д Шевардино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000948' as code, 'д Шейново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000950' as code, 'д Шеломово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000953' as code, 'д Шеляково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000955' as code, 'д Шибинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000958' as code, 'д Шиколово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000960' as code, 'д Шимоново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000963' as code, 'д Ширино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000965' as code, 'д Ширякино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000968' as code, 'д Шишиморово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000970' as code, 'д Шохово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000973' as code, 'д Шохово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000975' as code, 'д Юдинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000978' as code, 'д Юрлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000980' as code, 'д Юрьево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000983' as code, 'д Юрятино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000985' as code, 'д Ягодино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000988' as code, 'д Язево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46745000990' as code, 'д Ямская' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000001' as code, 'г Луховицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000056' as code, 'рп Белоомут' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000106' as code, 'д Аксеново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000111' as code, 'с Алпатьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000116' as code, 'д Асошники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46747000121' as code, 'д Астапово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000121' as code, 'д Астапово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000126' as code, 'д Астапово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46747000126' as code, 'с Астапово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000131' as code, 'д Барсуки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000136' as code, 'д Беляево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000141' as code, 'д Берхино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000146' as code, 'д Буково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000151' as code, 'д Булгаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000156' as code, 'д Власьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000161' as code, 'д Волохово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000166' as code, 'д Врачово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000171' as code, 'п Врачово-Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000176' as code, 'д Выкопанка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000181' as code, 'с Гавриловское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000186' as code, 'п Газопроводск' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000191' as code, 'д Ганькино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000196' as code, 'п Гидроузел' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000201' as code, 'д Головачево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000206' as code, 'д Гольный Бугор' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000211' as code, 'д Гольцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000216' as code, 'с Горетово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000221' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46747000221' as code, 'с Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000226' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46747000226' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000231' as code, 'с Городна' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000236' as code, 'с Григорьевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000241' as code, 'д Двуглинково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000246' as code, 'с Дединово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000251' as code, 'с Долгомостьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000256' as code, 'д Жеребятники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000261' as code, 'д Зекзюлино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000266' as code, 'д Золотухино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000271' as code, 'д Ивачево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000276' as code, 'д Ивняги' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000281' as code, 'д Игнатьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000286' as code, 'д Ильясово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000291' as code, 'п Каданок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000296' as code, 'д Калянинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000301' as code, 'д Кареево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000306' as code, 'д Клементьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000311' as code, 'д Кончаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000316' as code, 'п Красная Пойма' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000321' as code, 'д Круглово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000326' as code, 'д Курово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46747000326' as code, 'с Курово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000331' as code, 'д Ларино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000336' as code, 'д Лесное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000341' as code, 'д Лисьи Норы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000346' as code, 'д Ловецкие Борки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000351' as code, 'с Ловцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000356' as code, 'д Лучканцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000361' as code, 'с Любичи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000366' as code, 'д Марьина Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000371' as code, 'д Матвеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000376' as code, 'с Матыра' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000381' as code, 'д Моховое' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000386' as code, 'д Мухино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000391' as code, 'с Нижне-Маслово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000396' as code, 'д Новокошелево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000401' as code, 'д Новокунаково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000406' as code, 'д Новоходыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000411' as code, 'д Носово-1' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000416' as code, 'д Носово-2' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000421' as code, 'д Озерицы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000426' as code, 'д Ольшаны' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000431' as code, 'п Орешково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000436' as code, 'п отделения совхоза Дединово""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000441' as code, 'д Павловское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000446' as code, 'д Перевицкий Торжок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000451' as code, 'д Плешки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000456' as code, 'с Подлесная Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000461' as code, 'д Подлипки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000466' as code, 'д Протасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000471' as code, 'д Псотино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000476' as code, 'д Руднево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000481' as code, 'д Сарыбьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000486' as code, 'п Сельхозтехника' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000491' as code, 'с Слемские Борки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000496' as code, 'п совхоза Астапово""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000501' as code, 'д Солчино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000506' as code, 'п станции Черная' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000511' as code, 'д Старовнуково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000516' as code, 'д Старокошелево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000521' as code, 'д Староходыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000526' as code, 'д Строилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000531' as code, 'д Торжнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000536' as code, 'с Троицкие Борки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000541' as code, 'д Тюнино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000546' as code, 'д Федоровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000551' as code, 'п Фруктовая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46747000556' as code, 'д Чуприково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000001' as code, 'г Люберцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000056' as code, 'дп Красково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000061' as code, 'рп Малаховка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000066' as code, 'рп Октябрьский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000071' as code, 'рп Томилино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000106' as code, 'п Балластный Карьер' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000111' as code, 'п Егорово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000116' as code, 'п Жилино-1' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000121' as code, 'п Жилино-2' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000126' as code, 'д Кирилловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000131' as code, 'д Лукьяновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000136' as code, 'д Марусино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000141' as code, 'д Машково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000146' as code, 'п Мирный' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000151' as code, 'д Мотяково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000156' as code, 'д Пехорка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000161' as code, 'д Сосновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000166' as code, 'д Токарево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000171' as code, 'д Торбеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000176' as code, 'д Хлыстово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000181' as code, 'д Часовня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46748000186' as code, 'п Чкалово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000' as code, 'Ликино-Дулёво' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000001' as code, 'г Ликино-Дулёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000006' as code, 'г Дрезна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000011' as code, 'г Куровское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000106' as code, 'д Абрамовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000111' as code, 'д Авсюнино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000116' as code, 'п Авсюнино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000121' as code, 'д Аксёново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000126' as code, 'д Алексеевская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000131' as code, 'д Анциферово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000136' as code, 'д Аринино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000141' as code, 'д Асташково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000146' as code, 'д Ащерино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000151' as code, 'д Барское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000156' as code, 'д Барышово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000161' as code, 'д Беззубово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000166' as code, 'д Бекетовская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000171' as code, 'д Белавино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000176' as code, 'д Беливо' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000181' as code, 'п Беливо' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000186' as code, 'с Богородское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000191' as code, 'д Большое Кишнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000196' as code, 'д Ботагово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000201' as code, 'д Бяльково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000206' as code, 'д Вантино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000211' as code, 'д Васютино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000216' as code, 'д Велино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000221' as code, 'д Верещагино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000226' as code, 'д Вершина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000231' as code, 'д Власово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000236' as code, 'д Внуково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000241' as code, 'д Высоково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000246' as code, 'д Глебово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000251' as code, 'д Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000256' as code, 'д Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000261' as code, 'д Горбачиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000266' as code, 'д Грибчиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000271' as code, 'д Гридино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000276' as code, 'д Губино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000281' as code, 'д Давыдово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000286' as code, 'д Давыдовская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000291' as code, 'д Деревнищи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000296' as code, 'д Дорофеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000301' as code, 'д Дорохово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000306' as code, 'д Дуброво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000311' as code, 'д Дылдино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000316' as code, 'д Елизарово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000321' as code, 'д Емельяново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000326' as code, 'д Заволенье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000331' as code, 'д Загряжская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000336' as code, 'д Заполицы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000341' as code, 'д Запонорье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000346' as code, 'д Запрудино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000351' as code, 'д Запутное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000356' as code, 'д Зворково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000361' as code, 'д Зевнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000366' as code, 'д Иванищево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000371' as code, 'д Иванцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000376' as code, 'д Игнатово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000381' as code, 'с Ильинский Погост' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000386' as code, 'д Ионово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000391' as code, 'д Кабаново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000396' as code, 'д Каменцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000401' as code, 'д Киняево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000406' as code, 'п кирпичного завода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000411' as code, 'д Коровино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000416' as code, 'д Коротково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000421' as code, 'д Костенёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000426' as code, 'д Костино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000431' as code, 'д Красное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000436' as code, 'с Красное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000441' as code, 'д Круглово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000446' as code, 'д Кудыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000451' as code, 'д Лашино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000456' as code, 'д Лопаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000461' as code, 'д Лыщиково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000466' as code, 'д Ляхово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000471' as code, 'д Максимовская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000476' as code, 'д Малиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000481' as code, 'д Малое Кишнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000486' as code, 'д Мальково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000491' as code, 'д Мануйлово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000496' as code, 'д Минино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000501' as code, 'д Мисцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000506' as code, 'п Мисцево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000511' as code, 'д Молоково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000516' as code, 'д Мосягино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000521' as code, 'д Ненилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000526' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000531' as code, 'д Новое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000536' as code, 'д Новое Титово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000541' as code, 'д Новониколаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000546' as code, 'д Острово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000551' as code, 'д Пашнево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000556' as code, 'д Петрушино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000561' as code, 'д Писчёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000566' as code, 'д Пичурино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000571' as code, 'д Поминово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000576' as code, 'д Понарино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000581' as code, 'д Равенская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000586' as code, 'д Радованье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000591' as code, 'д Рудино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000596' as code, 'д Рудне-Никитское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000601' as code, 'д Савинская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000606' as code, 'д Савостьяново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000611' as code, 'д Сальково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000616' as code, 'д Селиваниха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000621' as code, 'д Сенькино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000626' as code, 'д Слободище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000631' as code, 'д Смолёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000636' as code, 'д Смолёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000641' as code, 'д Соболево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000646' as code, 'д Софряково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000651' as code, 'д Старая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000656' as code, 'д Старово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000661' as code, 'д Старое Титово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000666' as code, 'д Старская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000671' as code, 'д Старый Покров' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000676' as code, 'д Стенино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000681' as code, 'д Степановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000686' as code, 'д Столбуново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000691' as code, 'д Тереньково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000696' as code, 'д Тимонино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000701' as code, 'д Устьяново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000706' as code, 'д Федотово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000711' as code, 'д Филиппово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000716' as code, 'п Фокино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000721' as code, 'д Халтурино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000726' as code, 'п Хвойный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000731' as code, 'с Хотеичи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000736' as code, 'д Цаплино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000741' as code, 'п Чистое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000746' as code, 'п Чистое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000751' as code, 'д Чичёво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000756' as code, 'д Чукаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000761' as code, 'п Шевлягино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000766' as code, 'д Щетиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000771' as code, 'д Юркино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000776' as code, 'д Юрятино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000781' as code, 'д Язвищи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000786' as code, 'д Яковлево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46749000791' as code, 'д Яковлевская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000001' as code, 'г Наро-Фоминск' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000006' as code, 'г Апрелевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000011' as code, 'г Верея' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000056' as code, 'рп Калининец' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000061' as code, 'рп Селятино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000105' as code, 'д Акишево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000109' as code, 'д Алабино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000113' as code, 'п Александровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000117' as code, 'д Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000121' as code, 'д Алексино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000125' as code, 'д Алферьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000129' as code, 'д Архангельское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000133' as code, 'с Атепцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000137' as code, 'д Афанасовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000141' as code, 'д Афанасьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000145' as code, 'д Афинеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000149' as code, 'д Ахматово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000153' as code, 'д Бавыкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000157' as code, 'п Базисный Питомник' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000161' as code, 'д Башкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000165' as code, 'д Бекасово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000169' as code, 'д Бельково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000173' as code, 'д Берюлёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000177' as code, 'д Благовещенье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000181' as code, 'д Блознево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000185' as code, 'д Большие Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000189' as code, 'д Большие Семенычи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000193' as code, 'д Бурцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000197' as code, 'д Варварино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000201' as code, 'д Василисино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000205' as code, 'д Васильево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000209' as code, 'д Васильчиново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000213' as code, 'д Васькино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000217' as code, 'д Верховье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000221' as code, 'д Веселево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000225' as code, 'д Волково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000229' as code, 'д Волчёнки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000233' as code, 'д Воскресенки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000237' as code, 'д Вышегород' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000241' as code, 'д Глаголево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000245' as code, 'д Глинки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000249' as code, 'д Годуново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000253' as code, 'д Головеньки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000257' as code, 'д Головково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000261' as code, 'д Горчухино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000265' as code, 'д Григорово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000269' as code, 'д Гуляй Гора' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000273' as code, 'д Деденево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000277' as code, 'д Детенково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000281' as code, 'п дома отдыха Верея""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000285' as code, 'п дома отдыха Отличник""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000289' as code, 'п дома отдыха Бекасово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000293' as code, 'п Дубки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000297' as code, 'д Дубровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000301' as code, 'д Дуброво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000305' as code, 'д Дудкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000309' as code, 'д Дятлово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000313' as code, 'д Елагино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000317' as code, 'д Ерюхино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000321' as code, 'д Ефаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000325' as code, 'д Жёдочи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000329' as code, 'д Женаткино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000333' as code, 'д Жихарево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000337' as code, 'д Загряжское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000341' as code, 'д Залучное' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000345' as code, 'д Зинаевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000349' as code, 'д Золотьково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000353' as code, 'д Зубово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000357' as code, 'д Ивановка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000361' as code, 'д Ивково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000365' as code, 'д Ильинское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000369' as code, 'д Иневка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000373' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000377' as code, 'с Каменское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000381' as code, 'д Каурцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000385' as code, 'д Клин' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000389' as code, 'д Клово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000393' as code, 'д Князевое' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000397' as code, 'д Кобяково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000401' as code, 'д Ковригино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000405' as code, 'д Колодези' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000409' as code, 'д Коровино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000413' as code, 'д Котово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000417' as code, 'п Красноармейское Лесничество' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000421' as code, 'д Крестьянка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000425' as code, 'д Кромино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000429' as code, 'д Крюково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000433' as code, 'д Крюково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000437' as code, 'д Кузьминское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000441' as code, 'д Купелицы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000445' as code, 'д Курапово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000449' as code, 'д Лапино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000453' as code, 'д Латышская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000457' as code, 'п леспромхоза' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000461' as code, 'д Лисинцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000465' as code, 'д Литвиново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000469' as code, 'д Лобаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000473' as code, 'д Лужки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000477' as code, 'д Лукьяново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000481' as code, 'д Любаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000485' as code, 'д Макаровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000489' as code, 'д Малые Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000493' as code, 'д Малые Семенычи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000497' as code, 'д Мальцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000501' as code, 'д Мартемьяново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000505' as code, 'д Маурино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000509' as code, 'д Мельниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000513' as code, 'д Мерчалово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000517' as code, 'д Митенино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000521' as code, 'д Митяево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000525' as code, 'д Мишуткино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000529' as code, 'д Могутово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000533' as code, 'д Монаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000537' as code, 'д Мякишево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000541' as code, 'д Набережная Слобода' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000545' as code, 'д Назарьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000549' as code, 'д Настасьино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000553' as code, 'д Нефедово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000557' as code, 'д Нечаево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000561' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000565' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000569' as code, 'п Новая Ольховка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000573' as code, 'д Новинское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000577' as code, 'д Новоалександровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000581' as code, 'д Новоборисовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000585' as code, 'д Новоглаголево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000589' as code, 'д Новозыбинка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000593' as code, 'д Новоникольское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000597' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000601' as code, 'д Новосумино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000605' as code, 'д Носово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000609' as code, 'д Обухово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000613' as code, 'д Орешково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000617' as code, 'д Паново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000621' as code, 'д Пафнутовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000625' as code, 'д Пашково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000629' as code, 'д Перемешаево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000633' as code, 'д Першино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000637' as code, 'д Петровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000641' as code, 'с Петровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000645' as code, 'п Пионерский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000649' as code, 'д Плаксино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000653' as code, 'д Плесенское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000657' as code, 'д Подольное' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000661' as code, 'д Пожитково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000665' as code, 'д Покровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000669' as code, 'х Покровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000673' as code, 'д Порядино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000677' as code, 'д Пушкарка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000681' as code, 'д Радчино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000685' as code, 'д Ревякино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000689' as code, 'д Редькино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000693' as code, 'д Рождествено' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000697' as code, 'д Рождество' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000701' as code, 'д Романово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000705' as code, 'д Роща' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000709' as code, 'д Рубцово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000713' as code, 'д Рыжково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000717' as code, 'д Савеловка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000721' as code, 'д Самород' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000725' as code, 'д Санники' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000729' as code, 'д Свитино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000733' as code, 'д Секирино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000737' as code, 'д Селятино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000741' as code, 'д Семенково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000745' as code, 'д Семидворье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000749' as code, 'д Серенское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000753' as code, 'д Симбухово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000757' as code, 'д Скугорово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000761' as code, 'д Слепушкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000765' as code, 'д Слизнево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000769' as code, 'д Смолино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000773' as code, 'д Собакино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000777' as code, 'п совхоза Архангельский""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000781' as code, 'д Сотниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000785' as code, 'д Софьино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000789' as code, 'д Спасс-Косицы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000793' as code, 'п станции Башкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000797' as code, 'д Ступино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000801' as code, 'д Субботино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000805' as code, 'д Субботино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000809' as code, 'д Сумино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000813' as code, 'д Сырьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000817' as code, 'д Тарасково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000821' as code, 'д Татищево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000825' as code, 'д Таширово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000829' as code, 'д Телешово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000833' as code, 'д Тёрновка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000837' as code, 'д Тимонино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000841' as code, 'д Тимофеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000845' as code, 'д Тишинка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000849' as code, 'д Турейка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000853' as code, 'д Тютчево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000857' as code, 'д Устье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000861' as code, 'д Федюнькино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000865' as code, 'д Хлопово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000869' as code, 'д Чеблоково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000873' as code, 'д Чешково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000877' as code, 'д Чичково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000881' as code, 'д Шапкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000885' as code, 'д Шубино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000889' as code, 'д Шустиково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000893' as code, 'д Щекутино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000897' as code, 'д Юматово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000901' as code, 'д Юшково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46750000905' as code, 'д Ястребово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000' as code, 'Богородский' as name, 1 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000001' as code, 'г Ногинск' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000006' as code, 'г Старая Купавна' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000011' as code, 'г Электроугли' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000056' as code, 'рп им Воровского' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000061' as code, 'рп Обухово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000106' as code, 'п 2-й Бисеровский участок' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000111' as code, 'д Аборино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000116' as code, 'д Авдотьино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000121' as code, 'д Аксено-Бутырки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000126' as code, 'д Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000131' as code, 'д Афанасово-1' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000136' as code, 'с Балобаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000141' as code, 'д Бездедово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000146' as code, 'д Белая' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000151' as code, 'д Березовый Мостик' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000156' as code, 'с Бисерово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000161' as code, 'с Богослово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000166' as code, 'д Боково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000171' as code, 'д Большое Буньково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000176' as code, 'д Борилово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000181' as code, 'д Боровково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000186' as code, 'д Булгаково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000191' as code, 'д Вишняково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000196' as code, 'с Воскресенское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000201' as code, 'д Гаврилово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000206' as code, 'п Горбуша' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000211' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000216' as code, 'д Громково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000221' as code, 'д Дядькино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000226' as code, 'д Ельня' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000231' as code, 'д Жилино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000236' as code, 'д Загорново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000241' as code, 'п Затишье' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000246' as code, 'п Зеленый' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000251' as code, 'д Зубцово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000256' as code, 'д Ивашево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000261' as code, 'д Исаково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000266' as code, 'д Кабаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000271' as code, 'д Калитино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000276' as code, 'д Каменки-Дранишниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000281' as code, 'д Карабаново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000286' as code, 'д Караваево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000291' as code, 'д Кашино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000296' as code, 'д Клюшниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000301' as code, 'д Колонтаево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000306' as code, 'п Колышкино Болото' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000311' as code, 'д Кролики' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000316' as code, 'с Кудиново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000321' as code, 'с Мамонтово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000326' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000331' as code, 'д Марьино-2' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000336' as code, 'д Марьино-3' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000341' as code, 'д Меленки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000346' as code, 'д Мишуково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000351' as code, 'д Молзино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000356' as code, 'д Новая Купавна' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000361' as code, 'д Ново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000366' as code, 'д Новое Подвязново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000371' as code, 'с Новосергиево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000376' as code, 'п Новостройка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000381' as code, 'д Новые Псарьки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000386' as code, 'д Оселок' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000391' as code, 'д Пашуково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000396' as code, 'д Пешково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000401' as code, 'д Починки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000406' as code, 'д Пятково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000411' as code, 'п радиоцентра-9' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000416' as code, 'п Рыбхоз' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000421' as code, 'д Следово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000426' as code, 'д Соколово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000431' as code, 'д Старые Псарьки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000436' as code, 'с Стромынь' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000441' as code, 'д Стулово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000446' as code, 'д Тимково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000451' as code, 'д Тимохово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000456' as code, 'п турбазы Боровое""' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000461' as code, 'д Черепково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000466' as code, 'д Черново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000471' as code, 'д Шульгино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000476' as code, 'д Щекавцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000481' as code, 'д Щемилово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46751000486' as code, 'с Ямкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000106' as code, 'п Барская Гора' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000111' as code, 'д Большая Дубна' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000116' as code, 'д Будьково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000121' as code, 'п Верея' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000126' as code, 'д Войново-Гора' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000131' as code, 'д Демихово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000136' as code, 'п Дорогали Вторые' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000141' as code, 'д Дровосеки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000146' as code, 'п Исаакиевское Озеро' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000151' as code, 'п Кабановская Гора' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000156' as code, 'д Красная Дубрава' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000161' as code, 'м Крольчатник' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000166' as code, 'д Малая Дубна' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000171' as code, 'п Малиновские Луга' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000176' as code, 'д Нажицы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000181' as code, 'д Нестерово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000186' as code, 'д Никулино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000191' as code, 'д Ожерелки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000196' as code, 'п Озерецкий' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000201' as code, 'п Орловка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000206' as code, 'д Плотава' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000211' as code, 'д Поточино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000216' as code, 'п Пригородный' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000221' as code, 'п Приозерье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000226' as code, 'п Прокудино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000231' as code, 'д Сермино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000236' as code, 'п Снопок Новый' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000241' as code, 'п Снопок Старый' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000246' as code, 'п станции Поточино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000251' as code, 'д Тепёрки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000256' as code, 'п Тополиный' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000261' as code, 'д Трусово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000266' as code, 'д Федорово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000271' as code, 'д Щербинино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000276' as code, 'п Щучье Озеро' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '46757000281' as code, 'п 1-го Мая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000001' as code, 'г Павловский Посад' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000056' as code, 'рп Большие Дворы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000106' as code, 'д Аверкиево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000111' as code, 'п Аверкиевского лесничества' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000116' as code, 'д Алексеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000121' as code, 'д Алферово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000126' as code, 'д Андреево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000131' as code, 'д Борисово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000136' as code, 'д Бразуново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000141' as code, 'д Бывалино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000146' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000151' as code, 'д Васютино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000156' as code, 'д Власово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000161' as code, 'д Гаврино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000166' as code, 'д Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000171' as code, 'д Грибанино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000176' as code, 'д Грибаново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000181' as code, 'д Дальняя' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000186' as code, 'д Данилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000191' as code, 'д Демидово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000196' as code, 'д Дергаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000201' as code, 'д Дмитрово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000206' as code, 'д Евсеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000211' as code, 'д Ефимово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000216' as code, 'д Заозерье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000221' as code, 'д Игнатово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000226' as code, 'с Казанское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000231' as code, 'д Ковригино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000236' as code, 'д Козлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000241' as code, 'д Криулино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000246' as code, 'д Крупино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000251' as code, 'д Кузнецы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000256' as code, 'д Курово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000261' as code, 'д Левкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000266' as code, 'д Логиново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000271' as code, 'д Малыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000276' as code, 'п Мехлесхоза' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000281' as code, 'д Митино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000286' as code, 'д Михалево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000291' as code, 'д Назарьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000296' as code, 'д Ново-Загарье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000301' as code, 'д Носырево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000306' as code, 'д Перхурово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000311' as code, 'д Пестово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000316' as code, 'с Рахманово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000321' as code, 'д Саурово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000326' as code, 'д Семеново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000331' as code, 'д Сонино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000336' as code, 'д Стремянниково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000341' as code, 'д Субботино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000346' as code, 'д Сумино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000351' as code, 'д Тарасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000356' as code, 'д Теренино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000361' as code, 'д Улитино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000366' as code, 'д Фатеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000371' as code, 'д Фомино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000376' as code, 'д Часовня' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000381' as code, 'д Чисто-Перхурово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000386' as code, 'д Шебаново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46759000391' as code, 'д Щекутово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000001' as code, 'г Руза' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000056' as code, 'рп Тучково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000103' as code, 'д Акатово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000106' as code, 'д Акулово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000109' as code, 'д Алексино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000112' as code, 'д Алешино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000115' as code, 'д Алтыново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000118' as code, 'д Андрейково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000121' as code, 'с Аннино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000124' as code, 'д Апальщино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000127' as code, 'д Апухтино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000130' as code, 'д Артюхино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000133' as code, 'с Архангельское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000136' as code, 'п Бабаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000139' as code, 'д Бабино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000142' as code, 'д Бараново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000145' as code, 'д Барынино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000148' as code, 'д Белобородово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000151' as code, 'д Бельково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000154' as code, 'п Беляная Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000157' as code, 'д Бережки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000160' as code, 'д Березкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000163' as code, 'с Богородское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000166' as code, 'д Большие Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000169' as code, 'д Борзецово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000172' as code, 'п Бородёнки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000175' as code, 'д Ботино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000178' as code, 'п Брикет' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000181' as code, 'д Брыньково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000184' as code, 'д Буланино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000187' as code, 'д Булыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000190' as code, 'д Бунино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000193' as code, 'д Вандово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000196' as code, 'д Вараксино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000199' as code, 'д Варвариха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000202' as code, 'д Васильевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000202' as code, 'с Васильевское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000205' as code, 'д Васильевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000205' as code, 'д Васильевское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000208' as code, 'д Ватулино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000211' as code, 'д Ваюхино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000214' as code, 'д Ведерники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000217' as code, 'д Вертошино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000220' as code, 'д Верхнее Сляднево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000223' as code, 'д Вишенки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000226' as code, 'д Волково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000229' as code, 'д Волынщино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000232' as code, 'д Воробьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000235' as code, 'д Воскресенское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000238' as code, 'д Вражеское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000241' as code, 'д Высоково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000244' as code, 'п Гидроузел' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000247' as code, 'д Глиньково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000250' as code, 'д Глухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000253' as code, 'д Головинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000256' as code, 'д Гомнино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000259' as code, 'д Горбово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000262' as code, 'п Горбово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000265' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000268' as code, 'д Городилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000271' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000271' as code, 'с Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000274' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000274' as code, 'п Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000277' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000277' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000280' as code, 'д Грибцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000283' as code, 'д Григорово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000286' as code, 'д Грязново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000289' as code, 'д Деменково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000292' as code, 'д Демидково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000295' as code, 'д Денисиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000298' as code, 'п детского городка Дружба""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000301' as code, 'п дома отдыха Лужки""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000304' as code, 'п дома отдыха Тучково" ВЦСПС"' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000307' as code, 'п Дорохово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000310' as code, 'д Дробылево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000313' as code, 'д Ельники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000316' as code, 'д Ерденьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000319' as code, 'д Еськино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000322' as code, 'д Жиганово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000325' as code, 'д Жолобово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000328' as code, 'д Журавлево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000331' as code, 'д Заовражье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000334' as code, 'д Захнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000337' as code, 'д Землино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000340' as code, 'д Златоустово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000343' as code, 'д Иваново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000346' as code, 'д Ивойлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000349' as code, 'д Игнатьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000352' as code, 'д Ильинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000355' as code, 'д Ильятино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000358' as code, 'д Кожино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000358' as code, 'с Кожино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000361' as code, 'д Кожино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000361' as code, 'д Кожино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000364' as code, 'п Кожино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000367' as code, 'д Козлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000370' as code, 'д Коковино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000373' as code, 'д Кокшино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000376' as code, 'д Колодкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000379' as code, 'п Колюбакино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000382' as code, 'д Комлево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000385' as code, 'д Константиново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000388' as code, 'д Контемирово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000391' as code, 'д Копцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000394' as code, 'д Корчманово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000397' as code, 'п Космодемьянский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000400' as code, 'д Костино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000403' as code, 'д Красотино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000406' as code, 'д Кривошеино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000409' as code, 'д Крюково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000412' as code, 'д Кузянино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000415' as code, 'д Курово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000418' as code, 'д Ладыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000421' as code, 'д Лашино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000424' as code, 'д Ленинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000427' as code, 'д Леньково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000430' as code, 'д Лидино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000433' as code, 'д Лихачево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000436' as code, 'д Лобково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000439' as code, 'д Лукино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000442' as code, 'д Лунинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000445' as code, 'д Лызлово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000448' as code, 'д Лысково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000451' as code, 'д Лыщиково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000454' as code, 'д Макеиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000457' as code, 'д Малоиванцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000460' as code, 'д Малые Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000463' as code, 'д Мамошино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000466' as code, 'д Марково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000469' as code, 'д Марс' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000472' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000475' as code, 'д Матвейцево-I' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000478' as code, 'д Матвейцево-II' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000481' as code, 'д Митинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000484' as code, 'д Михайловское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000487' as code, 'д Мишинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000490' as code, 'д Молодиково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000493' as code, 'д Морево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000496' as code, 'д Мытники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000499' as code, 'д Накипелово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000502' as code, 'д Неверово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000505' as code, 'д Немирово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000508' as code, 'д Нестерово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000511' as code, 'д Нижнее Сляднево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000514' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000514' as code, 'с Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000517' as code, 'с Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000517' as code, 'с Никольское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000520' as code, 'д Никулкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000523' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000523' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000526' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000526' as code, 'с Новая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000529' as code, 'д Нововолково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000532' as code, 'д Новогорбово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000535' as code, 'д Новоивановское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000538' as code, 'д Новокурово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000541' as code, 'д Новомихайловское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000544' as code, 'д Новониколаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000547' as code, 'д Новониколаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000550' as code, 'д Новоникольское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000553' as code, 'д Новорождествено' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000556' as code, 'п Новотеряево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000559' as code, 'д Овсяники' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000562' as code, 'д Ожигово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000565' as code, 'д Орешки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000568' as code, 'д Оселье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000571' as code, 'д Палашкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000574' as code, 'д Паново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000577' as code, 'п пансионата Полушкино""' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000580' as code, 'д Пахомьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000583' as code, 'д Петрищево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000586' as code, 'д Петропавловское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000589' as code, 'д Петряиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000592' as code, 'д Писарево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000595' as code, 'д Подолы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000598' as code, 'д Покров' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000601' as code, 'с Покровское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000604' as code, 'д Полуэктово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000607' as code, 'д Помогаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000610' as code, 'д Поречье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000613' as code, 'д Потапово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000616' as code, 'д Притыкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000619' as code, 'д Пупки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000622' as code, 'д Ракитино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000625' as code, 'д Редькино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000628' as code, 'д Ремяница' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000631' as code, 'с Рождествено' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000634' as code, 'д Румянцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000637' as code, 'д Рупасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000640' as code, 'д Рыбушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000643' as code, 'д Рябцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000646' as code, 'д Самошкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000649' as code, 'д Сафониха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000652' as code, 'д Семенково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000655' as code, 'д Скирманово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000658' as code, 'д Слобода' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000661' as code, 'д Сонино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000664' as code, 'д Сорочнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000667' as code, 'д Старая Руза' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000670' as code, 'п Старая Руза' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000673' as code, 'д Старо' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000673' as code, 'д Старо' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000676' as code, 'д Старо' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000676' as code, 'с Старо' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000679' as code, 'д Старо' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '46766000679' as code, 'п Старо' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000682' as code, 'д Старониколаево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000685' as code, 'д Староникольское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000688' as code, 'п Старотеряево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000691' as code, 'д Строганка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000694' as code, 'д Стрыгино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000697' as code, 'д Сумароково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000700' as code, 'д Сухарево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000703' as code, 'д Сытьково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000706' as code, 'д Таблово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000709' as code, 'д Таганово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000712' as code, 'д Тимофеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000715' as code, 'д Тимохино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000718' as code, 'д Тишино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000721' as code, 'д Товарково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000724' as code, 'д Трубицино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000727' as code, 'д Углынь' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000730' as code, 'д Усадково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000733' as code, 'д Успенское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000736' as code, 'д Устье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000739' as code, 'д Федотово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000742' as code, 'д Федчино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000745' as code, 'д Федьково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000748' as code, 'д Филатово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000751' as code, 'д Фролково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000754' as code, 'д Хомьяново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000757' as code, 'д Хотебцово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000760' as code, 'д Хрущево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000763' as code, 'д Цыганово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000766' as code, 'д Чепасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000769' as code, 'д Шелковка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000772' as code, 'д Шилово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000775' as code, 'д Шорново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000778' as code, 'д Щелканово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000781' as code, 'д Щербинки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '46766000784' as code, 'д Ястребово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000001' as code, 'г Ступино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000056' as code, 'рп Жилёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000061' as code, 'рп Малино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000066' as code, 'рп Михнево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000104' as code, 'д Авдотьино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000107' as code, 'с Авдотьино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000110' as code, 'с Авдулово-1' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000113' as code, 'д Авдулово-2' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000116' as code, 'д Агарино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000119' as code, 'д Акатово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000122' as code, 'д Аксинькино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000125' as code, 'с Аксиньино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000128' as code, 'д Алеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000131' as code, 'д Алеево-2' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000134' as code, 'д Алешково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000137' as code, 'д Алфимово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000140' as code, 'д Ананьино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000143' as code, 'д Антипино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000146' as code, 'д Бабеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000149' as code, 'д Байдиково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000152' as code, 'с Батайки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000155' as code, 'д Бекетово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000158' as code, 'д Белыхино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000161' as code, 'с Березнецово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000164' as code, 'д Березня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000167' as code, 'д Беспятово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000170' as code, 'д Бессоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000173' as code, 'д Благовское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000176' as code, 'д Боброво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000179' as code, 'с Большое Алексеевское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000182' as code, 'д Большое Лупаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000185' as code, 'с Большое Скрябино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000188' as code, 'с Бортниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000191' as code, 'д Буньково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000194' as code, 'д Бурцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000197' as code, 'д Вальцово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000200' as code, 'д Василево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000203' as code, 'с Васильевское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000206' as code, 'д Васьково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000209' as code, 'п Вельяминово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000212' as code, 'с Верзилово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000215' as code, 'с Верховлянь' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000218' as code, 'с Вихорна' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000221' as code, 'д Владимирово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000224' as code, 'д Возрождение' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000227' as code, 'д Возцы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000230' as code, 'д Волково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000233' as code, 'с Воскресенки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000236' as code, 'д Гладково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000239' as code, 'д Глебово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000242' as code, 'д Головлино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000245' as code, 'с Голочелово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000248' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000251' as code, 'д Горностаево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000254' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000257' as code, 'с Городня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000260' as code, 'д Госконюшня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000263' as code, 'д Гридьково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000266' as code, 'д Гридюкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000269' as code, 'д Грызлово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000272' as code, 'д Дворяниново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000275' as code, 'д Девяткино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000278' as code, 'д Дорки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000281' as code, 'д Дорожники' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000284' as code, 'д Дубечино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000287' as code, 'д Дубнево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000290' as code, 'с Еганово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000293' as code, 'д Жилёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000296' as code, 'д Забелино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000299' as code, 'д Заворыкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000302' as code, 'д Залуги' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000305' as code, 'с Занкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000308' as code, 'д Захарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000311' as code, 'д Зевалово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000314' as code, 'д Зыбино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000317' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000320' as code, 'с Ивановское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000323' as code, 'д Ивантеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000326' as code, 'с Иван-Теремец' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000329' as code, 'д Игнатьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000332' as code, 'д Кабужское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000335' as code, 'д Каверино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000338' as code, 'д Калянино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000341' as code, 'д Каменищи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000344' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000347' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000350' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000353' as code, 'д Канищево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000356' as code, 'д Кануново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000359' as code, 'д Карпово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000362' as code, 'д Кишкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000365' as code, 'с Киясово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000368' as code, 'д Колдино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000371' as code, 'д Коледино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000374' as code, 'д Колычево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000377' as code, 'д Колычево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000380' as code, 'д Колюпаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000383' as code, 'с Кондрево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000386' as code, 'с Константиновское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000389' as code, 'с Короськово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000392' as code, 'д Костомарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000395' as code, 'д Кочкорево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000398' as code, 'д Кошелевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000401' as code, 'д Кошелевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000404' as code, 'д Кравцово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000407' as code, 'д Крапивня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000410' as code, 'д Красный Котельщик' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000413' as code, 'с Кременье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000416' as code, 'д Кубасово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000419' as code, 'с Кузьмино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000422' as code, 'д Кунавино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000425' as code, 'с Куртино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000428' as code, 'д Лаврентьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000431' as code, 'д Ламоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000434' as code, 'д Лапино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000437' as code, 'д Лаптево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000440' as code, 'д Лаптево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000443' as code, 'д Леньково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000446' as code, 'д Леонтьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000449' as code, 'с Липитино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000452' as code, 'д Лобынино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000455' as code, 'д Ловцово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000458' as code, 'с Лужники' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000461' as code, 'д Любановка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000464' as code, 'д Ляхово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000467' as code, 'д Макеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000470' as code, 'с Малое Алексеевское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000473' as code, 'д Малое Ивановское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000476' as code, 'д Малое Лупаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000479' as code, 'д Малюшина Дача' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000482' as code, 'с Мартыновское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000485' as code, 'д Марьинка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000488' as code, 'с Марьинка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000491' as code, 'д Марьинское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000494' as code, 'д Матвейково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000497' as code, 'д Матюково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000500' as code, 'д Медведево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000503' as code, 'с Мещерино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000506' as code, 'д Милино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000509' as code, 'д Миняево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000512' as code, 'д Мурзино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000515' as code, 'с Мышенское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000518' as code, 'д Мякинино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000521' as code, 'д Мясищево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000524' as code, 'д Мясное' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000527' as code, 'д Назарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000530' as code, 'с Нефедьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000533' as code, 'д Нивки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000536' as code, 'д Николо-Тители' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000539' as code, 'д Никольская Дача' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000542' as code, 'п Новоеганово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000545' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000548' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000551' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000554' as code, 'д Оглоблино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000557' as code, 'п Октябрьский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000560' as code, 'д Ольгино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000563' as code, 'д Ольховка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000566' as code, 'д Ольхово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000569' as code, 'д Орехово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000572' as code, 'д Орешково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000575' as code, 'д Останково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000578' as code, 'д Пасыкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000581' as code, 'д Песочня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000584' as code, 'д Пестриково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000587' as code, 'д Петрищево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000590' as code, 'д Петрово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000593' as code, 'с Покровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000596' as code, 'д Полупирогово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000599' as code, 'д Полушкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000602' as code, 'д Починки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000605' as code, 'д Починки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000608' as code, 'д Привалово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000611' as code, 'д Проскурниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000614' as code, 'д Протасово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000617' as code, 'д Прудно' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000620' as code, 'д Псарево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000623' as code, 'д Радужная' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000626' as code, 'с Разиньково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000629' as code, 'д Родоманово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000632' as code, 'д Рудины' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000635' as code, 'д Савельево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000638' as code, 'д Савино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000641' as code, 'д Сайгатово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000644' as code, 'с Сапроново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000647' as code, 'д Сафроново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000650' as code, 'д Секирино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000653' as code, 'с Семеновское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000656' as code, 'д Сенькино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000659' as code, 'д Сидорово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000662' as code, 'с Ситне-Щелканово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000665' as code, 'д Соколова Пустынь' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000668' as code, 'д Сотниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000671' as code, 'д Сотниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000674' as code, 'с Спасское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000677' as code, 'с Старая Кашира' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000680' as code, 'с Старая Ситня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000683' as code, 'д Старое' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000686' as code, 'с Старое' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000689' as code, 'д Старокурово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000692' as code, 'с Суково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000695' as code, 'д Сумароково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000698' as code, 'д Съяново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000701' as code, 'с Татариново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000704' as code, 'д Теняково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000707' as code, 'д Тишково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000710' as code, 'д Толбино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000713' as code, 'д Толочаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000716' as code, 'д Торбеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000719' as code, 'с Троице-Лобаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000722' as code, 'д Тростники' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000725' as code, 'д Тутыхино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000728' as code, 'д Тютьково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000731' as code, 'д Уварово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000734' as code, 'п Усады' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000737' as code, 'д Утенково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000740' as code, 'с Федоровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000743' as code, 'д Фоминка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000746' as code, 'д Фомино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000749' as code, 'с Хатунь' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000752' as code, 'д Хирино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000755' as code, 'д Хомутово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000758' as code, 'с Хонятино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000761' as code, 'д Хочёма' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000764' as code, 'с Чернышово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000767' as code, 'д Четряково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000770' as code, 'д Чиркино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000773' as code, 'д Чирково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000776' as code, 'д Шелково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000779' as code, 'д Шманаево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000782' as code, 'д Шматово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000785' as code, 'д Шугарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000788' as code, 'с Шугарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000791' as code, 'д Щапово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000794' as code, 'с Щапово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000797' as code, 'д Щербинино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000800' as code, 'д Ярцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46776000803' as code, 'д 2-я Пятилетка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000' as code, 'Талдомский' as name, 1 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000001' as code, 'г Талдом' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000056' as code, 'рп Вербилки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000061' as code, 'рп Запрудня' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000066' as code, 'рп Северный' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000106' as code, 'д Айбутово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000111' as code, 'д Аймусово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000116' as code, 'д Акишево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000121' as code, 'д Андрейково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000126' as code, 'д Арефьево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000131' as code, 'д Ахтимнеево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000136' as code, 'д Бабахино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000141' as code, 'д Бакшеиха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000146' as code, 'д Бардуково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000151' as code, 'д Батулино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000156' as code, 'д Бельское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000161' as code, 'д Береговское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000166' as code, 'д Бережок' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000171' as code, 'д Бобровниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000176' as code, 'д Бобылино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000181' as code, 'д Большое Семёновское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000186' as code, 'д Большое Курапово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000191' as code, 'д Большое Страшево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000196' as code, 'д Бородино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000201' as code, 'д Буртаки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000206' as code, 'д Бурцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000211' as code, 'д Бучево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000216' as code, 'д Васино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000221' as code, 'с Великий Двор' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000226' as code, 'д Веретьево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000231' as code, 'д Волдынь' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000236' as code, 'д Волково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000241' as code, 'д Волково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000246' as code, 'д Волкуша' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000251' as code, 'д Воргаш' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000256' as code, 'д Вороново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000261' as code, 'д Вотря' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000266' as code, 'д Высочки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000271' as code, 'д Высочки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000276' as code, 'д Глебово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000281' as code, 'д Глинки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000286' as code, 'д Головачево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000291' as code, 'д Головково-Марьино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000296' as code, 'д Григорово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000301' as code, 'д Гришково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000306' as code, 'д Гусёнки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000311' as code, 'д Гусёнки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000316' as code, 'д Гуслево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000321' as code, 'д Дмитровка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000326' as code, 'д Доброволец' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000331' as code, 'д Домославка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000336' as code, 'д Дубки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000341' as code, 'д Дубровки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000346' as code, 'д Дьяконово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000351' as code, 'д Ельцыново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000356' as code, 'д Ермолино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000361' as code, 'д Есаулово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000366' as code, 'д Желдыбино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000371' as code, 'д Жеребцово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000376' as code, 'д Жизнеево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000381' as code, 'д Жуково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000386' as code, 'д Затула' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000391' as code, 'д Зятьково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000396' as code, 'д Иванцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000401' as code, 'д Игумново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000406' as code, 'д Измайлово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000411' as code, 'д Калинкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000416' as code, 'д Карачуново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000421' as code, 'д Карманово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000426' as code, 'с Квашёнки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000431' as code, 'д Кишкиниха' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000436' as code, 'д Климово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000441' as code, 'д Князчино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000446' as code, 'д Колбасино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000451' as code, 'д Коришево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000456' as code, 'д Костенево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000461' as code, 'д Костино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000466' as code, 'д Костолыгино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000471' as code, 'д Кошелёво' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000476' as code, 'д Кривец' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000481' as code, 'д Крияново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000486' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000491' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000496' as code, 'д Куймино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000501' as code, 'д Кунилово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000506' as code, 'д Курилово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000511' as code, 'д Кутачи' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000516' as code, 'д Кушки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000521' as code, 'д Лебзино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000526' as code, 'д Леоново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000531' as code, 'д Лесоучастка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000536' as code, 'д Лозынино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000541' as code, 'д Льгово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000546' as code, 'д Людятино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000551' as code, 'д Лютиково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000556' as code, 'д Маклаково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000561' as code, 'д Маклыгино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000566' as code, 'д Малиновец' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000571' as code, 'д Малое Курапово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000576' as code, 'д Малое Страшево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000581' as code, 'д Манихино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000586' as code, 'д Мельдино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000591' as code, 'д Мякишево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000596' as code, 'д Наговицино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000601' as code, 'д Некрасово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000606' as code, 'д Никитино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000611' as code, 'д Никитское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000616' as code, 'с Николо-Кропотки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000621' as code, 'д Никулки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000626' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000631' as code, 'д Новая Хотча' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000636' as code, 'с Новогуслево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000641' as code, 'с Новоникольское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000646' as code, 'д Новотроица' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000651' as code, 'д Нушполы' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000656' as code, 'д Овсянниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000661' as code, 'д Ожигово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000666' as code, 'д Озерское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000671' as code, 'д Ольховик' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000676' as code, 'д Остров' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000681' as code, 'д Павловичи' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000686' as code, 'д Павловское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000691' as code, 'д Пановка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000696' as code, 'д Парашино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000701' as code, 'д Пашино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000706' as code, 'д Пенкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000711' as code, 'д Пенское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000716' as code, 'д Петрино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000721' as code, 'д Платунино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000726' as code, 'д Полудёновка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000731' as code, 'д Полутьево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000736' as code, 'д Попадьино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000741' as code, 'д Приветино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000746' as code, 'д Пригары' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000751' as code, 'д Припущаево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000756' as code, 'д Прусово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000761' as code, 'д Разорёно-Семёновское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000766' as code, 'д Рассадники' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000771' as code, 'д Растовцы' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000776' as code, 'д Рождество-Вьюлки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000781' as code, 'д Самково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000786' as code, 'д Семёновское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000791' as code, 'д Семягино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000796' as code, 'д Сенино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000801' as code, 'д Серебренниково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000806' as code, 'д Сляднево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000811' as code, 'д Смёнки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000816' as code, 'д Сорокино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000821' as code, 'д Сосково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000826' as code, 'д Сотское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000831' as code, 'с Спас-Угол' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000836' as code, 'д Станки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000841' as code, 'д Старая Хотча' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000846' as code, 'д Стариково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000851' as code, 'д Стариково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000856' as code, 'д Старково' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000861' as code, 'д Сущёво' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000866' as code, 'д Танино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000871' as code, 'д Тарусово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000876' as code, 'с Темпы' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000881' as code, 'д Терехово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000886' as code, 'д Троица-Вязники' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000891' as code, 'д Ульянцево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000896' as code, 'д Устье-Стрелка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000901' as code, 'д Утенино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000906' as code, 'д Фёдоровское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000911' as code, 'д Федотово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000916' as code, 'д Филиппово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000921' as code, 'д Фоминское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000926' as code, 'д Храброво' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000931' as code, 'д Чупаево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000936' as code, 'д Шабушево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000941' as code, 'д Шадрино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000946' as code, 'д Шатеево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000951' as code, 'д Ширятино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000956' as code, 'д Юдино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000961' as code, 'д Юрино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000966' as code, 'д Юркино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '46778000971' as code, 'д Ябдино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000001' as code, 'г Чехов' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000056' as code, 'рп Столбовая' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000106' as code, 'д Аксенчиково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000111' as code, 'д Алачково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000116' as code, 'д Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000121' as code, 'д Алфёрово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000126' as code, 'п Алфёрово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000131' as code, 'д Антропово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000136' as code, 'д Бавыкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000141' as code, 'д Баранцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000146' as code, 'д Бегичево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000151' as code, 'д Беляево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000156' as code, 'д Березенки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000164' as code, 'п Берёзки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000166' as code, 'д Бершово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000171' as code, 'д Богдановка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000176' as code, 'д Большое Петровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000181' as code, 'д Ботвинино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000186' as code, 'д Булгаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000191' as code, 'д Булычёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000196' as code, 'д Бутырки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000201' as code, 'д Васино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000206' as code, 'д Васькино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000211' as code, 'п Васькино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000216' as code, 'д Ваулово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000221' as code, 'д Венюково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000226' as code, 'д Верхнее Пикалово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000231' as code, 'д Волосово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000236' as code, 'д Высоково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000241' as code, 'д Гавриково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000246' as code, 'д Глуховка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000251' as code, 'д Голыгино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000256' as code, 'д Горелово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000261' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000266' as code, 'д Гришенки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000271' as code, 'д Гришино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000276' as code, 'д Детково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000281' as code, 'д Дидяково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000286' as code, 'д Дмитровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000291' as code, 'п дома отдыха Лопасня""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000296' as code, 'д Дубинино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000301' as code, 'с Дубна' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000306' as code, 'д Дубровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000311' as code, 'д Дулово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000316' as code, 'д Ермолово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000321' as code, 'д Еськино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000326' as code, 'д Ефимовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000331' as code, 'д Жальское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000336' as code, 'д Завалипьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000341' as code, 'д Захарково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000346' as code, 'д Змеёвка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000351' as code, 'д Зыкеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000356' as code, 'с Ивановское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000361' as code, 'д Ивачково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000366' as code, 'д Ивино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000371' as code, 'д Игумново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000376' as code, 'д Ишино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000381' as code, 'д Капустино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000386' as code, 'д Каргашиново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000391' as code, 'д Кармашовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000396' as code, 'д Карьково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000401' as code, 'д Климовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000406' as code, 'д Коровино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000411' as code, 'д Костомарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000416' as code, 'д Красные Орлы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000421' as code, 'д Красные Холмы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000426' as code, 'д Крюково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000431' as code, 'д Кудаево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000436' as code, 'д Кузьмино-Фильчаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000441' as code, 'д Кулаково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000446' as code, 'д Курниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000451' as code, 'д Легчищево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000456' as code, 'д Леониха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000461' as code, 'д Леоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000466' as code, 'д Лешино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000471' as code, 'д Лопино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000476' as code, 'п Луч' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000481' as code, 'д Любучаны' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000486' as code, 'п Любучаны' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000491' as code, 'д Люторецкое' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000496' as code, 'д Малое Петровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000501' as code, 'д Мальцы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000506' as code, 'д Манушкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000511' as code, 'д Масловка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000516' as code, 'д Масново-Жуково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000521' as code, 'с Мелихово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000526' as code, 'д Мерлеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000531' as code, 'д Мещерское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000536' as code, 'п Мещерское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000541' as code, 'с Молоди' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000546' as code, 'д Муковнино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000551' as code, 'д Нащёкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000556' as code, 'д Нижнее Пикалово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000561' as code, 'д Никоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000566' as code, 'д Новгородово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000571' as code, 'с Новоселки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000576' as code, 'с Новый Быт' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000581' as code, 'д Оксино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000586' as code, 'д Панино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000591' as code, 'д Перхурово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000596' as code, 'д Першино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000601' as code, 'п Песоченка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000606' as code, 'д Петропавловка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000611' as code, 'д Пешково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000616' as code, 'д Пешково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000621' as code, 'д Плешкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000626' as code, 'д Плужково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000631' as code, 'д Покров' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000636' as code, 'д Поповка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000641' as code, 'д Попово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000646' as code, 'д Поспелиха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000651' as code, 'д Пронино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000656' as code, 'д Прохорово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000661' as code, 'д Прудки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000666' as code, 'д Радутино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000671' as code, 'д Растовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000676' as code, 'д Репниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000681' as code, 'д Сандарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000686' as code, 'д Сафоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000691' as code, 'д Сенино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000696' as code, 'д Сергеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000701' as code, 'д Сидориха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000706' as code, 'д Скурыгино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000711' as code, 'д Слепушкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000716' as code, 'д Солнышково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000721' as code, 'д Солодовка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000726' as code, 'д Сохинки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000731' as code, 'д Спас-Темня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000736' as code, 'п станции Детково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000741' as code, 'с Стремилово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000746' as code, 'с Талалихино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000751' as code, 'с Талеж' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000756' as code, 'д Томарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000761' as code, 'с Троицкое' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000766' as code, 'д Тюфанка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000771' as code, 'д Углешня' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000776' as code, 'д Филипповское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000781' as code, 'д Хлевино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000786' as code, 'д Ходаево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000791' as code, 'д Хоросино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000796' as code, 'д Чепелёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000801' as code, 'д Чубарово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000806' as code, 'д Чудиново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000811' as code, 'д Шарапово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000816' as code, 'с Шарапово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46784000821' as code, 'д Якшино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000001' as code, 'г Шатура' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000056' as code, 'рп Мишеронский' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000061' as code, 'рп Черусти' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000105' as code, 'д Алексино-Туголес' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000109' as code, 'д Алёшино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000113' as code, 'д Ананкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000117' as code, 'д Ананьинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000121' as code, 'д Андреевские Выселки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000125' as code, 'д Антипино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000129' as code, 'д Артёмово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000133' as code, 'д Бабынино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000137' as code, 'п Бакшеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000141' as code, 'д Бармино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000145' as code, 'д Беловская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000149' as code, 'д Бордуки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000153' as code, 'д Бородино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000157' as code, 'д Бундово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000161' as code, 'д Вальковская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000165' as code, 'д Варюковка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000169' as code, 'д Васюковка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000173' as code, 'д Великодворье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000177' as code, 'с Власово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000181' as code, 'п Воймежный' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000185' as code, 'д Волово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000189' as code, 'д Волосунино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000193' as code, 'д Ворово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000197' as code, 'д Воронинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000201' as code, 'д Воропино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000205' as code, 'д Высоково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000209' as code, 'д Высокорёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000213' as code, 'д Вяхирево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000217' as code, 'д Гавриловская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000221' as code, 'д Гаврино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000225' as code, 'д Гармониха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000229' as code, 'п Глуховка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000233' as code, 'д Голыгино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000237' as code, 'д Гора' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000241' as code, 'д Горелово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000245' as code, 'д Горяновская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000249' as code, 'д Гришакино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000253' as code, 'д Губино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000257' as code, 'д Демино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000261' as code, 'д Денисьево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000265' as code, 'д Дерзсковская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000269' as code, 'д Дмитровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000273' as code, 'с Дмитровский Погост' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000277' as code, 'п Долгуша' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000281' as code, 'д Дорофеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000285' as code, 'д Дубасово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000289' as code, 'д Дубровка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000293' as code, 'д Дуреевская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000297' as code, 'д Евлево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000301' as code, 'д Емино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000305' as code, 'д Епихино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000309' as code, 'д Ершовская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000313' as code, 'д Ефремово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000317' as code, 'д Зименки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000321' as code, 'д Ивановская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000325' as code, 'д Ивановская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000329' as code, 'д Инюшинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000333' as code, 'д Казыкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000337' as code, 'д Катчиково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000341' as code, 'д Кашниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000345' as code, 'д Климовская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000349' as code, 'д Кобелёво' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000353' as code, 'д Коренец' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000357' as code, 'д Коробовская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000361' as code, 'п Красная Гора' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000365' as code, 'д Красная Горка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000369' as code, 'п Красные Луга' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000373' as code, 'с Кривандино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000377' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000381' as code, 'д Кузнецы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000385' as code, 'д Кузяевская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000389' as code, 'д Кулаковка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000393' as code, 'д Курьяниха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000397' as code, 'д Левинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000401' as code, 'д Левошево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000405' as code, 'д Лека' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000409' as code, 'д Лемёшино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000413' as code, 'п Лесозавода' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000417' as code, 'п Леспромхоз' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000421' as code, 'п Леспромхоза' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000425' as code, 'д Лешниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000429' as code, 'д Ловчиково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000433' as code, 'д Лузгарино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000437' as code, 'д Маврино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000441' as code, 'д Маланьинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000445' as code, 'д Малеиха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000449' as code, 'д Марковская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000453' as code, 'д Мелиховская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000457' as code, 'п Мещёрский Бор' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000461' as code, 'д Минино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000465' as code, 'д Митинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000469' as code, 'д Митинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000473' as code, 'д Митинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000477' as code, 'д Митрониха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000481' as code, 'д Михайловская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000485' as code, 'д Муравлёвская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000489' as code, 'д Надеино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000493' as code, 'д Наумовская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000497' as code, 'д Никитинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000501' as code, 'д Новосельцево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000505' as code, 'д Новосидориха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000509' as code, 'д Ново-Черкасово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000513' as code, 'д Новошино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000517' as code, 'д Обухово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000521' as code, 'п Осаново-Дубовое' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000525' as code, 'д Парфёновская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000529' as code, 'д Перхурово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000533' as code, 'д Першино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000537' as code, 'с Пески' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000541' as code, 'д Пестовская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000545' as code, 'с Петровское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000549' as code, 'д Петряиха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000553' as code, 'д Пиравино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000557' as code, 'д Погостище' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000561' as code, 'д Подлесная' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000565' as code, 'д Пожога' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000569' as code, 'д Поздняки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000573' as code, 'д Починки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000577' as code, 'д Пронино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000581' as code, 'д Пруды' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000585' as code, 'с Пустоша' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000589' as code, 'п Пустоши' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000593' as code, 'с Пышлицы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000597' as code, 'с Пятница' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000601' as code, 'п Радовицкий' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000605' as code, 'д Русановская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000609' as code, 'д Савинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000613' as code, 'п Саматиха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000617' as code, 'д Самойлиха' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000621' as code, 'п санатория Озеро Белое""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000625' as code, 'п Северная Грива' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000629' as code, 'д Селянино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000633' as code, 'д Семеновская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000637' as code, 'д Семёновская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000641' as code, 'д Семёновская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000645' as code, 'п Семёновский Завод' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000649' as code, 'с Середниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000653' as code, 'д Сидоровская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000657' as code, 'д Слобода' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000661' as code, 'п Соколья Грива' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000665' as code, 'д Спирино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000669' as code, 'п станции 32 км' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000673' as code, 'п станции Бармино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000677' as code, 'п станции Осаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000681' as code, 'п станции Пожога' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000685' as code, 'п станции Сазоново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000689' as code, 'д Старо-Черкасово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000693' as code, 'д Стенинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000697' as code, 'п Струя' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000701' as code, 'д Сычи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000705' as code, 'п Тархановка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000709' as code, 'д Тархановская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000713' as code, 'д Тельма' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000717' as code, 'д Терехово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000721' as code, 'с Туголес' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000725' as code, 'п Туголесский Бор' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000729' as code, 'д Тупицыно' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000733' as code, 'д Тюшино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000737' as code, 'д Федеевская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000741' as code, 'д Фединская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000745' as code, 'д Фёдоровская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000749' as code, 'д Филимакино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000753' as code, 'д Филинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000757' as code, 'д Филисово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000761' as code, 'д Филисово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000765' as code, 'п Фрол' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000769' as code, 'д Харинская' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000773' as code, 'д Харлампеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000777' as code, 'п центральной усадьбы совхоза Мир""' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000781' as code, 'д Чернятино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000785' as code, 'д Чисома' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000789' as code, 'с Шарапово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000793' as code, 'п Шатурторф' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000797' as code, 'д Шеино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000801' as code, 'д Шелогурово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000805' as code, 'д Широково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000809' as code, 'д Ширяево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000813' as code, 'д Шмели' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000817' as code, 'д Югино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000821' as code, 'д Якушевичи' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000825' as code, 'п 12 Посёлок' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000829' as code, 'п 18 Посёлок' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000833' as code, 'п 19 Посёлок' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46786000837' as code, 'п 21 Посёлок' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000106' as code, 'д Бабеево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000111' as code, 'д Всеволодово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000116' as code, 'п Елизаветино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000121' as code, 'д Есино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000126' as code, 'с Иванисово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000131' as code, 'п Новые Дома' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000136' as code, 'д Пушкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000141' as code, 'п Случайный' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000146' as code, 'д Степаново' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '46790000151' as code, 'п Фрязево' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '49606419166' as code, 'х Ручьи' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '49612419138' as code, 'д Ильина Гора' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '49616428424' as code, 'д Маклочиха' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405101' as code, 'п Тёсово-Нетыльский' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405106' as code, 'д Большое Замошье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405111' as code, 'д Вдицко' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405116' as code, 'д Глухая Кересть' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405121' as code, 'д Горенка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405126' as code, 'д Гузи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405131' as code, 'д Долгово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405136' as code, 'д Клепцы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405141' as code, 'д Малое Замошье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405146' as code, 'д Огорелье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405151' as code, 'д Осия' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405156' as code, 'д Поддубье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405161' as code, 'д Пятилипы' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405166' as code, 'д Радони' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405171' as code, 'д Раптица' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405176' as code, 'д Село-Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405181' as code, 'д Татино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405186' as code, 'д Финёв Луг' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405191' as code, 'д Чауни' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405196' as code, 'п Кересть' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '49625405201' as code, 'п Тёсовский' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '53622422121' as code, 'с Таналык' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '53622422126' as code, 'с Чапаевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '53622431126' as code, 'с Уртазым' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '53622431131' as code, 'с Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '53622431136' as code, 'с Берёзовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '53622431141' as code, 'с Сосновка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '54639425123' as code, 'д Курдяевка' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657402107' as code, 'с Верхний Мывал' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657402112' as code, 'с Нижний Мывал' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657419102' as code, 'с Альмяшевка' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657419111' as code, 'нп Дом Отдыха' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657419116' as code, 'с Нижняя Липовка' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657419121' as code, 'д Средняя Липовка' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657428112' as code, 'с Малая Садовка' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657428117' as code, 'д Ручим' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657428122' as code, 'с Сюзюмское' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657437102' as code, 'с Мордовский Качим' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '56657437107' as code, 'с Русский Качим' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410103' as code, 'д Аликино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410113' as code, 'д Дубренята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410118' as code, 'д Ерушниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410119' as code, 'д Ершовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410123' as code, 'д Заболотная' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410124' as code, 'д Иваньково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410147' as code, 'д Лазарята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410149' as code, 'д Макарово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410158' as code, 'д Нижний Кущер' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410159' as code, 'д Опалена' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410163' as code, 'д Петрята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410213' as code, 'с Козьмодемьянск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57622410214' as code, 'с Паздниково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428131' as code, 'с Бырма' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428136' as code, 'д Барановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428141' as code, 'д Березовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428146' as code, 'д Верх-Турка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428151' as code, 'д Каразельга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428156' as code, 'д Ключ' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428161' as code, 'д Красный Берег' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428166' as code, 'д Липовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428171' as code, 'д Пигасовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428176' as code, 'д Подлиповка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428181' as code, 'д Подъельничная' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428186' as code, 'д Савлек' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428191' as code, 'д Талачик' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428196' as code, 'п Татарская Шишмара' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630428201' as code, 'п Туркское Лесничество' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630443161' as code, 'д Теплая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630443166' as code, 'д Болотовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630443171' as code, 'д Колпашники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630443176' as code, 'д Мушкалово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630443181' as code, 'д Одина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630443186' as code, 'д Патраково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630443191' as code, 'с Тихановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57630443196' as code, 'с Троицк' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413112' as code, 'д Калино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413113' as code, 'д Кирпичи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413114' as code, 'п Красный Маяк' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413115' as code, 'с Кузнечиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413123' as code, 'д Пьянкова' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413131' as code, 'п Усть-Паль' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413136' as code, 'д Малая Паль' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413141' as code, 'д Новая Драчева' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413146' as code, 'с Паль' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57640413151' as code, 'д Пермякова' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57652402128' as code, 'д Ломь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57652402132' as code, 'д Курмакаш' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57652402134' as code, 'д Митрохи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57652402138' as code, 'п Первомайский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57652414102' as code, 'с Барсаи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57652414103' as code, 'с Воскресенское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57652414104' as code, 'д Грибаны' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57652414105' as code, 'д Губаны' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57652414121' as code, 'д Иштеряки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000006' as code, 'г Усолье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000106' as code, 'д Большое Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000111' as code, 'д Быстрая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000116' as code, 'д Быстринская база' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000121' as code, 'д Верхние Новинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000126' as code, 'д Васильева' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000131' as code, 'д Вересовая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000136' as code, 'д Высокова' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000141' as code, 'д Вяткино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000146' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000151' as code, 'д Гунина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000156' as code, 'д Загижга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000161' as code, 'д Заразилы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000166' as code, 'д Зыряна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000171' as code, 'д Игнашина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000176' as code, 'д Карандашева' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000181' as code, 'д Кедрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000186' as code, 'д Кекур' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000191' as code, 'д Кокуй' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000196' as code, 'д Комино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000201' as code, 'д Левино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000206' as code, 'д Лобаны' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000211' as code, 'д Лубянка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000216' as code, 'д Малютина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000221' as code, 'д Мостовая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000226' as code, 'д Мыслы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000231' as code, 'д Нижние Новинки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000236' as code, 'д Овиново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000241' as code, 'д Пишмино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000246' as code, 'д Плеханово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000251' as code, 'д Полом' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000256' as code, 'д Релка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000261' as code, 'д Селино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000266' as code, 'д Сгорки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000271' as code, 'д Сороковая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000276' as code, 'д Трезубы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000281' as code, 'д Шварева' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000286' as code, 'д Шварево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000291' as code, 'д Шишкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000296' as code, 'п Лемзер' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000301' as code, 'п Лысьва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000306' as code, 'п Расцветаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000311' as code, 'п Шемейный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000316' as code, 'с Березовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000321' as code, 'с Верх-Кондас' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000326' as code, 'с Ощепково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000331' as code, 'с Пыскор' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000336' as code, 'с Таман' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000341' as code, 'с Щекино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000346' as code, 'п Орел' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000351' as code, 'п Огурдино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000356' as code, 'д Турлавы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000361' as code, 'д Пешково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000366' as code, 'д Кондас' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000371' as code, 'д Петрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000376' as code, 'с Романово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000381' as code, 'д Белая Пашня' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000386' as code, 'п Вогулка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000391' as code, 'п Дзержинец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000396' as code, 'п Солнечный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000401' as code, 'д Володин Камень' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000406' as code, 'д Малое Романово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000411' as code, 'д Зуево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000416' as code, 'д Закаменная' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000421' as code, 'д Жуклино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000426' as code, 'д Разим' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000431' as code, 'д Сибирь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000436' as code, 'д Вогулка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000441' as code, 'п Железнодорожный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000446' as code, 'д Шиши' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000451' as code, 'с Троицк' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000456' as code, 'д Кокшарово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000461' as code, 'п Николаев Посад' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57708000466' as code, 'Казарма 192-й км' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000' as code, 'Горнозаводский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000001' as code, 'г Горнозаводск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000056' as code, 'рп Бисер' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000061' as code, 'рп Кусье-Александровский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000066' as code, 'рп Медведка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000071' as code, 'рп Нововильвенский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000076' as code, 'рп Пашия' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000081' as code, 'рп Промысла' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000086' as code, 'рп Сараны' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000091' as code, 'рп Старый Бисер' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000096' as code, 'рп Теплая Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000106' as code, 'п Вильва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000111' as code, 'п Средняя Усьва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000116' as code, 'п Усть-Койва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000121' as code, 'п Усть-Тырым' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000126' as code, 'п.ст Вижай' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000131' as code, 'п.ст Европейская' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000136' as code, 'п.ст Койва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000141' as code, 'п.ст Лаки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57714000146' as code, 'п.ст Усть-Тискос' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57715000' as code, 'Гремячинский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57715000001' as code, 'г Гремячинск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57715000056' as code, 'рп Усьва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57715000106' as code, 'п Безгодово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57715000111' as code, 'п Заготовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57715000116' as code, 'п Шумихинский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57715000121' as code, 'п Юбилейный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57715000126' as code, 'рзд п Басег' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57719000' as code, 'город Кизел' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57719000001' as code, 'г Кизел' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57719000106' as code, 'рзд п Расик' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57719000111' as code, 'п Шахта' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57719000116' as code, 'п Центральный Коспашский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57719000121' as code, 'п Северный Коспашский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57719000126' as code, 'п Большая Ослянка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57719000131' as code, 'п Южный Коспашский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000' as code, 'Краснокамский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000001' as code, 'г Краснокамск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000056' as code, 'рп Оверята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000106' as code, 'с Мысы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000111' as code, 'д Алешино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000116' as code, 'д Калининцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000121' as code, 'п Ласьва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000126' as code, 'д Мошни' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000131' as code, 'д Нагорная' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000136' as code, 'д Никитино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000141' as code, 'д Новоселы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000146' as code, 'д Осляна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000151' as code, 'д Семичи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000156' as code, 'д Хухрята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000161' as code, 'с Черная' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000166' as code, 'д Большая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000171' as code, 'д Брагино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000176' as code, 'д Бусырята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000181' as code, 'д Васенки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000186' as code, 'д Даньки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000191' as code, 'д Запальта' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000196' as code, 'д Кормильцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000201' as code, 'д Малые Шабуничи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000206' as code, 'д Мишкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000211' as code, 'п ж/д площадки Мишкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000216' as code, 'д Нижнее Брагино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000221' as code, 'д Никитино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000226' as code, 'д Новая Ивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000231' as code, 'д Пирожки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000236' as code, 'п.ст Шабуничи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000241' as code, 'ж/д будка 1401-й км' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000246' as code, 'ж/д будка 1402-й км' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000251' as code, 'ж/д будка 1403-й км' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000256' as code, 'ж/д будка 1405-й км' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000261' as code, 'ж/д будка 1406-й км' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000266' as code, 'д Якунята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000271' as code, 'п Майский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000276' as code, 'д Волеги' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000281' as code, 'д Нижние Симонята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000286' as code, 'д Мошево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000291' as code, 'д Фадеята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000296' as code, 'д Кабанов Мыс' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000301' as code, 'с Усть-Сыны' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000306' as code, 'д Большое Шилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000311' as code, 'д Верхнее Гуляево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000316' as code, 'д Гурино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000321' as code, 'д Заречная' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000326' as code, 'д Карабаи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000331' as code, 'д Клепики' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000336' as code, 'д Конец-Бор' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000341' as code, 'д Кузнецы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000346' as code, 'д Малое Шилово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000351' as code, 'д Нижнее Гуляево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000356' as code, 'с Стряпунята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000361' as code, 'д Абакшата' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000366' as code, 'д Абакшата' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000371' as code, 'д Абросы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000376' as code, 'д Батуры' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000381' as code, 'д Большие Калинята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000386' as code, 'д Екимята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000391' as code, 'д Ерошино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000396' as code, 'д Евстюничи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000401' as code, 'д Жаково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000406' as code, 'д Ильино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000411' as code, 'д Катыши' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000416' as code, 'д Осташата' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000421' as code, 'п Подстанция' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000426' as code, 'д Понылки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000431' as code, 'д Ананичи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000436' as code, 'д Дочки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000441' as code, 'д Залесная' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000446' as code, 'д Русаки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000451' as code, 'д Трубино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000456' as code, 'п Фроловичи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57720000461' as code, 'д Часовня' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000' as code, 'Оханский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000001' as code, 'г Оханск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000106' as code, 'с Андреевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000111' as code, 'д Гаревляна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000116' as code, 'д Суровцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000121' as code, 'д Чуран' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000126' as code, 'с Беляевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000131' as code, 'д Большая Гремяча' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000136' as code, 'д Гляденово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000141' as code, 'д Ерзовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000146' as code, 'д Заборье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000151' as code, 'д Мысы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000156' as code, 'д Пташки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000161' as code, 'с Дуброво' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000166' as code, 'д Галешник' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000171' as code, 'д Лариха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000176' as code, 'д Осиновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000181' as code, 'с Пономари' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000186' as code, 'д Посад' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000191' as code, 'д Мыльники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000196' as code, 'с Казанка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000201' as code, 'д Батаиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000206' as code, 'д Верхняя Шумиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000211' as code, 'д Замалая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000216' as code, 'д Замании' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000221' as code, 'д Заонохово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000226' as code, 'д Заполье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000231' as code, 'д Ключи 3-и' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000236' as code, 'д Окуловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000241' as code, 'д Осиновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000246' as code, 'д Подскопина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000251' as code, 'д Усолье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000256' as code, 'с Острожка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000261' as code, 'д Горюхалиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000266' as code, 'д Зародники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000271' as code, 'д Казымово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000276' as code, 'д Касьяново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000281' as code, 'д Кропачиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000286' as code, 'д Лыва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000291' as code, 'д Новые Селища' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000296' as code, 'д Старые Селища' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000301' as code, 'д Сычи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000306' as code, 'д Чуркино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000311' as code, 'д Шалыга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000316' as code, 'с Таборы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000321' as code, 'д Березник' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000326' as code, 'д Загора' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000331' as code, 'д Заполье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000336' as code, 'д Кочегары' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000341' as code, 'д Чугудаи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000346' as code, 'д Шаркан' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000351' as code, 'д Подволок' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000356' as code, 'д Першино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000361' as code, 'д Мерзляки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000366' as code, 'д Тулумбаиха' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000371' as code, 'д Притыка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000376' as code, 'д Березовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000381' as code, 'д Болгары' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000386' as code, 'д Копыловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000391' as code, 'д Красные Горки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000396' as code, 'д Половинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000401' as code, 'д Сухой Лог' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57729000406' as code, 'д Шалаши' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000106' as code, 'п Басим' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000111' as code, 'д Крутики' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000116' as code, 'с Уролка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000121' as code, 'д Оськино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000126' as code, 'д Ульва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000131' as code, 'с Половодово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000136' as code, 'д Харюшина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000141' as code, 'д Попова-Останина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000146' as code, 'д Тренина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000151' as code, 'п Усть-Сурмог' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000156' as code, 'п Черное' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000161' as code, 'д Лога' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000166' as code, 'с Осокино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000171' as code, 'с Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000176' as code, 'д Малое Городище' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000181' as code, 'д Лобанова' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000186' as code, 'п Профилакторий СМЗ' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000191' as code, 'п Уральские Самоцветы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000196' as code, 'с Касиб' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000201' as code, 'д Лызиб' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000206' as code, 'д Никино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000211' as code, 'д Сорвино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000216' as code, 'д Вильва' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000221' as code, 'д Елькина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000226' as code, 'д Ефремы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000231' as code, 'д Зуева' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000236' as code, 'п Нижний склад' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000241' as code, 'с Пегушино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000246' as code, 'д Пузаны' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000251' as code, 'д Пухирева' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000256' as code, 'п Тетерино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000261' as code, 'д Тетерина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000266' as code, 'д Григорова' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000271' as code, 'д Белкина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000276' as code, 'п Красный Берег' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000281' as code, 'п Сим' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000286' as code, 'д Володино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000291' as code, 'п Геологоразведка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000296' as code, 'с Родники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000301' as code, 'п Усовский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000306' as code, 'д Чашкина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000311' as code, 'с Тохтуева' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000316' as code, 'д Кокорино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000321' as code, 'д Сёла' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000326' as code, 'д Чертеж' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000331' as code, 'с Жуланово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000336' as code, 'д Кузнецова' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000341' as code, 'п Тюлькино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000346' as code, 'п Бараново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000351' as code, 'д Усть-Вишера' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000356' as code, 'д Тюлькино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000361' as code, 'д Толстик' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000366' as code, 'с Верхнее Мошево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000371' as code, 'п Затон' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000376' as code, 'д Левина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000381' as code, 'д Ескина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000386' as code, 'п Нижнее Мошево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57730000391' as code, 'д Нижнее Мошево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000' as code, 'Чайковский' as name, 1 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000001' as code, 'г Чайковский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000106' as code, 'п Буренка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000111' as code, 'п Детский Дом' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000116' as code, 'п Марковский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000121' as code, 'п Прикамский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000126' as code, 'п Чернушка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000131' as code, 'с Альняш' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000136' as code, 'с Большой Букор' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000141' as code, 'с Ваньки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000146' as code, 'с Вассята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000151' as code, 'с Завод Михайловский' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000156' as code, 'с Зипуново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000161' as code, 'с Кемуль' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000166' as code, 'с Ольховка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000171' as code, 'с Сосново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000176' as code, 'с Уральское' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000181' as code, 'с Фоки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000186' as code, 'д Аманеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000191' as code, 'д Белая Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000196' as code, 'д Бормист' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000201' as code, 'д Ваньчики' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000206' as code, 'д Векошинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000211' as code, 'д Гаревая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000216' as code, 'д Дедушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000221' as code, 'д Дубовая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000226' as code, 'д Жигалки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000231' as code, 'д Засечный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000236' as code, 'д Злодарь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000241' as code, 'д Ивановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000246' as code, 'д Каменный Ключ' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000251' as code, 'д Карша' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000256' as code, 'д Кирилловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000261' as code, 'д Лукинцы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000266' as code, 'д Малая Соснова' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000271' as code, 'д Малый Букор' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000276' as code, 'д Маракуши' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000281' as code, 'д Марково' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000286' as code, 'д Моховая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000291' as code, 'д Некрасово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000296' as code, 'д Нижняя Гарь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000301' as code, 'д Ольховочка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000306' as code, 'д Опары' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000311' as code, 'д Оралки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000316' as code, 'д Романята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000321' as code, 'д Русалевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000326' as code, 'д Сарапулка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000331' as code, 'д Соловьи' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000336' as code, 'д Степаново' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000341' as code, 'д Харнавы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000346' as code, 'д Чумна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '57735000351' as code, 'п.ст Каучук' as name, 2 as razd from dual union all 
select to_date('01.12.2017','dd.mm.yyyy') as version, '57817700' as code, 'Межселенные территории Косинского муниципального района' as name, 1 as razd from dual union all 
select to_date('01.12.2017','dd.mm.yyyy') as version, '57817701' as code, 'Межселенная территория Косинского муниципального района, кроме территорий сельских поселений' as name, 1 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '57817701' as code, 'Межселенные территории Косинского муниципального района, кроме территорий сельских поселений' as name, 1 as razd from dual union all 
select to_date('01.12.2017','dd.mm.yyyy') as version, '57819700' as code, 'Межселенные территории Кочевского муниципального района' as name, 1 as razd from dual union all 
select to_date('01.12.2017','dd.mm.yyyy') as version, '57819701' as code, 'Межселенная территория Кочевского муниципального района, кроме территорий сельских поселений' as name, 1 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '57821444212' as code, 'д Кузнецова' as name, 2 as razd from dual union all 
select to_date('01.12.2017','dd.mm.yyyy') as version, '57827700' as code, 'Межселенные территории Юсьвинского муниципального района' as name, 1 as razd from dual union all 
select to_date('01.12.2017','dd.mm.yyyy') as version, '57827701' as code, 'Межселенная территория Юсьвинского муниципального района, кроме территорий сельских поселений' as name, 1 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '60632460159' as code, 'х Северный Сад' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61608462171' as code, 'с Балушевы Починки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61608462176' as code, 'с Толстиково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61608462181' as code, 'д Ананьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61608462186' as code, 'д Сеитово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415111' as code, 'с Тюково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415116' as code, 'с Стружаны' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415121' as code, 'с Колычево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415126' as code, 'д Алтухово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415131' as code, 'д Карпово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415136' as code, 'д Лебедино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415141' as code, 'д Макеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415146' as code, 'д Мягково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415151' as code, 'д Нефедово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415156' as code, 'д Русаново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415161' as code, 'д Тетерино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415166' as code, 'д Фомино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415171' as code, 'д Шакино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415176' as code, 'д Дунино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415181' as code, 'д Аверькиево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415186' as code, 'д Анциферово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415191' as code, 'д Большая Матвеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415196' as code, 'д Зубово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415201' as code, 'д Малая Матвеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415206' as code, 'д Чебукино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415211' as code, 'д Аристово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415216' as code, 'д Беломутово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415221' as code, 'д Большое Курапово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415226' as code, 'д Давыдово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415231' as code, 'д Каширово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415236' as code, 'д Клин' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415241' as code, 'д Козельское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415246' as code, 'д Малое Курапово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415251' as code, 'д Маньщино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415256' as code, 'д Мосеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415261' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415266' as code, 'д Пансурово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415271' as code, 'д Прасковьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415276' as code, 'д Сонино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415281' as code, 'д Владычино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610415286' as code, 'х Сильма' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610448181' as code, 'с Малахово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610448186' as code, 'д Андроново' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610448191' as code, 'д Борисково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610448196' as code, 'д Ветчаны' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610448201' as code, 'д Иванково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610448206' as code, 'д Култуки' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610448211' as code, 'д Сергеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451141' as code, 'д Первушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451146' as code, 'д Макеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451151' as code, 'д Полушкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451156' as code, 'д Сергеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451161' as code, 'д Борисово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451166' as code, 'д Взвоз' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451171' as code, 'д Макарово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451176' as code, 'д Печурино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451181' as code, 'д Подгорье' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451186' as code, 'д Ухино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451191' as code, 'с Задне-Пилево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61610451196' as code, 'с Ершово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418186' as code, 'с Новое Киркино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418191' as code, 'с Старое Киркино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418196' as code, 'д Волшута' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418201' as code, 'д Александрово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418206' as code, 'д Каменный Хутор 1' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418211' as code, 'д Каменный Хутор 2' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418216' as code, 'д Николаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418221' as code, 'д Федоровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418226' as code, 'д Феняево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418231' as code, 'д Чесменка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418236' as code, 'п центрального отделения свх им Ильича' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418241' as code, 'п Заречье' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418246' as code, 'п Коровинского спиртзавода' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617418251' as code, 'п отделения Пролетарское" свх им Ильича"' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617467161' as code, 'с Печерники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617467166' as code, 'с Березово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61617467171' as code, 'д Саларьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61620410176' as code, 'с Ленино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61620410181' as code, 'д Аннинка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61620410186' as code, 'д Константиновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61620410191' as code, 'д Свистовка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61620410196' as code, 'п Аннино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61620410201' as code, 'п Нива' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61620450161' as code, 'д Мары' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61620450166' as code, 'с Бурминка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61620450171' as code, 'с Красное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634412121' as code, 'с Кораблино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634412126' as code, 'с Глебово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634412131' as code, 'с Горетово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634412136' as code, 'д Богданово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634412141' as code, 'д Путково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634412146' as code, 'д Юрасово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475151' as code, 'с Вышетравино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475156' as code, 'с Дашки-2' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475161' as code, 'с Новинское' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475166' as code, 'п Свобода' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475171' as code, 'д Аксиньино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475176' as code, 'д Арсентьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475181' as code, 'д Глядково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475186' as code, 'д Климантино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475191' as code, 'д Матвеевка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475196' as code, 'д Минеево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475201' as code, 'д Павловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475206' as code, 'д Романцево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475211' as code, 'д Сажнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475216' as code, 'д Слободка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475221' as code, 'д Сорокино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475226' as code, 'д Чичкино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634475231' as code, 'д Щегрово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634495151' as code, 'д Ровное' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634495156' as code, 'д Агарково' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634495161' as code, 'д Взметнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634495166' as code, 'д Городищево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634495171' as code, 'д Дубровка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634495176' as code, 'д Дьяконово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634495181' as code, 'д Матчино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634495186' as code, 'п Стенькино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61634495191' as code, 'с Пущино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656448116' as code, 'п Криволуцкие Дворики' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656448121' as code, 'с Кривая Лука' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656448126' as code, 'с Старая Покровка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656448131' as code, 'с Апушка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656448136' as code, 'д Александровка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656448141' as code, 'д Мельница' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656448146' as code, 'д Славка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454166' as code, 'с Тарадеи' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454171' as code, 'с Кулики' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454176' as code, 'с Казачий Дюк' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454181' as code, 'с Тюрино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454186' as code, 'с Шевырляй' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454191' as code, 'д Козино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454196' as code, 'д Андроновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454201' as code, 'д Липяной Дюк' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454206' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454211' as code, 'д Мишутино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454216' as code, 'д Студеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454221' as code, 'д Даниловка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454226' as code, 'д Новая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454231' as code, 'д Успеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656454236' as code, 'п Красный Городок' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656493136' as code, 'п Свеженькая' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '61656493141' as code, 'п Третий километр' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658430141' as code, 'с Красный Холм' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658430146' as code, 'д Непложа' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658430151' as code, 'д Чембар' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658430156' as code, 'п Краснохолмские Выселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658436116' as code, 'с Боровое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658436121' as code, 'с Терехово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658436126' as code, 'с Ирицы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658436131' as code, 'с Надеино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658436136' as code, 'д Ванчур' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658436141' as code, 'д Елизаветинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658436146' as code, 'д Уша' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658436151' as code, 'д Александровка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '61658436156' as code, 'д Константиновка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401116' as code, 'с Варфоломеевка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401121' as code, 'х Ветелки' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401126' as code, 'х Воропаев' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401131' as code, 'х Глубокий' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401136' as code, 'х Запрудный' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401141' as code, 'х Кокбие' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401146' as code, 'х Копылов' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401151' as code, 'х Кривой' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401156' as code, 'х Крутенький' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401161' as code, 'х Крутой' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401166' as code, 'х Кулацкий' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401171' as code, 'х Прудовой' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401176' as code, 'х Соловки' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401181' as code, 'х Сысоев' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401186' as code, 'х Урусов' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401191' as code, 'с Канавка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401196' as code, 'х Бабошкин' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401201' as code, 'х Бурдин' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401206' as code, 'х Доращивание' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401211' as code, 'х Жданов' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401216' as code, 'х Копанистый' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401221' as code, 'х Кругляков' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401226' as code, 'х Кушуков' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401231' as code, 'х Ляляев' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401236' as code, 'х Монахов' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401241' as code, 'х Морозов' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401246' as code, 'п Поливное' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401251' as code, 'х Утиный' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401256' as code, 'с Камышки' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401261' as code, 'х Бирюков' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401266' as code, 'х Новоселье' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401271' as code, 'х Старухин' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401276' as code, 'с Луков Кордон' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401281' as code, 'х Лиманный' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401286' as code, 'х Новостепное' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401291' as code, 'х Яшин' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401296' as code, 'п Приузенский' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401301' as code, 'п Ахматов' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401306' as code, 'х Ближний' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401311' as code, 'х Дальний' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401316' as code, 'х Жерпатер' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401321' as code, 'х Кошара' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401326' as code, 'х Крючков' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401331' as code, 'х Липин' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401336' as code, 'х Мезин' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401341' as code, 'х Моховой' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401346' as code, 'х Новостройка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401351' as code, 'х Новый Быт' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401356' as code, 'п Передовой' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401361' as code, 'х Разлой' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63602401366' as code, 'х Суходол' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405136' as code, 'с Петрово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405141' as code, 'д Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405146' as code, 'п Еленин' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405151' as code, 'д Михайловка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405156' as code, 'п Сазоново' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405161' as code, 'ж/д ст Чемизовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405166' as code, 'с Песчанка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405171' as code, 'д Александровка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405176' as code, 'д Новая Ивановка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405181' as code, 'д Новая Мотовиловка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405186' as code, 'д Старая Ивановка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604405191' as code, 'д Старая Мотовиловка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604423131' as code, 'с Большая Екатериновка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604423136' as code, 'д Булыгино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604423141' as code, 'д Сухая Палатовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604423146' as code, 'с Умет' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604423151' as code, 'д Филатовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604423156' as code, 'п Тургенево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604423161' as code, 'д Богдановка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604423166' as code, 'с Киселевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604423171' as code, 'д Николаевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604429126' as code, 'с Ивано-Языковка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604429131' as code, 'с Большая Осиновка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604429136' as code, 'д Гвардеевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604429141' as code, 'д Зубовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604429146' as code, 'д Ракитовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604429151' as code, 'с Сосновка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63604429156' as code, 'с Языковка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606156131' as code, 'с Хватовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606156136' as code, 'с Адоевщина' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606156141' as code, 'с Казанла' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606156146' as code, 'с Новая Жуковка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606156151' as code, 'с Рязайкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606156156' as code, 'с Степная Нееловка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606445121' as code, 'с Тепляковка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606445126' as code, 'с Арбузовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606445131' as code, 'с Березовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606445136' as code, 'с Малые Озерки' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606445141' as code, 'с Толстовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63606470111' as code, 'с Вязовка' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '63611410116' as code, 'ж/д ст Буровка' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '63611460121' as code, 'с Клюевка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63613151116' as code, 'с Петропавловка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63613151121' as code, 'с Антоновка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63613430126' as code, 'п Мирный' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63613430131' as code, 'п Комсомольский' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63613430136' as code, 'п Свободный' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63613430141' as code, 'п Славный' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63613435126' as code, 'с Жадовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63616460121' as code, 'п Прудовой' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63616460126' as code, 'с Переезд' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617416126' as code, 'с Рефлектор' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617416131' as code, 'с Большеузенка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617416136' as code, 'с Мавринка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617416141' as code, 'ст Мавринка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617416146' as code, 'с Михайловка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617456121' as code, 'с Орлов Гай' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617456126' as code, 'п Трудовое' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617456131' as code, 'х Лопатин' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617456136' as code, 'с Моховое' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63617456141' as code, 'с Новая Слободка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457121' as code, 'с Чапаевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457126' as code, 'с Дмитриевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457131' as code, 'с Коптевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457136' as code, 'п Кушумский' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457141' as code, 'с Верхний Кушум' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457146' as code, 'п Ветка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457151' as code, 'с Малый Перелаз' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457156' as code, 'п Михайло-Вербовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457161' as code, 'п Садовый' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63617457166' as code, 'с Светлое Озеро' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63629151131' as code, 'с Елшанка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63629151136' as code, 'д Алешкино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63629151141' as code, 'с Малая Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63629151146' as code, 'с Марьино-Лашмино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63629151151' as code, 'д Михайловка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63629151156' as code, 'с Лох' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63629151161' as code, 'с Гремячка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63629151166' as code, 'с Красная Речка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63644155131' as code, 'с Новокривовка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63644155136' as code, 'с Новолиповка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63644155141' as code, 'ст Наливная' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63644155146' as code, 'с Пионерское' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63647445141' as code, 'с Трубетчино' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63647445146' as code, 'с Чернавка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63647445151' as code, 'с Шепелевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63647445156' as code, 'с Каменка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63647450102' as code, 'д Агеевка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63647450107' as code, 'с Бороно-Михайловка' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63647450108' as code, 'с Боцманово' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63647450122' as code, 'с Колычево' as name, 2 as razd from dual union all 
select to_date('01.03.2019','dd.mm.yyyy') as version, '63647450161' as code, 'с Чириково' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63648430105' as code, 'с Калуга' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63648430116' as code, 'с Романовка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63648437106' as code, 'с Спартак' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63648437111' as code, 'с Серпогорское' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63648460111' as code, 'с Калдино' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63648460116' as code, 'с Красавка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63648460121' as code, 'с Николаевка' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63649406126' as code, 'с Апалиха' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '63649406131' as code, 'с Демкино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000001' as code, 'г Углегорск' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000056' as code, 'пгт Шахтерск' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000106' as code, 'с Приозерное' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000111' as code, 'с Медвежье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000116' as code, 'с Краснополье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000121' as code, 'с Прудное' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000126' as code, 'с Никольское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000131' as code, 'с Ольховка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000136' as code, 'с Поречье' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000141' as code, 'с Ольшанка' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000146' as code, 'с Орлово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000151' as code, 'с Лесогорское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000156' as code, 'с Тельновское' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000161' as code, 'с Надеждино' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000166' as code, 'с Ударное' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000171' as code, 'с Бошняково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '64752000176' as code, 'с Белые Ключи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404102' as code, 'д Аделаидено' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404103' as code, 'д Аношино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404104' as code, 'д Артемово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404105' as code, 'д Бараново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404107' as code, 'д Дорохово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404108' as code, 'д Дробыши' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404109' as code, 'д Жданово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404110' as code, 'д Золотаревка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404112' as code, 'д Всеволодкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404113' as code, 'д Годуново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404114' as code, 'д Горнево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404115' as code, 'д Гороховка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404122' as code, 'д Семеновское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404123' as code, 'д Спас-Курган' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404124' as code, 'д Степаньково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404125' as code, 'д Струково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404127' as code, 'д Черемушники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404128' as code, 'д Черное' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404129' as code, 'д Юфаново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404130' as code, 'д Ямново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404131' as code, 'д Барсуки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404136' as code, 'д Богданики' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404141' as code, 'д Горсткино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404146' as code, 'д Греково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404151' as code, 'д Григрево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404156' as code, 'д Иваново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404161' as code, 'д Киево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404166' as code, 'д Козулино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404171' as code, 'д Коробово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404176' as code, 'д Ломы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404181' as code, 'д Мартюхи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404186' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404191' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404196' as code, 'д Мишино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404201' as code, 'д Ново-Высокое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404206' as code, 'д Орлянка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404211' as code, 'д Относово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404216' as code, 'д Павлово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404221' as code, 'д Паново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404226' as code, 'д Пекарево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404231' as code, 'д Петино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404236' as code, 'д Победа' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404241' as code, 'д Походино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404246' as code, 'д Реутово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404251' as code, 'д Суровцево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404256' as code, 'д Тарасово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404261' as code, 'д Тихоново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404266' as code, 'п Березняки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404271' as code, 'с Богородицкое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404276' as code, 'с Вяземский' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404281' as code, 'с Ризское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404286' as code, 'с Хмелита' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605404291' as code, 'с Чепчугово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436102' as code, 'д Андрианы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436127' as code, 'д Горбы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436128' as code, 'д Давыдково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436132' as code, 'д Ежевицы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436133' as code, 'д Ефремово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436134' as code, 'д Желтовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436135' as code, 'д Колотовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436142' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436147' as code, 'д Мануйлино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436148' as code, 'д Мелихово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436149' as code, 'д Мельзино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436150' as code, 'д Минино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436157' as code, 'д Некрасово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436158' as code, 'д Неонилово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436159' as code, 'д Октябрьский' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436160' as code, 'д Панфилово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436162' as code, 'д Селиваново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436167' as code, 'д Соколово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436177' as code, 'д Тишино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436178' as code, 'д Тякино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436179' as code, 'д Устье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436180' as code, 'д Харьково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436187' as code, 'д Юшково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605436201' as code, 'д Кошелево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456102' as code, 'д Андроново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456107' as code, 'д Богданово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456117' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456122' as code, 'д Горлово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456132' as code, 'д Дрожжино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456133' as code, 'д Ефремово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456134' as code, 'д Зенкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456142' as code, 'д Касня' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456162' as code, 'д Лешутиха' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456167' as code, 'д Меркучево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456177' as code, 'д Новые Дворы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456202' as code, 'д Семово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456203' as code, 'д Сковородкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456204' as code, 'д Староселье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456227' as code, 'д Чернобаево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605456228' as code, 'д Щелканово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472102' as code, 'д Аверьково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472107' as code, 'д Бабенки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472112' as code, 'д Богданово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472113' as code, 'д Большая Калпита' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472114' as code, 'д Большие Лопатки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472115' as code, 'д Большое Петрово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472117' as code, 'д Веригино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472118' as code, 'д Володарец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472122' as code, 'д Годуновка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472123' as code, 'д Гредякино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472124' as code, 'д Гридино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472125' as code, 'д Дебрево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472127' as code, 'д Иваники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472128' as code, 'д Иванково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472132' as code, 'д Казаково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472152' as code, 'д Кухарево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472153' as code, 'д Ленкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472154' as code, 'д Леушино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472155' as code, 'д Лукьяново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472157' as code, 'д Орешки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472158' as code, 'д Осьма' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472159' as code, 'д Поляново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472162' as code, 'д Путьково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472163' as code, 'д Реброво' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472167' as code, 'д Сапегино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472168' as code, 'д Сельцо' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472169' as code, 'д Семеновское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472172' as code, 'д Старое Раменье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472177' as code, 'д Теплушка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472178' as code, 'д Усадище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472179' as code, 'д Хмельники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472180' as code, 'д Черемушки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472181' as code, 'д Большое Староселье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472186' as code, 'д Дружба' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472191' as code, 'д Дяглево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472196' as code, 'д Загорская' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472201' as code, 'д Малая Калпита' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472206' as code, 'д Малые Лопатки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472211' as code, 'д Мармоново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472216' as code, 'д Матюшино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472221' as code, 'д Мишино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472226' as code, 'д Молошино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472231' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472236' as code, 'д Никольское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472241' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472246' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472251' as code, 'д Новые Дворы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472256' as code, 'д Щелканово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472261' as code, 'с Хватов Завод' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472266' as code, 'ст Гредякино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605472271' as code, 'ст Семлево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476102' as code, 'д Большая Азаровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476103' as code, 'д Бочкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476112' as code, 'д Гужово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476117' as code, 'д Жегловка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476122' as code, 'д Клин' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476132' as code, 'д Костылевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476133' as code, 'д Коханово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476134' as code, 'д Крутое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476135' as code, 'д Кумовая Яма' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476147' as code, 'д Никитинка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476157' as code, 'д Пушкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476167' as code, 'д Рябиково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476177' as code, 'д Сомово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476197' as code, 'с Исаково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476206' as code, 'д Логвино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476211' as code, 'д Лукино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605476216' as code, 'д Малая Азаровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480102' as code, 'д Безобразово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480107' as code, 'д Бобрище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480108' as code, 'д Богданцево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480109' as code, 'д Большие Ломы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480110' as code, 'д Бровкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480112' as code, 'д Ермолинка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480117' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480118' as code, 'д Каськово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480119' as code, 'д Клоково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480120' as code, 'д Козлово Озеро' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480122' as code, 'д Коргино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480132' as code, 'д Красная Слобода' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480133' as code, 'д Криково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480134' as code, 'д Крутое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480137' as code, 'д Лаврово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480138' as code, 'д Леонтьево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480139' as code, 'д Менка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480142' as code, 'д Митьково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480143' as code, 'д Мишино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480144' as code, 'д Мочальники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480145' as code, 'д Никулинки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480147' as code, 'д Обухово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480148' as code, 'д Овсяники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480152' as code, 'д Охотино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480157' as code, 'д Парково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480158' as code, 'д Песочня' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480167' as code, 'д Рославец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480168' as code, 'д Савенки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480169' as code, 'д Сноски' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480170' as code, 'д Сороколетово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480172' as code, 'д Станы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480173' as code, 'д Тарасово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480174' as code, 'д Телепнево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480177' as code, 'д Турово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480178' as code, 'д Успенское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480182' as code, 'д Федяево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480183' as code, 'д Царево-Займище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480187' as code, 'д Шарапово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480188' as code, 'д Шаховка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480192' as code, 'с Шуйское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480196' as code, 'д Воросна' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480201' as code, 'д Вырубово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480206' as code, 'д Гаврилки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480211' as code, 'д Гашино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480216' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480221' as code, 'д Григорово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480226' as code, 'д Гришково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480231' as code, 'д Гряда' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480236' as code, 'д Демидово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480241' as code, 'д Дмитровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480246' as code, 'д Добринка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480251' as code, 'д Докунка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480256' as code, 'д Дьяковка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480261' as code, 'д Козловцы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66605480266' as code, 'д Ново-Никольское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405102' as code, 'д Афонино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405103' as code, 'д Барсуки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405104' as code, 'д Березовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405105' as code, 'д Бражино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405112' as code, 'д Городок' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405113' as code, 'д Гриднево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405114' as code, 'д Громово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405115' as code, 'д Дубровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405117' as code, 'д Княщина' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405118' as code, 'д Кряково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405119' as code, 'д Лепешки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405120' as code, 'д Лукты' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405122' as code, 'д Мархоткино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405123' as code, 'д Митишково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405124' as code, 'д Мясники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405125' as code, 'д Немерзь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405127' as code, 'д Подмощье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405137' as code, 'д Следнево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405138' as code, 'д Старинцы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405139' as code, 'д Ушаково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405151' as code, 'д Васюки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614405156' as code, 'д Петрыкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440102' as code, 'д Абрамово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440103' as code, 'д Бабаедово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440104' as code, 'д Белавка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440105' as code, 'д Бизюково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440113' as code, 'д Ивановское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440117' as code, 'д Карачарово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440118' as code, 'д Клешники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440119' as code, 'д Кузнецово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440120' as code, 'д Лелявино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440122' as code, 'д Мартынково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440123' as code, 'д Милоселье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440124' as code, 'д Молодилово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440125' as code, 'д Никулино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440127' as code, 'д Роги' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440128' as code, 'д Рязань' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440129' as code, 'д Садовая' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440130' as code, 'д Самцово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440143' as code, 'д Щербинино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440151' as code, 'д Болдино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440156' as code, 'д Борздилово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440161' as code, 'д Будка железной дороги 21 км' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440166' as code, 'д Василисино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440171' as code, 'д Васино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440176' as code, 'д Вороново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440181' as code, 'д Городок' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440186' as code, 'д Деревенщики' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440191' as code, 'д Ленкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440196' as code, 'д Леоньково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440201' as code, 'д Новый Двор' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440206' as code, 'д Полибино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440211' as code, 'д Полижакино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440216' as code, 'д Прослище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440221' as code, 'д Селюшки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440226' as code, 'д Славково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440231' as code, 'д Ставково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614440236' as code, 'д Струково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465102' as code, 'д Балакирево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465103' as code, 'д Болотово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465104' as code, 'д Боровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465105' as code, 'д Быково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465107' as code, 'д Выгорь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465112' as code, 'д Громаки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465113' as code, 'д Губино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465114' as code, 'д Давыдово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465115' as code, 'д Дежино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465117' as code, 'д Кузино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465122' as code, 'д Логиновка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465123' as code, 'д Лукьяненки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465127' as code, 'д Михайловка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465128' as code, 'д Наливки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465129' as code, 'д Недники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465130' as code, 'д Новоселье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465132' as code, 'д Симоново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465137' as code, 'д Смородиновка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465142' as code, 'д Усвятье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465147' as code, 'д Федоровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465148' as code, 'д Фомино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465149' as code, 'д Хатычка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465150' as code, 'д Шагаки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465156' as code, 'д Долгиново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465161' as code, 'д Дягилево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465166' as code, 'д Запрудье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465171' as code, 'д Ивашутино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465176' as code, 'д Каськово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465181' as code, 'д Озерище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465186' as code, 'д Пензево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465191' as code, 'д Ректы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465196' as code, 'д Селенка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465201' as code, 'д Шульгино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66614465206' as code, 'д Яковлево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416102' as code, 'д Амфилаты' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416137' as code, 'д Измайлово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416162' as code, 'д Никифорово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416167' as code, 'д Петрянино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416172' as code, 'д Рождество' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416182' as code, 'д Старое Устиново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416187' as code, 'д Ушаково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416192' as code, 'д Фенино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416197' as code, 'д Хлысты' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619416202' as code, 'д Черемисино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440102' as code, 'д Бодалино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440103' as code, 'д Бывалка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440104' as code, 'д Вава' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440105' as code, 'д Вараксино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440117' as code, 'д Замошье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440118' as code, 'д Зуево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440122' as code, 'д Каменец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440123' as code, 'д Коноплинка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440137' as code, 'д Лядцо' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440138' as code, 'д Мазово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440139' as code, 'д Мартинково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440140' as code, 'д Марьино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440142' as code, 'д Мосолы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440147' as code, 'д Новики' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440148' as code, 'д Ново-Никольское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440162' as code, 'д Пронино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440167' as code, 'д Селешня' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440172' as code, 'д Сос' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440173' as code, 'д Сосновка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440174' as code, 'д Стайки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440177' as code, 'д Теренино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440178' as code, 'д Титово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440192' as code, 'д Флясово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440193' as code, 'д Холбни' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440194' as code, 'д Холматы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440195' as code, 'д Чащи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440197' as code, 'д Ширково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440202' as code, 'д Щелкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440207' as code, 'с Теренино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440208' as code, 'ст Колошино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440211' as code, 'д Взглядье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440216' as code, 'д Вититнево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440221' as code, 'д Вититни' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440226' as code, 'д Голубев Мох' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440231' as code, 'д Ежевица' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440236' as code, 'д Матченки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440241' as code, 'д Мелихово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619440246' as code, 'д Чемуты' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452107' as code, 'д Бабичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452108' as code, 'д Барсуки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452109' as code, 'д Бибирево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452117' as code, 'д Большое Павлово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452118' as code, 'д Вербилово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452122' as code, 'д Высокое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452137' as code, 'д Заполье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452138' as code, 'д Зубаревка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452139' as code, 'д Зубово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452142' as code, 'д Коситчено' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452143' as code, 'д Костылево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452144' as code, 'д Лапино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452152' as code, 'д Липня' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452153' as code, 'д Луки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452157' as code, 'д Максаки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452158' as code, 'д Малое Павлово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452159' as code, 'д Малышевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452162' as code, 'д Мишуково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452163' as code, 'д Нешево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452164' as code, 'д Никиточкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452165' as code, 'д Новое Мутище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452167' as code, 'д Орлы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452168' as code, 'д Передельники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452172' as code, 'д Петуховка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452173' as code, 'д Погорное' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452177' as code, 'д Ренда' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452178' as code, 'д Ржавец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452182' as code, 'д Светилово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452183' as code, 'д Седлецкий Починок' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452187' as code, 'д Сигарево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452192' as code, 'д Средний Починок' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452193' as code, 'д Старое Мутище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452194' as code, 'д Старое Щербино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452195' as code, 'д Старшевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452197' as code, 'д Сухой Починок' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452202' as code, 'д Угрица' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452203' as code, 'д Федоровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452204' as code, 'д Филатка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452205' as code, 'д Холм' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452211' as code, 'д Новое Щербино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452216' as code, 'д Новоселье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452221' as code, 'д Новоспасское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452226' as code, 'д Оболоновец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452231' as code, 'д Хохловка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452236' as code, 'д Шатьково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66619452241' as code, 'д Шевелево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444102' as code, 'д Беловщина' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444103' as code, 'д Беседка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444122' as code, 'д Верховая' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444123' as code, 'д Высокий Борок' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444124' as code, 'д Доброносичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444125' as code, 'д Дубровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444127' as code, 'д Дятловка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444128' as code, 'д Жигаловка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444129' as code, 'д Жуковка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444130' as code, 'д Краснозаборье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444132' as code, 'д Лужная' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444137' as code, 'д Малый Бобрывец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444138' as code, 'д Мацилевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444139' as code, 'д Николаевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444140' as code, 'д Никулино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444141' as code, 'д Литвинова Буда' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444146' as code, 'д Литвиновка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444151' as code, 'д Новая Мацилевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444156' as code, 'д Новая Сенная' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444161' as code, 'д Пехтери' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444166' as code, 'д Пожарь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444171' as code, 'д Помозовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444176' as code, 'д Поселки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444181' as code, 'д Пустая Буда' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444186' as code, 'д Ржавец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444191' as code, 'д Ровки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444196' as code, 'д Сморкачи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444201' as code, 'д Старая Сенная' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444206' as code, 'д Телюковка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444211' as code, 'д Шадога' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444216' as code, 'п Кирпичного Завода' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621444221' as code, 'с Кузьмичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466102' as code, 'д Благодать' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466103' as code, 'д Бояркино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466104' as code, 'д Глухари' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466132' as code, 'д Лычники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466133' as code, 'д Ново-Троицкое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466134' as code, 'д Петраково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466142' as code, 'д Рухань' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466143' as code, 'д Свиридовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466147' as code, 'д Скоторж' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466152' as code, 'д Сукромля' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466153' as code, 'д Тросно-Ивакино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466154' as code, 'д Тросно-Исаево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66621466155' as code, 'д Ходынка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430107' as code, 'д Белеи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430112' as code, 'д Бовшево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430117' as code, 'д Буда' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430122' as code, 'д Выжимаки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430123' as code, 'д Герасименки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430127' as code, 'д Ермаки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430128' as code, 'д Жваненки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430129' as code, 'д Зюзьки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430130' as code, 'д Кисели' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430132' as code, 'д Коштуны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430133' as code, 'д Красная Горка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430134' as code, 'д Курган' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430137' as code, 'д Лонница' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430152' as code, 'д Ольша' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430153' as code, 'д Орловичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430172' as code, 'д Птушки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430177' as code, 'д Седневка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430178' as code, 'д Скворцы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430179' as code, 'д Стариненки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430180' as code, 'д Урали' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430182' as code, 'д Шашуки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430192' as code, 'ст Красное' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430196' as code, 'д Клименки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624430201' as code, 'д Хлыстовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445102' as code, 'д Авадово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445103' as code, 'д Алушково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445104' as code, 'д Алфимково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445105' as code, 'д Антоновичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445107' as code, 'д Викторово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445108' as code, 'д Винные Луки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445109' as code, 'д Волоедово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445110' as code, 'д Гвоздово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445112' as code, 'д Дуровичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445113' as code, 'д Забродье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445114' as code, 'д Застенки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445115' as code, 'д Зверовичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445117' as code, 'д Катково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445118' as code, 'д Клименти' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445119' as code, 'д Ковшичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445120' as code, 'д Козлы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445122' as code, 'д Литивля' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445123' as code, 'д Литивлянка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445127' as code, 'д Любаничи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445128' as code, 'д Ляхово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445129' as code, 'д Маклаково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445130' as code, 'д Малахово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445132' as code, 'д Павлово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445137' as code, 'д Первое Мая' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445138' as code, 'д Перхово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445139' as code, 'д Питьково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445140' as code, 'д Платоново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445142' as code, 'д Синяки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445143' as code, 'д Слобода' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445144' as code, 'д Старое Кудрино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445145' as code, 'д Струково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445147' as code, 'д Тригубово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445148' as code, 'д Трояны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445149' as code, 'д Туговищи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445152' as code, 'д Филаты' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445153' as code, 'д Хворостово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445157' as code, 'д Церковище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445158' as code, 'д Черныши' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445162' as code, 'д Шеено' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445163' as code, 'д Шелбаны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445164' as code, 'д Шилковичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445165' as code, 'д Ясенец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445166' as code, 'д Бежали' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445171' as code, 'д Богдановка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445176' as code, 'д Болтутино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445181' as code, 'д Борки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445186' as code, 'д Бубново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445191' as code, 'д Бухарино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445196' as code, 'д Василевичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445201' as code, 'д Глинное' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445206' as code, 'д Глубокое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445211' as code, 'д Горбово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445216' as code, 'д Гребени' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445221' as code, 'д Двуполяны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445226' as code, 'д Девичья Дубрава' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445231' as code, 'д Кошелево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445236' as code, 'д Красатинка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445241' as code, 'д Курганье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445246' as code, 'д Лисово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445251' as code, 'д Марково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445256' as code, 'д Недвижи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445261' as code, 'д Нейково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445266' as code, 'д Николаевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445271' as code, 'д Плауны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445276' as code, 'д Подберезье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445281' as code, 'д Полянки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445286' as code, 'д Пятницкое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445291' as code, 'д Расточино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445296' as code, 'д Рахово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445301' as code, 'д Речицы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445306' as code, 'д Самоны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445311' as code, 'д Самсоны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445316' as code, 'д Селец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624445321' as code, 'д Суймище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455107' as code, 'д Волково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455108' as code, 'д Горбачи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455109' as code, 'д Городец' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455110' as code, 'д Даниловка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455112' as code, 'д Железково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455117' as code, 'д Жули' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455132' as code, 'д Кончинка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455133' as code, 'д Корыбщина' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455134' as code, 'д Кохоново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455152' as code, 'д Маньково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455153' as code, 'д Мироедово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455154' as code, 'д Михайлово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455155' as code, 'д Мончино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455162' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455163' as code, 'д Ольховка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455172' as code, 'д Ракиты' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455173' as code, 'д Рогайлово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455174' as code, 'д Середнево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455175' as code, 'д Смилово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455182' as code, 'д Сыроквашино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455183' as code, 'д Сырокоренье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455184' as code, 'д Уварово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455185' as code, 'д Угриново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455187' as code, 'д Чальцево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455188' as code, 'д Черныши' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455189' as code, 'д Чистяки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66624455190' as code, 'д Яново' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '66641415118' as code, 'д Клинка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410102' as code, 'д Басманово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410112' as code, 'д Васильевское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410113' as code, 'д Вырье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410114' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410115' as code, 'д Долматово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410117' as code, 'д Жилино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410122' as code, 'д Курьяново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410132' as code, 'д Мызино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410147' as code, 'д Подсосонье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410152' as code, 'д Рязаново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410153' as code, 'д Семеновская' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410157' as code, 'д Силинки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410158' as code, 'д Скугорево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410159' as code, 'д Станино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410160' as code, 'д Теплихово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410162' as code, 'д Фатейково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410163' as code, 'д Химино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410164' as code, 'д Холм' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648410165' as code, 'д Холмино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435102' as code, 'д Абрамово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435103' as code, 'д Базулино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435107' as code, 'д Бариново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435117' as code, 'д Будка железной дороги 34 км' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435118' as code, 'д Будка железной дороги 35 км' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435122' as code, 'д Вязищи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435123' as code, 'д Горы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435127' as code, 'д Дерличино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435128' as code, 'д Дряголовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435129' as code, 'д Жижало' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435130' as code, 'д Замыцкое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435142' as code, 'д Карпищево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435143' as code, 'д Кикино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435144' as code, 'д Кобелево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435145' as code, 'д Красино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435147' as code, 'д Левенки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435157' as code, 'д Мамуши' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435167' as code, 'д Нижнее Болваново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435168' as code, 'д Николаевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435172' as code, 'д Новиково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435177' as code, 'д Осипово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435178' as code, 'д Острожки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435182' as code, 'д Прудки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435183' as code, 'д Рассолово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435184' as code, 'д Сельцо' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435192' as code, 'д Скоморохово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435202' as code, 'д Степанищево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435203' as code, 'д Толпыги' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435204' as code, 'д Федосово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648435207' as code, 'д Чехово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440107' as code, 'д Аносово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440122' as code, 'д Василево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440123' as code, 'д Воробьево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440124' as code, 'д Глинки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440125' as code, 'д Головкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440127' as code, 'д Колчужино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440132' as code, 'д Курчино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440133' as code, 'д Мотовилово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440134' as code, 'д Нарытка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440137' as code, 'д Перетес' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440142' as code, 'д Поздняково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440143' as code, 'д Рамоны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440152' as code, 'д Селенки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440153' as code, 'д Семешкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440154' as code, 'д Сергеенки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440155' as code, 'д Степаники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440157' as code, 'д Фалилеево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440158' as code, 'д Федюково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440167' as code, 'д Шубкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440168' as code, 'д Якшино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440171' as code, 'д Девяткино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440176' as code, 'д Денежное' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440181' as code, 'д Дорна' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440186' as code, 'д Дуброво' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440191' as code, 'д Еськово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440196' as code, 'д Засецкое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66648440201' as code, 'д Судимово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420107' as code, 'д Арнишицы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420108' as code, 'д Архамоны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420112' as code, 'д Баскаково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420113' as code, 'д Береговая' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420117' as code, 'д Большое Захарьевское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420118' as code, 'д Буда' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420119' as code, 'д Буда' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420120' as code, 'д Бурмакино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420122' as code, 'д Дубки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420123' as code, 'д Дубровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420124' as code, 'д Жули' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420125' as code, 'д Заборье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420127' as code, 'д Зиновино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420128' as code, 'д Каменка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420129' as code, 'д Каменка-1' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420130' as code, 'д Каменка-2' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420132' as code, 'д Ключики' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420142' as code, 'д Кочаны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420143' as code, 'д Красные Поделы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420147' as code, 'д Круча' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420152' as code, 'д Любогощь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420153' as code, 'д Малое Захарьевское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420154' as code, 'д Мариуполь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420157' as code, 'д Нележ' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420158' as code, 'д Новинка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420159' as code, 'д Новое Азарово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420160' as code, 'д Оселье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420162' as code, 'д Селибка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420163' as code, 'д Селище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420164' as code, 'д Семенково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420165' as code, 'д Сенное' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420167' as code, 'д Холмы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420168' as code, 'д Хутор Архангельский' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420169' as code, 'д Цаплино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420170' as code, 'д Шемени' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420171' as code, 'д Васино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420176' as code, 'д Велижка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420181' as code, 'д Вергово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420186' as code, 'д Вертехово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420191' as code, 'д Выгорь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420196' as code, 'д Высокое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420201' as code, 'д Глотовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420206' as code, 'д Городечня' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420211' as code, 'д Громша' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420216' as code, 'д Дворище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420221' as code, 'д Кислово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420226' as code, 'д Петрово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420231' as code, 'д Пищево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420236' as code, 'д Подлипки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420241' as code, 'д Подопхаи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420246' as code, 'д Полднево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420251' as code, 'д Пустошка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420256' as code, 'д Пустошка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420261' as code, 'д Речица' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420266' as code, 'д Рисавы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420271' as code, 'д Сергеевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420276' as code, 'д Старое Азарово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420281' as code, 'д Терентеево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420286' as code, 'д Фролово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420291' as code, 'д Шилово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420296' as code, 'д Щекино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420301' as code, 'д Яненки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420306' as code, 'п 10-го Участка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420311' as code, 'п 18-го Участка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420316' as code, 'п Лесничество' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420321' as code, 'п Ново-Милятино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420326' as code, 'с Баскаковка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650420331' as code, 'ст Завальный' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435102' as code, 'д Александровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435103' as code, 'д Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435104' as code, 'д Андроново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435105' as code, 'д Бельдюгино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435112' as code, 'д Большие Ермаки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435113' as code, 'д Борисенки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435114' as code, 'д Боталы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435115' as code, 'д Васильевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435117' as code, 'д Гатишино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435118' as code, 'д Глухово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435119' as code, 'д Горки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435120' as code, 'д Городище' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435127' as code, 'д Заречье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435128' as code, 'д Зинеевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435129' as code, 'д Комбайн' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435130' as code, 'д Коньшино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435132' as code, 'д Коршуны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435133' as code, 'д Красная Весна' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435137' as code, 'д Крутые' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435138' as code, 'д Леоново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435139' as code, 'д Ломенка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435140' as code, 'д Лохово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435142' as code, 'д Новое' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435143' as code, 'д Островки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435144' as code, 'д Островки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435145' as code, 'д Песьково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435147' as code, 'д Сафоново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435148' as code, 'д Свинцово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435149' as code, 'д Сидоровское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435150' as code, 'д Синиково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435152' as code, 'д Шумихино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435157' as code, 'д Якимцево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435158' as code, 'рзд Дебрянский' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435159' as code, 'с Вешки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435160' as code, 'ст Волоста-Пятница' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435161' as code, 'д Великополье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435166' as code, 'д Ветки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435171' as code, 'д Волокочаны' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435176' as code, 'д Волоста' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435181' as code, 'д Вороново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435186' as code, 'д Горячки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435191' as code, 'д Гремячка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435196' as code, 'д Гречишное' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435201' as code, 'д Гряда' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435206' as code, 'д Губино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435211' as code, 'д Деменино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435216' as code, 'д Дмитровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435221' as code, 'д Доброе' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435226' as code, 'д Дрожжино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435231' as code, 'д Еленка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435236' as code, 'д Желанья' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435241' as code, 'д Коптево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435246' as code, 'д Корнюшково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435251' as code, 'д Луги' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435256' as code, 'д Льнозавод' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435261' as code, 'д Лядное' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435266' as code, 'д Лядцы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435271' as code, 'д Малиновка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435276' as code, 'д Малые Ермаки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435281' as code, 'д Маньшино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435286' as code, 'д Марфино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435291' as code, 'д Минино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435296' as code, 'д Михали' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435301' as code, 'д Мохнатка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435306' as code, 'д Петрово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435311' as code, 'д Плеснево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435316' as code, 'д Подсосонки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435321' as code, 'д Полнышево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435326' as code, 'д Полуовчинки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435331' as code, 'д Слободка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435336' as code, 'д Согласие' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435341' as code, 'д Станино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435346' as code, 'д Старая Лука' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435351' as code, 'д Староселье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435356' as code, 'д Ступники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435361' as code, 'д Тетерино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435366' as code, 'д Цинеево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435371' as code, 'д Чернь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650435376' as code, 'ст Годуновка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477102' as code, 'д Березняки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477103' as code, 'д Водокачка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477104' as code, 'д Вознесенье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477105' as code, 'д Выгорь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477107' as code, 'д Фоминское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477108' as code, 'д Харино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477111' as code, 'д Дуденки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477116' as code, 'д Иванково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477121' as code, 'д Калинино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477126' as code, 'д Кореино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477131' as code, 'д Латорево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477136' as code, 'д Медведки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477141' as code, 'д Мзы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477146' as code, 'д Мытишино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477151' as code, 'д Петрово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477156' as code, 'д Раздоры' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477161' as code, 'д Раслово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477166' as code, 'д Руднево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477171' as code, 'д Русаново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477176' as code, 'д Сельцо' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477181' as code, 'д Сергеево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477186' as code, 'д Сидоровичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477191' as code, 'д Сорокино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477196' as code, 'д Субборь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477201' as code, 'д Субботники' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477206' as code, 'д Судаково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66650477211' as code, 'д Трубино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415107' as code, 'д Большое-Бердяево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415112' as code, 'д Бракулино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415113' as code, 'д Верхоповье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415114' as code, 'д Ветлицы' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415115' as code, 'д Глисница' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415122' as code, 'д Захолынь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415127' as code, 'д Исаково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415132' as code, 'д Климово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415133' as code, 'д Кречеца' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415134' as code, 'д Кротово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415135' as code, 'д Левашово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415137' as code, 'д Мирополье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415138' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415139' as code, 'д Павлово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415142' as code, 'д Плаксино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415147' as code, 'д Репино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415157' as code, 'д Слизино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415158' as code, 'д Солнечная' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415159' as code, 'д Староселье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415160' as code, 'д Суховарино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415167' as code, 'д Чуркино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415168' as code, 'д Шурково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415186' as code, 'д Голочево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415191' as code, 'д Городна' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415196' as code, 'д Дедово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415201' as code, 'д Дмитрово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415206' as code, 'д Ерзаки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415211' as code, 'д Жуково' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415216' as code, 'д Лосево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415221' as code, 'д Львово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658415226' as code, 'д Малое-Бердяево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658435112' as code, 'д Зайцево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658435113' as code, 'д Колковичи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658435114' as code, 'д Приселье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658435122' as code, 'д Скачихино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658435142' as code, 'д Шишкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480102' as code, 'д Боброво' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480103' as code, 'д Буравлево' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480104' as code, 'д Буяново' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480112' as code, 'д Дарьино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480113' as code, 'д Дубины' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480117' as code, 'д Замощье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480118' as code, 'д Засижье' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480119' as code, 'д Клемятино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480142' as code, 'д Ольхово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480143' as code, 'д Перелесь' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480144' as code, 'д Петрово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480145' as code, 'д Погуляевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480147' as code, 'д Прость' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480152' as code, 'д Федосово' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '66658480153' as code, 'д Хотеново' as name, 2 as razd from dual union all 
select to_date('01.11.2018','dd.mm.yyyy') as version, '68626440113' as code, 'с Егоровка' as name, 2 as razd from dual union all 
select to_date('01.11.2018','dd.mm.yyyy') as version, '68626470115' as code, 'п с-за Подъём", 1-е отделение"' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634408107' as code, 'д Балабаевка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634408108' as code, 'д Головкино' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634408110' as code, 'п Новоситовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634408114' as code, 'с Новоямское' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634408141' as code, 'д Ситовка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634408146' as code, 'д Стрелки' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634408151' as code, 'с Фёдоровка' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634424116' as code, 'с Савинские Карпели' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634424121' as code, 'с Троицкие Росляи' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634464116' as code, 'п Стёжинское лесничество' as name, 2 as razd from dual union all 
select to_date('01.04.2018','dd.mm.yyyy') as version, '68634464121' as code, 'с Стёжки' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '69632415116' as code, 'с Иванкино' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '69632415121' as code, 'с Копыловка' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '69632415126' as code, 'п Зайкино' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '69632455146' as code, 'п Дальнее' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '69632455151' as code, 'п Куржино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '71630432106' as code, 'п Новоселезнево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '71630432111' as code, 'д Шадринка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000' as code, 'Голышмановский' as name, 1 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000051' as code, 'рп Голышманово' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000106' as code, 'д Алексеевка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000111' as code, 'д Басаргина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000116' as code, 'с Бескозобово' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000121' as code, 'д Большие Чирки' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000126' as code, 'д Боровлянка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000131' as code, 'д Брованова' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000136' as code, 'д Быстрая' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000141' as code, 'д Винокурова' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000146' as code, 'с Гладилово' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000151' as code, 'д Глубокая' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000156' as code, 'с Голышманово' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000161' as code, 'д Горбунова' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000166' as code, 'д Гришина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000171' as code, 'д Дербень' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000176' as code, 'д Дранкова' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000181' as code, 'с Евсино' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000186' as code, 'д Земляная' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000191' as code, 'д Кармацкая' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000196' as code, 'д Козловка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000201' as code, 'п Комсомольский' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000206' as code, 'с Королево' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000211' as code, 'д Крупинина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000216' as code, 'д Кузнецова' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000221' as code, 'д Кутырева' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000226' as code, 'п Ламенский' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000231' as code, 'д Лапушина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000236' as code, 'д Малиновка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000241' as code, 'д Малоемецк' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000246' as code, 'с Малышенка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000251' as code, 'с Медведево' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000256' as code, 'д Мелкозерова' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000261' as code, 'д Михайловка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000266' as code, 'д Мокрушина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000271' as code, 'д Никольск' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000276' as code, 'д Новая Хмелевка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000281' as code, 'д Новоселки' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000286' as code, 'д Одина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000291' as code, 'д Одина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000296' as code, 'д Оськина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000301' as code, 'д Плотина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000306' as code, 'с Ражево' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000311' as code, 'д Робчуки' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000316' as code, 'д Русакова' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000321' as code, 'д Садовщикова' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000326' as code, 'д Свинина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000331' as code, 'д Свистуха' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000336' as code, 'д Святославка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000341' as code, 'д Скарединка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000346' as code, 'д Скаредная' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000351' as code, 'д Солодилова' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000356' as code, 'с Средние Чирки' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000361' as code, 'д Терехина' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000366' as code, 'д Темная' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000371' as code, 'д Турлаки' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000376' as code, 'д Успенка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000381' as code, 'с Усть-Ламенка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000386' as code, 'д Усть-Малые Чирки' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000391' as code, 'д Хмелевка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000396' as code, 'д Черемшанка' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '71702000401' as code, 'д Шулындино' as name, 2 as razd from dual union all 
select to_date('01.08.2017','dd.mm.yyyy') as version, '71819153111' as code, 'д Пасол' as name, 2 as razd from dual union all 
select to_date('01.08.2017','dd.mm.yyyy') as version, '71819153116' as code, 'д Соснина' as name, 2 as razd from dual union all 
select to_date('01.08.2017','dd.mm.yyyy') as version, '71819412106' as code, 'с Былино' as name, 2 as razd from dual union all 
select to_date('01.08.2017','dd.mm.yyyy') as version, '71819412111' as code, 'д Вампугол' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76602402106' as code, 'с Восточная Амитхаша' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76602404106' as code, 'с Западный Будулан' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76602412104' as code, 'с Верхний Кункур' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76602436106' as code, 'с Заречный Челутай' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76602440104' as code, 'с Аргалей' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76609424106' as code, 'с Контой' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76609452106' as code, 'с Юбилейное' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76609464106' as code, 'с Малый Соловьёвск' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76609472106' as code, 'с Малый Хада-Булак' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76610403121' as code, 'с Новый Батакан' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611404106' as code, 'с Северный Алханай' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611404111' as code, 'с Южный Алханай' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611420106' as code, 'с Восточный Зуткулей' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611420111' as code, 'с Северный Зуткулей' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611420116' as code, 'с Южный Зуткулей' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611424111' as code, 'с Верхний Таптанай' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611428106' as code, 'с Восточный Токчин' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611428111' as code, 'с Западный Токчин' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611432106' as code, 'с Западный Узон' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611432111' as code, 'с Северный Узон' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611432116' as code, 'с Южный Узон' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611436106' as code, 'с Новый Чиндалей' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76611436111' as code, 'с Старый Чиндалей' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76618411106' as code, 'с Бура 1-я' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76618422106' as code, 'с Доно 1-е' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76618455106' as code, 'с Нижний Калгукан 1-й' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76620425106' as code, 'с Северная Кадахта' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76625404106' as code, 'с Хайласан' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76625422106' as code, 'с Нарин' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76625432114' as code, 'с Верхняя Ага' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76625432119' as code, 'с Нижняя Ага' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76625440111' as code, 'с Толон' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76628425121' as code, 'с Северная Знаменка' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76632415104' as code, 'с Восточный Бурулятуй' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76632425104' as code, 'с Восточная Долгокыча' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76632440104' as code, 'с Верхний Ононск' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76632450106' as code, 'с Верхняя Турга' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76632455109' as code, 'с Нижний Улятуй' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76632460106' as code, 'п Центральный Хада-Булак' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76634422106' as code, 'с Нижний Кулусутай' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76634430106' as code, 'с Заря' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76636156106' as code, 'с Новопавловское' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76636158106' as code, 'с Нижний Тарбагатай' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76638409104' as code, 'с Новая Бырка' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76638418106' as code, 'с Восточный Досатуй' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76638428106' as code, 'с Нижний Зоргол' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76638457111' as code, 'с Новое Погадаево' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76640435106' as code, 'с Нижняя Алия' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76640440116' as code, 'с Наринзор' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76640445121' as code, 'с Фирсово 1-е' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76647440126' as code, 'с Харагун-Саранка' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76647440131' as code, 'с Центральный Харагун' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76648405108' as code, 'с Алеур 1-й' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76648405110' as code, 'с Алеур 2-й' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76648410106' as code, 'с Сухой Байгул' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76648435116' as code, 'с Нижний Мильгидун' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76650460109' as code, 'с Заречное' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76650473116' as code, 'с Новое Сивяково' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76654415109' as code, 'с Нижнее Галкино' as name, 2 as razd from dual union all 
select to_date('01.05.2018','dd.mm.yyyy') as version, '76654440111' as code, 'с Нижнее Номоконово' as name, 2 as razd from dual union all 
select to_date('01.06.2017','dd.mm.yyyy') as version, '80602431109' as code, 'д Баландино' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '81603700' as code, 'Межселенные территории Баргузинского муниципального района' as name, 1 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '81603701' as code, 'Межселенные территории Баргузинского муниципального района, кроме территорий городского и сельских поселений' as name, 1 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '81606415116' as code, 'п Ципикан' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '81606415121' as code, 'п Верхний Ципикан' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '81606415126' as code, 'п Курорт Баунт' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '81606415131' as code, 'п Окунево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '81606415136' as code, 'п Баунт' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '81615700' as code, 'Межселенные территории Еравнинского муниципального района' as name, 1 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '81615701' as code, 'Межселенные территории Еравнинского муниципального района, кроме территорий сельских поселений' as name, 1 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '81635700' as code, 'Межселенные территории Муйского муниципального района' as name, 1 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '81635701' as code, 'Межселенные территории Муйского муниципального района, кроме территорий сельского и городских поселений' as name, 1 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '81645700' as code, 'Межселенные территории Северо-Байкальского муниципального района' as name, 1 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '81645701' as code, 'Межселенные территории Северо-Байкальского муниципального района, кроме территорий сельских и городских поселений' as name, 1 as razd from dual union all 
select to_date('01.09.2018','dd.mm.yyyy') as version, '82627435121' as code, 'с Дагестанское' as name, 2 as razd from dual union all 
select to_date('01.09.2018','dd.mm.yyyy') as version, '82647425105' as code, 'с Новый Мамрач' as name, 2 as razd from dual union all 
select to_date('01.09.2018','dd.mm.yyyy') as version, '82648436105' as code, 'с Хараг' as name, 2 as razd from dual union all 
select to_date('01.09.2018','dd.mm.yyyy') as version, '82656425111' as code, 'с Итля' as name, 2 as razd from dual union all 
select to_date('01.09.2018','dd.mm.yyyy') as version, '82656450111' as code, 'с Тадколо' as name, 2 as razd from dual union all 
select to_date('01.09.2018','dd.mm.yyyy') as version, '83625460112' as code, 'ж/д ст Шарданово' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87608470107' as code, 'п Ветью' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87608470108' as code, 'д Весляна' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87608470109' as code, 'д Евдино' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87640152109' as code, 'с Ёртом' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87640152114' as code, 'д Лязюв' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87640152126' as code, 'д Устьево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87640152131' as code, 'д Шиляево' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87640152136' as code, 'д Ыб' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87648475103' as code, 'с Аныб' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87648475111' as code, 'д Малый Аныб' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87648485102' as code, 'п Паспом' as name, 2 as razd from dual union all 
select to_date('01.01.2018','dd.mm.yyyy') as version, '87648485104' as code, 'с Носим' as name, 2 as razd from dual union all 
select to_date('01.04.2019','dd.mm.yyyy') as version, '88644455123' as code, 'д Нижний Осиял' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89603488106' as code, 'с Жабино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89603492106' as code, 'с Жаренки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89605415103' as code, 'с Арга' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89605415111' as code, 'д Чудинка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89607412111' as code, 'с Капасово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89607412116' as code, 'п Пашино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89610440111' as code, 'п Красная Поляна' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89610440116' as code, 'с Черная Промза' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89613415102' as code, 'с Барахманы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89613415103' as code, 'д Инелейка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89613415105' as code, 'с Новое Качаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89613415111' as code, 'д Растислаевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89616405104' as code, 'с Кайбичево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460107' as code, 'д Желтоноговские Выселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460108' as code, 'п Карасевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460109' as code, 'п Красная Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460110' as code, 'с Краснофлотец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460116' as code, 'с Лысая Гора' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460121' as code, 'п Михайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460126' as code, 'д Новоканьгушанские Выселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460131' as code, 'д Новопичингушанские Выселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460136' as code, 'с Новоусадские Выселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460141' as code, 'д Старопичингушанские Выселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89618460146' as code, 'с Старотештелимские Выселки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89621404104' as code, 'п Искра' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89621404116' as code, 'с Студенец' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89621404121' as code, 'п Тупик 9 км' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89621418103' as code, 'п Известь' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89621418105' as code, 'п.ст Свеженькая' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89629483103' as code, 'с Высокое' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89629483114' as code, 'с Покровск' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89631425102' as code, 'д Красная Зорька' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89631425104' as code, 'с Мурань' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89631425105' as code, 'с Новая Пырма' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89631425111' as code, 'д Старая Пырма' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89637420103' as code, 'д Анучино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89637420107' as code, 'д Воротники' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89637420110' as code, 'с Михайловка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89637420116' as code, 'д Романовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89637420121' as code, 'д Чапаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89640445103' as code, 'с Вырыпаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89640445108' as code, 'д Новотроицкая Горка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89640457114' as code, 'д Васильевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89640457121' as code, 'с Курмачкасы' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89640480131' as code, 'с Уришка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89646461102' as code, 'д Верхняя Верченка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89646461103' as code, 'с Ингенер-Пятина' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89646461105' as code, 'д Нижняя Верченка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89646481107' as code, 'п Восход' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89646481110' as code, 'п Оржевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89646481116' as code, 'п Ровный' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89646481121' as code, 'с Рязановка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89646481126' as code, 'с Шувары' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89649488108' as code, 'д Бегишево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89649488110' as code, 'д Дасаево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89649488112' as code, 'с Енгуразово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89649488113' as code, 'д Идеево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89649488114' as code, 'с Ишейки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89651465113' as code, 'п Березово' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89651465126' as code, 'д Красный Яр' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89651465131' as code, 'д Телимерки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89654430108' as code, 'с Мордовские Юнки' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89654430109' as code, 'с Моховая Рахманка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89654430121' as code, 'д Семеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89654475102' as code, 'д Аксеновка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89654475103' as code, 'д Гальчевка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89654475104' as code, 'д Зарубята' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89654475105' as code, 'с Лопатино' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89654475111' as code, 'д Шмидовка' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89657415103' as code, 'п Красный Воин' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89657415105' as code, 'с Мокшалей' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89657415108' as code, 'с Пянгелей' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89657420111' as code, 'с Малое Маресево' as name, 2 as razd from dual union all 
select to_date('01.01.2019','dd.mm.yyyy') as version, '89657420116' as code, 'д Малые Ремезенки' as name, 2 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '92633101' as code, 'город Кукмор' as name, 1 as razd from dual union all 
select to_date('01.07.2018','dd.mm.yyyy') as version, '92633101001' as code, 'г Кукмор' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '92633151001' as code, 'г Кукмор' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '92648435121' as code, 'п Барсил' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635405103' as code, 'д Атабаево' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635405104' as code, 'д Верхние Юри' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635405108' as code, 'д Каменный Ключ' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635405118' as code, 'д Привольный' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635410103' as code, 'ст Люга' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635410131' as code, 'Дома 1016 км' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635418113' as code, 'ст Керамик' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635418126' as code, 'с Черемушки' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635418131' as code, 'д Чумойтло' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635418136' as code, 'рзд Чумойтло' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635418141' as code, 'Дома 1035 км' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635418146' as code, 'Дома 1038 км' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435102' as code, 'д Александрово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435103' as code, 'с Биляр' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435104' as code, 'д Большие Сибы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435105' as code, 'д Замостные Какси' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435111' as code, 'д Малые Сибы' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435116' as code, 'д Новопольск' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435121' as code, 'д Новые Какси' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435126' as code, 'д Новые Юбери' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435131' as code, 'д Почешур' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435136' as code, 'д Трактор' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435141' as code, 'д Санниково' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435146' as code, 'д Старые Какси' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435151' as code, 'д Старые Юбери' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94635435156' as code, 'д Юдрук' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94637425108' as code, 'д Лысово' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94637425109' as code, 'д Оленье Болото' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94637425116' as code, 'д Степной' as name, 2 as razd from dual union all 
select to_date('01.03.2018','dd.mm.yyyy') as version, '94637440106' as code, 'д Соколовка' as name, 2 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '96610430' as code, 'Ново-Бенойское' as name, 1 as razd from dual union all 
select to_date('01.03.2017','dd.mm.yyyy') as version, '96610430101' as code, 'с Новый Беной' as name, 2 as razd from dual
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