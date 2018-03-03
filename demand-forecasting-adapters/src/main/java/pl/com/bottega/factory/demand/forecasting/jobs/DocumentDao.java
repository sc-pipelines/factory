package pl.com.bottega.factory.demand.forecasting.jobs;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import pl.com.bottega.tools.ProjectionRepository;

import java.time.LocalDate;

@Repository("documentDao")
@RepositoryRestResource(path = "demand-documents",
        collectionResourceRel = "demand-documents",
        itemResourceRel = "demand-document")
public interface DocumentDao extends ProjectionRepository<DocumentEntity, Long> {
    @RestResource(exported = false)
    void deleteByCleanAfterGreaterThanEqual(LocalDate date);
}
