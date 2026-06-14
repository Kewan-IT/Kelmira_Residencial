package mz.kelmira;

import java.sql.Connection;
import java.sql.DriverManager;

public class TesteConexao {
    public static void main(String[] args) {
        String url = "jdbc:mariadb://localhost:3306/kelmira_db";
        String user = "kelmira_user";
        String pass = "senha123";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Conexão bem-sucedida!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}