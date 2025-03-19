package com.kt.fire.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.List;

@Entity
@Table(name = "regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Regions {
    
    @Id
    @Column(name = "district_id")
    private String districtId;

    @Column(name = "district_name", nullable = false)
    private String districtName;

    @Column(name = "province_name", nullable = false)
    private String provinceName;

    @OneToMany(mappedBy = "region")
    @JsonBackReference
    private List<FireReports> fireReports;
} 