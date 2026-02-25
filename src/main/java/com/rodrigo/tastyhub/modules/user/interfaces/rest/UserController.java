package com.rodrigo.tastyhub.modules.user.interfaces.rest;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(
    name = "User",
    description = "Endpoints for managing user profiles, identity information, and social settings."
)
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Get user profile by ID",
        description = "Retrieves public profile information and statistics (followers, following, etc.) for a specific user by their unique ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found with the provided ID"),
        @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource"),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred while processing the request"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserFullStatsDto> getUserProfileById(@PathVariable("id") Long id) {
        UserFullStatsDto user = this.userService.getUserProfileById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
        summary = "Update user profile picture",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Uploads a new profile picture and updates the alternative text. The previous file will be replaced."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Profile picture updated successfully",
            content = @Content(schema = @Schema(implementation = UserSummaryDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error during file storage")
    })
    @PatchMapping(
        value = "/profile-picture",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserSummaryDto> updateProfilePicture(
        @Parameter(description = "Profile picture file")
        @RequestPart(value = "file") MultipartFile file,

        @Parameter(description = "Profile picture file")
        @RequestPart(value = "alternative_text", required = false) String alternativeText
    ) throws BadRequestException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is mandatory and cannot be empty");
        }

        UserSummaryDto response = this.userService.updateProfilePicture(file, alternativeText);

        return ResponseEntity.ok(response);
    }
}
