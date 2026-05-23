package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    private static final String URL =
            "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres?sslmode=require";

    private static final String USER = "postgres.xxfetkbnbeayaczpftre";
    private static final String PASSWORD = "Shekinahbantatua";

    public static Connection connect() {
        try {

            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("CONNECTED SUCCESSFULLY!");

            return conn;

        } catch (Exception e) {
            System.out.println("CONNECTION FAILED");
            e.printStackTrace();

            return null;
        }
    }
}