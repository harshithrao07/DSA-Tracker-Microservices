package com.harshith.dsa_question_picker.controller;

import com.harshith.dsa_question_picker.dto.ApiResponseDTO;
import com.harshith.dsa_question_picker.dto.note.NoteResponseDTO;
import com.harshith.dsa_question_picker.dto.note.PostNoteDTO;
import com.harshith.dsa_question_picker.dto.note.UpdateNote;
import com.harshith.dsa_question_picker.model.User;
import com.harshith.dsa_question_picker.service.NotesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notes")
public class NotesController {
    private final NotesService notesService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<NoteResponseDTO>> addNote(@Valid @RequestBody PostNoteDTO postNoteDTO, @AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return notesService.addNote(postNoteDTO, createdBy);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<ApiResponseDTO<NoteResponseDTO>> getNote(@PathVariable("noteId") String noteId, @AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return notesService.getNote(noteId, createdBy);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<ApiResponseDTO<NoteResponseDTO>> updateNote(@Valid @RequestBody UpdateNote updateNote, @PathVariable("noteId") String noteId, @AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return notesService.updateNote(updateNote, noteId, createdBy);
    }
}
