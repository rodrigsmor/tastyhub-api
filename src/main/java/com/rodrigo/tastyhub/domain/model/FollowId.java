package com.rodrigo.tastyhub.domain.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record FollowId(
    Long followerId,
    Long followingId
) implements Serializable {}
