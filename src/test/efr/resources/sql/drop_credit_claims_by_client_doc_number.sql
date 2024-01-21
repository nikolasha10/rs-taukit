do $$
DECLARE
    docNumber text := '${номер паспорта}';
BEGIN
    DELETE FROM credit.refinancing_credit_bank_decision_items WHERE parameter_id IN (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.income_records where income_document_id in (SELECT id FROM credit.member_income_documents WHERE member_id in (select id FROM credit.members where document_number = docNumber));
    DELETE FROM credit.contacts WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.jobs WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.visual_questions WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.insurance_data WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.bank_decisions_history WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.member_income_documents WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.etl_fct_application_dop WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.credit_claim_statuses_history WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.integration_exchange WHERE claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.abs_credit_card_claims WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.insurance_data WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.jobs WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.visual_questions WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.contacts WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.attach_documents WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.salary_receipts WHERE member_id in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.attach_documents WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.credit_options WHERE bank_decision_id in (SELECT id FROM credit.bank_decisions WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber));
    DELETE FROM credit.bank_decisions WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
	DELETE FROM credit.bank_decisions_history WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.solution WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
	DELETE FROM credit.integration_exchange WHERE claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
	DELETE FROM credit.skfo_data WHERE id in (SELECT skfo_data_id FROM credit.members WHERE credit_claim_id in (select id FROM credit.members where document_number = docNumber));
    DELETE FROM credit.etl_fct_application_dop WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.credit_claims WHERE id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.preliminary_credit_parameters WHERE credit_claim_id in (select credit_claim_id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.send_to_kkr_queue WHERE record_key in (select id FROM credit.members where document_number = docNumber);
    DELETE FROM credit.members WHERE document_number = docNumber;
END;
$$