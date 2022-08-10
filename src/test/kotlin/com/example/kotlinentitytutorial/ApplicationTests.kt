package com.example.kotlinentitytutorial

import java.util.UUID
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ApplicationTests(
    @LocalServerPort private val port: Int,
) {
    private val restTemplate: RestTemplate = RestTemplateBuilder().rootUri("http://localhost:$port").build()

    @Test
    fun test_create_user() {
        // Given
        val request = UserCreationCommand("홍길동")

        // When
        val actual = restTemplate.postForEntity("/users", request, UserDto::class.java)

        // Then
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals("홍길동", actual.body?.name)
    }

    @Test
    fun test_create_board() {
        // Given
        val user = createUser()
        val request = BoardCreationCommand(
            title = "게시판",
            content = "내용",
            information = BoardInformationCommand(null, 1),
            writerId = user.id,
            tags = setOf(
                TagCreationCommand("카테고리", "자유게시판"),
                TagCreationCommand("분류", "IT")
            ),
        )

        // When
        val actual = restTemplate.postForEntity("/boards", request, BoardDto::class.java)

        // Then
        assertEquals(HttpStatus.OK, actual.statusCode)

        val actualBody = actual.body!!
        assertAll(
            { assertEquals("게시판", actualBody.title) },
            { assertEquals("내용", actualBody.content) },
            { assertEquals(BoardInformationDto(null, 1), actualBody.information) },
            { assertEquals(user, actualBody.writer) },
            { assertEquals(2, actualBody.tags.size) },
        )
    }

    @Test
    fun test_update_board() {
        // Given
        val board = createBoard(createUser())
        val request = BoardUpdateCommand(
            title = "제목 수정",
            content = "내용 수정",
            information = BoardInformationCommand("https://google.com", 2),
        )

        // When
        val actual = restTemplate.putForEntity("/boards/${board.id}", request, BoardDto::class.java)

        // Then
        assertEquals(HttpStatus.OK, actual.statusCode)

        val actualBody = actual.body!!
        assertAll(
            { assertEquals("제목 수정", actualBody.title) },
            { assertEquals("내용 수정", actualBody.content) },
            { assertEquals(BoardInformationDto("https://google.com", 2), actualBody.information) },
        )
    }

    @Test
    fun test_add_tag() {
        // Given
        val board = createBoard(createUser())
        val request = TagCreationCommand(
            key = "색상",
            value = "빨강",
        )

        // When
        val actual = restTemplate.postForEntity("/boards/${board.id}/tags", request, BoardDto::class.java)

        // Then
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals(3, actual.body?.tags?.size)
    }

    @Test
    fun test_remove_tag() {
        // Given
        val board = createBoard(createUser())
        val removeTagId = board.tags.first().id

        // When
        val actual = restTemplate.deleteForEntity("/boards/${board.id}/tags/$removeTagId", BoardDto::class.java)

        // Then
        assertEquals(HttpStatus.OK, actual.statusCode)
        assertEquals(1, actual.body?.tags?.size)
    }

    @Test
    fun test_add_comment() {
        // Given
        val board = createBoard(createUser())
        val user = createUser()
        val request = CommentCreationCommand(
            content = "코멘트",
            writerId = user.id,
        )

        // When
        val actual = restTemplate.postForEntity("/boards/${board.id}/comments", request, BoardDto::class.java)

        // Then
        assertEquals(HttpStatus.OK, actual.statusCode)

        val actualBody = actual.body!!.comments.last()
        assertAll(
            { assertEquals("코멘트", actualBody.content) },
            { assertEquals(user, actualBody.writer) },
        )
    }

    @Test
    fun test_get_board() {
        // Given
        val user = createUser()
        val board = createBoard(user)
        addComment(board)

        // When
        val actual = restTemplate.getForEntity("/boards/${board.id}", BoardDto::class.java)

        // Then
        assertEquals(HttpStatus.OK, actual.statusCode)

        val actualBody = actual.body!!
        assertAll(
            { assertEquals("게시판", actualBody.title) },
            { assertEquals("내용", actualBody.content) },
            { assertEquals(BoardInformationDto(null, 1), actualBody.information) },
            { assertEquals(user, actualBody.writer) },
            { assertEquals(2, actualBody.tags.size) },
            { assertEquals(1, actualBody.comments.size) },
        )
    }

    @Test
    fun test_delete_user() {
        // Given
        val user = createUser()
        addComment(createBoard(user))

        // When
        val actual = restTemplate.deleteForEntity("/users/${user.id}", Unit::class.java)

        // Then
        assertEquals(HttpStatus.OK, actual.statusCode)
    }

    private fun createUser(): UserDto =
        restTemplate.postForEntity("/users", UserCreationCommand(UUID.randomUUID().toString()), UserDto::class.java).body!!

    private fun createBoard(user: UserDto): BoardDto {
        val request = BoardCreationCommand(
            title = "게시판",
            content = "내용",
            information = BoardInformationCommand(null, 1),
            writerId = user.id,
            tags = setOf(
                TagCreationCommand("카테고리", "자유게시판"),
                TagCreationCommand("분류", "IT")
            ),
        )

        return restTemplate.postForEntity("/boards", request, BoardDto::class.java).body!!
    }

    private fun addComment(board: BoardDto): BoardDto {
        val request = CommentCreationCommand(
            content = "코멘트",
            writerId = board.writer.id,
        )

        return restTemplate.postForEntity("/boards/${board.id}/comments", request, BoardDto::class.java).body!!
    }
}

fun <T> RestTemplate.putForEntity(url: String, request: Any, responseType: Class<T>): ResponseEntity<T> {
    return this.exchange(url, HttpMethod.PUT, HttpEntity(request), responseType)
}

fun <T> RestTemplate.deleteForEntity(url: String, responseType: Class<T>): ResponseEntity<T> {
    return this.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, responseType)
}
