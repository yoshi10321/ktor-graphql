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
import io.ktor.http.content.file
import io.ktor.http.content.static
import io.ktor.request.receive
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


    val querySchema = File(ClassLoader.getSystemResource("query.graphqls").file)
    val humanTypeSchema = File(ClassLoader.getSystemResource("humanType.graphqls").file)
    val droidTypeSchema = File(ClassLoader.getSystemResource("droidType.graphqls").file)

    val schemaParser = SchemaParser()

    val typeRegistry = TypeDefinitionRegistry()
    typeRegistry.merge(schemaParser.parse(querySchema))
    typeRegistry.merge(schemaParser.parse(humanTypeSchema))
    typeRegistry.merge(schemaParser.parse(droidTypeSchema))

    val runtimeWiring = RuntimeWiring.newRuntimeWiring()
        .type("QueryType") { builder -> builder
            .dataFetcher("hero", StaticDataFetcher("JEDI"))
            .dataFetcher("name", StaticDataFetcher("Luke"))
            .dataFetcher("human", HumanDataFetcher())
            .dataFetcher("droid", DroidDataFetcher())
        }
        .build()

    val schemaGenerator = SchemaGenerator()
    val graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring)

    val build = GraphQL.newGraphQL(graphQLSchema).build()

    routing {
        post("/graphql") {
            val request = call.receive(GraphQLRequest::class)
            val executionResult = build.execute(request.query)
            val result = executionResult.getData<LinkedHashMap<String, String>>()
            call.respond(result)
        }
        static {
            file("index.html", File("./resources/index.html"))
        }
    }
}


data class GraphQLRequest(val query: String)