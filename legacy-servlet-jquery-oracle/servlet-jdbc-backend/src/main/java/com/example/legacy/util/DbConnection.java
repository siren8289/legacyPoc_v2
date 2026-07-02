package com.example.legacy.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    // 레거시 재현: Oracle 접속 정보 하드코딩
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "legacy_user";
    private static final String PASSWORD = "legacy_pass";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
