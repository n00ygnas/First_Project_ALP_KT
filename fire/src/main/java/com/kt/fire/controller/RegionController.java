package com.kt.fire.controller;

import com.kt.fire.entity.Regions;
import com.kt.fire.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping("/provinces")
    public ResponseEntity<List<String>> getAllProvinces() {
        try {
            List<String> provinces = regionService.getAllProvinces();
            if (provinces == null || provinces.isEmpty()) {
                System.out.println("No provinces found in the database");
                return ResponseEntity.noContent().build();
            }
            System.out.println("Found provinces: " + provinces);
            return ResponseEntity.ok(provinces);
        } catch (Exception e) {
            System.err.println("Error getting provinces: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/provinces/{provinceName}/districts")
    public ResponseEntity<List<Regions>> getDistrictsInProvince(@PathVariable String provinceName) {
        List<Regions> districts = regionService.getDistrictsInProvince(provinceName);
        return ResponseEntity.ok(districts);
    }

    @GetMapping
    public ResponseEntity<List<Regions>> getAllRegions() {
        return ResponseEntity.ok(regionService.getAllRegionsOrdered());
    }
} 