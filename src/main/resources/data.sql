DROP TABLE IF EXISTS documents;
CREATE TABLE documents
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64),
    version       INT         NOT NULL DEFAULT 0
);
