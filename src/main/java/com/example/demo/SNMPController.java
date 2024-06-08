package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/snmp")
public class SNMPController {

    @Autowired
    private SNMPService snmpService;

    @GetMapping("/get")
    public ResponseEntity<List<String>> getSnmpGet(@RequestParam String address, @RequestParam String oid) {
        try {
            List<String> results = snmpService.getPortsUp(address , oid);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
