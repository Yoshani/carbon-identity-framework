-- NOTE: This procedure assumes that the SAML IDP metadata is stored under the path
-- '/_system/governance/repository/identity/provider/saml' in the registry and only two corresponding RESOURCE entries
-- (one for the collection and one for the resource object) and one CONTENT entry exist for each SAML IDP metadata file.

CREATE OR REPLACE PROCEDURE WSO2_SAML_IDP_METADATA_CLEANUP AS
BEGIN

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE batchSize INT;
    DECLARE chunkSize INT;
    DECLARE batchCount INT;
    DECLARE chunkCount INT;
    DECLARE rowCount INT;
    DECLARE enableLog SMALLINT;
    DECLARE backupTables SMALLINT;

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    SET batchSize    = 10000; -- SET BATCH SIZE TO AVOID TABLE LOCKS [DEFAULT : 10000]
    SET chunkSize    = 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    SET enableLog    = 1; -- ENABLE LOGGING [DEFAULT : 1]
    SET backupTables = 1; -- SET IF REGISTRY TABLES NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : 1].

    SET rowCount = 0;
    SET batchCount = 1;
    SET chunkCount = 1;

    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        IF (enableLog = 1)
        THEN
            CALL DBMS_OUTPUT.PUT_LINE('ERROR OCCURRED: ' || SQLERRM);
        END IF;
        ROLLBACK;
    END;

    IF (enableLog = 1)
    THEN
        CALL DBMS_OUTPUT.PUT_LINE('WSO2_SAML_IDP_METADATA_CLEANUP() STARTED...!');
    END IF;

    -- ------------------------------------------
    -- GET PATH ID LIST TO DELETE
    -- ------------------------------------------
    DECLARE GLOBAL TEMPORARY TABLE RootPathIdList (ROOT_PATH_ID INT) ON COMMIT DELETE ROWS;
    DECLARE GLOBAL TEMPORARY TABLE PathIdList (REG_PATH_ID INT) ON COMMIT DELETE ROWS;
    INSERT INTO RootPathIdList (ROOT_PATH_ID) SELECT REG_PATH_ID FROM REG_PATH WHERE
     REG_PATH_VALUE = '/_system/governance/repository/identity/provider/saml';
    INSERT INTO PathIdList (REG_PATH_ID) SELECT REG_PATH_ID FROM REG_PATH WHERE REG_PATH_PARENT_ID IN
     (SELECT ROOT_PATH_ID FROM RootPathIdList);

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables = 1)
    THEN
        IF (enableLog = 1)
        THEN
            CALL DBMS_OUTPUT.PUT_LINE('TABLE BACKUP STARTED...!');
        END IF;

        IF (EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME = 'BAK_REG_RESOURCE'))
        THEN
            IF (enableLog = 1)
            THEN
                CALL DBMS_OUTPUT.PUT_LINE('DELETING OLD BACKUP...');
            END IF;
            DROP TABLE BAK_REG_RESOURCE;
            DROP TABLE BAK_REG_CONTENT;
        END IF;

        -- BACKUP REG_RESOURCE TABLE
        CREATE TABLE BAK_REG_RESOURCE AS (SELECT * FROM REG_RESOURCE WHERE REG_PATH_ID IN
         (SELECT REG_PATH_ID FROM PathIdList)) WITH DATA;

        -- BACKUP REG_CONTENT TABLE
        DECLARE GLOBAL TEMPORARY TABLE ContentIdList (REG_CONTENT_ID INT) ON COMMIT DELETE ROWS;
        INSERT INTO ContentIdList (REG_CONTENT_ID) SELECT DISTINCT REG_CONTENT_ID FROM BAK_REG_RESOURCE WHERE
         REG_CONTENT_ID IS NOT NULL;
        CREATE TABLE BAK_REG_CONTENT AS (SELECT * FROM REG_CONTENT WHERE REG_CONTENT_ID IN (SELECT REG_CONTENT_ID
         FROM ContentIdList)) WITH DATA;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------

    BEGIN TRANSACTION

    WHILE (chunkCount > 0)
    DO
        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS REG_RESOURCE_CHUNK_TMP;
        DROP TABLE IF EXISTS REG_CONTENT_CHUNK_TMP;

        CREATE TABLE REG_RESOURCE_CHUNK_TMP(REG_VERSION INT, REG_TENANT_ID INT, REG_CONTENT_ID INT);
        CREATE TABLE REG_CONTENT_CHUNK_TMP(REG_CONTENT_ID INT);

        INSERT INTO REG_RESOURCE_CHUNK_TMP SELECT REG_VERSION, REG_TENANT_ID, REG_CONTENT_ID FROM REG_RESOURCE WHERE
         REG_PATH_ID IN (SELECT REG_PATH_ID FROM PathIdList) LIMIT chunkSize;
        GET DIAGNOSTICS chunkCount = ROW_COUNT;
        INSERT INTO REG_CONTENT_CHUNK_TMP SELECT REG_CONTENT_ID FROM REG_RESOURCE_CHUNK_TMP WHERE REG_CONTENT_ID
         IS NOT NULL;

        CREATE INDEX REG_RESOURCE_CHUNK_TMP on REG_RESOURCE_CHUNK_TMP (REG_VERSION, REG_TENANT_ID, REG_CONTENT_ID)
        CREATE INDEX REG_CONTENT_CHUNK_TMP on REG_CONTENT_CHUNK_TMP (REG_CONTENT_ID)

        IF (enableLog = 1)
        THEN
            CALL DBMS_OUTPUT.PUT_LINE('CREATED REG_RESOURCE_CHUNK_TMP...');
        END IF;

        -- BATCH LOOP
        SET batchCount = 1;
        WHILE (batchCount > 0)
        DO
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS REG_RESOURCE_BATCH_TMP;
            DROP TABLE IF EXISTS REG_CONTENT_BATCH_TMP;

            CREATE TABLE REG_RESOURCE_BATCH_TMP(REG_VERSION INT, REG_TENANT_ID INT, REG_CONTENT_ID INT);
            CREATE TABLE REG_CONTENT_BATCH_TMP(REG_CONTENT_ID INT);

            INSERT INTO REG_RESOURCE_BATCH_TMP(REG_VERSION, REG_TENANT_ID, REG_CONTENT_ID) SELECT REG_VERSION,
             REG_TENANT_ID, REG_CONTENT_ID FROM REG_RESOURCE_CHUNK_TMP LIMIT batchSize;
            GET DIAGNOSTICS batchCount = ROW_COUNT;
            INSERT INTO REG_CONTENT_BATCH_TMP(REG_CONTENT_ID) SELECT REG_CONTENT_ID FROM REG_RESOURCE_BATCH_TMP
             WHERE REG_CONTENT_ID IS NOT NULL;

            CREATE INDEX REG_RESOURCE_BATCH_TMP on REG_RESOURCE_BATCH_TMP (REG_VERSION, REG_TENANT_ID);
            CREATE INDEX REG_CONTENT_BATCH_TMP on REG_CONTENT_BATCH_TMP (REG_CONTENT_ID);

            IF (enableLog = 1)
            THEN
                CALL DBMS_OUTPUT.PUT_LINE('CREATED REG_RESOURCE_BATCH_TMP...');
            END IF;

            -- BATCH DELETION
            IF (enableLog = 1)
            THEN
                CALL DBMS_OUTPUT.PUT_LINE('BATCH DELETE STARTED ON REG_RESOURCE...');
            END IF;

            DELETE r FROM REG_RESOURCE r INNER JOIN REG_RESOURCE_BATCH_TMP tmp ON r.REG_VERSION = tmp.REG_VERSION
             AND r.REG_TENANT_ID = tmp.REG_TENANT_ID;
            GET DIAGNOSTICS rowCount = ROW_COUNT;
            DELETE FROM REG_CONTENT WHERE REG_CONTENT_ID IN (SELECT REG_CONTENT_ID FROM REG_CONTENT_BATCH_TMP);

            IF (enableLog = 1)
            THEN
                CALL DBMS_OUTPUT.PUT_LINE('BATCH DELETE FINISHED ON REG_RESOURCE : ' || rowCount);
            END IF;

            -- DELETE FROM CHUNK
            DELETE r FROM REG_RESOURCE_CHUNK_TMP r INNER JOIN  REG_RESOURCE_BATCH_TMP tmp ON
             r.REG_VERSION = tmp.REG_VERSION AND r.REG_TENANT_ID = tmp.REG_TENANT_ID;
        END WHILE;
    END WHILE;

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS REG_RESOURCE_BATCH_TMP;
    DROP TABLE IF EXISTS REG_CONTENT_BATCH_TMP;
    DROP TABLE IF EXISTS REG_RESOURCE_CHUNK_TMP;
    DROP TABLE IF EXISTS REG_CONTENT_CHUNK_TMP;

    COMMIT;

    IF (enableLog = 1)
    THEN
        CALL DBMS_OUTPUT.PUT_LINE('CLEANUP COMPLETED...!');
    END IF;

END;
