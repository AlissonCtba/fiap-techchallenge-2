package com.restaurant.user;

import com.restaurant.security.UserPrincipal;
import com.restaurant.user.dto.ChangePasswordRequest;
import com.restaurant.user.dto.UpdateUserRequest;
import com.restaurant.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Buscar meu perfil",
        responses = {
            @ApiResponse(responseCode = "200", description = "Perfil retornado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getUsername()));
    }

    @Operation(summary = "Atualizar dados do usuário (exceto senha)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ou login já em uso",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(principal.getUsername(), request));
    }

    @Operation(summary = "Alterar senha",
        responses = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Senha atual incorreta",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @PatchMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getUsername(), request);
        return ResponseEntity.ok(new MessageResponse("Senha alterada com sucesso"));
    }

    @Operation(summary = "Remover minha conta",
        responses = {
            @ApiResponse(responseCode = "200", description = "Conta removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> deleteAccount(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteByEmail(principal.getUsername());
        return ResponseEntity.ok(new MessageResponse("Conta removida com sucesso"));
    }

    public record MessageResponse(String message) {}

    @Operation(summary = "Buscar usuários por nome",
        parameters = {
            @Parameter(name = "name", description = "Trecho do nome a buscar", required = true),
            @Parameter(name = "page", description = "Número da página (0-based)"),
            @Parameter(name = "size", description = "Tamanho da página")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista paginada de usuários")
        })
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(userService.findByName(name, pageable));
    }
}
