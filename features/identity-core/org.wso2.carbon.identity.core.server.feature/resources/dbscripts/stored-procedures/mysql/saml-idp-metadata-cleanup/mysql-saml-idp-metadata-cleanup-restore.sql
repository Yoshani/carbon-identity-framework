-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE rowCount INT;
DECLARE enableLog BOOLEAN;
DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
    ROLLBACK;
    IF (enableLog)
    THEN
        SELECT 'ERROR OCCURRED: TRANSACTION ROLLED BACK' AS 'INFO LOG';
    END IF;
END;

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET enableLog = TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]

DROP TEMPORARY TABLE IF EXISTS LOGGER;
CREATE TEMPORARY TABLE LOGGER (MESSAGE TEXT);
IF (enableLog)
THEN
    INSERT INTO LOGGER VALUES ('SAML IDP METADATA CLEANUP DATA RESTORATION STARTED...!');
END IF;

SELECT COUNT(1) INTO rowCount  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA IN (SELECT DATABASE()) AND TABLE_NAME IN ('REG_RESOURCE');
IF (rowCount = 1)
THEN
    START TRANSACTION;
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

    INSERT INTO REG_CONTENT (REG_CONTENT_ID, REG_CONTENT_DATA, REG_TENANT_ID) SELECT A.REG_CONTENT_ID,
     A.REG_CONTENT_DATA, A.REG_TENANT_ID FROM BAK_REG_CONTENT A LEFT JOIN REG_CONTENT B ON
     A.REG_CONTENT_ID = B.REG_CONTENT_ID WHERE B.REG_CONTENT_ID IS NULL;

    INSERT INTO REG_RESOURCE (REG_PATH_ID, REG_NAME, REG_VERSION, REG_MEDIA_TYPE, REG_CREATOR, REG_CREATED_TIME,
     REG_LAST_UPDATOR, REG_LAST_UPDATED_TIME, REG_DESCRIPTION, REG_CONTENT_ID, REG_TENANT_ID, REG_UUID) SELECT
     A.REG_PATH_ID, A.REG_NAME, A.REG_VERSION, A.REG_MEDIA_TYPE, A.REG_CREATOR, A.REG_CREATED_TIME, A.REG_LAST_UPDATOR,
     A.REG_LAST_UPDATED_TIME, A.REG_DESCRIPTION, A.REG_CONTENT_ID, A.REG_TENANT_ID, A.REG_UUID FROM BAK_REG_RESOURCE A LEFT
     JOIN REG_RESOURCE B ON A.REG_VERSION = B.REG_VERSION AND A.REG_TENANT_ID = B.REG_TENANT_ID WHERE B.REG_VERSION IS NULL;
    SELECT row_count() INTO rowCount;
    IF (enableLog)
    THEN
        INSERT INTO LOGGER VALUES (CONCAT('CLEANUP DATA RESTORATION COMPLETED ON REG_RESOURCE WITH ', rowCount));
    END IF;
    COMMIT;
END IF;

IF (enableLog)
THEN
    INSERT INTO LOGGER VALUES ('CLEANUP DATA RESTORATION COMPLETED...!');
END IF;

SELECT * FROM LOGGER;
