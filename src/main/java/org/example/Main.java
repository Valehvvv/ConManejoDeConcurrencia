package org.example;

import java.sql.*;
import java.util.concurrent.*;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3306/tienda_online";
    private static final String USER = "root"; // Cambia esto según tu configuración
    private static final String PASSWORD = "tom1"; // Cambia esto según tu configuración

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.schedule(() -> processOrder("Ana", 10), getDelay("11:29"), TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> processOrder("Juan", 10), getDelay("11:29"), TimeUnit.MILLISECONDS);
    }

    private static long getDelay(String time) {
        // Implementar la lógica para calcular el retraso en milisegundos hasta la hora deseada
        return 0; // Ajusta este retorno según tu lógica
    }

    public static void processOrder(String clientName, int quantity) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            connection.setAutoCommit(false); // Iniciar transacción
            System.out.println("Iniciando transacción para " + clientName);

            // Leer el stock
            String readStockSQL = "SELECT stock FROM productos WHERE id = ?";
            try (PreparedStatement readStockStmt = connection.prepareStatement(readStockSQL)) {
                readStockStmt.setInt(1, 1); // ID del producto
                ResultSet rs = readStockStmt.executeQuery();
                if (rs.next()) {
                    int stock = rs.getInt("stock");
                    System.out.println("Stock actual para el producto: " + stock);

                    // Verificar y actualizar stock
                    if (stock >= quantity) {
                        String updateStockSQL = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";
                        try (PreparedStatement updateStockStmt = connection.prepareStatement(updateStockSQL)) {
                            updateStockStmt.setInt(1, quantity);
                            updateStockStmt.setInt(2, 1); // ID del producto
                            updateStockStmt.setInt(3, quantity);
                            int rowsAffected = updateStockStmt.executeUpdate();
                            if (rowsAffected > 0) {
                                connection.commit();
                                System.out.println(clientName + " ha realizado la compra. Stock actualizado.");
                            } else {
                                System.out.println("Rollback: Stock insuficiente para " + clientName);
                                connection.rollback();
                            }
                        }
                    } else {
                        System.out.println("Rollback: Stock insuficiente para " + clientName + ". Se requerían " + quantity + " pero solo hay " + stock);
                        connection.rollback();
                    }
                } else {
                    System.out.println("No se encontró el producto.");
                    connection.rollback();
                }
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Rollback debido a error en la transacción: " + e.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
