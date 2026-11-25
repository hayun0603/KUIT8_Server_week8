package webserver;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpMethod;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.from(reader);
            HttpResponse response = new HttpResponse(out);
            route(request, response);
        } catch (Exception e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void route(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();

        if (request.getMethod() == HttpMethod.GET && (path.equals("/") || path.equals("/index.html"))) {
            response.forward("/index.html");
            return;
        }
        if (request.getMethod() == HttpMethod.GET && path.equals("/css/styles.css")) {
            response.forward("/css/styles.css");
            return;
        }
        if (request.getMethod() == HttpMethod.GET && path.equals("/user/signup")) {
            signup(request, response);
            return;
        }
        if (request.getMethod() == HttpMethod.POST && path.equals("/user/signup")) {
            signup(request, response);
            return;
        }
        if (request.getMethod() == HttpMethod.POST && path.equals("/user/login")) {
            login(request, response);
            return;
        }
        if (request.getMethod() == HttpMethod.GET
                && (path.equals("/user/userList") || path.equals("/user/list.html"))) {
            if (request.isLogined()) {
                response.forward("/user/list.html");
            } else {
                response.redirect("/user/login.html");
            }
            return;
        }
        if (request.getMethod() == HttpMethod.GET && path.endsWith(".html")) {
            response.forward(path);
            return;
        }
        response.forward(path);
    }

    private void signup(HttpRequest request, HttpResponse response) throws IOException {
        MapParameter parameter = new MapParameter(request);
        User user = new User(
                parameter.get("userId"),
                parameter.get("password"),
                parameter.get("name"),
                parameter.get("email"));
        MemoryUserRepository.getInstance().addUser(user);
        response.redirect("/index.html");
    }

    private void login(HttpRequest request, HttpResponse response) throws IOException {
        MapParameter parameter = new MapParameter(request);
        User user = MemoryUserRepository.getInstance().findUserById(parameter.get("userId"));
        if (user != null && user.getPassword().equals(parameter.get("password"))) {
            response.redirect("/index.html", "logined=true; Path=/");
        } else {
            response.redirect("/user/login_failed.html");
        }
    }

    private static class MapParameter {
        private final HttpRequest request;

        private MapParameter(HttpRequest request) {
            this.request = request;
        }

        private String get(String name) {
            String value = request.getParameter(name);
            return value == null ? "" : value;
        }
    }
}