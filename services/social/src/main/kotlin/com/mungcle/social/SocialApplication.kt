package com.mungcle.social

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SocialApplication

fun main(args: Array<String>) {
    runApplication<SocialApplication>(*args)
}
