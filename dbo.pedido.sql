USE InventarioDB;
GO

IF OBJECT_ID('dbo.pedido', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.pedido (
        id BIGINT IDENTITY(1,1) NOT NULL,
        cliente VARCHAR(120) NOT NULL,
        producto_codigo VARCHAR(30) NOT NULL,
        producto_nombre VARCHAR(120) NOT NULL,
        precio_unitario DECIMAL(12,2) NOT NULL,
        cantidad INT NOT NULL,
        total DECIMAL(12,2) NOT NULL,
        fecha DATETIME2 NOT NULL,

        CONSTRAINT PK_pedido PRIMARY KEY (id)
    );
END;
GO