package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Data
public class Member {

    @Id
    private int Id;
    @Column(nullable = false, name = "first_name")
    private String firstName;
    @Column(nullable = false, name = "last_name")
    private  String lastName;
    private LocalDate dateOfBirth;
    @Column(nullable = false, name = "phone_number")
    private String phoneNumber;
    private String email;
//    TODO : check the way to get the current date here
//    private LocalDate joinedDate = LocalDate.ofInstant(new Date().toInstant().atZone(ZoneId.systemDefault()));
    @Enumerated(EnumType.STRING)
    private MemberStatus status;
}
