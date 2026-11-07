-- No contiene secretos: sqlcmd lee los valores de las variables de entorno.
-- Se puede ejecutar varias veces: solo crea lo que falta.

IF DB_ID('$(DB_NAME)') IS NULL
BEGIN
    CREATE DATABASE [$(DB_NAME)];
END
GO

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = '$(DB_USER)')
BEGIN
    CREATE LOGIN [$(DB_USER)] WITH PASSWORD = '$(DB_PASSWORD)';
END
GO

USE [$(DB_NAME)];
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = '$(DB_USER)')
BEGIN
    CREATE USER [$(DB_USER)] FOR LOGIN [$(DB_USER)];
END
GO

ALTER ROLE db_owner ADD MEMBER [$(DB_USER)];
GO
