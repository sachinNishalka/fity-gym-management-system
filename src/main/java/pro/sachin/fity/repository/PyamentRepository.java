package pro.sachin.fity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pro.sachin.fity.model.Payments;

public interface PyamentRepository extends JpaRepository<Payments, Integer> {
    
}
