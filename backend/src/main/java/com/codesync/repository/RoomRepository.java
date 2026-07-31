package com.codesync.repository;

import com.codesync.model.Room;
import com.codesync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByName(String name);

    List<Room> findByCreatedBy(User createdBy);

    List<Room> findByIsActiveTrue();

    boolean existsByName(String name);
}
