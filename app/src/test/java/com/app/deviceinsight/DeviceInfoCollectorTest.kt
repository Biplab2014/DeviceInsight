package com.app.deviceinsight

import com.app.deviceinsight.domain.models.DeviceInfoItem
import com.app.deviceinsight.domain.models.DeviceInfoSection
import org.junit.Assert.*
import org.junit.Test

class DeviceInfoTest {

    @Test
    fun testDeviceInfoItem() {
        val item = DeviceInfoItem("Test Label", "Test Value")
        assertEquals("Test Label", item.label)
        assertEquals("Test Value", item.value)
    }

    @Test
    fun testDeviceInfoSection() {
        val items = listOf(
            DeviceInfoItem("Label 1", "Value 1"),
            DeviceInfoItem("Label 2", "Value 2")
        )
        val section = DeviceInfoSection("Test Section", items, true)

        assertEquals("Test Section", section.title)
        assertEquals(2, section.items.size)
        assertTrue(section.isExpanded)
        assertEquals("Label 1", section.items[0].label)
        assertEquals("Value 1", section.items[0].value)
    }

    @Test
    fun testDeviceInfoSectionDefaultExpanded() {
        val items = listOf(DeviceInfoItem("Label", "Value"))
        val section = DeviceInfoSection("Test Section", items)

        assertFalse(section.isExpanded) // Default should be false
    }
}
