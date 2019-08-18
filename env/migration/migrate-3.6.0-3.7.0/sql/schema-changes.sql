--
--  Copyright 2009 Denys Pavlov, Igor Azarnyi
--
--     Licensed under the Apache License, Version 2.0 (the "License");
--     you may not use this file except in compliance with the License.
--     You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
--     Unless required by applicable law or agreed to in writing, software
--     distributed under the License is distributed on an "AS IS" BASIS,
--     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--     See the License for the specific language governing permissions and
--     limitations under the License.
--

--
-- This script is for MySQL only with some Derby hints inline with comments
-- We highly recommend you seek YC's support help when upgrading your system
-- for detailed analysis of your code.
--
-- Upgrades organised in blocks representing JIRA tasks for which they are
-- necessary - potentially you may hand pick the upgrades you required but
-- to keep upgrade process as easy as possible for future we recommend full
-- upgrades
--

--
-- YC-982 Swiss billing feature in PostFinance payment gateway
--

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15180, 'postFinancePaymentGateway', 'PF_DELIVERY_AND_INVOICE_ON', null, 'Enable invoice and delivery data',
  'Invoice and delivery information will be sent with the order (ECOM_*)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15181, 'postFinancePaymentGateway', 'PF_ITEMISED_ITEM_CAT', null, 'Enabled itemised data item category',
  'Refer to PostFinance documentation on what values can be set (if not blank will be set for all itemised parameters)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15182, 'postFinancePaymentGateway', 'PF_ITEMISED_SHIP_CAT', null, 'Enabled itemised data shipping category',
  'Refer to PostFinance documentation on what values can be set (if not blank will be set for all itemised parameters)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15184, 'postFinancePaymentGateway', 'PF_DELIVERY_AND_INVOICE_ADDR2_IS_NUMBER', null, 'Enable invoice and delivery data (line 2 is number)',
  'When invoice parameters (ECOM_*) are generated address line 2 will be used as *STREET_NUMBER');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15185, 'postFinancePaymentGateway', 'PF_DELIVERY_AND_INVOICE_ADDR1_NUMBER_REGEX', null, 'Enable invoice and delivery data (line 1 regex)',
  'When invoice parameters (ECOM_*) are generated regex is used on address line 1 to extract and populate *STREET_NUMBER');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15280, 'postFinanceManualPaymentGateway', 'PF_DELIVERY_AND_INVOICE_ON', null, 'Enable invoice and delivery data',
  'Invoice and delivery information will be sent with the order (ECOM_*)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15281, 'postFinanceManualPaymentGateway', 'PF_ITEMISED_ITEM_CAT', null, 'Enabled itemised data item category',
  'Refer to PostFinance documentation on what values can be set (if not blank will be set for all itemised parameters)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15282, 'postFinanceManualPaymentGateway', 'PF_ITEMISED_SHIP_CAT', null, 'Enabled itemised data shipping category',
  'Refer to PostFinance documentation on what values can be set (if not blank will be set for all itemised parameters)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15284, 'postFinanceManualPaymentGateway', 'PF_DELIVERY_AND_INVOICE_ADDR2_IS_NUMBER', null, 'Enable invoice and delivery data (line 2 is number)',
  'When invoice parameters (ECOM_*) are generated address line 2 will be used as *STREET_NUMBER');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (15285, 'postFinanceManualPaymentGateway', 'PF_DELIVERY_AND_INVOICE_ADDR1_NUMBER_REGEX', null, 'Enable invoice and delivery data (line 1 regex)',
  'When invoice parameters (ECOM_*) are generated regex is used on address line 1 to extract and populate *STREET_NUMBER');

--
-- YC-983 No payment required payment gateway
--


INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (10701, 'noPaymentRequired', 'name', 'No payment required', 'Gateway name (default)', 'Gateway name (default)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (10702, 'noPaymentRequired', 'name_en', 'No payment required', 'Gateway name (EN)', 'Gateway name (EN)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (10703, 'noPaymentRequired', 'name_ru', 'Оплата не требуется', 'Название платежного шлюза (RU)', 'Название платежного шлюза (RU)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (10704, 'noPaymentRequired', 'name_uk', 'Оплата не потрібна', 'Назва платіжного шлюзу (UK)', 'Назва платіжного шлюзу (UK)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (10705, 'noPaymentRequired', 'name_de', 'Keine Zahlung erforderlich', 'Gateway-Namen (DE)', 'Gateway-Namen (DE)');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (10750, 'noPaymentRequired', 'priority', '100', 'Gateway priority', 'Gateway priority');

INSERT INTO TPAYMENTGATEWAYPARAMETER (PAYMENTGATEWAYPARAMETER_ID, PG_LABEL, P_LABEL, P_VALUE, P_NAME, P_DESCRIPTION)
VALUES (10751, 'noPaymentRequired', 'restrictToCustomerTags', null, 'Gateway restrictions (Customer tags)', 'Gateway restrictions (Customer tags)');