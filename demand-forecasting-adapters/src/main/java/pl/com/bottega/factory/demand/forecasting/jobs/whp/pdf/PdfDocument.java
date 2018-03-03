package pl.com.bottega.factory.demand.forecasting.jobs.whp.pdf;

import org.springframework.util.DigestUtils;
import pl.com.bottega.factory.demand.forecasting.Document;

import java.time.Instant;
import java.util.stream.Stream;

public class PdfDocument {

    public static PdfDocument parse(byte[] data) {
        return new PdfDocument();
    }

    public Instant getModificationDate() {
        return Instant.now();
    }

    public String getContentHash() {
        return DigestUtils.md5DigestAsHex(new byte[0]);
    }

    public Stream<Document> content() {
        return Stream.of();
    }
}
