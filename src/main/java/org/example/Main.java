package org.example;

import java.sql.*;

public class Main {
    // Configuración de la base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/tienda_online";
    private static final String USER = "root"; // Cambia esto según tu configuración
    private static final String PASSWORD = "tu contraseña"; // Cambia esto según tu configuración

    public static void main(String[] args) {
        // Procesar pedidos de Ana y Juan
        processOrder("Ana", 10);
        processOrder("Juan", 10);
    }

    // Método para procesar un pedido
    public static void processOrder(String clientName, int quantity) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            connection.setAutoCommit(false); // Iniciar transacción
            System.out.println("Iniciando transacción para " + clientName);

            // Leer el stock del producto
            String readStockSQL = "SELECT stock FROM productos WHERE id = ?";
            try (PreparedStatement readStockStmt = connection.prepareStatement(readStockSQL)) {
                readStockStmt.setInt(1, 1); // ID del producto
                ResultSet rs = readStockStmt.executeQuery();

                // Verificar si se encontró el producto
                if (rs.next()) {
                    int stock = rs.getInt("stock");
                    System.out.println("Stock actual para el producto: " + stock);

                    // Verificar y actualizar el stock
                    if (stock >= quantity) {
                        String updateStockSQL = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";
                        try (PreparedStatement updateStockStmt = connection.prepareStatement(updateStockSQL)) {
                            updateStockStmt.setInt(1, quantity);
                            updateStockStmt.setInt(2, 1); // ID del producto
                            updateStockStmt.setInt(3, quantity);
                            int rowsAffected = updateStockStmt.executeUpdate();

                            // Comprobar si se actualizó el stock correctamente
                            if (rowsAffected > 0) {
                                connection.commit(); // Confirmar transacción
                                System.out.println(clientName + " ha realizado la compra. Stock actualizado.");
                            } else {
                                System.out.println("Rollback: Stock insuficiente para " + clientName);
                                connection.rollback(); // Deshacer cambios
                            }
                        }
                    } else {
                        System.out.println("Rollback: Stock insuficiente para " + clientName + ". Se requerían " + quantity + " pero solo hay " + stock);
                        connection.rollback(); // Deshacer cambios
                    }
                } else {
                    System.out.println("No se encontró el producto.");
                    connection.rollback(); // Deshacer cambios
                }
            } catch (SQLException e) {
                connection.rollback(); // Deshacer cambios en caso de error
                System.out.println("Rollback debido a error en la transacción: " + e.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejar excepciones
        }
    }
}
