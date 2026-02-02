package pro.sachin.fity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sachin.fity.model.Family;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
}
