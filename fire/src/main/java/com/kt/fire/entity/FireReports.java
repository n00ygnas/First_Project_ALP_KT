package com.kt.fire.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "fire_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FireReports {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "district_id", nullable = false)
    private String districtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", referencedColumnName = "district_id", insertable = false, updatable = false)
    @JsonManagedReference
    private Regions region;

    @Column(name = "fire_type")
    private String fireType;

    @Column(name = "ignition_cause_sub")
    private String ignitionCauseSub;

    @Column(name = "casualty_total")
    private Integer casualtyTotal;

    @Column(name = "death")
    private Integer death;

    @Column(name = "injury")
    private Integer injury;

    @Column(name = "property_damage_total")
    private Long propertyDamageTotal;

    @Column(name = "location_category_sub")
    private String locationCategorySub;
} 