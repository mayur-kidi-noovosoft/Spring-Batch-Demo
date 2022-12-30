package com.example.springbatchdemo

import org.slf4j.*
import java.util.*
import org.springframework.batch.item.ItemProcessor;

class UserItemProcessor : ItemProcessor<User, User> {

    override fun process(user: User): User {
        val firstName = user.firstName.uppercase(Locale.getDefault())
        val lastName = user.lastName.uppercase(Locale.getDefault())
        val email = user.email.uppercase(Locale.getDefault())
        val transformedPerson = User(firstName = firstName, lastName = lastName, email = email)
        log.info("Converting ($user) into ($transformedPerson)")
        return transformedPerson
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(UserItemProcessor::class.java)
    }
}