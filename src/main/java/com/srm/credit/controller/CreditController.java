package com.srm.credit.controller;

import com.srm.credit.api.CreditApi;
import com.srm.credit.dto.CreditProposalRequest;
import com.srm.credit.dto.CreditProposalResponse;
import com.srm.credit.service.CreditProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreditController implements CreditApi {

    private final CreditProposalService creditProposalService;

    @Override
    public ResponseEntity<CreditProposalResponse> createCreditProposal(CreditProposalRequest creditProposalRequest) {
        CreditProposalResponse response = creditProposalService.createProposal(creditProposalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<CreditProposalResponse> getCreditProposalById(Long id) {
        CreditProposalResponse response = creditProposalService.findById(id);
        return ResponseEntity.ok(response);
    }
}