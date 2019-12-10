package com.wisrc.etl

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class BootstrapApplication {

}

object BootstrapApplication {
  def main(args: Array[String]):Unit ={
    SpringApplication.run(classOf[BootstrapApplication])
  }
}
