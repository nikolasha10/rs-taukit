do $$
DECLARE
    l_claim_id integer;
    l_member_id integer;
BEGIN
    select id from credit.credit_claims where number='${номер заявки}' into l_claim_id;
    select id from credit.members where credit_claim_id=l_claim_id into l_member_id;
    select id from credit.bank_decisions where credit_claim_id = l_claim_id
    DELETE FROM credit.income_records where income_document_id = l_member_id;
    DELETE FROM credit.contacts WHERE member_id = l_member_id;
    DELETE FROM credit.jobs WHERE member_id = l_member_id;
    DELETE FROM credit.visual_questions WHERE member_id = l_member_id;
    DELETE FROM credit.insurance_data WHERE member_id = l_member_id;
    DELETE FROM credit.bank_decisions_history WHERE credit_claim_id = l_claim_id;
    DELETE FROM credit.member_income_documents WHERE member_id = l_member_id;
    DELETE FROM credit.members WHERE credit_claim_id = l_claim_id;
    DELETE FROM credit.etl_fct_application_dop WHERE credit_claim_id = l_claim_id;
    DELETE FROM credit.credit_claim_statuses_history WHERE credit_claim_id = l_claim_id;
    DELETE FROM credit.integration_exchange WHERE claim_id = l_claim_id;
    DELETE FROM credit.abs_credit_card_claims WHERE credit_claim_id = l_claim_id;
    DELETE FROM credit.insurance_data WHERE member_id = l_member_id;
    DELETE FROM credit.jobs WHERE member_id = l_member_id;
    DELETE FROM credit.visual_questions WHERE member_id = l_member_id;
    DELETE FROM credit.contacts WHERE member_id = l_member_id;
    DELETE FROM credit.attach_documents WHERE member_id = l_member_id;
    DELETE FROM credit.salary_receipts WHERE member_id = l_member_id;
    DELETE FROM credit.attach_documents WHERE credit_claim_id = l_claim_id;
    DELETE FROM credit.credit_options WHERE bank_decision_id in (SELECT id FROM credit.bank_decisions WHERE credit_claim_id = l_claim_id);
    DELETE FROM credit.bank_decisions WHERE credit_claim_id = l_claim_id;
	DELETE FROM credit.bank_decisions_history WHERE credit_claim_id = l_claim_id;
    DELETE FROM credit.members WHERE credit_claim_id = l_claim_id;
    DELETE FROM credit.solution WHERE credit_claim_id = l_claim_id;
	DELETE FROM credit.integration_exchange WHERE claim_id = l_claim_id;
	DELETE FROM credit.skfo_data WHERE id in (SELECT skfo_data_id FROM credit.members WHERE credit_claim_id = l_member_id);
    DELETE FROM credit.etl_fct_application_dop WHERE credit_claim_id = l_claim_id;
    DELETE FROM credit.credit_claims WHERE id = l_claim_id;
    DELETE FROM credit.preliminary_credit_parameters WHERE credit_claim_id = l_claim_id;
END;
$$