package com.picknquicks.service.brand;

import com.picknquicks.dto.request.CreateBrandRequest;
import com.picknquicks.dto.request.UpdateBrandRequest;
import com.picknquicks.dto.response.BrandResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BrandService {

    BrandResponse createBrand(CreateBrandRequest request);

    BrandResponse updateBrand(UUID brandId, UpdateBrandRequest request);

    void deleteBrand(UUID brandId);

    BrandResponse getBrandById(UUID brandId);

    BrandResponse getBrandBySlug(String slug);

    PaginatedResponse<BrandResponse> getAllBrands(Pageable pageable);

    List<BrandResponse> getActiveBrands();

    List<BrandResponse> getFeaturedBrands();

    PaginatedResponse<BrandResponse> searchBrands(String search, Pageable pageable);

    List<BrandResponse> getBrandsByCountry(String country);

    List<String> getAllCountries();

    void reorderBrands(List<UUID> brandIds);

    BrandResponse uploadBrandLogo(UUID brandId, MultipartFile logoFile);

    BrandResponse uploadBrandBanner(UUID brandId, MultipartFile bannerFile);

    BrandResponse removeBrandLogo(UUID brandId);

    BrandResponse removeBrandBanner(UUID brandId);
}