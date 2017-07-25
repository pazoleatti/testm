--> Добавления поля id в таблицу REF_BOOK_CALENDAR (SBRFNDFL-1585)
ALTER TABLE ref_book_calendar ADD id NUMBER(18);
CREATE UNIQUE INDEX i_ref_book_calendar_id ON ref_book_calendar(id);
UPDATE ref_book_calendar SET id = rownum;
    
