package com.example.springbatchdemo

import org.slf4j.*
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.listener.JobExecutionListenerSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.SQLException


@Component
class JobCompletionNotificationListener @Autowired constructor(private val jdbcTemplate: JdbcTemplate) :
    JobExecutionListenerSupport() {
    override fun afterJob(jobExecution: JobExecution) {
        if (jobExecution.getStatus() === BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED !! It's time to verify the results!!")
            val results: List<User?> = jdbcTemplate.query(
                "SELECT first_name, last_name, email FROM USERS"
            ) { rs, row -> User(rs.getString(1), rs.getString(2), rs.getString(3)) }.toList()
            for (person in results) {
                log.info("Found <$person> in the database.")
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(JobCompletionNotificationListener::class.java)
    }
}