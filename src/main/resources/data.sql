DROP TABLE IF EXISTS documents;
CREATE TABLE documents
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    type          VARCHAR(64) NOT NULL,
    serial_number VARCHAR(64),
    pages         INT         NOT NULL DEFAULT 0,
    version       INT         NOT NULL DEFAULT 0,
    node VARCHAR(100),
    thread VARCHAR(100)
);
