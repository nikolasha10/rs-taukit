package rs.taukit.records;

public record TestResults(String status, String finished, String errorMessage, String errorCategory, String autotestId,
                          String tmsLink) {
}
