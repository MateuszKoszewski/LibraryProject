package com.mateusz.library.repositories;

import com.mateusz.library.model.dao.HistoryOfBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryOfBooksRepository extends JpaRepository<HistoryOfBookEntity, Long> {

    List<HistoryOfBookEntity> findByUserEntity_username(String username);

    List<HistoryOfBookEntity> findByUserEntity_id(Long id);
}
