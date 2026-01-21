package pro.sachin.fity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pro.sachin.fity.dto.GraceExtensionDTO;
import pro.sachin.fity.model.GraceExtension;
import pro.sachin.fity.sercives.GraceExtensionService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/grace")
public class GraceExtensionController {

    private final GraceExtensionService graceExtensionService;
    
    @PostMapping("/extend")
    ResponseEntity<GraceExtension> extendGrace(@RequestBody GraceExtensionDTO graceExtensionDTO){
       GraceExtension graceExtension = graceExtensionService.extend(graceExtensionDTO);

      return ResponseEntity.status(HttpStatus.CREATED).body(graceExtension);
    }
}
