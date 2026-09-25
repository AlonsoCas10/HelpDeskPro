-- Crea la base solo si no existe
IF DB_ID('HelpDeskDB') IS NULL
BEGIN
    CREATE DATABASE HelpDeskDB;
END
GO

-- Crea el login solo si no existe (mismo usuario/clave del application.properties)
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'helpdesk_user')
BEGIN
    CREATE LOGIN helpdesk_user WITH PASSWORD = 'Alonso10.';
END
GO

USE HelpDeskDB;
GO

-- Vincula el login como usuario dentro de HelpDeskDB
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'helpdesk_user')
BEGIN
    CREATE USER helpdesk_user FOR LOGIN helpdesk_user;
END
GO

-- Le da permisos suficientes para que Hibernate cree/actualice tablas (ddl-auto=update)
ALTER ROLE db_owner ADD MEMBER helpdesk_user;
GO
