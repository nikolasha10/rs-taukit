do $$
DECLARE
    clientID integer;
BEGIN
    FOR clientID IN (select id from esb_stub_main.clients where surname='${фамилия}' and firstname='${имя}') LOOP
        PERFORM esb_stub_main.drop_client(clientID);
    END LOOP;
END;
$$