CREATE TABLE data_cell (
  row_id NUMBER(18) NOT NULL,
  column_id NUMBER(9) NOT NULL,
  svalue VARCHAR2(2000 CHAR),
  nvalue DECIMAL(27, 10),
  dvalue DATE,
  style_id NUMBER(9),
  editable NUMBER(1) DEFAULT 0,
  colspan NUMBER(3),
  rowspan NUMBER(3)
);

COMMENT ON TABLE data_cell IS 'Значения налоговых форм типа дата';
COMMENT ON COLUMN data_cell.column_id IS 'Идентификатор столбца';
COMMENT ON COLUMN data_cell.row_id IS 'Идентификатор строки';
COMMENT ON COLUMN data_cell.svalue IS 'Строковое значение';
COMMENT ON COLUMN data_cell.nvalue IS 'Числовое значение (в том числе и для ссылок)';
COMMENT ON COLUMN data_cell.dvalue IS 'Значение для даты-времени';
COMMENT ON COLUMN data_cell.style_id IS 'Идентификатор стиля ячейки';
COMMENT ON COLUMN data_cell.editable IS 'Признак редактируемости ячейки (0 - только чтение, 1 - доступна на запись)';
COMMENT ON COLUMN data_cell.colspan IS 'Количество объединяемых по горизонтали ячеек';
COMMENT ON COLUMN data_cell.rowspan IS 'Количество объединяемых по вертикали ячеек';

ALTER TABLE data_cell ADD CONSTRAINT data_cell_pk PRIMARY KEY (row_id, column_id);
ALTER TABLE data_cell ADD CONSTRAINT data_cell_fk_column_id FOREIGN KEY (column_id) REFERENCES form_column(id);
ALTER TABLE data_cell ADD CONSTRAINT data_cell_fk_data_row FOREIGN KEY (row_id) REFERENCES data_row(id) ON DELETE CASCADE;
ALTER TABLE data_cell ADD CONSTRAINT data_cell_chk_editable CHECK (editable IN (0, 1));

--предварительная очистка таблицы
DELETE FROM data_cell;
INSERT INTO data_cell (row_id, column_id, svalue, nvalue, dvalue, style_id, editable, colspan, rowspan) 
(SELECT row_id, column_id, MAX(sv) svalue, MAX(nv) nvalue, MAX(dv) dvalue, MAX(style_id) style_id, MAX(edit) editable, MAX(colspan) colspan, MAX(rowspan) rowspan FROM
(SELECT column_id, row_id, style_id, NULL as edit, NULL as colspan, NULL as rowspan, NULL as nv, NULL as dv, NULL as sv
        FROM cell_style n join data_row rr on rr.id = n.row_id
        UNION ALL
        SELECT column_id, row_id,NULL, 1 as edit, NULL, NULL, NULL, NULL, NULL
        FROM cell_editable n join data_row rr on rr.id = n.row_id
        UNION ALL
        SELECT column_id, row_id, NULL, NULL, colspan, rowspan, NULL, NULL, NULL
        FROM cell_span_info n join data_row rr on rr.id = n.row_id
        UNION ALL
        SELECT column_id, row_id, NULL, NULL, NULL, NULL, value, NULL, NULL
        FROM numeric_value n join data_row rr on rr.id = n.row_id
        UNION ALL
        SELECT column_id, row_id, NULL, NULL, NULL, NULL, NULL, value, NULL
        FROM date_value n join data_row rr on rr.id = n.row_id
        UNION ALL
        SELECT column_id, row_id, NULL, NULL, NULL, NULL, NULL, NULL, value
        FROM string_value n join data_row rr on rr.id = n.row_id)
GROUP BY column_id, row_id);
--чистка пустых ячеек
DELETE FROM data_cell WHERE svalue IS NULL AND dvalue IS NULL AND nvalue IS NULL AND style_id IS NULL AND colspan IS NULL AND rowspan IS NULL AND editable IS NULL;

DROP TABLE string_value;
DROP TABLE numeric_value;
DROP TABLE date_value;
DROP TABLE cell_span_info;
DROP TABLE cell_editable;
DROP TABLE cell_style;

COMMIT;
EXIT;