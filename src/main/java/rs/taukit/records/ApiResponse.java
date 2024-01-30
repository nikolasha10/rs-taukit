package rs.taukit.records;

public record ApiResponse(boolean ok, int status, String statusText, String text) {
}
