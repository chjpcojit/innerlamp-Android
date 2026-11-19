package com.example.innerlamp.core.security

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A2/A4/A6 (G1) guards on the files that decide what leaves the device. A text check is intentional: the
 * manifest and the backup rules are configuration, and a wrong line here is a PII leak, not a coding style
 * issue. Paths are resolved from the module directory (Gradle) or the repository root. /
 * Chốt A2/A4/A6 trên các file quyết định dữ liệu rời máy. Kiểm dạng text là cố ý: manifest và rules là cấu
 * hình, một dòng sai ở đây là rò PII.
 */
class MemorialSecurityConfigTest {

    private fun read(relative: String): String {
        val candidates = listOf(File(relative), File("../$relative"))
        val file = candidates.firstOrNull { it.isFile }
            ?: error("missing $relative (cwd=${File(".").absolutePath})")
        return file.readText()
    }

    private val manifest = read("src/main/AndroidManifest.xml")
    private val backupRules = read("src/main/res/xml/backup_rules.xml")
    private val extractionRules = read("src/main/res/xml/data_extraction_rules.xml")

    @Test
    fun cloud_backup_is_off_entirely() {
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        assertFalse(manifest.contains("android:allowBackup=\"true\""))
    }

    @Test
    fun the_app_declares_network_and_notification_permissions() {
        assertTrue(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android.permission.POST_NOTIFICATIONS"))
    }

    @Test
    fun the_reminder_activity_is_single_top() {
        assertTrue(manifest.contains("android:launchMode=\"singleTop\""))
    }

    @Test
    fun both_backup_rule_files_exclude_the_memorial_and_work_manager_databases() {
        listOf(backupRules, extractionRules).forEach { rules ->
            assertTrue(rules.contains("innerlamp_memorial.db"))
            assertTrue(rules.contains("androidx.work.workdb"))
        }
    }

    @Test
    fun device_transfer_excludes_the_same_databases_as_cloud_backup() {
        assertTrue(extractionRules.contains("<cloud-backup>"))
        assertTrue(extractionRules.contains("<device-transfer>"))
        // One exclusion inside cloud-backup, one inside device-transfer.
        assertTrue(countOf(extractionRules, "innerlamp_memorial.db") >= 2)
        assertTrue(countOf(extractionRules, "androidx.work.workdb") >= 2)
    }

    private fun countOf(text: String, needle: String): Int = text.windowed(needle.length).count { it == needle }
}
