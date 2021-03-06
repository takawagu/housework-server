package jp.co.takawagu.houseworkserver.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class Duty(
        @Id
        val dutyId: UUID?,
        val userName: String,
        val contents: String,
        val price: BigDecimal,
        @JsonFormat(pattern="yyyy/MM/dd HH:mm:ss")
        val insertTime: LocalDateTime?,
        @JsonFormat(pattern="yyyy/MM/dd HH:mm:ss")
        val updateTime: LocalDateTime?)