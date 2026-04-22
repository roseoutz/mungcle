package com.mungcle.petprofile

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PetProfileApplication

fun main(args: Array<String>) {
    runApplication<PetProfileApplication>(*args)
}
