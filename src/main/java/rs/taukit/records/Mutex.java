package rs.taukit.records;

import java.util.concurrent.Semaphore;

public record Mutex(String scenarioName, long startFrom, Semaphore semaphore) {
}
