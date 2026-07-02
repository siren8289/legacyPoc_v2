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

/**
 * 레거시 샘플 통합 서버.
 * WAS 없이 com.sun.net.httpserver로 API와 jQuery 정적 파일을 한 프로세스에서 제공한다.
 */
public class LegacySampleServer {

    private static final int PORT = 8080;
    private static final String WEB_ROOT = "jquery-webapp/src/main/webapp";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        WarehouseIoServlet servlet = new WarehouseIoServlet();
        InvestorServlet investorServlet = new InvestorServlet();

        // 입출고 action API: /warehouse?action=list|detail|createEmptyRow|update*|delete
        server.createContext("/warehouse", exchange -> {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String body = servlet.processAction(params.get("action"), params);
            send(exchange, 200, "application/json; charset=UTF-8", body);
        });

        // 주문/정산/대시보드 화면 호환용 목록 API (warehouse list 재사용)
        server.createContext("/api/warehouse-io", exchange -> {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            params.put("action", "list");
            String body = servlet.processAction("list", params);
            send(exchange, 200, "application/json; charset=UTF-8", body);
        });

        // 투자자 action API: /investor?action=list|detail|create
        server.createContext("/investor", exchange -> {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String body = investorServlet.processAction(params.get("action"), params);
            send(exchange, 200, "application/json; charset=UTF-8", body);
        });

        server.createContext("/", exchange -> handleStatic(exchange));
        server.setExecutor(null);
        server.start();

        System.out.println("Legacy sample server started");
        System.out.println("Open: http://localhost:" + PORT + "/index.html");
    }

    private static void handleStatic(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        if ("/".equals(requestPath)) {
            requestPath = "/index.html";
        }

        Path resolved = Paths.get(WEB_ROOT + requestPath).normalize();
        if (!Files.exists(resolved) || Files.isDirectory(resolved)) {
            send(exchange, 404, "text/plain; charset=UTF-8", "Not Found");
            return;
        }

        byte[] body = Files.readAllBytes(resolved);
        send(exchange, 200, contentType(resolved.toString()), body);
    }

    private static Map<String, String> parseQuery(URI uri) {
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
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        return "text/plain; charset=UTF-8";
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        send(exchange, status, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
