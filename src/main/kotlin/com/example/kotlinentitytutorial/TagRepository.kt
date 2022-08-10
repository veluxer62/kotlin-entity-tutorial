package com.example.kotlinentitytutorial

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface TagRepository : JpaRepository<Tag, UUID> {
    fun findByKeyAndValue(key: String, value: String): Optional<Tag>
}
