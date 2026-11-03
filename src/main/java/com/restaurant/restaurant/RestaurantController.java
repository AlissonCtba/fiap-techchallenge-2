package com.restaurant.restaurant;

import com.restaurant.restaurant.dto.RestaurantRequest;
import com.restaurant.restaurant.dto.RestaurantResponse;
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
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurantes", description = "Gerenciamento de restaurantes")
@SecurityRequirement(name = "bearerAuth")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Operation(summary = "Cadastrar restaurante",
        responses = {
            @ApiResponse(responseCode = "201", description = "Restaurante criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Apenas donos de restaurante podem cadastrar",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @PostMapping
    public ResponseEntity<RestaurantResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.create(principal.getUsername(), request));
    }

    @Operation(summary = "Listar todos os restaurantes")
    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> findAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(restaurantService.findAll(pageable));
    }

    @Operation(summary = "Buscar restaurante por ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Restaurante encontrado"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @Operation(summary = "Buscar restaurantes por nome",
        parameters = @Parameter(name = "name", description = "Trecho do nome", required = true))
    @GetMapping("/search/name")
    public ResponseEntity<Page<RestaurantResponse>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(restaurantService.searchByName(name, pageable));
    }

    @Operation(summary = "Buscar restaurantes por tipo de cozinha",
        parameters = @Parameter(name = "cuisineType", description = "Tipo de cozinha", required = true))
    @GetMapping("/search/cuisine")
    public ResponseEntity<Page<RestaurantResponse>> searchByCuisine(
            @RequestParam String cuisineType,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(restaurantService.searchByCuisine(cuisineType, pageable));
    }

    @Operation(summary = "Atualizar restaurante",
        responses = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.update(principal.getUsername(), id, request));
    }

    @Operation(summary = "Remover restaurante",
        responses = {
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        restaurantService.delete(principal.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
