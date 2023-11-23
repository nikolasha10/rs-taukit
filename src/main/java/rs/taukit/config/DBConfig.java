package rs.taukit.config;

public record DBConfig(String type, String host, String port, String name, String scheme, String user,
                       String password) {
}
