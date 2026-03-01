package com.harshith.dsa_question_picker.client;

import com.harshith.dsa_question_picker.dto.ApiResponseDTO;
import com.harshith.dsa_question_picker.dto.question.AutofillRequestDTO;
import com.harshith.dsa_question_picker.dto.question.QuestionAutofillDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "autofill-service")
public interface AutofillClient {
    @PostMapping("/autofill")
    ResponseEntity<ApiResponseDTO<QuestionAutofillDTO>> autofillQuestion(@Valid @RequestBody AutofillRequestDTO autofillRequestDTO);
}
