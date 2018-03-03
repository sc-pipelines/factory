package pl.com.bottega.factory.demand.forecasting.jobs;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import pl.com.bottega.factory.demand.forecasting.DemandService;
import pl.com.bottega.factory.demand.forecasting.jobs.DocumentStore.Key;
import pl.com.bottega.factory.demand.forecasting.jobs.DocumentStore.Stored;
import pl.com.bottega.factory.demand.forecasting.jobs.whp.pdf.PdfDocument;

import java.net.URL;
import java.time.Instant;

@Setter
@Transactional
@RequiredArgsConstructor
public class ImportDemandsJob implements Job {

    private final DocumentStore store;
    private final DocumentDao dao;
    private final DemandService service;

    private String uri;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            URL resource = new URL(this.uri);
            byte[] data = StreamUtils.copyToByteArray(resource.openStream());
            PdfDocument document = PdfDocument.parse(data);
            Instant instant = document.getModificationDate();
            String hash = document.getContentHash();
            Stored info = store.saveIfNotExists(new Key(uri, instant, hash), data);
            if (!info.isAlreadyPresent()) {
                document.content().forEach(chunk -> {
                    dao.save(new DocumentEntity(uri, info.getUri(), chunk));
                    service.process(chunk);
                });
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
