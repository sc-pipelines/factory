package pl.com.bottega.factory.product.management;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.com.bottega.tools.JsonConverter;

import javax.persistence.*;

@Entity(name = "ProductDescription")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "refNo")
public class ProductDescriptionEntity {

    @Id
    @Column
    private String refNo;

    @Column
    @Convert(converter = DescriptionAsJson.class)
    ProductDescription description;

    public ProductDescriptionEntity(String refNo, ProductDescription description) {
        this.refNo = refNo;
        this.description = description;
    }

    public static class DescriptionAsJson extends JsonConverter<ProductDescription> {
        public DescriptionAsJson() {
            super(ProductDescription.class);
        }
    }
}