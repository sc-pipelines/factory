package pl.com.bottega.factory.demand.forecasting.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(exported = false)
public interface ProductDemandDao extends JpaRepository<ProductDemandEntity, Long> {
    ProductDemandEntity findByRefNo(String refNo);
}
