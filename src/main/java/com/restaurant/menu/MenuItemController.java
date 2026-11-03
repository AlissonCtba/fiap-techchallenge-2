package com.restaurant.menu;

import com.restaurant.menu.dto.MenuItemRequest;
import com.restaurant.menu.dto.MenuItemResponse;
import com.restaurant.security.UserPrincipal;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
@Tag(name = "Cardápio", description = "Gerenciamento de itens do cardápio")
@SecurityRequirement(name = "bearerAuth")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @Operation(summary = "Adicionar item ao cardápio",
        responses = {
            @ApiResponse(responseCode = "201", description = "Item criado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @PostMapping
    public ResponseEntity<MenuItemResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuItemService.create(principal.getUsername(), restaurantId, request));
    }

    @Operation(summary = "Listar itens do cardápio de um restaurante")
    @GetMapping
    public ResponseEntity<Page<MenuItemResponse>> findAll(
            @PathVariable Long restaurantId,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(menuItemService.findByRestaurant(restaurantId, pageable));
    }

    @Operation(summary = "Buscar item por ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @GetMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> findById(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(menuItemService.findById(restaurantId, itemId));
    }

    @Operation(summary = "Buscar itens por nome",
        parameters = @Parameter(name = "name", description = "Trecho do nome", required = true))
    @GetMapping("/search")
    public ResponseEntity<Page<MenuItemResponse>> searchByName(
            @PathVariable Long restaurantId,
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(menuItemService.searchByName(restaurantId, name, pageable));
    }

    @Operation(summary = "Atualizar item do cardápio",
        responses = {
            @ApiResponse(responseCode = "200", description = "Item atualizado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Item não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @PutMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.update(principal.getUsername(), restaurantId, itemId, request));
    }

    @Operation(summary = "Remover item do cardápio",
        responses = {
            @ApiResponse(responseCode = "204", description = "Item removido"),
            @ApiResponse(responseCode = "403", description = "Sem permissão",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Item não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        menuItemService.delete(principal.getUsername(), restaurantId, itemId);
        return ResponseEntity.noContent().build();
    }
}
