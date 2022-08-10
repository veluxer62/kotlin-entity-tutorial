package com.example.kotlinentitytutorial

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun create(command: UserCreationCommand): User {
        return userRepository.save(command.toEntity())
    }

    @Transactional
    fun delete(id: UUID) {
        userRepository.deleteById(id)
    }
}

data class UserCreationCommand(
    val name: String,
) {
    fun toEntity() = User(name)
}
