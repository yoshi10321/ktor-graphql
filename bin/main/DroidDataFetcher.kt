package com.example

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class DroidDataFetcher: DataFetcher<DroidDto> {

    override fun get(environment: DataFetchingEnvironment): DroidDto {
        return DroidDto("R2-D2")
    }


}