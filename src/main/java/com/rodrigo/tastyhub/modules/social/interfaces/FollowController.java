package com.rodrigo.tastyhub.modules.social.interfaces;

import com.rodrigo.tastyhub.modules.social.application.usecase.FollowUserUseCase;
import com.rodrigo.tastyhub.modules.social.application.usecase.UnfollowUserUseCase;
import com.rodrigo.tastyhub.shared.dto.response.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Follows",
    description = "Endpoints for managing social relationships, allowing users to follow and unfollow other profiles."
)
@RestController
@RequestMapping("/api/follows")
public class FollowController {
    private final FollowUserUseCase followUserUseCase;
    private final UnfollowUserUseCase unfollowUserUseCase;

    public FollowController(FollowUserUseCase userUseCase, UnfollowUserUseCase unfollowUserUseCase) {
        this.followUserUseCase = userUseCase;
        this.unfollowUserUseCase = unfollowUserUseCase;
    }

    @Operation(
        summary = "Follow a user",
        description = """
            Establishes a follow relationship between the authenticated user and the target user. 
            The request will fail if the target user does not exist, if the users are already connected, 
            or if a user attempts to follow themselves.
        """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "User followed successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request (e.g., self-following or already following)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User must be logged in",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Target user not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    @PutMapping("/{followingId}/follow")
    public ResponseEntity<Void> followUser(
        @Parameter(description = "ID of the user", required = true)
        @PathVariable("followingId")
        @Min(value = 1, message = "The User ID must be a positive number")
        Long followingId
    ) {
        this.followUserUseCase.execute(followingId);
        return ResponseEntity.noContent().build();
    }
    @Operation(
        summary = "Unfollow a user",
        description = """
            Terminates the follow relationship between the authenticated user and the target user. 
            The request will fail if the target user does not exist or if the authenticated user 
            is not currently following them.
        """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "User unfollowed successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request (e.g., trying to unfollow someone you don't follow)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User must be logged in",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Target user not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    @DeleteMapping("/{followingId}/unfollow")
    public ResponseEntity<Void> unfollowUser(
        @Parameter(description = "ID of the user", required = true)
        @PathVariable("followingId")
        @Min(value = 1, message = "The User ID must be a positive number")
        Long followingId
    ) {
        this.unfollowUserUseCase.execute(followingId);
        return ResponseEntity.noContent().build();
    }
}
