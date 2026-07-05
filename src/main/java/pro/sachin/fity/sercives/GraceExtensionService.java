package pro.sachin.fity.sercives;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import pro.sachin.fity.dto.GraceExtensionDTO;
import pro.sachin.fity.model.GraceExtension;

public interface GraceExtensionService {

    ResponseEntity<?> extend(GraceExtensionDTO graceExtensionDTO);

    ResponseEntity<?> extendGracePeriod(Long subscriptionId, LocalDate newGraceEndDate, String reason,
            Long extendedByUserId);
}