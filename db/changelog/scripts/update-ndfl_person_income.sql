DECLARE
  v_cnt_oper_date NUMBER := 0;
  v_cnt_act_date NUMBER := 0;
  v_cnt_row_type NUMBER := 0;
BEGIN
  FOR c IN ( 
            select 
            ndfl_person_income_id, 
            operation_date, new_operation_date, 
            action_date, new_action_date, 
            row_type, new_row_type
            from (
            select
            ndfl_person_income_id, operation_date, action_date, row_type, 
            nvl(min_income_accrued_date, nvl(min_income_payout_date, min_payment_date)) new_operation_date,
            nvl(tax_date, payment_date) new_action_date,
            case 
              when income_accrued_date is not null then 100
              when income_payout_date is not null then 200
              else 300
            end new_row_type
            from (
            select
            p.declaration_data_id, npi.operation_id, p.inp,
            npi.id as ndfl_person_income_id,
            npi.operation_date,
            min(npi.income_accrued_date) over (partition by dd.id, npi.operation_id, p.inp) min_income_accrued_date,
            min(npi.income_payout_date) over (partition by dd.id, npi.operation_id, p.inp) min_income_payout_date,
            min(npi.payment_date) over (partition by dd.id, npi.operation_id, p.inp) min_payment_date,
            npi.income_accrued_date,
            npi.income_payout_date,
            npi.payment_date,
            npi.action_date, 
            npi.tax_date,
            npi.row_type
            from
            ndfl_person_income npi
            join ndfl_person p on (p.id = npi.ndfl_person_id)
            join declaration_data dd on (dd.id = p.declaration_data_id)
            )) 
           )
  LOOP
    IF c.operation_date is null and c.new_operation_date is not null THEN
      update ndfl_person_income
      set operation_date = c.new_operation_date
      where id = c.ndfl_person_income_id;
      v_cnt_oper_date := v_cnt_oper_date + 1;
    END IF;
    IF c.action_date is null and c.new_action_date is not null THEN
      update ndfl_person_income
      set action_date = c.new_action_date
      where id = c.ndfl_person_income_id;
      v_cnt_act_date := v_cnt_act_date + 1;
    END IF;
    IF c.row_type is null and c.new_row_type is not null THEN
      update ndfl_person_income
      set row_type = c.new_row_type
      where id = c.ndfl_person_income_id;
      v_cnt_row_type := v_cnt_row_type + 1;
    END IF;
  END LOOP; 

  COMMIT;
  
EXCEPTION
  WHEN others THEN
    ROLLBACK;
END;
