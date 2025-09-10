package com.academy.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.academy"])
@EntityScan("com.academy.admin.domain")
@EnableJpaRepositories("com.academy.admin.repo")
class SecurityAcademyAdminApp

fun main(args: Array<String>) {
    runApplication<SecurityAcademyAdminApp>(*args)
}