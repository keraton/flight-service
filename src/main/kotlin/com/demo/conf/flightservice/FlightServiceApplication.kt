package com.demo.conf.flightservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.CrudRepository
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne

@SpringBootApplication
@EnableScheduling
class FlightServiceApplication {

    @Bean
    fun init(flightRepository: FlightRepository, priceRepository: PriceRepository) = CommandLineRunner {
        // Flights
        flightRepository.save(Flight("AF", "AF001", 200.00, "2019-12-01 08h00", "CDG", "2019-12-01 09h00", "TLS"))
        flightRepository.save(Flight("AF", "AFX01", 500.00, "2019-12-01 09h30", "ORY", "2019-12-01 11h00", "TLS"))
        flightRepository.save(Flight("AF", "AFX02", 500.00, "2019-12-01 09h30", "ORY", "2019-12-01 11h00", "TLS"))
        flightRepository.save(Flight("AF", "AF002", 200.00, "2019-12-01 14h00", "CDG", "2019-12-01 15h00", "TLS"))
        flightRepository.save(Flight("BA", "BA001", 500.00, "2019-12-01 18h00", "CDG", "2019-12-01 19h00", "TLS"))
        flightRepository.save(Flight("DS", "DSX01", 500.00, "2019-12-01 15h30", "ORY", "2019-12-01 17h00", "TLS"))
        flightRepository.save(Flight("AF", "DSX02", 500.00, "2019-12-01 20h30", "ORY", "2019-12-01 22h00", "TLS"))

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
    @Value("\${build.version}")
    lateinit var buildVersion: String

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.demo.conf.flightservice"))
                .paths(PathSelectors.ant("/flight-service/**"))
                .build()
    }

    fun apiInfo(): ApiInfo {
        return ApiInfo(
                "Flight-Service API",
                "Elementary booking API",
                buildVersion,
                "Personal demonstration only",
                Contact("Paul Newman", "https://paul.newman", "paul@newman.com"),
                "MIT License", "https://mit-license.org/", emptyList()
        )
    }
}

@Configuration
@EnableWebSecurity
class CustomSecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        super.configure(http)
        http.csrf().disable()
    }
}

@RestController
@RequestMapping("/flight-service")
class FlightServiceController(val flightRepository: FlightRepository,
                              val priceRepository: PriceRepository,
                              val bookingRepository: BookingRepository) {

    @GetMapping("/search")
    fun search(@RequestParam ori: String,
               @RequestParam dest: String,
               @RequestParam dep: String) = flightRepository.findAll()
                                                                        .map { updateDate(it, dep) }
                                                                        .toList()

    @GetMapping("/price/{flightNumber}")
    fun price(@PathVariable flightNumber: String) = priceRepository.findById(flightNumber)


    @PostMapping("/booking")
    fun book(@RequestBody price: Price) = bookingRepository.save(Booking(price))

    @GetMapping("/booking/{bookingId}")
    fun getBooking(@PathVariable bookingId: Long) = bookingRepository.findById(bookingId)

    private fun updateDate(it: Flight, departureTime: String): Flight {
        val hourDepart = it.departureTime.split(" ").last()
        val hourArrive = it.arrivalTime.split(" ").last()
        return Flight(airlines = it.airlines,
                flightNumber = it.flightNumber,
                price = it.price,
                departureTime = "$departureTime $hourDepart",
                arrivalTime = "$departureTime $hourArrive",
                departureLocation = it.departureLocation,
                arrivalLocation = it.arrivalLocation,
                id = it.id)
    }


}

@Component
class BookingService(val bookingRepository: BookingRepository) {

    @Scheduled(fixedRate = 2000)
    fun bookWorker() {
        println("==== BookingWorker =====")

        bookingRepository.findAll()
                .filter { it.status == "PENDING" }
                .map { Thread.sleep(2000); it }
                .forEach {
                    if (it.price.airlines == "BA") it.status = "FAILED" else it.status = "SUCCESS"
                    bookingRepository.save(it)
                }
    }
}

@Entity
data class Flight(val airlines: String,
                  val flightNumber: String,
                  val price: Double,
                  val departureTime: String,
                  val departureLocation: String,
                  val arrivalTime: String,
                  val arrivalLocation: String,
                  @Id @GeneratedValue(strategy = GenerationType.AUTO)
                  val id: Long = 0)

@Entity
data class Price(val airlines: String,
                 @Id val flightNumber: String,
                 val base: Double,
                 val tax: Double)

@Entity
data class Booking(@OneToOne val price: Price,
                   @Id @GeneratedValue(strategy = GenerationType.AUTO)
                   val id: Long = 0,
                   var status: String = "PENDING")

interface FlightRepository : CrudRepository<Flight, Long>

interface PriceRepository : CrudRepository<Price, String>

interface BookingRepository : CrudRepository<Booking, Long>