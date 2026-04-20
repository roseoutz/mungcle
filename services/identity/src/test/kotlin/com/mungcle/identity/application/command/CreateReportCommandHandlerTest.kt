package com.mungcle.identity.application.command

import com.mungcle.identity.domain.model.Report
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.out.ReportRepositoryPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Instant

class CreateReportCommandHandlerTest {

    private val reportRepository: ReportRepositoryPort = mockk()
    private val userRepository: UserRepositoryPort = mockk()
    private val handler = CreateReportCommandHandler(reportRepository, userRepository)

    private fun makeUser(id: Long, flagged: Boolean = false) = User(
        id = id,
        nickname = "user$id",
        flaggedForReview = flagged,
        createdAt = Instant.now(),
    )

    @Test
    fun `정상 신고 저장`() = runTest {
        every { reportRepository.save(any()) } answers { firstArg() }
        every { reportRepository.countByReportedId(2L) } returns 1L

        handler.execute(reporterId = 1L, reportedId = 2L, reason = "부적절한 행동")

        verify { reportRepository.save(any()) }
    }

    @Test
    fun `신고 3건 이상이면 피신고자 flaggedForReview = true`() = runTest {
        val reported = makeUser(2L, flagged = false)
        every { reportRepository.save(any()) } answers { firstArg() }
        every { reportRepository.countByReportedId(2L) } returns 3L
        coEvery { userRepository.findById(2L) } returns reported
        coEvery { userRepository.save(any()) } answers { firstArg() }

        handler.execute(reporterId = 1L, reportedId = 2L, reason = "부적절한 행동")

        coVerify { userRepository.save(match { it.flaggedForReview }) }
    }

    @Test
    fun `이미 flagged 된 사용자는 중복 저장 없음`() = runTest {
        val reported = makeUser(2L, flagged = true)
        every { reportRepository.save(any()) } answers { firstArg() }
        every { reportRepository.countByReportedId(2L) } returns 5L
        coEvery { userRepository.findById(2L) } returns reported

        handler.execute(reporterId = 1L, reportedId = 2L, reason = "또 신고")

        coVerify(exactly = 0) { userRepository.save(any()) }
    }
}
