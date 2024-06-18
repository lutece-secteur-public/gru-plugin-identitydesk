DROP TABLE IF EXISTS identitydesk_account_creation_tasks;
CREATE TABLE identitydesk_account_creation_tasks
(
    customer_id VARCHAR(50) UNIQUE,
    task_code   VARCHAR(50) UNIQUE
);