package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int Id;

    @Column(nullable = false, name = "family_name")
    private String familyName;

    private LocalDate createdAt = LocalDate.now();
}
