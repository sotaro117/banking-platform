package com.example.ledger.controller;

import com.example.ledger.domain.Party;
import com.example.ledger.service.PartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/party")
public class PartyController {

    @Autowired
    private PartyService partyService;

    @PostMapping(path = "/create")
    private ResponseEntity<Void> createParty(@RequestBody Party party) {
        partyService.saveParty(party);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    private ResponseEntity<Party> getPartyByDispplayName(@RequestParam("display-name") String displayName) {
        Party party = partyService.getPartyByName(displayName);
        return ResponseEntity.ok().body(party);
    }
}
