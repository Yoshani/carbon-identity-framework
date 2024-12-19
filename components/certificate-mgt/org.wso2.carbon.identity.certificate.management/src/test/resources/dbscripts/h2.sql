CREATE TABLE IF NOT EXISTS IDN_CERTIFICATE (
            ID INTEGER NOT NULL AUTO_INCREMENT,
            UUID CHAR(36),
            NAME VARCHAR(100),
            CERTIFICATE_IN_PEM BLOB,
            TENANT_ID INTEGER DEFAULT 0,
            PRIMARY KEY(ID),
            CONSTRAINT CERTIFICATE_UNIQUE_KEY UNIQUE (NAME, TENANT_ID)
);
