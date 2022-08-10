package com.example.kotlinentitytutorial

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class BoardService(
    private val userRepository: UserRepository,
    private val boardRepository: BoardRepository,
    private val tagRepository: TagRepository,
) {
    @Transactional
    fun create(command: BoardCreationCommand): Board {
        val user = getUserById(command.writerId)
        val tags = command.tags.map { findOrCreateTag(it) }.toSet()

        return boardRepository.save(command.toEntity(user, tags))
    }

    @Transactional
    fun update(id: UUID, command: BoardUpdateCommand): Board {
        return getById(id).apply { update(command.toData()) }
    }

    @Transactional
    fun addTag(id: UUID, command: TagCreationCommand,): Board {
        return getById(id).apply { addTag(findOrCreateTag(command)) }
    }

    @Transactional
    fun removeTag(id: UUID, tagId: UUID): Board {
        return getById(id).apply { removeTag(tagId) }
    }

    @Transactional
    fun addComment(id: UUID, command: CommentCreationCommand): Board {
        return getById(id)
            .apply {
                val user = getUserById(command.writerId)
                val comment = Comment(command.content, user)
                addComment(comment)
            }
    }

    fun getById(id: UUID): Board = boardRepository.findById(id).orElseThrow()

    private fun getUserById(writerId: UUID): User = userRepository.findById(writerId).orElseThrow()

    private fun findOrCreateTag(command: TagCreationCommand): Tag =
        tagRepository.findByKeyAndValue(command.key, command.value).orElse(command.toEntity())
}

data class CommentCreationCommand(
    val content: String,
    val writerId: UUID,
)

data class BoardUpdateCommand(
    val title: String,
    val content: String,
    val information: BoardInformationCommand,
) {
    fun toData() = BoardUpdateData(
        title = title,
        content = content,
        information = information.toEntity(),
    )
}

data class BoardCreationCommand(
    val title: String,
    val content: String,
    val information: BoardInformationCommand,
    val writerId: UUID,
    val tags: Set<TagCreationCommand>,
) {
    fun toEntity(writer: User, tags: Set<Tag>) = Board(
        title = title,
        content = content,
        information = information.toEntity(),
        writer = writer,
        tags = tags,
    )
}

data class BoardInformationCommand(
    val link: String?,
    val rank: Int,
) {
    fun toEntity() = BoardInformation(link, rank)
}

data class TagCreationCommand(
    val key: String,
    val value: String,
) {
    fun toEntity() = Tag(key, value)
}
