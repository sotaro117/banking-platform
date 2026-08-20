package com.example.ledger.controller;

import com.example.ledger.domain.Party;
import com.example.ledger.service.PartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/party")
public class PartyController {

    @Autowired
    private PartyService partyService;

    @PostMapping(path = "/create")
    private ResponseEntity<Void> createParty(@RequestBody Party party) {
        if (partyService.isPartyExist(party)){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        };

        partyService.saveParty(party);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    private ResponseEntity<Party> getPartyByDispplayName(@PathVariable UUID id) {
        Party party = partyService.getPartyById(id);
        return ResponseEntity.ok().body(party);
    }
}
