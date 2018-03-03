package pl.com.bottega.factory.demand.forecasting.jobs;

import lombok.Value;

import java.time.Instant;
import java.util.Optional;

public interface DocumentStore {

    Stored saveIfNotExists(Key key, byte[] value);

    @Value
    class Key {
        String uri;
        Instant instant;
        String hash;
    }

    @Value
    class Stored {
        String uri;
        boolean storedSuccessfully;
        boolean alreadyPresent;
        String message;

        public Optional<String> getMessage() {
            return Optional.ofNullable(message);
        }
    }
}
