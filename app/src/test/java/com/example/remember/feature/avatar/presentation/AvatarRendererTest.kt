package com.example.remember.feature.avatar.presentation

import com.example.remember.feature.avatar.domain.model.AvatarColors
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarRendererTest {
    @Test
    fun `hair follows oval and square face widths`() {
        val hair = """<svg><g stroke-linejoin="round"><path d="M320 370L704 370" /></g></svg>"""

        val oval = adaptHairToFace(hair, "face_02")
        val square = adaptHairToFace(hair, "face_03")
        val round = adaptHairToFace(hair, "face_01")

        assertTrue(oval.contains("transform=\"matrix(0.90 0 0 1 51.2 -4)\""))
        assertTrue(square.contains("transform=\"matrix(1.08 0 0 1 -40.96 6)\""))
        assertFalse(round.contains("transform="))
        assertEquals(1, Regex("transform=").findAll(oval).count())
    }

    @Test
    fun `equipped hat covers the top while preserving lateral hair`() {
        val hair = """<svg viewBox="0 0 1024 1024"><g><path id="top" d="M300 200V700"/><path id="sides" d="M320 300V700"/></g></svg>"""
        val masked = maskHairForHat(hair, "hat_03")

        assertTrue(masked.contains("clip-path=\"url(#avatarHairBesideHat)\""))
        assertTrue(masked.contains("x=\"0\" y=\"280\" width=\"390\""))
        assertTrue(masked.contains("x=\"634\" y=\"280\" width=\"390\""))
        assertFalse(masked.contains("id=\"top\""))
        assertTrue(masked.contains("id=\"sides\""))
        assertFalse(maskHairForHat(hair, "hat_01").contains("clipPath"))
    }

    @Test
    fun `hats follow the selected face width`() {
        val hat = """<svg><g><path d="M300 200H724" /></g></svg>"""

        assertTrue(adaptHatToFace(hat, "face_02").contains("matrix(0.90 0 0 1 51.2 0)"))
        assertTrue(adaptHatToFace(hat, "face_03").contains("matrix(1.08 0 0 1 -40.96 0)"))
        assertFalse(adaptHatToFace(hat, "face_01").contains("transform="))
    }

    @Test
    fun `structural clothes follow the selected body width`() {
        val shirt = """<svg><g><path d="M174 730H850" /></g></svg>"""

        assertTrue(adaptShirtToBody(shirt, "shirt_06", "body_01").contains("matrix(0.8876 0 0 1 57.55 0)"))
        assertTrue(adaptShirtToBody(shirt, "shirt_07", "body_02").contains("matrix(0.9408 0 0 1 30.31 0)"))
        assertFalse(adaptShirtToBody(shirt, "shirt_05", "body_01").contains("transform="))
        assertFalse(adaptShirtToBody(shirt, "shirt_08", "body_03").contains("transform="))
    }

    @Test
    fun `color markers are replaced without removing metadata`() {
        val source = """
            <path fill="#000000" data-color="SKIN" />
            <path stroke="#000000" data-stroke-color="HAIR" />
        """.trimIndent()

        val result = colorizeSvg(
            source,
            AvatarColors(skin = "#F2C4A4", hair = "#7B6A9C")
        )

        assertTrue(result.contains("fill=\"#F2C4A4\" data-color=\"SKIN\""))
        assertTrue(result.contains("stroke=\"#7B6A9C\" data-stroke-color=\"HAIR\""))
        assertFalse(result.contains("fill=\"#000000\" data-color=\"SKIN\""))
    }

    @Test
    fun `avatar layers are combined into one ordered svg`() {
        val result = composeAvatarSvg(
            listOf(
                """<svg viewBox="0 0 1024 1024"><g id="face"><path fill="#000000" data-color="SKIN" /></g></svg>""",
                """<svg viewBox="0 0 1024 1024"><g id="hair"><path fill="#000000" data-color="HAIR" /></g></svg>"""
            ),
            AvatarColors(skin = "#F2C4A4", hair = "#29232A")
        )

        assertEquals(1, Regex("<svg\\b").findAll(result).count())
        assertTrue(result.indexOf("id=\"face\"") < result.indexOf("id=\"hair\""))
        assertTrue(result.contains("fill=\"#F2C4A4\" data-color=\"SKIN\""))
        assertTrue(result.contains("fill=\"#29232A\" data-color=\"HAIR\""))
    }
}
