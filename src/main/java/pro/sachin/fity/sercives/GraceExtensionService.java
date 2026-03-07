package pro.sachin.fity.sercives;

import java.time.LocalDate;

import pro.sachin.fity.dto.GraceExtensionDTO;
import pro.sachin.fity.model.GraceExtension;

public interface GraceExtensionService {

    GraceExtension extend(GraceExtensionDTO graceExtensionDTO);

    GraceExtension extendGracePeriod(Long subscriptionId, LocalDate newGraceEndDate, String reason, Long extendedByUserId);
}