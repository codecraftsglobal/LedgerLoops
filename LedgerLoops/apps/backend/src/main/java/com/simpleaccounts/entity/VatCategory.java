package com.simpleaccounts.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

/**
 * Created by Uday on 9/28/2017.
 */
@NamedQueries({
    @NamedQuery(name = "allVatCategory",
            query = "SELECT v FROM VatCategory v  where v.deleteFlag = FALSE order by v.defaultFlag DESC, v.orderSequence,v.name ASC ")
})
@Entity
@Table(name = "VAT_CATEGORY")
@Data
@NoArgsConstructor
public class VatCategory implements Serializable {

    private static final long serialVersionUID = 1L;
    	@Id
	@SequenceGenerator(name="VAT_CATEGORY_SEQ", sequenceName="VAT_CATEGORY_SEQ", allocationSize=1, initialValue = 10000)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="VAT_CATEGORY_SEQ")
    @Basic(optional = false)
    @Column(name = "VAT_CATEGORY_ID", updatable = false, nullable = false)
    private Integer id;
    @Basic
    @Column(name = "NAME")
    private String name;
    @Basic
    @Column(name = "VAT")
    private BigDecimal vat;
    @Column(name = "TAX_TYPE", nullable = false, length = 32)
    private String taxType = "SALES_TAX";
    @Column(name = "TAX_CODE", length = 64)
    private String taxCode;
    @Column(name = "COUNTRY_ISO_CODE", length = 3)
    private String countryIsoCode;
    @Column(name = "REGION_CODE", length = 64)
    private String regionCode;
    @Column(name = "EFFECTIVE_FROM")
    private LocalDate effectiveFrom;
    @Column(name = "EFFECTIVE_TO")
    private LocalDate effectiveTo;
    @Column(name = "CALCULATION_MODE", nullable = false, length = 32)
    private String calculationMode = "FLAT_RATE";
    @Column(name = "MINIMUM_AMOUNT", precision = 19, scale = 2)
    private BigDecimal minimumAmount;
    @Column(name = "MAXIMUM_AMOUNT", precision = 19, scale = 2)
    private BigDecimal maximumAmount;
    @Column(name = "DEFAULT_FLAG")
    @ColumnDefault(value = "'N'")
    private Character defaultFlag;
    @Column(name = "ORDER_SEQUENCE")
    @Basic(optional = true)
    private Integer orderSequence;
    @Column(name = "CREATED_BY")
    @ColumnDefault(value = "0")
    @Basic(optional = false)
    private Integer createdBy = 0;
    @Column(name = "CREATED_DATE")
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    @Basic(optional = false)

    private LocalDateTime createdDate = LocalDateTime.now();
    @Column(name = "LAST_UPDATED_BY")
    private Integer lastUpdateBy;
    @Column(name = "LAST_UPDATE_DATE")

    private LocalDateTime lastUpdateDate = LocalDateTime.now();
    @Column(name = "DELETE_FLAG")
    @ColumnDefault(value = "false")
    private Boolean deleteFlag = Boolean.FALSE;
    @Column(name = "VERSION_NUMBER")
    @ColumnDefault(value = "1")
    @Version
    private Integer versionNumber = 1;
    @Transient
    private String vatLabel;

    public VatCategory(Integer id) {
        this.id = id;
    }

    public String getVatLabel() {
        return name + "(" + vat + ")";
    }

}
