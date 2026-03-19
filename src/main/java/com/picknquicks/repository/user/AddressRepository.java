package com.picknquicks.repository.user;

import com.picknquicks.domain.user.Address;
import com.picknquicks.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUser(User user);

    Optional<Address> findByUserAndIsDefaultTrue(User user);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user")
    void clearDefaultForUser(User user);
}