package com.rodrigo.tastyhub.shared.infrastructure.seed;

import java.time.LocalDate;

record UserSeedData(
    String firstName,
    String lastName,
    String email,
    LocalDate birthDate,
    String bio,
    String phone,
    String coverUrl,
    String photoUrl,
    String coverAlt,
    String photoAlt
) {}
