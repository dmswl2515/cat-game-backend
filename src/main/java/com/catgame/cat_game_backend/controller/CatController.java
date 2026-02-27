package com.catgame.cat_game_backend.controller;

import com.catgame.cat_game_backend.dto.request.CatCreateRequest;
import com.catgame.cat_game_backend.dto.response.ApiResponse;
import com.catgame.cat_game_backend.dto.response.CatResponse;
import com.catgame.cat_game_backend.dto.response.CatStatusResponse;
import com.catgame.cat_game_backend.service.CatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "고양이 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/cats")
@RequiredArgsConstructor
public class CatController {

    private final CatService catService;

    @Operation(summary = "내 고양이 목록 조회", description = "로그인한 유저의 모든 고양이를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CatResponse>>> getMyCats(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<CatResponse> response = catService.getMyCats(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "고양이 생성", description = "새로운 고양이를 생성합니다. (최대 5마리)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "고양이 수 초과 또는 유효성 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CatResponse>> createCat(@Valid @RequestBody CatCreateRequest request,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CatResponse response = catService.createCat(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("고양이가 생성되었습니다.", response));
    }

    @Operation(summary = "고양이 상세 조회", description = "특정 고양이의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "고양이를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CatResponse>> getCat(@PathVariable Long id,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CatResponse response = catService.getCat(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "밥 주기", description = "고양이에게 밥을 줍니다. (hunger +30, happiness +5)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "고양이를 찾을 수 없음")
    })
    @PostMapping("/{id}/feed")
    public ResponseEntity<ApiResponse<CatResponse>> feed(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CatResponse response = catService.feed(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("밥을 주었습니다.", response));
    }

    @Operation(summary = "물 주기", description = "고양이에게 물을 줍니다. (thirst +30, happiness +5)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "고양이를 찾을 수 없음")
    })
    @PostMapping("/{id}/water")
    public ResponseEntity<ApiResponse<CatResponse>> water(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CatResponse response = catService.water(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("물을 주었습니다.", response));
    }

    @Operation(summary = "청소하기", description = "고양이를 청소합니다. (cleanliness +30, happiness +5)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "고양이를 찾을 수 없음")
    })
    @PostMapping("/{id}/clean")
    public ResponseEntity<ApiResponse<CatResponse>> clean(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CatResponse response = catService.clean(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("청소를 했습니다.", response));
    }

    @Operation(summary = "고양이 상태 조회", description = "고양이의 현재 상태와 돌봄 필요 여부를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "고양이를 찾을 수 없음")
    })
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CatStatusResponse>> getStatus(@PathVariable Long id,
                                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CatStatusResponse response = catService.getStatus(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
