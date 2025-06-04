package com.contentreader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ContentReaderApplication

fun main(args: Array<String>) {
    runApplication<ContentReaderApplication>(*args)
} 