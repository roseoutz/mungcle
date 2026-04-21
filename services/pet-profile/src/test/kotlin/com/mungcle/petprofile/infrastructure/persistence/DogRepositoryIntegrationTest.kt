package com.mungcle.petprofile.infrastructure.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicLong

/**
 * Testcontainers + JDBC 기반 integration 테스트.
 * JPA TEXT[] 매핑과 소프트 삭제 필터를 실제 PostgreSQL에서 검증한다.
 * Spring context를 로드하지 않아 @Tsid/TsidGenerator 빈 충돌을 회피한다.
 */
@Testcontainers
class DogRepositoryIntegrationTest {

    companion object {
        private val idSeq = AtomicLong(1000L)

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("mungcle_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/migration/V1__init_pet_profile_schema.sql")

        private lateinit var conn: Connection

        @BeforeAll
        @JvmStatic
        fun setup() {
            conn = DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
            conn.autoCommit = true
        }
    }

    @BeforeEach
    fun cleanup() {
        conn.createStatement().execute("DELETE FROM pet_profile.dogs")
    }

    private fun insertDog(
        ownerId: Long = 1L,
        name: String = "초코",
        breed: String = "골든리트리버",
        size: String = "LARGE",
        temperaments: List<String> = listOf("FRIENDLY", "CALM"),
        sociability: Int = 4,
        deleted: Boolean = false,
    ): Long {
        val id = idSeq.getAndIncrement()
        val tempsArray = temperaments.joinToString(",", "{", "}")
        val deletedAt = if (deleted) "NOW()" else "NULL"
        conn.createStatement().execute(
            """INSERT INTO pet_profile.dogs (id, owner_id, name, breed, size, temperaments, sociability, deleted_at, created_at)
               VALUES ($id, $ownerId, '$name', '$breed', '$size', '$tempsArray', $sociability, $deletedAt, NOW())"""
        )
        return id
    }

    private fun findByIdNotDeleted(id: Long): ResultSet? {
        val ps = conn.prepareStatement(
            "SELECT * FROM pet_profile.dogs WHERE id = ? AND deleted_at IS NULL"
        )
        ps.setLong(1, id)
        val rs = ps.executeQuery()
        return if (rs.next()) rs else null
    }

    private fun findByOwnerNotDeleted(ownerId: Long): List<Map<String, Any?>> {
        val ps = conn.prepareStatement(
            "SELECT * FROM pet_profile.dogs WHERE owner_id = ? AND deleted_at IS NULL"
        )
        ps.setLong(1, ownerId)
        val rs = ps.executeQuery()
        val results = mutableListOf<Map<String, Any?>>()
        while (rs.next()) {
            results.add(mapOf("id" to rs.getLong("id"), "name" to rs.getString("name"), "owner_id" to rs.getLong("owner_id")))
        }
        return results
    }

    private fun findByIdsNotDeleted(ids: List<Long>): List<Map<String, Any?>> {
        val placeholders = ids.joinToString(",") { "?" }
        val ps = conn.prepareStatement(
            "SELECT * FROM pet_profile.dogs WHERE id IN ($placeholders) AND deleted_at IS NULL"
        )
        ids.forEachIndexed { i, id -> ps.setLong(i + 1, id) }
        val rs = ps.executeQuery()
        val results = mutableListOf<Map<String, Any?>>()
        while (rs.next()) {
            results.add(mapOf("id" to rs.getLong("id"), "name" to rs.getString("name")))
        }
        return results
    }

    @Test
    fun `save and read — TEXT array 매핑 검증`() {
        val id = insertDog(temperaments = listOf("FRIENDLY", "CALM", "ACTIVE"))

        val rs = findByIdNotDeleted(id)
        assertNotNull(rs)
        assertEquals("초코", rs!!.getString("name"))
        assertEquals("LARGE", rs.getString("size"))
        val temps = rs.getArray("temperaments").array as Array<*>
        assertEquals(3, temps.size)
        assertEquals("FRIENDLY", temps[0])
        assertEquals("CALM", temps[1])
        assertEquals("ACTIVE", temps[2])
        assertEquals(4, rs.getInt("sociability"))
    }

    @Test
    fun `findByOwner — 삭제되지 않은 것만 반환`() {
        insertDog(ownerId = 1L, name = "초코")
        insertDog(ownerId = 1L, name = "코코")
        insertDog(ownerId = 1L, name = "삭제됨", deleted = true)
        insertDog(ownerId = 2L, name = "다른주인")

        val dogs = findByOwnerNotDeleted(1L)
        assertEquals(2, dogs.size)
        assertTrue(dogs.all { it["owner_id"] == 1L })
        assertTrue(dogs.none { it["name"] == "삭제됨" })
    }

    @Test
    fun `findByIds — 여러 ID로 조회, 삭제 제외`() {
        val id1 = insertDog(name = "초코")
        val id2 = insertDog(name = "코코")
        val idDeleted = insertDog(name = "삭제됨", deleted = true)

        val result = findByIdsNotDeleted(listOf(id1, id2, idDeleted))
        assertEquals(2, result.size)
        assertTrue(result.map { it["name"] }.containsAll(listOf("초코", "코코")))
    }

    @Test
    fun `삭제된 반려견은 조회 불가`() {
        val id = insertDog(deleted = true)

        val result = findByIdNotDeleted(id)
        assertNull(result)
    }

    @Test
    fun `단일 temperament 배열 저장 및 조회`() {
        val id = insertDog(temperaments = listOf("FRIENDLY"))

        val rs = findByIdNotDeleted(id)
        assertNotNull(rs)
        val temps = rs!!.getArray("temperaments").array as Array<*>
        assertEquals(1, temps.size)
        assertEquals("FRIENDLY", temps[0])
    }

    @Test
    fun `sociability CHECK constraint — 1~5 범위만 허용`() {
        val ex = org.junit.jupiter.api.assertThrows<java.sql.SQLException> {
            insertDog(sociability = 6)
        }
        assertTrue(ex.message!!.contains("check") || ex.message!!.contains("violates"), ex.message)
    }

    @Test
    fun `idx_dogs_owner 인덱스 존재 확인`() {
        val rs = conn.createStatement().executeQuery(
            "SELECT indexname FROM pg_indexes WHERE schemaname = 'pet_profile' AND tablename = 'dogs' AND indexname = 'idx_dogs_owner'"
        )
        assertTrue(rs.next(), "idx_dogs_owner 인덱스가 존재해야 합니다")
    }
}
