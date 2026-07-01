package com.example.legacy.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    // 레거시 재현을 위해 DB 접속 정보를 코드에 하드코딩했다(의도적).
    // 원본 프로젝트는 Oracle 기준으로 접속한다.
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "legacy_user";
    private static final String PASSWORD = "legacy_pass";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
