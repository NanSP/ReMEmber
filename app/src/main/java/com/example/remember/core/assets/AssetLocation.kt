package com.example.remember.core.assets

object AssetLocation {
    private const val ANDROID_ASSET_ROOT = "file:///android_asset"

    fun uri(relativePath: String): String =
        "$ANDROID_ASSET_ROOT/${relativePath.trimStart('/')}"
}
