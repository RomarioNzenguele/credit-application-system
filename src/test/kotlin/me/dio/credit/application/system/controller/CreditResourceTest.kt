package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.repository.CreditRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper


    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setup() = creditRepository.deleteAll()

    @AfterEach
    fun tearDown() = creditRepository.deleteAll()


    @Test
    fun `should create a credit and return 201 status`() {
        // Given
        val creditDto: CreditDto = builderCreditDTO()

        // When
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        // Then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value(1L))
            .andExpect(MockMvcResultMatchers.jsonPath("$.dayFirstInstallment").value(LocalDate.of(2025, 6, 1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallments").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(1L))
            .andExpect(MockMvcResultMatchers.content().string("Credit ${creditDto.toEntity().id} - Customer ${creditDto.customerId} saved!"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a credit with invalid customerId and return 400 status`() {
        // Given
        val creditDto: CreditDto = builderCreditDTO(customerId = 999)

        // When
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        // Then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find credit with invalid id and return 400 status`() {
        //given
        val invalidId: UUID = UUID.randomUUID()
        val customerId: Long = 1L

        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.get("${CreditResourceTest.URL}/$invalidId")
                .accept(MediaType.APPLICATION_JSON)
                .param("customerId", "${customerId}")
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }



    @Test
    fun `should find all credits and return 200 status`() {

        //given
        val creditDto: CreditDto = builderCreditDTO()
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        mockMvc.perform(
            MockMvcRequestBuilders.get(URL)
                .param("customerId", "${creditDto.customerId}")
                .content(valueAsString)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }


    private fun builderCreditDTO(
        creditValue: BigDecimal = BigDecimal.valueOf(260000.0),
        dayFirstInstallment: LocalDate = LocalDate.now().plusYears(1),
        numberOfInstallments: Int = 1,
        customerId: Long = 1L
    ): CreditDto = CreditDto(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId
    )


}



