package com.rodrigo.tastyhub.modules.collections.domain.repository;

import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCollectionRepository extends JpaRepository<UserCollection, Long> {
}
