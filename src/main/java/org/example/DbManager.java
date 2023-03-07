package org.example;

import java.sql.*;

public class DbManager {
    static Connection conn = null;
    static String url = "jdbc:sqlite:ExamResultsDB.db";

    public static Connection getConn() {
        if (conn == null) {
            try {
                conn = DriverManager.getConnection(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            if (!conn.isValid(1000)) {
                conn = DriverManager.getConnection(url);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
