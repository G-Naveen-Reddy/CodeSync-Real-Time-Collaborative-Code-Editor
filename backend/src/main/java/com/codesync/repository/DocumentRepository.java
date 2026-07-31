package com.codesync.repository;

import com.codesync.model.Document;
import com.codesync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwner(User owner);

    List<Document> findByTitleContaining(String title);
}
