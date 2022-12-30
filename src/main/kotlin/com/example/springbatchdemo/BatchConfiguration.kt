package com.example.springbatchdemo

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import javax.sql.DataSource


@Configuration
@EnableBatchProcessing
class BatchConfiguration {
    @Autowired
    var jobBuilderFactory: JobBuilderFactory? = null

    @Autowired
    var stepBuilderFactory: StepBuilderFactory? = null

    @Autowired
    var dataSource: DataSource? = null

    @Bean
    fun reader(): FlatFileItemReader<User> {
        val reader = FlatFileItemReader<User>()
        reader.setResource(PathResource("src/main/resources/data.csv"))
        reader.setLineMapper(object : DefaultLineMapper<User?>() {
            init {
                setLineTokenizer(object : DelimitedLineTokenizer() {
                    init {
                        setNames("firstName", "lastName", "email")
                    }
                })
                setFieldSetMapper(object : BeanWrapperFieldSetMapper<User?>() {
                    init {
                        setTargetType(User::class.java)
                    }
                })
            }
        })
        return reader
    }

    @Bean
    fun processor(): UserItemProcessor {
        return UserItemProcessor()
    }

    @Bean
    fun writer(): JdbcBatchItemWriter<User> {
        val writer = JdbcBatchItemWriter<User>()
        writer.setItemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
        writer.setSql("INSERT INTO USERS (first_name, last_name, email) VALUES (:firstName, :lastName, :email)")
        dataSource?.let { writer.setDataSource(it) }
        return writer
    }

    @Bean
    fun importUserJob(listener: JobCompletionNotificationListener?): Job {
        return jobBuilderFactory!!["importUserJob"].incrementer(
            RunIdIncrementer()
        ).listener(listener!!).flow(step1()).end().build()
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory!!["step1"].chunk<User, User>(10).reader(reader()).processor(processor())
            .writer(writer()).build()
    }
}