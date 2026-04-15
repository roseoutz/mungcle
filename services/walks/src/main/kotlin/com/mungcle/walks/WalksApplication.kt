package com.mungcle.walks

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class WalksApplication

fun main(args: Array<String>) {
    runApplication<WalksApplication>(*args)
}
