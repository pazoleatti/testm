begin

MERGE INTO ref_book_calendar a USING
(
select to_date('01.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('02.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('03.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('04.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('05.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('06.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('07.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('08.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('09.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('10.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('11.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('13.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('14.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('15.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('16.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('17.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('18.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('20.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('21.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('22.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('23.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('24.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('25.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('27.01.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('28.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('29.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('30.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('31.01.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('01.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('02.02.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('03.02.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('04.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('05.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('06.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('07.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('08.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('09.02.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('10.02.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('11.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('13.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('14.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('15.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('16.02.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('17.02.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('18.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('20.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('21.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('22.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('23.02.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('24.02.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('25.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('27.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('28.02.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('01.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('02.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('03.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('04.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('05.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('06.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('07.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('08.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('09.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('10.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('11.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('13.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('14.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('15.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('16.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('17.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('18.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('20.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('21.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('22.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('23.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('24.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('25.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('27.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('28.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('29.03.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('30.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('31.03.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('01.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('02.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('03.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('04.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('05.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('06.04.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('07.04.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('08.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('09.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('10.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('11.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('13.04.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('14.04.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('15.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('16.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('17.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('18.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('20.04.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('21.04.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('22.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('23.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('24.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('25.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('27.04.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('28.04.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('29.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('30.04.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('01.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('02.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('03.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('04.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('05.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('06.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('07.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('08.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('09.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('10.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('11.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('12.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('13.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('14.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('15.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('16.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('17.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('18.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('19.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('20.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('21.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('22.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('23.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('24.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('25.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('26.05.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('27.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('28.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('29.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('30.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('31.05.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('01.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('02.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('03.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('04.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('05.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('06.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('07.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('08.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('09.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('10.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('11.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('13.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('14.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('15.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('16.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('17.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('18.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('20.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('21.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('22.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('23.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('24.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('25.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('27.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('28.06.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('29.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('30.06.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('01.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('02.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('03.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('04.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('05.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('06.07.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('07.07.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('08.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('09.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('10.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('11.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('13.07.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('14.07.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('15.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('16.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('17.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('18.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('20.07.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('21.07.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('22.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('23.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('24.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('25.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('27.07.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('28.07.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('29.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('30.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('31.07.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('01.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('02.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('03.08.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('04.08.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('05.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('06.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('07.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('08.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('09.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('10.08.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('11.08.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('12.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('13.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('14.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('15.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('16.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('17.08.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('18.08.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('19.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('20.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('21.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('22.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('23.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('24.08.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('25.08.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('26.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('27.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('28.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('29.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('30.08.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('31.08.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('01.09.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('02.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('03.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('04.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('05.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('06.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('07.09.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('08.09.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('09.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('10.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('11.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('13.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('14.09.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('15.09.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('16.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('17.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('18.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('20.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('21.09.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('22.09.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('23.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('24.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('25.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('27.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('28.09.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('29.09.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('30.09.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('01.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('02.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('03.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('04.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('05.10.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('06.10.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('07.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('08.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('09.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('10.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('11.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.10.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('13.10.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('14.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('15.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('16.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('17.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('18.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.10.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('20.10.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('21.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('22.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('23.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('24.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('25.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.10.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('27.10.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('28.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('29.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('30.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('31.10.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('01.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('02.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('03.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('04.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('05.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('06.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('07.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('08.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('09.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('10.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('11.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('13.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('14.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('15.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('16.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('17.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('18.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('20.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('21.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('22.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('23.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('24.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('25.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('27.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('28.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('29.11.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('30.11.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('01.12.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('02.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('03.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('04.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('05.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('06.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('07.12.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('08.12.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('09.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('10.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('11.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('12.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('13.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('14.12.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('15.12.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('16.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('17.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('18.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('19.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('20.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('21.12.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('22.12.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('23.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('24.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('25.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('26.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('27.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('28.12.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('29.12.2019','dd.mm.yyyy') AS cdate, 1 AS ctype FROM DUAL 
UNION ALL
select to_date('30.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
UNION ALL
select to_date('31.12.2019','dd.mm.yyyy') AS cdate, 0 AS ctype FROM DUAL 
) b
ON (a.cdate=b.cdate)
when NOT MATCHED THEN
INSERT (cdate, ctype)
VALUES (b.cdate, b.ctype);

merge into ref_book_calendar a 
using (select cdate,ctype,id, row_number() over (ORDER by id, cdate) as rn from ref_book_calendar) b on (a.cdate=b.cdate)
when matched then update set a.id=b.rn;

commit;
end;
/