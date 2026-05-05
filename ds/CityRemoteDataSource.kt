package com.example.egypttravel.data.remote.datasource

import com.example.egypttravel.data.remote.api.CityApiService
import com.example.egypttravel.data.remote.dto.CityDetailJson
import com.example.egypttravel.data.remote.dto.ManifestJson
import com.example.egypttravel.domain.exception.CityException
import com.squareup.moshi.JsonDataException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

/**
 * Wraps [CityApiService] with error translation. Callers get domain-typed
 * exceptions rather than Retrofit/OkHttp/Moshi specifics, keeping the
 * repository free of framework leakage.
 *
 * No caching here — that's HTTP cache (OkHttp) and Room's job. This data
 * source is purely "go to the network and bring back a parsed DTO."
 */
@Singleton
class CityRemoteDataSource @Inject constructor(
    private val api: CityApiService
) {

    suspend fun fetchManifest(): ManifestJson = withTranslatedErrors {
        api.fetchManifest()
    }

    suspend fun fetchCityDetail(url: String): CityDetailJson = withTranslatedErrors {
        api.fetchCityDetail(url)
    }

    /**
     * Runs [block] and rewraps known exceptions as [CityException] variants.
     * Anything unrecognized propagates unchanged so it surfaces in logs
     * rather than being silently swallowed.
     */
    private inline fun <T> withTranslatedErrors(block: () -> T): T = try {
        block()
    } catch (e: IOException) {
        // No connectivity, DNS failure, socket timeout, etc.
        throw CityException.NetworkUnavailable
    } catch (e: HttpException) {
        // Non-2xx response — map 404 to NotFound, anything else stays as-is for logging
        if (e.code() == 404) {
            throw CityException.NotFound("(remote)")
        } else {
            throw e
        }
    } catch (e: JsonDataException) {
        throw CityException.ParseError(e.message ?: "Malformed JSON")
    } catch (e: com.squareup.moshi.JsonEncodingException) {
        throw CityException.ParseError(e.message ?: "Invalid JSON encoding")
    }
}
