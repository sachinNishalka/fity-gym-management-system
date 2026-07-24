package pro.sachin.fity.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pro.sachin.fity.dto.GraceExtensionDTO;
import pro.sachin.fity.dto.GraceExtensonResponseDTO;
import pro.sachin.fity.exception.NotInGracePeriodException;
import pro.sachin.fity.model.GraceExtension;
import pro.sachin.fity.sercives.GraceExtensionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/grace")
public class GraceExtensionController {

    private final GraceExtensionService graceExtensionService;

    @PostMapping("/extend")
    ResponseEntity<?> extendGrace(@RequestBody GraceExtensionDTO graceExtensionDTO) {
        ResponseEntity<?> graceExtension = graceExtensionService.extend(graceExtensionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(graceExtension);
    }

    @GetMapping("/all")
    public ResponseEntity<List<GraceExtensonResponseDTO>> getAllGraceExtends() {
        List<GraceExtensonResponseDTO> list = graceExtensionService.extendedList();
        return new ResponseEntity<List<GraceExtensonResponseDTO>>(list, HttpStatus.OK);

    }

}
