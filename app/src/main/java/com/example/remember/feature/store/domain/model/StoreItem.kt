package com.example.remember.feature.store.domain.model

data class StoreItem(
    val id: String,
    val avatarItemId: String,
    val priceInCoins: Int,
    val isPurchased: Boolean = false,
    val isEquipped: Boolean = false
)
