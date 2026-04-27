package com.example.legacy;

import com.example.legacy.servlet.WarehouseIoServlet;
import com.example.legacy.servlet.InvestorServlet;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LegacySampleServer {

    // 단일 프로세스 샘플 서버: API와 정적 파일 서빙을 한 JVM에서 함께 처리한다.
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "front/src/main/webapp";

    public static void main(String[] args) throws IOException {
        // 로컬 데모 편의를 위해 전통적인 WAS 대신 경량 HTTP 서버를 사용한다.
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        WarehouseIoServlet servlet = new WarehouseIoServlet();
        InvestorServlet investorServlet = new InvestorServlet();

        // 창고(warehouse) 레거시 action API 엔드포인트.
        server.createContext("/warehouse", exchange -> {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String body = servlet.processAction(params.get("action"), params);
            send(exchange, 200, "application/json; charset=UTF-8", body);
        });

        // 기존 화면 호환용 엔드포인트.
        server.createContext("/api/warehouse-io", exchange -> {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            params.put("action", "list");
            String body = servlet.processAction("list", params);
            send(exchange, 200, "application/json; charset=UTF-8", body);
        });

        server.createContext("/investor", exchange -> {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String body = investorServlet.processAction(params.get("action"), params);
            send(exchange, 200, "application/json; charset=UTF-8", body);
        });

        // HTML/CSS/JS 정적 파일 제공 엔드포인트.
        server.createContext("/", exchange -> handleStatic(exchange));
        server.setExecutor(null);
        server.start();

        System.out.println("Legacy sample server started");
        System.out.println("Open: http://localhost:" + PORT + "/index.html");
    }

    private static void handleStatic(HttpExchange exchange) throws IOException {
        // "/" 요청은 메인 메뉴 페이지로 라우팅한다.
        String requestPath = exchange.getRequestURI().getPath();
        if ("/".equals(requestPath)) {
            requestPath = "/index.html";
        }

        // 웹 루트 기준으로 정적 경로를 계산하고, 없는 경로는 404 처리한다.
        Path resolved = Paths.get(WEB_ROOT + requestPath).normalize();
        if (!Files.exists(resolved) || Files.isDirectory(resolved)) {
            send(exchange, 404, "text/plain; charset=UTF-8", "Not Found");
            return;
        }

        byte[] body = Files.readAllBytes(resolved);
        send(exchange, 200, contentType(resolved.toString()), body);
    }

    private static Map<String, String> parseQuery(URI uri) {
        // 최소 쿼리 파서. action 스타일 API가 이 맵을 기반으로 동작한다.
        Map<String, String> result = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return result;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = decode(kv[0]);
            String value = kv.length > 1 ? decode(kv[1]) : "";
            result.put(key, value);
        }
        return result;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String contentType(String path) {
        // 정적 리소스용 기본 Content-Type 매핑.
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        return "text/plain; charset=UTF-8";
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        send(exchange, status, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        // 모든 엔드포인트에서 공통으로 사용하는 응답 작성 함수.
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
