package com.example

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class HumanDataFetcher: DataFetcher<HumanDto> {

    override fun get(environment: DataFetchingEnvironment): HumanDto {
        return HumanDto(1, "taro", "earth")
    }


}