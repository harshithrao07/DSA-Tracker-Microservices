package com.harshith.autofill_service.controller;

import com.harshith.autofill_service.dto.ApiResponseDTO;
import com.harshith.autofill_service.dto.AutofillRequestDTO;
import com.harshith.autofill_service.dto.QuestionAutofillDTO;
import com.harshith.autofill_service.service.QuestionAutofillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class AutofillController {
    private final QuestionAutofillService questionAutofillService;

    public AutofillController(QuestionAutofillService questionAutofillService) {
        this.questionAutofillService = questionAutofillService;
    }

    @PostMapping("/autofill")
    public ResponseEntity<ApiResponseDTO<QuestionAutofillDTO>> autofillQuestion(@RequestBody AutofillRequestDTO autofillRequestDTO) {
        return questionAutofillService.autofillQuestion(autofillRequestDTO);
    }
}
