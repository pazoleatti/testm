DECLARE
  v_id NUMBER(19,0);
  v_type_id NUMBER(19,0);
  v_count NUMBER;
BEGIN
  FOR rec IN (
  SELECT '1530' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL
  UNION ALL
  SELECT '1531' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL 
  UNION ALL
  SELECT '1533' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL
  UNION ALL
  SELECT '1535' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL  
  UNION ALL
  SELECT '1536' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL
  UNION ALL
  SELECT '1537' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL  
  UNION ALL
  SELECT '1539' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL   
  UNION ALL
  SELECT '1541' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL   
  UNION ALL
  SELECT '1542' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL   
  UNION ALL
  SELECT '1543' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL
  UNION ALL
  SELECT '1544' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL  
  UNION ALL
  SELECT '1545' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL  
  UNION ALL
  SELECT '1546' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL    
  UNION ALL
  SELECT '1547' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL   
  UNION ALL
  SELECT '1548' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL 
  UNION ALL
  SELECT '1549' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL         
  UNION ALL
  SELECT '1551' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL   
  UNION ALL
  SELECT '1552' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL 
  UNION ALL
  SELECT '1553' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL 
  UNION ALL
  SELECT '1554' AS code, '01' AS mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' AS name FROM DUAL 
  UNION ALL
  SELECT '1530' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL
  UNION ALL
  SELECT '1531' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL 
  UNION ALL
  SELECT '1533' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL
  UNION ALL
  SELECT '1535' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL  
  UNION ALL
  SELECT '1536' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL
  UNION ALL
  SELECT '1537' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL  
  UNION ALL
  SELECT '1539' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL   
  UNION ALL
  SELECT '1541' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL   
  UNION ALL
  SELECT '1542' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL   
  UNION ALL
  SELECT '1543' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL
  UNION ALL
  SELECT '1544' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL  
  UNION ALL
  SELECT '1545' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL  
  UNION ALL
  SELECT '1546' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL    
  UNION ALL
  SELECT '1547' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL   
  UNION ALL
  SELECT '1548' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL 
  UNION ALL
  SELECT '1549' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL         
  UNION ALL
  SELECT '1551' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL   
  UNION ALL
  SELECT '1552' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL 
  UNION ALL
  SELECT '1553' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL 
  UNION ALL
  SELECT '1554' AS code, '02' AS mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' AS name FROM DUAL 
  UNION ALL
  SELECT '1530' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL
  UNION ALL
  SELECT '1531' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL 
  UNION ALL
  SELECT '1533' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL
  UNION ALL
  SELECT '1535' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL  
  UNION ALL
  SELECT '1536' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL
  UNION ALL
  SELECT '1537' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL  
  UNION ALL
  SELECT '1539' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL   
  UNION ALL
  SELECT '1541' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL   
  UNION ALL
  SELECT '1542' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL   
  UNION ALL
  SELECT '1543' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL
  UNION ALL
  SELECT '1544' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL  
  UNION ALL
  SELECT '1545' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL  
  UNION ALL
  SELECT '1546' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL    
  UNION ALL
  SELECT '1547' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL   
  UNION ALL
  SELECT '1548' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL 
  UNION ALL
  SELECT '1549' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL         
  UNION ALL
  SELECT '1551' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL   
  UNION ALL
  SELECT '1552' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL 
  UNION ALL
  SELECT '1553' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL 
  UNION ALL
  SELECT '1554' AS code, '03' AS mark, 'Начисление дохода при расторжении договора брокерского обслуживания' AS name FROM DUAL 
  UNION ALL
  SELECT '1530' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL
  UNION ALL
  SELECT '1531' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL 
  UNION ALL
  SELECT '1533' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL
  UNION ALL
  SELECT '1535' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL  
  UNION ALL
  SELECT '1536' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL
  UNION ALL
  SELECT '1537' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL  
  UNION ALL
  SELECT '1539' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL   
  UNION ALL
  SELECT '1541' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL   
  UNION ALL
  SELECT '1542' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL   
  UNION ALL
  SELECT '1543' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL
  UNION ALL
  SELECT '1544' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL  
  UNION ALL
  SELECT '1545' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL  
  UNION ALL
  SELECT '1546' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL    
  UNION ALL
  SELECT '1547' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL   
  UNION ALL
  SELECT '1548' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL 
  UNION ALL
  SELECT '1549' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL         
  UNION ALL
  SELECT '1551' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL   
  UNION ALL
  SELECT '1552' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL 
  UNION ALL
  SELECT '1553' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL 
  UNION ALL
  SELECT '1554' AS code, '04' AS mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' AS name FROM DUAL 
  UNION ALL
  SELECT '2000' AS code, '05' AS mark, 'Заработная плата' AS name FROM DUAL 
  UNION ALL
  SELECT '2000' AS code, '06' AS mark, 'Заработная плата при увольнении' AS name FROM DUAL 
  UNION ALL
  SELECT '2000' AS code, '07' AS mark, 'Разовая премия' AS name FROM DUAL 
  UNION ALL
  SELECT '2000' AS code, '08' AS mark, 'Ежемесячная премия' AS name FROM DUAL 
  UNION ALL
  SELECT '2000' AS code, '09' AS mark, 'Ежеквартальная премия' AS name FROM DUAL 
  UNION ALL
  SELECT '2000' AS code, '10' AS mark, 'Премия по итогам года' AS name FROM DUAL 
  UNION ALL
  SELECT '2002' AS code, '07' AS mark, 'Разовая премия' AS name FROM DUAL 
  UNION ALL
  SELECT '2002' AS code, '08' AS mark, 'Ежемесячная премия' AS name FROM DUAL 
  UNION ALL
  SELECT '2002' AS code, '09' AS mark, 'Ежеквартальная премия' AS name FROM DUAL 
  UNION ALL
  SELECT '2002' AS code, '10' AS mark, 'Премия по итогам года' AS name FROM DUAL 
  UNION ALL
  SELECT '2000' AS code, '11' AS mark, 'Возмещение командировочных расходов' AS name FROM DUAL 
  UNION ALL
  SELECT '2000' AS code, '12' AS mark, 'Доплата до среднего заработка за время нахождения работника в командировке' AS name FROM DUAL 
  UNION ALL
  SELECT '2003' AS code, '13' AS mark, 'Суммы вознаграждений, выплачиваемых за счет средств прибыли организации, средств специального назначения или целевых поступлений' AS name FROM DUAL
  UNION ALL
  SELECT '2740' AS code, '13' AS mark, 'Выплата дохода в денежной форме' AS name FROM DUAL  
  UNION ALL
  SELECT '2750' AS code, '13' AS mark, 'Выплата дохода в денежной форме' AS name FROM DUAL  
  UNION ALL
  SELECT '2790' AS code, '13' AS mark, 'Выплата дохода в денежной форме' AS name FROM DUAL  
  UNION ALL
  SELECT '4800' AS code, '13' AS mark, 'Выплата дохода в денежной форме' AS name FROM DUAL  
  UNION ALL
  SELECT '2520' AS code, '14' AS mark, 'Выплата дохода в натуральной форме' AS name FROM DUAL
  UNION ALL
  SELECT '2720' AS code, '14' AS mark, 'Выплата дохода в натуральной форме' AS name FROM DUAL
  UNION ALL
  SELECT '2740' AS code, '14' AS mark, 'Выплата дохода в натуральной форме' AS name FROM DUAL
  UNION ALL
  SELECT '2750' AS code, '14' AS mark, 'Выплата дохода в натуральной форме' AS name FROM DUAL
  UNION ALL
  SELECT '2790' AS code, '14' AS mark, 'Выплата дохода в натуральной форме' AS name FROM DUAL
  UNION ALL
  SELECT '4800' AS code, '14' AS mark, 'Выплата дохода в натуральной форме' AS name FROM DUAL
    )
  LOOP
    SELECT MAX(id) INTO v_type_id FROM REF_BOOK_INCOME_TYPE WHERE CODE=rec.code AND STATUS=0;
    SELECT COUNT(1) INTO v_count FROM REF_BOOK_INCOME_KIND WHERE INCOME_TYPE_ID=v_type_id AND MARK=rec.mark;
    IF v_count>0 THEN
      UPDATE REF_BOOK_INCOME_KIND SET NAME=rec.name WHERE INCOME_TYPE_ID=v_type_id AND MARK=rec.mark;
    ELSE
      v_id:=SEQ_REF_BOOK_RECORD.NEXTVAL;
      INSERT INTO REF_BOOK_INCOME_KIND(ID, INCOME_TYPE_ID, MARK, NAME)
      VALUES(v_id, v_type_id, rec.mark, rec.name);
    END IF;
  END LOOP;
END;
/