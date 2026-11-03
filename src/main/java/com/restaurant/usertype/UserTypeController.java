package com.restaurant.usertype;

import com.restaurant.usertype.dto.UserTypeRequest;
import com.restaurant.usertype.dto.UserTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-types")
@RequiredArgsConstructor
@Tag(name = "Tipos de Usuário", description = "Gerenciamento de tipos de usuário")
@SecurityRequirement(name = "bearerAuth")
public class UserTypeController {

    private final UserTypeService userTypeService;

    @Operation(summary = "Criar tipo de usuário",
        responses = {
            @ApiResponse(responseCode = "201", description = "Tipo criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Tipo já existe",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @PostMapping
    public ResponseEntity<UserTypeResponse> create(@Valid @RequestBody UserTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userTypeService.create(request));
    }

    @Operation(summary = "Listar todos os tipos de usuário")
    @GetMapping
    public ResponseEntity<List<UserTypeResponse>> findAll() {
        return ResponseEntity.ok(userTypeService.findAll());
    }

    @Operation(summary = "Buscar tipo de usuário por ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tipo encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @GetMapping("/{id}")
    public ResponseEntity<UserTypeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userTypeService.findById(id));
    }

    @Operation(summary = "Atualizar tipo de usuário",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tipo atualizado"),
            @ApiResponse(responseCode = "404", description = "Tipo não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Nome já em uso",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @PutMapping("/{id}")
    public ResponseEntity<UserTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserTypeRequest request) {
        return ResponseEntity.ok(userTypeService.update(id, request));
    }

    @Operation(summary = "Remover tipo de usuário",
        responses = {
            @ApiResponse(responseCode = "204", description = "Tipo removido"),
            @ApiResponse(responseCode = "404", description = "Tipo não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
