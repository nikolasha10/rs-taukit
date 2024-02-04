#language: ru
Функционал: КЗ через сайт. Короткая анкета

  Сценарий: Приём заявки
    * отправить POST-запрос "CreateApplicationShortEFR" с параметрами:
      | Параметр                  | Значение                                                  |
      | CanalSales                | ${CreateApplicationShortEFR} -> CanalSales                |
      | CurrentDate               | ${CreateApplicationShortEFR} -> CurrentDate               |
      | Summa                     | ${CreateApplicationShortEFR} -> Summa                     |
      | PeriodLoan                | ${CreateApplicationShortEFR} -> PeriodLoan                |
      | LoanTarget                | ${CreateApplicationShortEFR} -> LoanTarget                |
      | Direction                 | ${CreateApplicationShortEFR} -> Direction                 |
      | IncomeMedium              | ${CreateApplicationShortEFR} -> IncomeMedium              |
      | ProductCode               | ${CreateApplicationShortEFR} -> ProductCode               |
      | CurrencyCode              | ${CreateApplicationShortEFR} -> CurrencyCode              |
      | BranchId                  | ${CreateApplicationShortEFR} -> BranchId                  |
      | LoanId                    | ${CreateApplicationShortEFR} -> LoanId                    |
      | ClientType                | ${CreateApplicationShortEFR} -> ClientType                |
      | IsCheckBKI                | ${CreateApplicationShortEFR} -> IsCheckBKI                |
      | IsConsentAssignmentClaims | ${CreateApplicationShortEFR} -> IsConsentAssignmentClaims |
      | IsProcessPersonalData     | ${CreateApplicationShortEFR} -> IsProcessPersonalData     |
      | IsRegistrationLocation    | ${CreateApplicationShortEFR} -> IsRegistrationLocation    |
      | IsReliableCustomer        | ${CreateApplicationShortEFR} -> IsReliableCustomer        |
      | IsBudgetOrgEmployee       | ${CreateApplicationShortEFR} -> IsBudgetOrgEmployee       |
      | IsSalaryRSHB              | ${CreateApplicationShortEFR} -> IsSalaryRSHB              |
      | IsPensionRSHB             | ${CreateApplicationShortEFR} -> IsPensionRSHB             |
      | ConsentBKIDate            | ${CreateApplicationShortEFR} -> ConsentBKIDate            |
      | IsCheckScore              | ${CreateApplicationShortEFR} -> IsCheckScore              |
      | IsConsentSMSBKI           | ${CreateApplicationShortEFR} -> IsConsentSMSBKI           |
      | BKIHashCode               | ${CreateApplicationShortEFR} -> BKIHashCode               |
      | BKIConsentPeriod          | ${CreateApplicationShortEFR} -> BKIConsentPeriod          |
      | ConsentUserType           | ${CreateApplicationShortEFR} -> ConsentUserType           |
      | StockCode                 | ${CreateApplicationShortEFR} -> StockCode                 |
      | DocumentType              | Паспорт                                                   |
      | docSeries                 | ${Паспорт} -> серия                                       |
      | docNumber                 | ${Паспорт} -> номер                                       |
      | Issued                    | ${Паспорт} -> кем выдан                                   |
      | IssueDate                 | ${Паспорт} -> дата выдачи                                 |
      | SubdivisionCode           | ${Паспорт} -> код подразделения                           |
      | clientLastName            | ${Клиент} -> фамилия                                      |
      | clientFirstName           | ${Клиент} -> имя                                          |
      | clientMiddleName          | ${Клиент} -> отчество                                     |
      | clientBirthDate           | ${Клиент} -> дата рождения                                |
      | clientBirthPlace          | ${Клиент} -> место рождения                               |
      | clientPhone               | ${Клиент} -> телефон                                      |
      | clientEMail               | ${Клиент} -> e-mail                                       |
      | clientRegDate             | ${Клиент} -> дата регистрации                             |
      | clientGender              | ${Клиент} -> пол                                          |
      | residenceIndex            | ${Адрес регистрации} -> индекс                            |
      | residenceDistrict         | ${Адрес регистрации} -> район                             |
      | residenceCity             | ${Адрес регистрации} -> город                             |
      | residenceLocality         | ${Адрес регистрации} -> населенный пункт                  |
      | residenceStreet           | ${Адрес регистрации} -> улица                             |
      | residenceHouse            | ${Адрес регистрации} -> номер дома                        |
      | residenceBuilding         | ${Адрес регистрации} -> строение                          |
      | residenceCorps            | ${Адрес регистрации} -> корпус                            |
      | residenceApartment        | ${Адрес регистрации} -> номер квартиры                    |
      | residenceRegionCode       | ${Адрес регистрации} -> код региона                       |
      | realIndex                 | ${Адрес регистрации} -> индекс                            |
      | realDistrict              | ${Адрес регистрации} -> район                             |
      | realCity                  | ${Адрес регистрации} -> город                             |
      | realLocality              | ${Адрес регистрации} -> населенный пункт                  |
      | realStreet                | ${Адрес регистрации} -> улица                             |
      | realHouse                 | ${Адрес регистрации} -> номер дома                        |
      | realBuilding              | ${Адрес регистрации} -> строение                          |
      | realCorps                 | ${Адрес регистрации} -> корпус                            |
      | realApartment             | ${Адрес регистрации} -> номер квартиры                    |
      | realRegionCode            | ${Адрес регистрации} -> код региона                       |
    * разобрать ответ запроса "CreateApplicationShortEFR", сохранив значения в параметрах:
      | Параметр             | Регулярное выражение                     |
      | Номер заявки         | LoanId.+?ObjectId>(.+?)<.+ObjectId>      |
      | Идентификатор заявки | ExternalId.+?ObjectId>(\d+?)<.+ObjectId> |
    * отправить POST-запрос "creditCalcEFR" с параметрами:
      | Параметр     | Значение        |
      | номер заявки | ${Номер заявки} |
    * выполнить сценарий "Кредитная заявка -> Ожидание обработки заявки" с параметрами:
      | Параметр             | Значение                                                                                     |
      | Регулярное выражение | edit-claim-data.+?preliminary-limit-calc.+?(Предварительный расчет лимита).+?edit-claim-data |
      | Название параметра   | Ожидаемое значение                                                                           |
      | Ожидаемый текст      | Предварительный расчет лимита                                                                |
    * отправить POST-запрос "creditCalcEFR" с параметрами:
      | Параметр     | Значение        |
      | номер заявки | ${Номер заявки} |
    * выполнить сценарий "Кредитная заявка -> Ожидание обработки заявки" с параметрами:
      | Параметр           | Значение                                     |
      | Название параметра | Результат запроса 'История кредитной заявки' |
      | Ожидаемый текст    | Расчетная сумма кредита равна 50000          |

  Сценарий: Приём полной анкеты
    * отправить POST-запрос "CreateApplicationShortEFR" с параметрами:
      | Параметр                  | Значение                                                  |
      | CanalSales                | ${CreateApplicationShortEFR} -> CanalSales                |
      | CurrentDate               | ${CreateApplicationShortEFR} -> CurrentDate               |
      | Summa                     | ${CreateApplicationShortEFR} -> Summa                     |
      | PeriodLoan                | ${CreateApplicationShortEFR} -> PeriodLoan                |
      | LoanTarget                | ${CreateApplicationShortEFR} -> LoanTarget                |
      | Direction                 | ${CreateApplicationShortEFR} -> Direction                 |
      | IncomeMedium              | ${CreateApplicationShortEFR} -> IncomeMedium              |
      | ProductCode               | ${CreateApplicationShortEFR} -> ProductCode               |
      | CurrencyCode              | ${CreateApplicationShortEFR} -> CurrencyCode              |
      | BranchId                  | ${CreateApplicationShortEFR} -> BranchId                  |
      | LoanId                    | ${CreateApplicationShortEFR} -> LoanId                    |
      | ClientType                | ${CreateApplicationShortEFR} -> ClientType                |
      | IsCheckBKI                | ${CreateApplicationShortEFR} -> IsCheckBKI                |
      | IsConsentAssignmentClaims | ${CreateApplicationShortEFR} -> IsConsentAssignmentClaims |
      | IsProcessPersonalData     | ${CreateApplicationShortEFR} -> IsProcessPersonalData     |
      | IsRegistrationLocation    | ${CreateApplicationShortEFR} -> IsRegistrationLocation    |
      | IsReliableCustomer        | ${CreateApplicationShortEFR} -> IsReliableCustomer        |
      | IsBudgetOrgEmployee       | ${CreateApplicationShortEFR} -> IsBudgetOrgEmployee       |
      | IsSalaryRSHB              | ${CreateApplicationShortEFR} -> IsSalaryRSHB              |
      | IsPensionRSHB             | ${CreateApplicationShortEFR} -> IsPensionRSHB             |
      | ConsentBKIDate            | ${CreateApplicationShortEFR} -> ConsentBKIDate            |
      | IsCheckScore              | ${CreateApplicationShortEFR} -> IsCheckScore              |
      | IsConsentSMSBKI           | ${CreateApplicationShortEFR} -> IsConsentSMSBKI           |
      | BKIHashCode               | ${CreateApplicationShortEFR} -> BKIHashCode               |
      | BKIConsentPeriod          | ${CreateApplicationShortEFR} -> BKIConsentPeriod          |
      | ConsentUserType           | ${CreateApplicationShortEFR} -> ConsentUserType           |
      | StockCode                 | ${CreateApplicationShortEFR} -> StockCode                 |
      | DocumentType              | Паспорт                                                   |
      | docSeries                 | ${Паспорт} -> серия                                       |
      | docNumber                 | ${Паспорт} -> номер                                       |
      | Issued                    | ${Паспорт} -> кем выдан                                   |
      | IssueDate                 | ${Паспорт} -> дата выдачи                                 |
      | SubdivisionCode           | ${Паспорт} -> код подразделения                           |
      | clientLastName            | ${Клиент} -> фамилия                                      |
      | clientFirstName           | ${Клиент} -> имя                                          |
      | clientMiddleName          | ${Клиент} -> отчество                                     |
      | clientBirthDate           | ${Клиент} -> дата рождения                                |
      | clientBirthPlace          | ${Клиент} -> место рождения                               |
      | clientPhone               | ${Клиент} -> телефон                                      |
      | clientEMail               | ${Клиент} -> e-mail                                       |
      | clientRegDate             | ${Клиент} -> дата регистрации                             |
      | clientGender              | ${Клиент} -> пол                                          |
      | residenceIndex            | ${Адрес регистрации} -> индекс                            |
      | residenceDistrict         | ${Адрес регистрации} -> район                             |
      | residenceCity             | ${Адрес регистрации} -> город                             |
      | residenceLocality         | ${Адрес регистрации} -> населенный пункт                  |
      | residenceStreet           | ${Адрес регистрации} -> улица                             |
      | residenceHouse            | ${Адрес регистрации} -> номер дома                        |
      | residenceBuilding         | ${Адрес регистрации} -> строение                          |
      | residenceCorps            | ${Адрес регистрации} -> корпус                            |
      | residenceApartment        | ${Адрес регистрации} -> номер квартиры                    |
      | residenceRegionCode       | ${Адрес регистрации} -> код региона                       |
      | realIndex                 | ${Адрес регистрации} -> индекс                            |
      | realDistrict              | ${Адрес регистрации} -> район                             |
      | realCity                  | ${Адрес регистрации} -> город                             |
      | realLocality              | ${Адрес регистрации} -> населенный пункт                  |
      | realStreet                | ${Адрес регистрации} -> улица                             |
      | realHouse                 | ${Адрес регистрации} -> номер дома                        |
      | realBuilding              | ${Адрес регистрации} -> строение                          |
      | realCorps                 | ${Адрес регистрации} -> корпус                            |
      | realApartment             | ${Адрес регистрации} -> номер квартиры                    |
      | realRegionCode            | ${Адрес регистрации} -> код региона                       |
    * разобрать ответ запроса "CreateApplicationShortEFR", сохранив значения в параметрах:
      | Параметр             | Регулярное выражение                     |
      | Номер заявки         | LoanId.+?ObjectId>(.+?)<.+ObjectId>      |
      | Идентификатор заявки | ExternalId.+?ObjectId>(\d+?)<.+ObjectId> |
    * отправить POST-запрос "creditCalcEFR" с параметрами:
      | Параметр     | Значение        |
      | номер заявки | ${Номер заявки} |
    * выполнить сценарий "Кредитная заявка -> Ожидание обработки заявки" с параметрами:
      | Параметр             | Значение                                                                                     |
      | Регулярное выражение | edit-claim-data.+?preliminary-limit-calc.+?(Предварительный расчет лимита).+?edit-claim-data |
      | Название параметра   | Ожидаемое значение                                                                           |
      | Ожидаемый текст      | Предварительный расчет лимита                                                                |
    * отправить POST-запрос "creditCalcEFR" с параметрами:
      | Параметр     | Значение        |
      | номер заявки | ${Номер заявки} |
    * выполнить сценарий "Кредитная заявка -> Ожидание обработки заявки" с параметрами:
      | Параметр           | Значение                                     |
      | Название параметра | Результат запроса 'История кредитной заявки' |
      | Ожидаемый текст    | Расчетная сумма кредита равна 50000          |
    * отправить POST-запрос "CreateApplicationLoanEFR" с параметрами:
      | Параметр     | Значение        |
      | номер заявки | ${Номер заявки} |
    * отправить POST-запрос "AsoaDocReadyEFR" с параметрами:
      | Параметр     | Значение        |
      | номер заявки | ${Номер заявки} |
    * выполнить сценарий "Поиск заявки на форме заявок подразделения" с параметрами:
      | Параметр           | Значение                  |
      | Канал подачи       | Интернет-сайт             |
      | Заемщик            | ${Клиент} -> фамилия      |
      | Продуктовая группа | Потребительский кредит    |
      | Статус заявки      | Анализ и принятие решения |
    * отправить POST-запрос "efr_SaveApplication" с параметрами:
      | Параметр          | Значение                                    |
      | ApplicationDate   | ${CreateApplicationShortEFR} -> CurrentDate |
      | ApplicationNumber | ${Номер заявки}                             |
    * выполнить сценарий "Ожидание смены статуса заявки на форме заявок подразделения" с параметрами:
      | Параметр      | Значение                   |
      | Статус заявки | Ожидание клиента на сделку |
