package com.picknquicks.controller;
import com.picknquicks.dto.request.CreateBrandRequest;
import com.picknquicks.dto.request.UpdateBrandRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.BrandResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.service.brand.BrandService;
import com.picknquicks.util.PagingAndSortingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Brand management endpoints")
public class BrandController {

    private final BrandService brandService;
    private final PagingAndSortingHelper pagingHelper;

    @GetMapping
    @Operation(summary = "Get all brands", description = "Get paginated list of all brands")
    public ResponseEntity<ApiResponse> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<BrandResponse> response = brandService.getAllBrands(pageable);
        return ResponseEntity.ok(ApiResponse.success("Brands fetched successfully", response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active brands")
    public ResponseEntity<ApiResponse> getActiveBrands() {
        return ResponseEntity.ok(ApiResponse.success("Active brands fetched successfully", brandService.getActiveBrands()));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured brands")
    public ResponseEntity<ApiResponse> getFeaturedBrands() {
        return ResponseEntity.ok(ApiResponse.success("Featured brands fetched successfully", brandService.getFeaturedBrands()));
    }

    @GetMapping("/{brandId}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<ApiResponse> getBrandById(@PathVariable UUID brandId) {
        BrandResponse response = brandService.getBrandById(brandId);
        return ResponseEntity.ok(ApiResponse.success("Brand fetched successfully", response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get brand by slug")
    public ResponseEntity<ApiResponse> getBrandBySlug(@PathVariable String slug) {
        BrandResponse response = brandService.getBrandBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Brand fetched successfully", response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search brands")
    public ResponseEntity<ApiResponse> searchBrands(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Pageable pageable = pagingHelper.buildPageable(page, size, sortBy, sortDirection);
        PaginatedResponse<BrandResponse> response = brandService.searchBrands(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Brands search completed successfully", response));
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Get brands by country")
    public ResponseEntity<ApiResponse> getBrandsByCountry(@PathVariable String country) {
        return ResponseEntity.ok(ApiResponse.success("Brands fetched successfully", brandService.getBrandsByCountry(country)));
    }

    @GetMapping("/countries")
    @Operation(summary = "Get all countries")
    public ResponseEntity<ApiResponse> getAllCountries() {
        return ResponseEntity.ok(ApiResponse.success("Countries fetched successfully", brandService.getAllCountries()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create brand")
    public ResponseEntity<ApiResponse> createBrand(@Valid @ModelAttribute CreateBrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.ok(ApiResponse.success("Brand created successfully", response));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create brand")
    public ResponseEntity<ApiResponse> createBrandJson(@Valid @RequestBody CreateBrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.ok(ApiResponse.success("Brand created successfully", response));
    }

    @PutMapping(value = "/{brandId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update brand")
    public ResponseEntity<ApiResponse> updateBrand(
            @PathVariable UUID brandId,
            @Valid @ModelAttribute UpdateBrandRequest request
    ) {
        BrandResponse response = brandService.updateBrand(brandId, request);
        return ResponseEntity.ok(ApiResponse.success("Brand updated successfully", response));
    }

    @PutMapping(value = "/{brandId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update brand")
    public ResponseEntity<ApiResponse> updateBrandJson(
            @PathVariable UUID brandId,
            @Valid @RequestBody UpdateBrandRequest request
    ) {
        BrandResponse response = brandService.updateBrand(brandId, request);
        return ResponseEntity.ok(ApiResponse.success("Brand updated successfully", response));
    }

    @PostMapping(value = "/{brandId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload brand logo")
    public ResponseEntity<ApiResponse> uploadBrandLogo(
            @PathVariable UUID brandId,
            @RequestParam("file") MultipartFile file
    ) {
        BrandResponse response = brandService.uploadBrandLogo(brandId, file);
        return ResponseEntity.ok(ApiResponse.success("Brand logo uploaded successfully", response));
    }

    @PostMapping(value = "/{brandId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload brand banner")
    public ResponseEntity<ApiResponse> uploadBrandBanner(
            @PathVariable UUID brandId,
            @RequestParam("file") MultipartFile file
    ) {
        BrandResponse response = brandService.uploadBrandBanner(brandId, file);
        return ResponseEntity.ok(ApiResponse.success("Brand banner uploaded successfully", response));
    }

    @DeleteMapping("/{brandId}/logo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove brand logo")
    public ResponseEntity<ApiResponse> removeBrandLogo(@PathVariable UUID brandId) {
        BrandResponse response = brandService.removeBrandLogo(brandId);
        return ResponseEntity.ok(ApiResponse.success("Brand logo removed successfully", response));
    }

    @DeleteMapping("/{brandId}/banner")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove brand banner")
    public ResponseEntity<ApiResponse> removeBrandBanner(@PathVariable UUID brandId) {
        BrandResponse response = brandService.removeBrandBanner(brandId);
        return ResponseEntity.ok(ApiResponse.success("Brand banner removed successfully", response));
    }

    @DeleteMapping("/{brandId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete brand")
    public ResponseEntity<ApiResponse> deleteBrand(@PathVariable UUID brandId) {
        brandService.deleteBrand(brandId);
        return ResponseEntity.ok(ApiResponse.success("Brand deleted successfully"));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Reorder brands")
    public ResponseEntity<ApiResponse> reorderBrands(@RequestBody List<UUID> brandIds) {
        brandService.reorderBrands(brandIds);
        return ResponseEntity.ok(ApiResponse.success("Brands reordered successfully"));
    }
}