package com.example.innerlamp.data.remote

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.JsonProvider
import com.example.innerlamp.data.remote.dto.ItemsDto
import com.example.innerlamp.data.remote.dto.TempleOptionPageDto
import com.example.innerlamp.data.remote.dto.WardDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Parse JSON mẫu của backend (snake_case, `province_code` nullable) bằng `JsonProvider.json`. */
class MetadataDtoParsingTest {

    @Test
    fun templeOptions_parsesSnakeCaseAndNullableProvinceCode() {
        val body = """
            {"success":true,"data":{"items":[
              {"id":"temple_north_011","name":"Chùa Một Cột","province":"Hà Nội","province_code":"01"},
              {"id":"t2","name":"Chùa X","province":"Huế","province_code":null},
              {"id":"t3","name":"Chùa Y","province":"Huế"}
            ],"page":1,"page_size":100,"total":3,"total_pages":1}}
        """.trimIndent()

        val response = JsonProvider.json.decodeFromString<ApiResponse<TempleOptionPageDto>>(body)

        assertTrue(response.success)
        val page = response.data!!
        assertEquals(100, page.pageSize)
        assertEquals(1, page.totalPages)
        assertEquals("01", page.items[0].provinceCode)
        assertNull(page.items[1].provinceCode)
        assertNull(page.items[2].provinceCode)
    }

    @Test
    fun wards_parsesItemsEnvelope_withoutDistrictCode() {
        val body = """
            {"success":true,"data":{"items":[
              {"code":"26737","name":"Tân Định","full_name":"Phường Tân Định","type":"ward","province_code":"79"}
            ]}}
        """.trimIndent()

        val response = JsonProvider.json.decodeFromString<ApiResponse<ItemsDto<WardDto>>>(body)

        val ward = response.data!!.items.single()
        assertEquals("Phường Tân Định", ward.fullName)
        assertEquals("79", ward.provinceCode)
        assertNull(ward.districtCode)
    }
}
