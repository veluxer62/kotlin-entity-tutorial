package com.example.kotlinentitytutorial

import java.time.LocalDateTime
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class Controller(
    private val userService: UserService,
    private val boardService: BoardService,
) {
    @PostMapping("/users")
    fun createUser(@RequestBody command: UserCreationCommand): UserDto =
        UserDto(userService.create(command))

    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: UUID) = userService.delete(id)

    @PostMapping("/boards")
    fun createBoard(@RequestBody command: BoardCreationCommand): BoardDto =
        BoardDto(boardService.create(command))

    @PutMapping("/boards/{id}")
    fun updateBoard(@PathVariable id: UUID, @RequestBody command: BoardUpdateCommand): BoardDto =
        BoardDto(boardService.update(id, command))

    @PostMapping("/boards/{id}/tags")
    fun addTag(@PathVariable id: UUID, @RequestBody command: TagCreationCommand): BoardDto =
        BoardDto(boardService.addTag(id, command))

    @DeleteMapping("/boards/{id}/tags/{tagId}")
    fun removeTag(@PathVariable id: UUID, @PathVariable tagId: UUID): BoardDto =
        BoardDto(boardService.removeTag(id, tagId))

    @PostMapping("/boards/{id}/comments")
    fun addComment(@PathVariable id: UUID, @RequestBody command: CommentCreationCommand): BoardDto =
        BoardDto(boardService.addComment(id, command))

    @GetMapping("/boards/{id}")
    fun getBoard(@PathVariable id: UUID): BoardDto = BoardDto(boardService.getById(id))
}

data class UserDto(
    val id: UUID,
    val name: String,
) {
    constructor(entity: User) : this(entity.id, entity.name)
}

data class BoardInformationDto(
    val link: String?,
    val rank: Int,
) {
    constructor(entity: BoardInformation) : this(entity.link, entity.rank)
}

data class TagDto(
    val id: UUID,
    val key: String,
    val value: String,
) {
    constructor(entity: Tag) : this(entity.id, entity.key, entity.value)
}

data class CommentDto(
    val content: String,
    val writer: UserDto,
) {
    constructor(entity: Comment) : this(entity.content, UserDto(entity.writer))
}

data class BoardDto(
    val id: UUID,
    val createdAt: LocalDateTime,
    val title: String,
    val content: String,
    val information: BoardInformationDto,
    val writer: UserDto,
    val tags: Set<TagDto>,
    val comments: List<CommentDto>,
) {
    constructor(entity: Board) : this(
        id = entity.id,
        createdAt = entity.createdAt,
        title = entity.title,
        content = entity.content,
        information = BoardInformationDto(entity.information),
        writer = UserDto(entity.writer),
        tags = entity.tags.map { TagDto(it) }.toSet(),
        comments = entity.comments.map { CommentDto(it) },
    )
}
