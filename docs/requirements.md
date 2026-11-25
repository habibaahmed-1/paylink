# PayLink Phase 1 Requirements and Design

**Lab section:** S5,6.

| Name and student ID | Phase 1 contribution or lead responsibility |
| :--- | :--- |
| Habiba Ahmed Hamed 20240157 | Requirements & Product Quality & DevOps& Architecture |
| Roaa Hany Mostafa 20240204 | Requirements & Product Quality & DevOps& Architecture |
| Jana Ahmed Farid 20242084 | Requirements & Product Quality &Architecture |
| Rahma Ahmed Samy 20242121 | Requirements & Product Quality &Architecture |

---

## Problem summary

PayLink is a digital wallet and bill-payment backend for customers and agents in Egypt. It supports utility bill payments and mobile top-ups through external billers. The system manages wallet funds, service fees, agent commissions, payment limits and payment outcomes. It must prevent duplicate charges, maintain accurate and auditable financial records, and handle delayed or failed biller responses through status checks and daily reconciliation.

---

## Stakeholders

| Stakeholder | Category | Role & Responsibilities in PayLink | Key Goals & Expectations |
| :--- | :--- | :--- | :--- |
| **Registered Customer** | Primary End User | Creates an account (BASIC or VERIFIED), tops up their digital wallet, browses billers/services, pays personal utility/telecom bills, tracks payment history, receives notifications, and requests refunds for completed payments. | Fast, transparent bill payment; accurate fee/discount calculation; instant SMS/Push receipts; guaranteed fund safety (no lost money on failures). |
| **Unregistered (Guest) Customer** | Secondary End User | Walk-in customer without a PayLink app account who pays bills in cash through a local Agent by providing their bill reference and mobile number. | Immediate bill settlement at a local kiosk/shop and receiving an official SMS payment confirmation receipt without needing to register. |
| **Agent (Merchant / Kiosk)** | Primary End User | Verified partner (VERIFIED tier) who tops up customer wallets with cash (AGENT_CASH) and pays bills on behalf of both registered and guest customers using their own wallet, earning a 40% commission on service fees upon completion. | Reliable, quick transaction execution; clear visibility of wallet balance and earned commissions; fair reversal rules on refunded transactions. |
| **Support Agent** | Internal Operations | Reviews customer/agent refund requests (Approve or Reject), investigates stuck UNKNOWN payments, and resolves discrepancies (MISMATCHED / PENDING_REVIEW) flagged during daily biller reconciliation. | Complete payment state history, biller interaction logs, and audit trails to make accurate, accountable resolution decisions. |
| **System Administrator (Admin)** | Internal Operations | Manages the biller and service catalog (activate/disable), configures fee rules (FIXED, PERCENTAGE, TIERED), creates promotions, updates tier transaction/velocity limits (SystemLimitConfig), and manually suspends or lifts blocks on user accounts. | Full administrative control over business rules, fraud prevention thresholds, and immutable audit logging for every configuration change. |
| **External Biller** | External System | Third-party utility, telecom, or service provider (e.g., electricity, water, internet) integrated via REST_JSON, SOAP_XML, or BATCH_CSV (IMMEDIATE, ASYNCHRONOUS, or BATCH response modes) that processes payments, sends async callbacks, and provides daily settlement files. | Validated, idempotent payment requests (idempotencyKey / requestReference), accurate settlement matching, and reliable callback acknowledgment. |
| **Payment Gateway / Card Processor (Simulated)** | External System | Simulated external card processing service (SIMULATED_CARD) used by customers and agents to fund their PayLink wallets. | Accurate authorization responses (COMPLETED / FAILED) and unique external transaction references for ledger reconciliation. |
| **Notification Providers (SMS / Push / Email)** | External System | Third-party messaging gateways responsible for delivering SMS receipts (including to walk-in guests), mobile push notifications, and email alerts upon payment completion, failure, or ongoing UNKNOWN status. | High delivery reliability, clear retry handling (FR-23) on transient provider outages, and non-blocking execution that never alters financial states. |
| **Platform Owner (PayLink Business)** | Business Sponsor | Operates the PayLink platform, collects net service fee revenue (FEE_REVENUE) into the PAYLINK system wallet, funds agent commissions (COMMISSION_DEBIT), and enforces financial compliance and audit integrity. | Zero financial leakage, 100% append-only ledger accuracy, automated reconciliation, and strict adherence to tier limits and anti-fraud velocity controls. |

---

## Story references and priorities

| Story / requirement | Priority | Story / requirement | Priority |
| :--- | :--- | :--- | :--- |
| US #1 / FR-1 | Must | US #15 / FR-15 | Must |
| US #2 / FR-2 | Should | US #16 / FR-16 | Must |
| US #3 / FR-3 | Must | US #17 / FR-17 | Should |
| US #4 / FR-4 | Must | US #18 / FR-18 | Must |
| US #5 / FR-5 | Must | US #19 / FR-19 | Should |
| US #6 / FR-6 | Must | US #20 / FR-20 | Must |
| US #7 / FR-7 | Must | US #21 / FR-21 | Should |
| US #8 / FR-8 | Must | US #22 / FR-22 | Must |
| US #9 / FR-9 | Must | US #23 / FR-23 | Should |
| US #10 / FR-10 | Must | US #24 / FR-24 | Should |
| US #11 / FR-11 | Must | US #25 / FR-25 | Must |
| US #12 / FR-12 | Must | US #26 / FR-26 | Should |
| US #13 / FR-13 | Must | US #27 / FR-27 | Could |
| US #14 / FR-14 | Must | US #28 / FR-28 | Could |

---

## User Stories

### User Story #1

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #1 (FR-1) |
| **User Story Name** | Customer Registration with Basic Verification |
| **Actors** | Customer |
| **Description** | As a Customer, I want to register for a PayLink account, so that I can access a digital wallet and use PayLink's bill-payment services. |
| **Precondition** | The Customer does not already have a registered account using the same unique registration identifier and provides the required registration information. |
| **Postcondition** | A Customer account is created with Basic verification, an associated wallet with 0.00 EGP available and 0.00 EGP held, and a daily payment limit of 5,000.00 EGP. No ledger entry is required because no money has moved. |
| **Acceptance Criteria** | **AC1 – Successful Registration**<br>Given a new Customer provides valid registration information.<br>When the Customer submits the registration request.<br>Then the system creates an account with Basic verification and an associated wallet.<br><br>**AC2 – Default Verification Level**<br>Given a Customer has successfully registered.<br>When the system retrieves the Customer's verification level.<br>Then the verification level is Basic and the daily payment limit is 5,000.00 EGP.<br><br>**AC3 – Initial Wallet Balance**<br>Given a Customer account has just been created.<br>When the Customer's wallet balance is retrieved.<br>Then the available and held balances are both 0.00 EGP.<br><br>**AC4 – Duplicate Registration**<br>Given an account already exists with the same unique registration identifier.<br>When another registration request uses that identifier.<br>Then the system rejects the request without creating another account or wallet.<br><br>**AC5 – Invalid Registration Information**<br>Given the Customer submits missing or invalid required registration information.<br>When the system validates the request.<br>Then registration is rejected with a clear validation reason, and no account or wallet is created. |

---

### User Story #2

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #2 (FR-2) |
| **User Story Name** | Upgrade Customer Verification Level |
| **Actors** | Customer |
| **Description** | As a Customer, I want to upgrade my account from Basic to Verified, so that I can make payments under a higher daily limit. |
| **Precondition** | The Customer has an existing Basic account and provides the verification information required by the system. |
| **Postcondition** | Upon successful verification, the account level becomes Verified and the daily payment limit increases from 5,000.00 EGP to 30,000.00 EGP. Existing wallet balances and ledger entries remain unchanged. |
| **Acceptance Criteria** | **AC1 – Successful Verification Upgrade**<br>Given a Basic Customer provides valid verification information.<br>When the verification is approved.<br>Then the system updates the account level to Verified.<br><br>**AC2 – Increased Daily Limit**<br>Given a Customer has been upgraded to Verified.<br>When the system evaluates the Customer's daily payment limit.<br>Then the applicable limit is 30,000.00 EGP instead of 5,000.00 EGP.<br><br>**AC3 – Verification Failure**<br>Given a Basic Customer provides invalid or insufficient verification information.<br>When the verification is evaluated.<br>Then the upgrade is rejected and the account remains Basic with a 5,000.00 EGP daily limit.<br><br>**AC4 – Already Verified Account**<br>Given the Customer is already Verified.<br>When another upgrade request is submitted.<br>Then the account remains Verified and its limits are not modified.<br><br>**AC5 – Single-Payment Limit Remains Enforced**<br>Given a Verified Customer has sufficient daily allowance.<br>When the Customer attempts a single payment exceeding 10,000.00 EGP.<br>Then the payment is rejected because verification does not override the single-payment maximum.<br><br>**AC6 – Wallet Integrity During Upgrade**<br>Given a Basic Customer has existing wallet funds and transaction history.<br>When the account is successfully upgraded.<br>Then the wallet balances and existing ledger entries remain unchanged, and the verification action is audit-logged. |

---

### User Story #3

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #3 (FR-3) |
| **User Story Name** | View Wallet Balance and Transaction History |
| **Actors** | Customer |
| **Description** | As a Customer, I want to view my wallet's available balance, held balance, and transaction history, so that I can monitor my money and understand my financial activities |
| **Precondition** | The Customer has an existing account, is authenticated, and has an associated wallet. |
| **Postcondition** | The system returns the wallet balances and transaction history without changing any balance, payment state, or ledger entry. |
| **Acceptance Criteria** | **AC1 – View Available and Held Balances**<br>Given an authenticated Customer has an associated wallet.<br>When the Customer requests wallet information.<br>Then the system displays the available balance and held balance separately in EGP, calculated from ledger entries.<br><br>**AC2 – View Transaction History**<br>Given a Customer has previous wallet transactions.<br>When the Customer requests transaction history.<br>Then the system returns the Customer's recorded transactions with their references, amounts, transaction types, and applicable statuses.<br><br>**AC3 – Empty Transaction History**<br>Given a newly registered Customer has no wallet transactions.<br>When the Customer requests wallet information.<br>Then both balances are 0.00 EGP and the transaction history is empty.<br><br>**AC4 – Funds Held for Pending Payment**<br>Given a Customer has 500.00 EGP available and 0.00 EGP held.<br>When a payment requiring 102.50 EGP is successfully placed on hold.<br>Then the displayed available balance becomes 397.50 EGP and the held balance becomes 102.50 EGP.<br><br>**AC5 – Unauthorized Wallet Access**<br>Given a Customer attempts to access another Customer's wallet.<br>When the system checks access authorization.<br>Then access is denied and the other Customer's financial information is not disclosed.<br><br>**AC6 – Read-Only Operation**<br>Given a Customer has existing wallet balances and transaction history.<br>When the Customer views the wallet information.<br>Then no ledger entry is created and no balance or transaction status is changed. |

---

### User Story #4

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #4 (FR-4) |
| **User Story Name** | Top Up Customer Wallet |
| **Actors** | Customer, Agent |
| **Description** | As a Customer, I want to add money to my wallet using a simulated card payment or Agent cash, so that I have sufficient funds to pay bills through PayLink. |
| **Precondition** | The Customer has an existing wallet. A valid positive top-up amount and one of the supported funding methods are provided. For Agent cash top-ups, an existing Agent participates in the operation. |
| **Postcondition** | For a successful top-up, the Customer's available balance increases by the confirmed amount, an immutable ledger entry is created, and the money movement is recorded in the audit log. A failed top-up does not change wallet balances. |
| **Acceptance Criteria** | **AC1 – Successful Simulated Card Top-Up**<br>Given a Customer has an active wallet and provides a valid positive top-up amount.<br>When the simulated card payment succeeds.<br>Then the Customer's available balance increases by exactly the confirmed amount, with a corresponding ledger entry and audit record.<br><br>**AC2 – Successful Agent Cash Top-Up**<br>Given a Customer provides cash to an existing Agent for a valid top-up.<br>When the Agent confirms receipt of the cash and the top-up is accepted.<br>Then the Customer's available balance increases by the confirmed amount and the operation is recorded in the ledger and audit log.<br><br>**AC3 – Failed Card Top-Up**<br>Given the simulated card payment is declined.<br>When the top-up request is processed.<br>Then the Customer's wallet balances remain unchanged and the failed attempt is reported.<br><br>**AC4 – Invalid Top-Up Amount**<br>Given the submitted top-up amount is zero, negative, or not a valid monetary value.<br>When the system validates the request.<br>Then the request is rejected without changing wallet balances or creating a money-movement ledger entry.<br><br>**AC5 – Duplicate Top-Up Request**<br>Given a top-up request has already been successfully processed with a particular request identifier.<br>When the identical request is submitted again using that identifier.<br>Then the system returns the original outcome without crediting the wallet a second time.<br><br>**AC6 – Held Balance Unchanged**<br>Given a Customer has an existing held balance for a pending payment.<br>When a wallet top-up succeeds.<br>Then only the available balance increases; the held balance remains unchanged. |

---

### User Story #5

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #5 (FR-5) |
| **User Story Name** | Maintain an Immutable Wallet Ledger |
| **Actors** | System |
| **Description** | As the System, I want to record every wallet balance change as an immutable ledger entry and calculate balances from those entries, so that all financial movements are accurate, traceable, and auditable. |
| **Precondition** | A valid wallet exists and a financial operation requiring a balance change has been authorized. |
| **Postcondition** | Each successful money movement is represented by immutable ledger entries, the available and held balances reflect the recorded movements, and the operation is captured in the audit log. Failed operations do not leave partial balance changes. |
| **Acceptance Criteria** | **AC1 – Ledger Entry for Every Balance Change**<br>Given a valid financial operation changes a wallet's available or held balance.<br>When the operation is successfully completed.<br>Then the system creates the necessary immutable ledger entries representing the change, and the resulting balances equal the sums of the relevant entries.<br><br>**AC2 – Balance Computed from Ledger**<br>Given a wallet contains multiple ledger entries.<br>When the system calculates its available and held balances.<br>Then both balances are derived from the ledger entries rather than independently overwritten values.<br><br>**AC3 – Immutable Ledger Records**<br>Given a ledger entry has already been recorded.<br>When an operation attempts to modify or delete that entry.<br>Then the modification or deletion is rejected and the original entry remains unchanged.<br><br>**AC4 – Failed Financial Operation**<br>Given a financial operation cannot be completed successfully.<br>When the operation fails before its balance changes are committed.<br>Then no partial balance changes remain and the wallet's ledger-derived balances remain consistent.<br><br>**AC5 – Concurrent Wallet Operations**<br>Given a wallet has 100.00 EGP available and two simultaneous payments each require 80.00 EGP.<br>When both payments attempt to reserve funds.<br>Then only one payment can reserve 80.00 EGP, the other is Rejected, and the available balance never becomes negative.<br><br>**AC6 – Exact Monetary Calculation and Auditability**<br>Given a financial operation involves amounts, fees, or commissions.<br>When the system calculates and records the movement.<br>Then exact decimal arithmetic is used without floating-point calculations, and the money movement has an audit record identifying who initiated it, when it occurred, and why. |

---

### User Story #6

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #6 (FR-6) |
| **User Story Name** | Manage Billers and Service |
| **Actors** | Admin |
| **Description** | As an Admin, I want to add, edit, and disable billers and services, so that I can manage the billers and services available in PayLink. |
| **Precondition** | The admin is authenticated and authorized to manage the Biller Catalog. |
| **Postcondition** | The requested addition, modification, or disabling operation is reflected in the Biller Catalog, and the admin action is recorded in the audit log. |
| **Acceptance Criteria** | **AC1 – Add a Biller or Service**<br>Given the Admin is authorized to manage the Biller Catalog.<br>When the Admin submits all required information to add a new Biller or Service.<br>Then the system adds the Biller or Service to the catalog and records the admin action in the audit log<br><br>**AC2 – Edit a Biller or Service**<br>Given an existing Biller or Service is available in the catalog.<br>When the Admin submits valid updated information.<br>Then the system updates the corresponding information and records the admin action in the audit log.<br><br>**AC3 – Disable a Biller or Service**<br>Given an existing Biller or Service is available in the catalog.<br>When the Admin requests to disable it.<br>Then the system marks it as disabled and records the admin action in the audit log.<br><br>**AC4 – Reject Invalid Operations**<br>Given the Admin submits incomplete required information, invalid configuration values, or requests to edit or disable a Biller or Service that does not exist.<br>When the system validates the request.<br>Then the system rejects the operation, displays a clear error message, and does not modify the catalog. |

---

### User Story #7

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #7 (FR-7) |
| **User Story Name** | View Available Billers and Required Input Fields |
| **Actors** | Customer, Agent |
| **Description** | As a Customer, I want to see the list of available billers and the input fields required by each service, so that I know what information I need to provide before making a bill of payment. |
| **Precondition** | The Customer can access the bill-payment section. |
| **Postcondition** | The Customer can view the available Billers and Services and identify the information required for each service. |
| **Acceptance Criteria** | **AC1 – View Available Billers**<br>Given the Biller Catalog contains active Billers.<br>When the Customer opens the bill-payment section.<br>Then the system displays the available Billers.<br><br>**AC2 – View Services and Required Input Fields**<br>Given the Customer selects an available Biller.<br>When the system displays its available Services.<br>Then the system displays each Service's required input fields, such as a meter number or phone number.<br><br>**AC3 – Display Only Available Services**<br>Given a Biller or Service is marked as disabled.<br>When the Customer views the available Billers and Services.<br>Then the disabled Biller or Service is not displayed as an available payment option.<br><br>**AC4 – Handle an Empty Catalog**<br>Given there are no active Billers or Services available.<br>When the Customer opens the bill-payment section.<br>Then the system displays a clear message indicating that no billers or services are currently available. |

---

### User Story #8

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #8 (FR-8) |
| **User Story Name** | Perform Bill Inquiry |
| **Actors** | Customer, Agent |
| **Description** | As a Customer, I want to perform a bill inquiry and see the bill amount, service fee, and total amount before paying, so that I can review the payment details before confirming the transaction. |
| **Precondition** | The Customer has selected an active Service and provided its required input information. |
| **Postcondition** | The system displays the bill amount, applicable service fee, and total amount before payment. No payment is completed as part of the inquiry. |
| **Acceptance Criteria** | **AC1 – Successful Bill Inquiry**<br>Given the Customer has selected an active Service and entered valid required information.<br>When the Customer requests a bill inquiry.<br>Then the system sends the inquiry to the corresponding Biller and retrieves the bill amount.<br><br>**AC2 – Display Payment Breakdown**<br>Given the Biller returns a successful inquiry result.<br>When the system receives the bill amount.<br>Then the system calculates the applicable service fee and displays:<br>- The bill amount.<br>- The service fee.<br>- The total amount to be paid.<br><br>**AC3 – Apply the Correct Fee**<br>Given the bill inquiry is successful.<br>When the system calculates the service fee.<br>Then the system applies the configured fee rules and any applicable promotion before displaying the total amount.<br><br>**AC4 – Handle Invalid Inquiry Information**<br>Given the Customer submits missing or invalid required input information.<br>When the Customer requests a bill inquiry.<br>Then the system rejects the inquiry and displays a clear validation error without proceeding with the inquiry.<br><br>**AC5 – Handle Failed Inquiry**<br>Given the Biller does not return a successful inquiry result.<br>When the system processes the inquiry response.<br>Then the system informs the Customer that the inquiry could not be completed and does not display an unconfirmed bill amount as a successful result. |

---

### User Story #9

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #9 (FR-9) |
| **User Story Name** | Pay a Bill from the Wallet |
| **Actors** | Customer |
| **Description** | As a Customer, I want to pay a utility bill from my wallet, so that I can settle the bill without visiting the biller |
| **Precondition** | The Customer is registered and not blocked. A bill inquiry (FR-8) was completed, so the bill amount, service fee, and total amount are known. The selected Biller and Service are active. |
| **Postcondition** | A Payment exists in a valid life cycle state. Reserved funds remain held while the outcome is pending, are converted into a final charge on Completed, and are returned to the available balance of the funding wallet on Refunded. Every money movement creates an immutable ledger entry and an audit record. |
| **Acceptance Criteria** | **AC1 – Successful Payment**<br>Given the funding wallet has an available balance equal to or greater than the total amount (bill amount plus service fee) and all fraud checks pass.<br>When the Customer submits the payment.<br>Then the payment moves Initiated, FundsHeld, SentToBiller, then Completed. The hold is converted into a final charge exactly once and the service fee is recorded as PayLink income.<br><br>**AC2 – Insufficient Funds**<br>Given the available balance of the funding wallet is less than the total amount.<br>When the Customer submits the payment.<br>Then the payment becomes Rejected with the reason insufficient funds, the Biller is not called, and no balance changes.<br><br>**AC3 – Amount Outside the Allowed Range**<br>Given the bill amount is below 5.00 EGP or above 10,000.00 EGP.<br>When the Customer submits the payment.<br>Then the payment becomes Rejected with a clear reason and the Biller is not called.<br><br>**AC4 – Biller Refuses the Payment**<br>Given the payment is SentToBiller and its total amount is held.<br>When the Biller refuses the payment.<br>Then the payment becomes Failed with the Biller's reason, the full held amount is returned to the available balance of the funding wallet, and the payment becomes Refunded when the hold is released. The Customer is told the reason.<br><br>**AC5 – Biller Timeout**<br>Given the payment is SentToBiller and the Biller gives no response within 10 seconds.<br>When the 10 seconds pass.<br>Then the payment becomes Unknown, the total amount remains held, and the Customer is informed that the payment is being processed.<br><br>**AC6 – Disabled Biller or Service**<br>Given the selected Biller or Service is disabled.<br>When the Customer submits the payment.<br>Then the payment becomes Rejected, no funds are held, and the Biller is not called. |

---

### User Story #10

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #10 (FR-10) |
| **User Story Name** | Return the Original Result for a Repeated Payment Request |
| **Actors** | Customer |
| **Description** | As a Customer, I want a repeated payment request with the same idempotency key to return the original result, so that I am never charged twice when the network is slow. |
| **Precondition** | The payment request includes an idempotency key generated by the client. The key is unique per funding wallet. |
| **Postcondition** | For each idempotency key there is at most one Payment and one set of ledger entries. A repeated request creates no new payment, no new hold, and no new charge. |
| **Acceptance Criteria** | **AC1 – Duplicate After Processing**<br>Given a payment with idempotency key K has already been processed.<br>When the same request with key K is sent again.<br>Then the system returns the original result and creates no new payment and no new ledger entry.<br><br>**AC2 – Duplicate While Processing**<br>Given a payment with key K is still in progress.<br>When the same request with key K arrives.<br>Then the system returns the current state of the original payment and does not start a second payment.<br><br>**AC3 – Simultaneous Duplicates**<br>Given two identical requests with key K arrive at the same moment.<br>When both requests are processed.<br>Then exactly one payment is created and at most one charge is made, only if the payment completes.<br><br>**AC4 – Same Key with Different Content**<br>Given key K was used for a payment of 100.00 EGP.<br>When a request with key K and a different amount or Service arrives.<br>Then the system rejects the request with a conflict error and the original payment is unchanged.<br><br>**AC5 – Replay of a Rejected Payment**<br>Given the payment with key K is Rejected.<br>When the same request with key K is sent again.<br>Then the system returns the original Rejected result and does not re-evaluate it. The Customer must use a new key to try again.<br><br>**AC6 – Missing Key**<br>Given a payment request has no idempotency key.<br>When the Customer submits it.<br>Then the system rejects the request with a validation error and creates no payment. |

---

### User Story #11

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #11 (FR-11) |
| **User Story Name** | Enforce the Payment Life Cycle |
| **Actors** | System |
| **Description** | As the System, I want every payment to follow only the allowed life cycle transitions, so that money is never handled in an inconsistent state. |
| **Precondition** | A Payment exists in one of the defined states: Initiated, FundsHeld, SentToBiller, Completed, Failed, Unknown, Rejected, or Refunded. |
| **Postcondition** | The payment is in exactly one state. Every successful transition is recorded with its time and reason in the payment history and the audit log. Refused transitions leave the state unchanged. |
| **Acceptance Criteria** | **AC1 – Allowed Transition**<br>Given a payment is in a given state.<br>When an allowed event occurs, for example the Biller confirms a payment in SentToBiller.<br>Then the payment moves to the target state defined in the life cycle.<br><br>**AC2 – Forbidden Transition**<br>Given a payment is in FundsHeld.<br>When the system tries to move it directly to Completed.<br>Then the change is refused, the state does not change, and the attempt is logged.<br><br>**AC3 – Final States**<br>Given a payment is Completed, Rejected, or Refunded.<br>When any event tries to change its state.<br>Then the change is refused and the state stays the same. A late Biller response for such a payment is ignored and logged.<br><br>**AC4 – Resolve an Unknown Payment**<br>Given a payment is Unknown.<br>When the Biller later confirms or refuses it through a callback, a status check, or a settlement file.<br>Then the payment becomes Completed or Failed accordingly.<br><br>**AC5 – No Automatic Refund of Unknown**<br>Given a payment is Unknown and the Biller has not confirmed non-receipt.<br>When time passes.<br>Then the payment stays Unknown and its total amount stays held.<br><br>**AC6 – Transition History**<br>Given a payment has changed state several times.<br>When Support views the payment.<br>Then every transition is listed in order with its time and reason. |

---

### User Story #12

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #12 (FR-12) |
| **User Story Name** | Hold Funds Before Calling the Biller |
| **Actors** | System |
| **Description** | As the System, I want to hold the total payment amount in the funding wallet before calling the Biller, so that the same money cannot be spent twice and the money is released if the payment fails. |
| **Precondition** | The payment is Initiated and has passed all fraud checks, including the limit and velocity rules. |
| **Postcondition** | The hold is recorded as a ledger entry that moves the total amount from the available balance to the held balance. When the payment ends, the hold is converted into a final charge or released. Balances are computed from ledger entries and every entry has an audit record. |
| **Acceptance Criteria** | **AC1 – Create the Hold**<br>Given the available balance of the funding wallet is equal to or greater than the total amount.<br>When the payment moves to FundsHeld.<br>Then a ledger entry moves the total amount from the available balance to the held balance, and the Biller is called only after this entry exists.<br><br>**AC2 – Convert the Hold on Success**<br>Given a payment has its total amount held.<br>When the Biller confirms the payment.<br>Then the hold is converted into a final charge exactly once and the held balance decreases by the total amount.<br><br>**AC3 – Release the Hold on Failure**<br>Given a payment with its total amount held becomes Failed.<br>When the failure is recorded.<br>Then the full held amount is returned to the available balance of the funding wallet and the payment becomes Refunded when the hold is released.<br><br>**AC4 – Keep the Hold While Unknown**<br>Given a payment is Unknown.<br>When time passes without a definitive Biller result.<br>Then the total amount stays held and is not released.<br><br>**AC5 – Concurrent Payments**<br>Given the available balance covers only one of two simultaneous payments.<br>When both payments are processed.<br>Then one payment becomes FundsHeld, the other becomes Rejected, and the balance never becomes negative.<br><br>**AC6 – Ledger Consistency**<br>Given any hold, conversion, or release happens.<br>When the system records it.<br>Then the balance is computed from ledger entries, no balance is edited directly, and the entry has an audit record. |

---

### User Story #13

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #13 (FR-13) |
| **User Story Name** | Handle Payment Timeouts and Retry Status Checks |
| **Actors** | Customer |
| **Description** | As a Customer, I want PayLink to check the status of my payment when the Biller does not respond, so that I can know the outcome without being charged twice. |
| **Precondition** | The payment has been sent to a live Biller using a unique reference. Its state is SentToBiller, and the total payment amount, including the service fee, is held in the funding wallet. |
| **Postcondition** | The payment is Completed if the Biller confirms receipt. If the Biller refuses the payment, it becomes Failed, then Refunded when the hold is released. An unresolved payment remains Unknown with its funds held, pending further status checks or reconciliation. Every resulting balance change creates an immutable ledger entry and an audit record. |
| **Acceptance Criteria** | **AC1 – Handle a Biller Timeout**<br>• Given the payment is SentToBiller and its total amount is held.<br>• When 10 seconds pass without a response from the live Biller.<br>• Then the payment becomes Unknown, the total amount remains held, and the Customer is informed that the payment is being processed.<br><br>**AC2 – Retry the Payment Status Check**<br>• Given a timed-out payment remains Unknown.<br>• When 1, 5, or 15 minutes have passed since the payment entered Unknown, using the proposed timing interpretation in D13-01.<br>• Then the system queries the Biller once at each scheduled time using the original payment reference, without creating another payment, hold, or charge.<br><br>**AC3 – Receive Confirmation During a Retry**<br>• Given the payment is Unknown and its total amount remains held.<br>• When a status check confirms that the Biller received the payment.<br>• Then the payment becomes Completed, the hold is converted into a final charge exactly once, and further scheduled status checks are cancelled.<br><br>**AC4 – Receive a Refusal During a Retry**<br>• Given the payment is Unknown and its total amount remains held.<br>• When a status check returns a definitive refusal from the Biller.<br>• Then the payment becomes Failed, further scheduled status checks are cancelled, and the full held amount is returned to the available balance of the funding wallet. The payment becomes Refunded when the hold is released.<br><br>**AC5 – Exhaust the Three Retries**<br>• Given the payment remains Unknown after two status checks without a definitive result.<br>• When the third status check also returns no definitive result.<br>• Then the payment remains Unknown pending reconciliation, its total amount remains held, and no fourth retry or automatic refund occurs. |

---

### User Story #14

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #14 (FR-14) |
| **User Story Name** | Process TopUpNet Callbacks Once |
| **Actors** | Customer |
| **Description** | As a Customer, I want PayLink to process TopUpNet’s final response once, so that my mobile top-up has the correct outcome even if TopUpNet sends the response more than once. |
| **Precondition** | PayLink has sent mobile top-ups to TopUpNet and retains their payment references and outcomes so callbacks can be matched and duplicates identified. For an unresolved payment, the total amount, including the service fee, remains held. A callback may also contain an unmatched reference. |
| **Postcondition** | A successful top-up becomes Completed and is charged exactly once. A refused top-up becomes Failed, then Refunded when the full held amount is returned to the funding wallet. Every resulting balance change creates an immutable ledger entry and an audit record. Duplicate callbacks are ignored and logged. |
| **Acceptance Criteria** | **AC1 – Receive an Initial Acknowledgement**<br>• Given the mobile top-up is SentToBiller and its total amount is held.<br>• When TopUpNet replies accepted.<br>• Then the payment remains SentToBiller with its total amount held while PayLink waits for the final outcome.<br><br>**AC2 – Process a Success Callback**<br>• Given the matching mobile top-up is SentToBiller or Unknown and its total amount is held.<br>• When TopUpNet sends a final success callback.<br>• Then the payment becomes Completed, and the hold is converted into a final charge exactly once.<br><br>**AC3 – Process a Failure Callback**<br>• Given the matching mobile top-up is SentToBiller or Unknown and its total amount is held.<br>• When TopUpNet sends a final failed callback.<br>• Then the payment becomes Failed, and the full held amount is returned to the available balance of the funding wallet. The payment becomes Refunded when the hold is released.<br><br>**AC4 – Ignore a Duplicate Callback**<br>• Given PayLink has already processed a final callback for a payment.<br>• When TopUpNet sends the same final result again for the same payment reference.<br>• Then the system ignores and logs the duplicate. The payment state and ledger entries remain unchanged.<br><br>**AC5 – Handle an Unmatched Payment Reference**<br>• Given a callback contains a reference that matches no PayLink payment.<br>• When the system processes the callback.<br>• Then no payment is updated and no wallet ledger entry is created.<br><br>**AC6 – Handle a Missing Final Callback**<br>• Given TopUpNet has returned accepted, the payment remains SentToBiller, and no final outcome has been received.<br>• When 5 minutes have elapsed since the accepted acknowledgement.<br>• Then the payment becomes Unknown, its total amount remains held, and status checks follow FR-13 using the original payment reference. No automatic refund occurs. |

---

### User Story #15

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #15 (FR-15) |
| **User Story Name** | Support the Three Required Billers |
| **Actors** | Customer |
| **Description** | As a Customer, I want to pay my electricity bill, water bill, or mobile top-up through PayLink, so that I can use my wallet for all three services. |
| **Precondition** | The selected Biller and Service are active, and the Customer has provided the required inquiry or payment details. For a payment, the total amount, including the service fee, is held in the funding wallet before the request is sent to the Biller. |
| **Postcondition** | An inquiry returns the Biller’s result without charging the wallet. A confirmed payment becomes Completed and is charged once. A refused payment becomes Failed, then Refunded when the hold is released. A TopUpNet acknowledgement leaves the payment awaiting its final result with its funds held. Every resulting balance change creates an immutable ledger entry and an audit record. |
| **Acceptance Criteria** | **AC1 – Perform an ElectroCo Bill Inquiry**<br>• Given ElectroCo has a bill of 350.00 EGP for a valid meter number.<br>• When the Customer requests an inquiry using that meter number.<br>• Then the system uses ElectroCo’s REST/JSON contract and returns 350.00 EGP as the amount due. The inquiry creates no wallet charge.<br><br>**AC2 – Process an ElectroCo Payment Confirmation**<br>• Given the electricity payment is SentToBiller and its total amount is held.<br>• When ElectroCo returns an immediate payment confirmation.<br>• Then the matching payment becomes Completed, and the hold is converted into a final charge exactly once.<br><br>**AC3 – Process an AquaCo Payment**<br>• Given AquaCo reports a bill of 100.00 EGP and the payment total is held.<br>• When PayLink sends 100.00 EGP as the bill-payment amount using AquaCo’s SOAP-style XML over HTTP contract and receives confirmation after four seconds.<br>• Then the payment becomes Completed, and the hold is converted into a final charge exactly once.<br><br>**AC4 – Handle an AquaCo Overpayment Refusal**<br>• Given the AquaCo bill amount is 100.00 EGP.<br>• When PayLink sends 100.01 EGP as the bill-payment amount and AquaCo returns its overpayment error.<br>• Then the payment becomes Failed with a clear reason stating that the payment exceeds the bill amount. The full held total is returned to the funding wallet, and the payment becomes Refunded when the hold is released.<br><br>**AC5 – Process a TopUpNet Asynchronous Confirmation**<br>• Given PayLink has submitted a mobile top-up using TopUpNet’s asynchronous REST/JSON contract and its total amount is held.<br>• When TopUpNet first returns accepted and later sends a success callback.<br>• Then the payment remains SentToBiller with its funds held after accepted. On success, the payment becomes Completed, and the hold is converted into a final charge exactly once. |

---

### User Story #16

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #16 (FR-16) |
| **User Story Name** | Calculate Service Fees |
| **Actors** | System |
| **Description** | As a System, I want to calculate service fees according to the defined fee rules, including promotional periods, so that Customers are charged the correct fee for each bill payment. |
| **Precondition** | The applicable fee configuration and promotion settings are available in the system. |
| **Postcondition** | The system calculates the applicable service fee and makes it available for the payment breakdown and payment processing. |
| **Acceptance Criteria** | **AC1 – Electricity and Water Fees**<br>Given the Customer performs an inquiry for an electricity or water service.<br>When the system calculates the service fee.<br>Then the system applies the configured fixed fee of 2.50 EGP.<br><br>**AC2 – Mobile Top-Up Fees**<br>Given the Customer performs an inquiry for a mobile top-up service.<br>When the system calculates the service fee.<br>Then the system applies a fee of 1.5% of the top-up amount, with a minimum fee of 1 EGP.<br><br>**AC3 – Internet Fees**<br>Given the Customer performs an inquiry for an internet service.<br>When the system calculates the service fee.<br>Then the system applies:<br>- 2 EGP for amounts up to and including 200 EGP.<br>- 4 EGP for amounts above 200 EGP.<br><br>**AC4 – Promotional Period**<br>Given a promotion is configured for a selected Biller with a start date and an end date.<br>When a Customer performs an inquiry for that Biller during the promotional period.<br>Then the system applies a service fee of 0 EGP.<br><br>**AC5 – Promotion Outside Its Configured Period**<br>Given a promotion is configured for a selected Biller.<br>When a Customer performs an inquiry before the promotion start date or after its end date.<br>Then the system applies the normal configured service fee.<br><br>**AC6 – Accurate Fee Calculation**<br>Given a percentage-based service fee calculation (such as mobile top-up) produces fractional piasters beyond two decimal places.<br>When the system finalizes the fee amount.<br>Then the system rounds the fee consistently to two decimal places (nearest piaster) using the exact decimal arithmetic. |

---

### User Story #17

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #17 (FR-17) |
| **User Story Name** | Pay on Behalf of a Customer |
| **Actors** | Agent |
| **Description** | As an Agent, I want to pay a customer's bill using my Agent wallet, so that I can provide bill-payment services on behalf of Customers and earn a commission for completed payments. |
| **Precondition** | The Agent is authorized to perform payments on behalf of Customers, has sufficient available wallet balance, and has selected a valid registered Customer or provided a guest customer's bill account reference and mobile number for an active Service. |
| **Postcondition** | The payment is processed using the Agent's wallet. If the payment completes successfully, the Agent receives the applicable commission in the Agent's wallet. |
| **Acceptance Criteria** | **AC1 – Initiate Payment on Behalf of a Customer**<br>Given the Agent is authorized, has sufficient available wallet balance, and has selected a valid Customer and active Service.<br>When the Agent submits the required payment information (selecting a registered Customer or entering guest customer contact and bill details). Then the system initiates the payment using the Agent's wallet for that bill.<br>Then the system initiates the payment using the Agent's wallet on behalf of the selected Customer.<br><br>**AC2 – Insufficient Agent Wallet Balance**<br>Given the Agent's available wallet balance is insufficient to cover the total payment amount.<br>When the Agent attempts to initiate the payment.<br>Then the system rejects the payment and informs the Agent that the wallet balance is insufficient.<br><br>**AC3 – Calculate Agent Commission**<br>Given a payment has been initiated by an Agent.<br>When the system calculates the Agent's commission.<br>Then the system calculates the commission as 40% of the applicable service fee.<br><br>**AC4 – Credit Commission After Successful Completion**<br>Given the payment initiated by the Agent reaches the Completed state.<br>When the system processes the completed payment.<br>Then the system credits the Agent's wallet with the calculated commission exactly once upon payment completion and records the commission movement in an immutable ledger entry and the audit log.<br><br>**AC5 – No Commission for Uncompleted Payments**<br>Given the payment initiated by the Agent does not reach the Completed state.<br>When the system processes the payment result.<br>Then the system does not credit the Agent's wallet with a commission.<br><br>**AC6 – Commission During a Promotion**<br>Given the payment is made during an active promotion where the service fee is 0 EGP.<br>When the payment reaches the Completed state.<br>Then the system calculates the Agent's commission as 40% of 0 EGP, resulting in a commission of 0 EGP. |

---

### User Story #18

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #18 (FR-18) |
| **User Story Name** | Reject Payments Above the Limits |
| **Actors** | System |
| **Description** | As the System, I want to reject payments above the single-payment limit or the daily limit with a clear reason, so that accounts follow their verification level and the Customer understands why the payment was refused. |
| **Precondition** | The payment is Initiated and the verification level (Basic or Verified) of the funding wallet is known. The limit values are available in the configuration. |
| **Postcondition** | A payment above a limit is Rejected with a clear reason, no funds are held, and the rejection is recorded in the audit log. |
| **Acceptance Criteria** | **AC1 – Single Payment Above the Maximum**<br>Given the bill amount is 10,000.01 EGP.<br>When the Customer submits the payment.<br>Then the payment becomes Rejected with the reason above single-payment limit.<br><br>**AC2 – Single Payment at the Boundaries**<br>Given the bill amount is exactly 5.00 EGP or exactly 10,000.00 EGP and the daily limit is not exceeded.<br>When the Customer submits the payment.<br>Then the payment is not rejected for the single-payment limit.<br><br>**AC3 – Basic Daily Limit**<br>Given a Basic account has already paid 4,800.00 EGP today.<br>When it submits a payment of 300.00 EGP.<br>Then the payment becomes Rejected with the reason daily limit exceeded.<br><br>**AC4 – Verified Daily Limit**<br>Given a Verified account has already paid 29,900.00 EGP today.<br>When it submits a payment of 100.00 EGP, then another payment of 100.01 EGP in a separate case.<br>Then the 100.00 EGP payment passes the limit check and the 100.01 EGP payment becomes Rejected.<br><br>**AC5 – Unsuccessful Payments Do Not Use the Limit**<br>Given a payment ended Rejected, Failed, or Refunded earlier today.<br>When the daily total is calculated.<br>Then the amount of that payment is not counted.<br><br>**AC6 – Limits Come from Configuration**<br>Given an Admin changes a limit value in the configuration (FR-24).<br>When the next payment is checked.<br>Then the new value is used without redeploying. |

---

### User Story #19

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #19 (FR-19) |
| **User Story Name** | Block an Account After Too Many Payments |
| **Actors** | System |
| **Description** | As the System, I want to block an account for 10 minutes when it makes more than 5 payments in 60 seconds, so that automated abuse and mistakes are stopped quickly. |
| **Precondition** | The account is active and is not already blocked. The velocity thresholds (5 payments, 60 seconds, 10 minutes) are available in the configuration. |
| **Postcondition** | If the rule is triggered, the account is blocked until the block expires and the block is recorded in the audit log. If it is not triggered, the payment continues normally. |
| **Acceptance Criteria** | **AC1 – Within the Limit**<br>Given an account made 4 payments in the last 60 seconds.<br>When it submits a 5th payment.<br>Then the payment is processed normally because 5 payments are not more than 5.<br><br>**AC2 – Rule Triggered**<br>Given an account already made 5 payments in the last 60 seconds.<br>When it submits a 6th payment inside the same window.<br>Then the 6th payment becomes Rejected with the reason too many payments and the account is blocked for 10 minutes.<br><br>**AC3 – Blocked Account**<br>Given an account is blocked by the velocity rule.<br>When it submits any payment during the 10 minutes.<br>Then the payment becomes Rejected with the reason account temporarily blocked and no funds are held.<br><br>**AC4 – Block Expiry**<br>Given a velocity block started 10 minutes ago.<br>When the account submits a payment.<br>Then the velocity block expires, and the payment remains subject to any active manual block and all other payment checks.<br><br>**AC5 – Repeated Requests Are Not Counted**<br>Given a request is repeated with an existing idempotency key (FR-10).<br>When the velocity count is calculated.<br>Then the repeated request is not counted as a new payment. |

---

### User Story #20

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #20 (FR-20) |
| **User Story Name** | Search Payment by Reference and View Full History |
| **Actors** | Support |
| **Description** | As a Support employee, I want to search for a payment using its unique reference and view its complete history, so that I can investigate payment problems and provide accurate information to Customers. |
| **Precondition** | The Support employee is authenticated and authorized to access payment information. A payment reference is provided. |
| **Postcondition** | If the reference exists, the system returns the matching payment and its recorded history without changing its state or financial records. The Support access action is recorded in the audit log. |
| **Acceptance Criteria** | **AC1 – Successful Payment Search**<br>Given an authorized Support employee provides an existing payment reference.<br>When the employee searches for the payment.<br>Then the system returns the matching payment with its amount, service fee, biller, current state, and recorded history.<br><br>**AC2 – Complete Payment State History**<br>Given a payment has passed through multiple valid lifecycle states.<br>When Support views its history.<br>Then the system displays the recorded state transitions in chronological order, including their timestamps and available reasons.<br><br>**AC3 – Payment Not Found**<br>Given the submitted payment reference does not exist.<br>When Support performs the search.<br>Then the system returns a clear not-found result without modifying any payment or wallet data.<br><br>**AC4 – Invalid Payment Reference**<br>Given the submitted payment reference is empty or invalid.<br>When Support attempts to search.<br>Then the system rejects the search with a validation message.<br><br>**AC5 – Unauthorized Access**<br>Given a user does not have Support authorization.<br>When the user attempts to access payment history through the Support function.<br>Then the system denies access and does not disclose the payment information.<br><br>**AC6 – Unknown Payment Investigation**<br>Given a payment has reached Unknown after a biller timeout.<br>When Support retrieves the payment history.<br>Then the history shows the recorded timeout, the current Unknown state, and any subsequent retry or reconciliation events, without automatically refunding the payment. |

---

### User Story #21

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #21 (FR-21) |
| **User Story Name** | Block and Unblock Customer Accounts |
| **Actors** | Support |
| **Description** | As a Support employee, I want to block and unblock Customer accounts, so that I can prevent unauthorized or suspicious payment activity and restore payment access when appropriate. |
| **Precondition** | The Support employee is authenticated and authorized to manage account blocking. The target Customer account exists. |
| **Postcondition** | The account's blocking status is updated when a valid change is requested. The action is recorded in the audit log with the responsible Support employee, timestamp, and reason. Wallet balances and existing ledger entries remain unchanged. |
| **Acceptance Criteria** | **AC1 – Successfully Block an Account**<br>Given an authorized Support employee selects an existing unblocked Customer account.<br>When the employee requests a block with a valid reason.<br>Then the account becomes blocked and new bill-payment attempts are rejected while the block is active.<br><br>**AC2 – Successfully Unblock an Account**<br>Given an account is manually blocked by Support and has no other active blocking restriction.<br>When an authorized Support employee requests an unblock.<br>Then the manual block is removed and the account can submit new payments subject to the normal payment rules.<br><br>**AC3 – Block an Already Blocked Account**<br>Given a Customer account is already manually blocked.<br>When Support requests another block.<br>Then the account remains blocked and no duplicate blocking effect is introduced.<br><br>**AC4 – Unblock an Account That Is Not Blocked**<br>Given a Customer account has no active manual block.<br>When Support requests an unblock.<br>Then the system reports that the account is not manually blocked and leaves its status unchanged.<br><br>**AC5 – Unauthorized Account Management**<br>Given a user does not have Support authorization.<br>When the user attempts to block or unblock an account.<br>Then the system denies the action and leaves the account's blocking status unchanged.<br><br>**AC6 – Blocking and Automatic Velocity Restriction**<br>Given an account has an active 10-minute velocity restriction after exceeding five payments within 60 seconds.<br>When Support removes a separate manual block from that account.<br>Then the velocity restriction remains effective until its 10-minute period expires, and the Support action is audit-logged. |

---

### User Story #22

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #22 (FR-22) |
| **User Story Name** | Notify the Customer of Payment Completion or Failure |
| **Actors** | Customer |
| **Description** | As a Customer, I want to receive an SMS and a push message when my payment completes or fails, so that I know the outcome of my payment. |
| **Precondition** | The payment is linked to a registered Customer or includes a guest customer's mobile number, and the available delivery channel details (SMS and/or push) are present. |
| **Postcondition** | PayLink attempts to send both an SMS and a push message describing the payment outcome. Failed SMS delivery follows the notification retry rule. Notification of delivery does not change the payment state or create wallet ledger entries. |
| **Acceptance Criteria** | **AC1 – Notify the Customer of Payment Completion**<br>• Given the Customer’s SMS and push channels are available.<br>• When the payment enters Completed.<br>• Then the Customer receives an SMS and a push message confirming that the payment completed successfully.<br><br>**AC2 – Notify the Customer of Payment Failure**<br>• Given a Biller refuses the payment and the Customer’s SMS and push channels are available.<br>• When the payment enters Failed.<br>• Then the Customer receives a failure SMS and a failure push message. The SMS states the failure reason in clear language. For an AquaCo overpayment refusal, it states that the payment amount exceeded the bill amount.<br><br>**AC3 – Handle an Unavailable SMS Provider**<br>• Given push delivery is available but the SMS provider is unavailable.<br>• When the payment enters Completed.<br>• Then the push confirmation is delivered, the failed SMS follows the retry rule in FR-23, and the payment remains Completed.<br><br>**AC4 – Handle an Unavailable Push Channel**<br>• Given SMS delivery is available but the push channel is unavailable.<br>• When the payment enters Completed.<br>• Then the SMS confirmation is delivered, the push delivery attempt fails, and the payment remains Completed.<br><br>**AC5 – Handle an Unknown Payment Outcome**<br>• Given the Biller has provided no definitive payment result.<br>• When the payment becomes Unknown.<br>• Then the Customer is informed that processing is ongoing under FR-13. No completion or failure notification is sent while the outcome remains Unknown.<br><br>**AC6 – Notify an Unregistered (Guest) Customer via SMS Only**<br>Given an Agent initiated the payment for an unregistered guest customer with a valid mobile number and no PayLink app account.<br>When the payment enters Completed or Failed.<br>Then the system sends the outcome notification via SMS to the provided mobile number and skips push delivery without treating the skipped push channel as a delivery failure. |

---

### User Story #23

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #23 (FR-23) |
| **User Story Name** | Retry Failed Notifications |
| **Actors** | Customer |
| **Description** | As a Customer, I want PayLink to retry a failed notification, so that a temporary delivery problem does not prevent me from receiving my payment update. |
| **Precondition** | The initial delivery attempt for an SMS or email has failed, and the system can identify the notification, recipient, channel, and number of retries already attempted. |
| **Postcondition** | A successful retry stops further attempts. If all three retries fail, the notification is marked undelivered. Retrying a notification does not change the payment state or create additional wallet ledger entries. |
| **Acceptance Criteria** | **AC1 – Retry a Failed Notification**<br>• Given the initial SMS or email delivery attempt has failed and no retry has been attempted.<br>• When the system processes the delivery failure.<br>• Then it retries the same notification for the same recipient through the same channel.<br><br>**AC2 – Stop Retrying After Successful Delivery**<br>• Given the initial delivery attempt failed.<br>• When any of the three permitted retries succeeds, including the third retry.<br>• Then the system stops further delivery attempts and does not mark the notification undelivered.<br><br>**AC3 – Mark the Notification Undelivered**<br>• Given the initial delivery attempt and the first two retries have failed.<br>• When the third retry also fails.<br>• Then the system marks the notification undelivered after four failed delivery attempts in total and makes no further attempt.<br><br>**AC4 – Retry Only the Failed Notification**<br>• Given a completion push message was delivered successfully, but the corresponding SMS failed.<br>• When the system retries the failed SMS.<br>• Then it retries only the SMS. The delivered push message is not resent, the payment remains Completed, and no additional charge is recorded. |

---

### User Story #24

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #24 (FR-24) |
| **User Story Name** | Manage Fee and Limit Configuration |
| **Actors** | Admin |
| **Description** | As an Admin, I want to edit fee and limit values without redeploying the system, so that I can manage PayLink's financial configurations when needed. |
| **Precondition** | The Admin is authenticated and authorized to manage fee and limit configurations. |
| **Postcondition** | Valid updated fee and limit values are saved in the system configuration and can be used without redeploying the application. The admin action is recorded in the audit log. |
| **Acceptance Criteria** | **AC1 – Edit Fee Values**<br>Given the Admin is authorized to manage fee configurations.<br>When the Admin submits valid updated fee values.<br>Then the system saves the updated values in the configuration and records the Admin action in the audit log.<br><br>**AC2 – Edit Limit Values**<br>Given the Admin is authorized to manage limit configurations.<br>When the Admin submits valid updated limit values.<br>Then the system saves the updated values in the configuration and records the Admin action in the audit log.<br><br>**AC3 – Apply Updated Configuration Without Redeployment**<br>Given the Admin has successfully updated fee or limit values.<br>When a new fee calculation or payment-limit validation is performed.<br>Then the system uses the updated configuration without requiring an application redeployment.<br><br>**AC4 – Reject Invalid Configuration Values**<br>Given the Admin submits missing, invalid, or inconsistent fee or limit values.<br>When the system validates the submitted configuration.<br>Then the system rejects the update, displays a clear error message, and retains the previous valid configuration.<br><br>**AC5 — Configure Promotional Period**<br>Given the Admin wants to configure a promotional period for one selected biller.<br>When the Admin sets a zero-fee promotion with valid start and end dates and saves the configuration.<br>Then the system applies the promotional fee of 0 EGP to the selected biller during the configured period without redeployment and records the admin action in the audit log. |

---

### User Story #25

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #25 (FR-25) |
| **User Story Name** | Audit Money Movements and Admin or Support Actions |
| **Actors** | Support |
| **Description** | As a Support employee, I want PayLink to record every money movement and action performed by Admin or Support, so that I can trace what happened, who initiated it, and when it occurred. |
| **Precondition** | A money movement occurs, or an Admin or Support employee performs an action covered by the audit rule. The system can identify the action’s initiator and the affected payment, wallet, account, or configuration item. |
| **Postcondition** | Every money movement has an audit record identifying the movement, exact amount, initiator, time, and reason. Every covered Admin or Support action has an audit record identifying the actor, action, affected item, and time. |
| **Acceptance Criteria** | **AC1 – Record a Wallet Movement**<br>• Given a wallet contains sufficient available funds for a payment.<br>• When the system moves 352.50 EGP from the available balance to the held balance.<br>• Then the audit log records the payment reference, wallet, exact amount, initiator, time, and reason for the hold.<br><br>**AC2 – Record the Release of Held Funds**<br>• Given a payment has failed while 352.50 EGP is held in the funding wallet.<br>• When the system releases the held amount to the wallet’s available balance.<br>• Then the release has its own audit record identifying the amount, initiator, time, and reason, linked to the failed payment. The original hold remains traceable.<br><br>**AC3 – Record Fees and Agent Commissions**<br>• Given an Agent payment has a service fee of 2.50 EGP and an Agent commission of 1.00 EGP.<br>• When the payment completes and the system records the fee and commission movements.<br>• Then every resulting money movement has an audit record linked to the payment, identifying its exact amount, initiator, time, and reason.<br><br>**AC4 – Record an Admin Action**<br>• Given an Admin is authorized to manage Billers.<br>• When the Admin disables a Biller.<br>• Then the audit log records the Admin’s identity, the affected Biller, the disable action, and the time of the action, even though no wallet balance changed.<br><br>**AC5 – Record a Support Action**<br>• Given a Support employee is authorized to block accounts.<br>• When the employee blocks an account.<br>• Then the audit log records the employee’s identity, the affected account, the block action, and the time of the action. |

---

### User Story #26

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #26 (FR-26) |
| **User Story Name** | Produce a Daily Reconciliation Report |
| **Actors** | Support |
| **Description** | As a Support employee, I want PayLink to compare a Biller’s daily settlement file with its payment records, so that I can identify discrepancies and review unresolved payments. |
| **Precondition** | The Biller’s settlement file is available, its reporting period can be identified, and PayLink has payment records for comparison. |
| **Postcondition** | PayLink produces a report identifying the Biller, reporting period, and any discrepancies for Support review. An Unknown payment is resolved only when reconciliation establishes its outcome. Every resulting balance change creates an immutable ledger entry and an audit record. |
| **Acceptance Criteria** | **AC1 – Produce a Report When All Records Match**<br>• Given a complete settlement file and PayLink’s records agree on every payment within the reporting period.<br>• When the system performs daily reconciliation.<br>• Then it produces a report identifying the Biller, reporting period, and zero discrepancies.<br><br>**AC2 – Flag a Payment Recorded as Failed**<br>• Given the settlement file lists a payment as received, while PayLink records the same payment reference as Failed.<br>• When the system compares the records.<br>• Then the report identifies the payment reference and both recorded outcomes for Support review. The payment is not changed from Failed to Completed.<br><br>**AC3 – Confirm an Unknown Payment**<br>• Given a payment is Unknown, its total amount is held, and a matching settlement entry confirms that the Biller received it.<br>• When the system processes the settlement entry.<br>• Then the payment becomes Completed, and the hold is converted into a final charge exactly once.<br><br>**AC4 – Resolve Confirmed Non-Receipt**<br>• Given an Unknown payment falls within the period covered by a complete settlement file.<br>• When reconciliation establishes that the Biller did not receive the payment.<br>• Then the payment becomes Failed, and the full held amount is returned to the available balance of the funding wallet. The payment becomes Refunded when the hold is released.<br><br>**AC5 – Handle an Incomplete Settlement File**<br>• Given an Unknown payment is absent from a settlement file known to be incomplete.<br>• When the system reviews that payment reference.<br>• Then the payment remains Unknown with its funds held, and its absence from the incomplete file does not trigger a refund. |

---

### User Story #27

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #27 (FR-27) |
| **User Story Name** | Request and Approve a Refund of a Completed Payment |
| **Actors** | Customer, Support |
| **Description** | As a Customer, I want to request a refund of a completed payment and have Support approve it, so that I can get my money back when a payment was made by mistake. |
| **Precondition** | The payment is Completed and belongs to the Customer. The Support employee is authenticated and authorized to review refund requests. |
| **Postcondition** | If approved, the funding wallet is credited through an immutable ledger entry and the decision is recorded in the audit log. The original payment stays Completed. If rejected, no balance changes. |
| **Acceptance Criteria** | **AC1 – Create a Refund Request**<br>Given a Completed payment belongs to the Customer.<br>When the Customer requests a refund with a reason.<br>Then a refund request is created with the status Pending and the payment stays Completed.<br><br>**AC2 – Support Approves the Request**<br>Given a refund request is Pending.<br>When Support approves it.<br>Then the refund amount is credited to the funding wallet exactly once through a ledger entry, the request becomes Approved, and the approver and the time are recorded in the audit log. Repeating approval of the same request creates no additional wallet credit.<br><br>**AC3 – Support Rejects the Request**<br>Given a refund request is Pending.<br>When Support rejects it with a reason.<br>Then the request becomes Rejected, no ledger entry is created, and the Customer is informed of the reason.<br><br>**AC4 – Payment Not Eligible**<br>Given a payment is in any state other than Completed.<br>When the Customer requests a refund for it.<br>Then the request is refused with a clear reason.<br><br>**AC5 – Duplicate Refund Request**<br>Given a payment already has a Pending or Approved refund request.<br>When a new refund request is sent for the same payment.<br>Then the request is refused and a second refund is not possible.<br><br>**AC6 – Payment of Another Customer**<br>Given a payment belongs to another Customer.<br>When a Customer requests a refund for it.<br>Then the request is refused and nothing is created. |

---

### User Story #28

| Field | Details |
| :--- | :--- |
| **User Story ID** | US #28 (FR-28) |
| **User Story Name** | View Daily Payment and Financial Totals |
| **Actors** | Admin |
| **Description** | As an Admin, I want to view daily totals for payments, fees earned, commissions paid, and failures per Biller, so that I can monitor PayLink's daily payment activity and financial performance. |
| **Precondition** | The Admin is authenticated and authorized to access daily reports. |
| **Postcondition** | The system displays the daily payment and financial totals, including the number of failures for each Biller. |
| **Acceptance Criteria** | **AC1 – View Daily Payment Totals**<br>Given the Admin requests the daily financial report for a specific day.<br>When the system generates the report.<br>Then the system displays the count and total amount in EGP of completed payments for that day.<br><br>**AC2 – View Daily Fees Earned**<br>Given completed payments with applicable service fees exist for a particular day.<br>When the Admin views the daily totals report.<br>Then the system displays the total fees earned from completed payments for that day.<br><br>**AC3 – View Daily Commissions Paid**<br>Given Agent commissions have been credited during a particular day.<br>When the Admin views the daily totals report.<br>Then the system displays the total commissions paid to Agents for that day.<br><br>**AC4 – View Failures Per Biller**<br>Given payment failures exist for one or more Billers during a particular day.<br>When the Admin views the daily totals report.<br>Then the system displays the number of failed payments for each Biller.<br><br>**AC5 – Handle Days with No Activity**<br>Given that no payment or financial activity exists for the selected day.<br>When the Admin opens the daily totals report.<br>Then the system displays zero values for the corresponding totals and indicates that there was no activity for that day.<br><br>**AC6 – Record Report Access**<br>Given the Admin is authorized to access the daily totals report.<br>When the Admin opens the report.<br>Then the system records the admin action in the audit log. |

---

## Nonfunctional requirements

| Quality | Target | Measurement method |
| :--- | :--- | :--- |
| **Correctness** | Zero double charges and zero negative wallet balances. | Automated tests sending simultaneous payments exceeding the available balance from the same wallet, and repeated payment requests using the same idempotency key. Verify that only one valid charge occurs and repeated requests return the original result. |
| **Performance** | Sustain at least 100 payment requests per second for 60 seconds against mock billers. | A 60-second load test generating at least 100 payment requests per second against local mock billers, while recording achieved throughput and response times. |
| **Resilience** | An outage of one biller does not prevent payments to other healthy billers. | Fault-injection test stopping or timing out one mock biller while sending payment requests to other healthy billers. Verify that payments to healthy billers continue successfully. |
| **Modifiability** | Adding a new biller requires changes only to the Biller Integration module and its configuration, without modifying existing core modules. | Git diff inspection after adding a new mock biller. Verify that no existing core payment or wallet modules were modified. |
| **Auditability** | 100% of money movements and admin actions have corresponding audit records containing who, when, and why. | Automated tests comparing all money movements and admin actions against their audit records, verifying the presence of actor, timestamp, and reason for each record. |
| **Testability** | At least 70% line coverage on domain logic, measured in CI. | Automated CI coverage report measuring line coverage of the domain logic after running the test suite. |

---

## Assumptions and decisions

| Reference | Actual Ambiguity | Team Decision / Current Status |
| :--- | :--- | :--- |
| **FR-9, FR-18** | Do payment limits apply to the bill amount or the total including fees? Which payment states count toward the daily limit? | Limits apply to the bill amount excluding fees. Payments in FundsHeld, SentToBiller, Unknown, and Completed count toward the daily limit. Rejected, Failed, and Refunded payments do not. Open: Confirm the daily time boundary and which day includes a payment resolved after midnight. |
| **FR-17, FR-18, FR-19** | Whose daily limit and velocity checks apply when an Agent pays on behalf of a Customer? | Proposed: Apply the checks to the Agent’s funding account, using its verification level to determine the daily limit. No separate Agent-specific limit is assumed. |
| **FR-17, FR-22** | Must a Customer be registered to pay through an Agent, and which notifications are sent to an unregistered Customer? | An Agent may pay for an unregistered guest using the guest’s bill account reference and valid mobile number. The Agent’s wallet funds the payment. Completion or failure notifications are sent by SMS to the provided number. Push delivery is skipped without being treated as a delivery failure. |
| **FR-10** | What happens when an existing idempotency key is reused with different payment details or after a rejected payment? | Different payment details with the same key cause a conflict rejection without changing the original payment or creating a new payment. An identical replay returns the original result, including Rejected. A new attempt requires a new key. |
| **FR-13** | Are status-check intervals measured from entry into Unknown or from the preceding check? | All intervals are measured from entry into Unknown. The original payment reference is queried after 1, 5, and 15 minutes while the outcome remains unresolved. Further scheduled checks stop when the outcome is confirmed. |
| **FR-14** | How long should PayLink wait for a final callback after TopUpNet returns accepted? | PayLink waits 5 minutes from receipt of accepted. If no final outcome is received, the payment becomes Unknown, the full amount remains held, and status checks follow FR-13. |
| **FR-19** | Does the velocity rule count successful payments only or all new payment attempts? | Every new payment attempt with a new idempotency key counts, even if subsequently rejected. Identical replays using an existing key do not count again. |
| **FR-26** | When can a missing settlement reference establish that the Biller did not receive the payment? | Absence is evidence only when the settlement file is confirmed complete and covers the payment. Otherwise, the payment remains Unknown with its funds held. Open: Define how completeness and coverage are established. |
| **FR-11, FR-17, FR-27** | How can a Completed payment be refunded while Completed is final, and what happens to its fee and Agent commission? | The original payment remains Completed. A separate Support-approved refund request produces separate ledger entries and credits the original funding wallet. Open: Confirm the refund amount and commission-reversal policy, including insufficient Agent funds. |
| **FR-17, FR-27** | Who may request a refund of a Completed payment made for an unregistered guest, and how is the requester verified? | Open: Define who submits the request on the guest’s behalf and how entitlement to request the refund is verified. The guest-payment decision does not automatically grant refund-request authority to the Agent. |
| **FR-16, FR-17** | How are fractional piastres rounded when calculating fees and commissions? | Monetary calculations use exact decimal arithmetic and amounts are rounded to two decimal places. Open: Specify the rounding rule for halfway cases and the calculation stage at which rounding occurs. |