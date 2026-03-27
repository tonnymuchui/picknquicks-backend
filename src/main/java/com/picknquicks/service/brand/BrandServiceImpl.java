package com.picknquicks.service.brand;

import com.picknquicks.domain.brand.Brand;
import com.picknquicks.dto.request.CreateBrandRequest;
import com.picknquicks.dto.request.UpdateBrandRequest;
import com.picknquicks.dto.response.BrandResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.mapper.BrandMapper;
import com.picknquicks.repository.brand.BrandRepository;
import com.picknquicks.service.brand.BrandService;
import com.picknquicks.service.storage.FileStorageService;
import com.picknquicks.util.PagingAndSortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final PagingAndSortingHelper pagingHelper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        validateUniqueness(request.getSlug(), request.getName(), null);

        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .logoUrl(resolveCreateFilePath(request.getLogoFile(), request.getLogoUrl(), "brand"))
                .bannerUrl(resolveCreateFilePath(request.getBannerFile(), request.getBannerUrl(), "brand"))
                .websiteUrl(request.getWebsiteUrl())
                .countryOfOrigin(request.getCountryOfOrigin())
                .active(request.getActive() != null ? request.getActive() : true)
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .build();

        Brand savedBrand = brandRepository.save(brand);
        log.info("Created brand: {} with ID: {}", savedBrand.getName(), savedBrand.getId());

        return brandMapper.toBrandResponse(savedBrand);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(UUID brandId, UpdateBrandRequest request) {
        Brand brand = findBrandOrThrow(brandId);

        if (request.getSlug() != null && !request.getSlug().equals(brand.getSlug())) {
            validateSlugUniqueness(request.getSlug(), brandId);
            brand.setSlug(request.getSlug());
        }

        if (request.getName() != null && !request.getName().equals(brand.getName())) {
            validateNameUniqueness(request.getName(), brandId);
            brand.setName(request.getName());
        }

        updateIfPresent(request.getDescription(), brand::setDescription);
        updateIfPresent(request.getWebsiteUrl(), brand::setWebsiteUrl);
        updateIfPresent(request.getCountryOfOrigin(), brand::setCountryOfOrigin);
        updateIfPresent(request.getActive(), brand::setActive);
        updateIfPresent(request.getFeatured(), brand::setFeatured);
        updateIfPresent(request.getDisplayOrder(), brand::setDisplayOrder);
        updateIfPresent(request.getMetaTitle(), brand::setMetaTitle);
        updateIfPresent(request.getMetaDescription(), brand::setMetaDescription);
        updateIfPresent(request.getMetaKeywords(), brand::setMetaKeywords);

        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            brand.setLogoUrl(replaceFile(brand.getLogoUrl(), request.getLogoFile(), "brand"));
        } else if (request.getLogoUrl() != null) {
            brand.setLogoUrl(request.getLogoUrl());
        }

        if (request.getBannerFile() != null && !request.getBannerFile().isEmpty()) {
            brand.setBannerUrl(replaceFile(brand.getBannerUrl(), request.getBannerFile(), "brand"));
        } else if (request.getBannerUrl() != null) {
            brand.setBannerUrl(request.getBannerUrl());
        }

        Brand updatedBrand = brandRepository.save(brand);
        log.info("Updated brand with ID: {}", brandId);

        return brandMapper.toBrandResponse(updatedBrand);
    }

    @Override
    @Transactional
    public void deleteBrand(UUID brandId) {
        Brand brand = findBrandOrThrow(brandId);

        if (brand.hasProducts()) {
            throw new BadRequestException("Cannot delete brand with " + brand.getProductCount() + " products. Remove products first.");
        }

        deleteFileIfPresent(brand.getLogoUrl());
        deleteFileIfPresent(brand.getBannerUrl());

        brandRepository.delete(brand);
        log.info("Deleted brand with ID: {}", brandId);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(UUID brandId) {
        Brand brand = findBrandOrThrow(brandId);
        return brandMapper.toBrandResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with slug: " + slug));
        return brandMapper.toBrandResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<BrandResponse> getAllBrands(Pageable pageable) {
        Page<Brand> brandPage = brandRepository.findAll(pageable);
        return pagingHelper.toPaginatedResponse(brandPage, brandMapper::toBrandResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getActiveBrands() {
        List<Brand> brands = brandRepository.findAllActive();
        return brandMapper.toResponseList(brands);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getFeaturedBrands() {
        List<Brand> brands = brandRepository.findAllFeatured();
        return brandMapper.toResponseList(brands);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<BrandResponse> searchBrands(String search, Pageable pageable) {
        Page<Brand> brandPage = brandRepository.search(search, pageable);
        return pagingHelper.toPaginatedResponse(brandPage, brandMapper::toBrandResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getBrandsByCountry(String country) {
        List<Brand> brands = brandRepository.findByCountry(country);
        return brandMapper.toResponseList(brands);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCountries() {
        return brandRepository.findAllCountries();
    }

    @Override
    @Transactional
    public void reorderBrands(List<UUID> brandIds) {
        for (int i = 0; i < brandIds.size(); i++) {
            UUID brandId = brandIds.get(i);
            Brand brand = findBrandOrThrow(brandId);
            brand.setDisplayOrder(i);
            brandRepository.save(brand);
        }
        log.info("Reordered {} brands", brandIds.size());
    }

    @Override
    @Transactional
    public BrandResponse uploadBrandLogo(UUID brandId, MultipartFile logoFile) {
        Brand brand = findBrandOrThrow(brandId);
        brand.setLogoUrl(replaceFile(brand.getLogoUrl(), logoFile, "brand"));
        Brand updatedBrand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(updatedBrand);
    }

    @Override
    @Transactional
    public BrandResponse uploadBrandBanner(UUID brandId, MultipartFile bannerFile) {
        Brand brand = findBrandOrThrow(brandId);
        brand.setBannerUrl(replaceFile(brand.getBannerUrl(), bannerFile, "brand"));
        Brand updatedBrand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(updatedBrand);
    }

    @Override
    @Transactional
    public BrandResponse removeBrandLogo(UUID brandId) {
        Brand brand = findBrandOrThrow(brandId);
        deleteFileIfPresent(brand.getLogoUrl());
        brand.setLogoUrl(null);
        Brand updatedBrand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(updatedBrand);
    }

    @Override
    @Transactional
    public BrandResponse removeBrandBanner(UUID brandId) {
        Brand brand = findBrandOrThrow(brandId);
        deleteFileIfPresent(brand.getBannerUrl());
        brand.setBannerUrl(null);
        Brand updatedBrand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(updatedBrand);
    }

    private Brand findBrandOrThrow(UUID brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + brandId));
    }

    private void validateUniqueness(String slug, String name, UUID excludeBrandId) {
        validateSlugUniqueness(slug, excludeBrandId);
        validateNameUniqueness(name, excludeBrandId);
    }

    private void validateSlugUniqueness(String slug, UUID excludeBrandId) {
        if (excludeBrandId != null) {
            boolean exists = brandRepository.findBySlug(slug)
                    .map(brand -> !brand.getId().equals(excludeBrandId))
                    .orElse(false);
            if (exists) {
                throw new BadRequestException("Slug already exists: " + slug);
            }
        } else {
            if (brandRepository.existsBySlug(slug)) {
                throw new BadRequestException("Slug already exists: " + slug);
            }
        }
    }

    private void validateNameUniqueness(String name, UUID excludeBrandId) {
        if (excludeBrandId != null) {
            boolean exists = brandRepository.findByName(name)
                    .map(brand -> !brand.getId().equals(excludeBrandId))
                    .orElse(false);
            if (exists) {
                throw new BadRequestException("Brand name already exists: " + name);
            }
        } else {
            if (brandRepository.existsByName(name)) {
                throw new BadRequestException("Brand name already exists: " + name);
            }
        }
    }

    private <T> void updateIfPresent(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private String resolveCreateFilePath(MultipartFile file, String fallbackUrl, String entityType) {
        if (file == null || file.isEmpty()) {
            return fallbackUrl;
        }
        return storeFile(file, entityType);
    }

    private String replaceFile(String existingPath, MultipartFile file, String entityType) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        String newPath = storeFile(file, entityType);
        deleteFileIfPresent(existingPath);
        return newPath;
    }

    private String storeFile(MultipartFile file, String entityType) {
        try {
            return fileStorageService.storeFile(file, entityType);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    private void deleteFileIfPresent(String filePath) {
        if (filePath != null && !filePath.isBlank()) {
            fileStorageService.deleteFile(filePath);
        }
    }
}