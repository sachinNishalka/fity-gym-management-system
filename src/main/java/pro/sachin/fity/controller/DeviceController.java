package pro.sachin.fity.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pro.sachin.fity.model.DeviceCommand;
import pro.sachin.fity.sercives.DeviceCommandService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/device")
@CrossOrigin
public class DeviceController {

    private final DeviceCommandService deviceCommandService;

    @GetMapping("/commands")
    ResponseEntity<List<DeviceCommand>> getPendingCommands() {
        List<DeviceCommand> commands = deviceCommandService.getPendingCommands();
        return new ResponseEntity<>(commands, HttpStatus.OK);
    }

    @PostMapping("/commands/{id}/ack")
    ResponseEntity<String> acknowledgeCommand(@PathVariable("id") Long id, @RequestParam boolean success) {
        deviceCommandService.acknowledgeCommand(id, success);
        return new ResponseEntity<>("Command acknowledged", HttpStatus.OK);
    }
}