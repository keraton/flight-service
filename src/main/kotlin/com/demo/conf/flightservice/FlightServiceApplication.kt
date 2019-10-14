package com.demo.conf.flightservice

import com.google.common.base.Predicates
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.CrudRepository
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import javax.persistence.*

@SpringBootApplication
@EnableScheduling
class FlightServiceApplication {

	@Bean
	fun init(flightRepository: FlightRepository, priceRepository: PriceRepository) = CommandLineRunner {
		// Flights
		flightRepository.save(Flight("AF", "AF001", 200.00, "2019-12-01 08h00", "CDG", "2019-12-01 09h00", "TLS"))
		flightRepository.save(Flight("AF", "AF002", 200.00, "2019-12-01 09h00", "CDG", "2019-12-01 10h00", "TLS"))
		flightRepository.save(Flight("BA", "BA001", 500.00, "2019-12-01 08h00", "CDG", "2019-12-01 09h00", "TLS"))

		// Price
		priceRepository.save(Price("AF", "AF001", 150.00, 50.00))
		priceRepository.save(Price("BA", "BA001", 450.00, 50.00))
	}

}

fun main(args: Array<String>) {
	runApplication<FlightServiceApplication>(*args)
}

@Configuration
@EnableSwagger2
class SwaggerConfig {
	@Bean
	fun api(): Docket {
		return Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(Predicates.not(PathSelectors.regex("/error")))
				.build()
	}

	fun apiInfo(): ApiInfo {
		return ApiInfo(
				"Flight-Service API",
				"Elementary booking API",
				"Flight-Service TOS", //TODO update version
				"Personal demonstration only",
				Contact("Paul Newman", "https://paul.newman", "paul@newman.com"),
				"MIT License","https://mit-license.org/", emptyList()
		)
	}
}

@RestController
@RequestMapping("/flight-service")
class FlightServiceController (val flightRepository: FlightRepository,
							   val priceRepository: PriceRepository,
							   val bookingRepository: BookingRepository) {

	@GetMapping("/search")
	fun search() = flightRepository.findAll()

	@GetMapping("/price/{flightNumber}")
	fun price(@PathVariable flightNumber: String) = priceRepository.findById(flightNumber)

	@PostMapping("/booking")
	fun book(@RequestBody price: Price) = bookingRepository.save(Booking(price))

	@GetMapping("/booking/{bookingId}")
	fun getBooking(@PathVariable bookingId: Long) = bookingRepository.findById(bookingId)



}

@Component
class BookingService(val bookingRepository: BookingRepository) {

	@Scheduled(fixedRate = 3000)
	fun bookWorker() {
		println("==== BookingWorker =====")

		bookingRepository.findAll()
				.filter { it.status == "PENDING" }
				.forEach {
					if (it.price.airlines == "BA") it.status = "FAILED" else it.status = "SUCCESS"
					bookingRepository.save(it)
				}
	}
}

@Entity
data class Flight(val airlines : String,
				  val flightNumber: String,
				  val price: Double,
				  val departureTime: String,
				  val departureLocation: String,
				  val arrivalTime: String,
				  val arrivalLocation: String,
				  @Id @GeneratedValue(strategy = GenerationType.AUTO)
				  val id : Long = 0)
@Entity
data class Price(val airlines : String,
				 @Id val flightNumber: String,
				 val base: Double,
				 val tax: Double)

@Entity
data class Booking(@OneToOne val price: Price,
				   @Id @GeneratedValue(strategy = GenerationType.AUTO)
					val id : Long = 0,
				   var status: String = "PENDING")

interface FlightRepository : CrudRepository<Flight, Long>

interface PriceRepository : CrudRepository<Price, String>

interface BookingRepository : CrudRepository<Booking, Long>