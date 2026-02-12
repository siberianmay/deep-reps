package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DetectAbandonedSessionUseCaseTest {

    private val repository = mockk<WorkoutSessionRepository>()
    private lateinit var useCase: DetectAbandonedSessionUseCase

    @BeforeEach
    fun setup() {
        useCase = DetectAbandonedSessionUseCase(repository)
    }

    @Test
    fun `returns active session when one exists`() = runTest {
        val session = createSession(id = 1L, status = SessionStatus.ACTIVE)
        coEvery { repository.getActiveSession() } returns session

        val result = useCase()

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1L)
        assertThat(result?.status).isEqualTo(SessionStatus.ACTIVE)
    }

    @Test
    fun `returns paused session when one exists`() = runTest {
        val session = createSession(id = 2L, status = SessionStatus.PAUSED)
        coEvery { repository.getActiveSession() } returns session

        val result = useCase()

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(2L)
        assertThat(result?.status).isEqualTo(SessionStatus.PAUSED)
    }

    @Test
    fun `returns null when no active or paused session exists`() = runTest {
        coEvery { repository.getActiveSession() } returns null

        val result = useCase()

        assertThat(result).isNull()
    }

    @Test
    fun `does not return completed sessions`() = runTest {
        // getActiveSession() DAO query only returns active/paused,
        // so a completed session will not be returned.
        coEvery { repository.getActiveSession() } returns null

        val result = useCase()

        assertThat(result).isNull()
    }

    @Test
    fun `does not return abandoned sessions`() = runTest {
        // Abandoned sessions have been cleaned up by CleanupStaleSessionsUseCase.
        // The DAO query filters them out.
        coEvery { repository.getActiveSession() } returns null

        val result = useCase()

        assertThat(result).isNull()
    }

    @Test
    fun `does not return crashed sessions`() = runTest {
        // Crashed sessions have been marked by previous startup flow.
        coEvery { repository.getActiveSession() } returns null

        val result = useCase()

        assertThat(result).isNull()
    }

    private fun createSession(
        id: Long,
        status: SessionStatus,
        startedAt: Long = System.currentTimeMillis() - 600_000, // 10 min ago
    ) = WorkoutSession(
        id = id,
        startedAt = startedAt,
        completedAt = null,
        durationSeconds = null,
        pausedDurationSeconds = 0L,
        status = status,
        notes = null,
        templateId = null,
    )
}
