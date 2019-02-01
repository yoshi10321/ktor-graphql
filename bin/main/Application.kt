package com.example

import graphql.GraphQL
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson()
        register(ContentType.Application.Json, GsonConverter())
    }


    val schema1 = File(ClassLoader.getSystemResource("example.graphqls").file)
    val schema2 = File(ClassLoader.getSystemResource("humanType.graphqls").file)
    val schema3 = File(ClassLoader.getSystemResource("droidType.graphqls").file)

    val schemaParser = SchemaParser()

    val typeRegistry = TypeDefinitionRegistry()
    typeRegistry.merge(schemaParser.parse(schema1))
    typeRegistry.merge(schemaParser.parse(schema2))
    typeRegistry.merge(schemaParser.parse(schema3))

    val runtimeWiring = RuntimeWiring.newRuntimeWiring()
        .type("QueryType", { builder -> builder
            .dataFetcher("hero", StaticDataFetcher("JEDI"))
            .dataFetcher("name", StaticDataFetcher("Luke"))
            .dataFetcher("human", HumanDataFetcher())
            .dataFetcher("droid", DroidDataFetcher())
        })
        .build()

    val schemaGenerator = SchemaGenerator()
    val graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring)

    val build = GraphQL.newGraphQL(graphQLSchema).build()
    val executionResult = build.execute("{hero, name, human{name, homePlanet}, droid{name}}")
    val result = executionResult.getData<LinkedHashMap<String, String>>()

    routing {
        post("/graphql") {
            call.respond(result)
        }
    }
}


