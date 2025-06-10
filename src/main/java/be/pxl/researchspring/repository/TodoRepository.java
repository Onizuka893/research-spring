package be.pxl.researchspring.repository;

import be.pxl.researchspring.domain.Todo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Todo t WHERE t.id = :id")
    Optional<Todo> findByIdWithLock(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Todo t SET t.completed = :completed, t.version = t.version + 1 WHERE t.id = :id")
    int updateCompleted(@Param("id") Long id, @Param("completed") Boolean completed);
}
