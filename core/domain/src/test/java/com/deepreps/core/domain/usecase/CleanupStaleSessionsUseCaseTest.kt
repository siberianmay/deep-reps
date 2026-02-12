package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CleanupStaleSessionsUseCaseTest {

    private val repository = mockk<WorkoutSessionRepository>(relaxed = true)
    private lateinit var useCase: CleanupStaleSessionsUseCase

    private val now = 1_700_000_000_000L // fixed reference time
    private val twentyFiveHoursAgo = now - (25 * 60 * 60 * 1000)
    private val twentyThreeHoursAgo = now - (23 * 60 * 60 * 1000)

    @BeforeEach
    fun setup() {
        useCase = CleanupStaleSessionsUseCase(repository)
    }

    @Test
    fun `marks sessions older than 24 hours as abandoned`() = runTest {
        val staleSession = createSession(id = 1L, startedAt = twentyFiveHoursAgo)
        coEvery { repository.getStaleActiveSessions(any()) } returns listOf(staleSession)

        val count = useCase(currentTimeMillis = now)

        assertThat(count).isEqualTo(1)
        coVerify {
            repository.updateStatus(
                id = 1L,
                status = SessionStatus.ABANDONED.value,
                completedAt = null,
            )
        }
    }

    @Test
    fun `does not mark sessions younger than 24 hours`() = runTest {
        // Sessions younger than 24h are not returned by getStaleActiveSessions
        coEvery { repository.getStaleActiveSessions(any()) } returns emptyList()

        val count = useCase(currentTimeMillis = now)

        assertThat(count).isEqualTo(0)
        coVerify(exactly = 0) { repository.updateStatus(any(), any(), any()) }
    }

    @Test
    fun `marks multiple stale sessions`() = runTest {
        val session1 = createSession(id = 1L, startedAt = twentyFiveHoursAgo)
        val session2 = createSession(id = 2L, startedAt = twentyFiveHoursAgo - 3_600_000)
        coEvery { repository.getStaleActiveSessions(any()) } returns listOf(session1, session2)

        val count = useCase(currentTimeMillis = now)

        assertThat(count).isEqualTo(2)
        coVerify(exactly = 2) { repository.updateStatus(any(), eq(SessionStatus.ABANDONED.value), eq(null)) }
    }

    @Test
    fun `passes correct cutoff to repository`() = runTest {
        coEvery { repository.getStaleActiveSessions(any()) } returns emptyList()

        useCase(currentTimeMillis = now)

        val expectedCutoff = now - (24 * 60 * 60 * 1000)
        coVerify { repository.getStaleActiveSessions(expectedCutoff) }
    }

    @Test
    fun `returns zero when no stale sessions exist`() = runTest {
        coEvery { repository.getStaleActiveSessions(any()) } returns emptyList()

        val count = useCase(currentTimeMillis = now)

        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `stale threshold is exactly 24 hours`() {
        assertThat(CleanupStaleSessionsUseCase.STALE_THRESHOLD_HOURS).isEqualTo(24L)
    }

    private fun createSession(
        id: Long,
        startedAt: Long,
    ) = WorkoutSession(
        id = id,
        startedAt = startedAt,
        completedAt = null,
        durationSeconds = null,
        pausedDurationSeconds = 0L,
        status = SessionStatus.ACTIVE,
        notes = null,
        templateId = null,
    )
}
