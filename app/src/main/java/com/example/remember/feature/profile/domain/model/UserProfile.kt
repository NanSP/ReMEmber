package com.example.remember.feature.profile.domain.model

import com.example.remember.feature.avatar.domain.model.AvatarConfiguration

data class UserProfile(
    val displayName: String,
    val avatar: AvatarConfiguration
)
