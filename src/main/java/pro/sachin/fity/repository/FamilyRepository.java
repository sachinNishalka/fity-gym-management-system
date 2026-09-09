package pro.sachin.fity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import pro.sachin.fity.model.Family;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    @Override
    @EntityGraph(attributePaths = "members")
    Optional<Family> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "members")
    List<Family> findAll();
}
