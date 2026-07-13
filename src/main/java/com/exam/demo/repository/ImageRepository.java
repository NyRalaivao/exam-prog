package com.exam.demo.repository;

import com.exam.demo.entity.ImageEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {
  @Query("SELECT i FROM ImageEntity i ORDER BY i.dateCreation DESC")
  List<ImageEntity> findAllOrderByDateCreationDesc();

  void save(ImageEntity entity);
}
